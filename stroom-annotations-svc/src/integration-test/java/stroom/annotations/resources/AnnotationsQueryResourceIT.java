package stroom.annotations.resources;

import org.eclipse.jetty.http.HttpStatus;
import org.junit.Before;
import org.junit.Test;
import stroom.annotations.App;
import stroom.annotations.config.Config;
import stroom.annotations.hibernate.Annotation;
import stroom.annotations.hibernate.AnnotationsDocRefEntity;
import stroom.annotations.hibernate.Status;
import stroom.datasource.api.v2.DataSource;
import stroom.datasource.api.v2.DataSourceField;
import stroom.query.api.v2.*;
import stroom.query.audit.authorisation.DocumentPermission;
import stroom.query.audit.service.DocRefEntity;
import stroom.query.testing.QueryResourceIT;

import javax.ws.rs.core.Response;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import static org.junit.Assert.*;
import static stroom.annotations.service.AnnotationsServiceImpl.SEARCH_PAGE_LIMIT;

public class AnnotationsQueryResourceIT extends QueryResourceIT<AnnotationsDocRefEntity, Config, App> {
    private AnnotationsHttpClient annotationsClient;

    public AnnotationsQueryResourceIT() {
        super(App.class, AnnotationsDocRefEntity.class, AnnotationsDocRefEntity.TYPE);
    }

    @Override
    protected void assertValidDataSource(final DataSource dataSource) {
        final Set<String> resultFieldNames = dataSource.getFields().stream()
                .map(DataSourceField::getName)
                .collect(Collectors.toSet());

        assertTrue(resultFieldNames.contains(Annotation.ID));
        assertTrue(resultFieldNames.contains(Annotation.CONTENT));
        assertTrue(resultFieldNames.contains(Annotation.ASSIGN_TO));
        assertTrue(resultFieldNames.contains(DocRefEntity.CREATE_TIME));
        assertTrue(resultFieldNames.contains(DocRefEntity.CREATE_USER));
        assertTrue(resultFieldNames.contains(DocRefEntity.UPDATE_TIME));
        assertTrue(resultFieldNames.contains(DocRefEntity.UPDATE_USER));
        assertTrue(resultFieldNames.contains(Annotation.STATUS));
    }

    @Override
    protected AnnotationsDocRefEntity getValidEntity(final DocRef docRef) {
        return new AnnotationsDocRefEntity.Builder()
                .docRef(docRef)
                .build();
    }

    @Before
    public final void beforeTest() {
        annotationsClient = new AnnotationsHttpClient(getAppHost());
    }

    @Test
    public void testQuerySearch() {
        final DocRef docRef = createDocument();

        final int NUMBER_SEARCH_TERMS = 2;
        final int NUMBER_PAGES_EXPECTED = 3;
        final int ANNOTATIONS_PER_SEARCH_TERM = SEARCH_PAGE_LIMIT * NUMBER_PAGES_EXPECTED;
        final int TOTAL_ANNOTATIONS = ANNOTATIONS_PER_SEARCH_TERM * NUMBER_SEARCH_TERMS;
        final List<String> contentSearchTerms = IntStream.range(0, NUMBER_SEARCH_TERMS)
                .mapToObj(i -> UUID.randomUUID().toString())
                .collect(Collectors.toList());

        // Create some test data for each search term
        final Map<String, Set<Annotation>> annotationsBySearchTerm = new HashMap<>();
        for (final String contentSearchTerm : contentSearchTerms) {
            final Set<Annotation> annotations = IntStream.range(0, ANNOTATIONS_PER_SEARCH_TERM)
                    .mapToObj(i -> UUID.randomUUID().toString()) // Generate an ID
                    .map(uuid -> new Annotation.Builder().id(uuid)
                            .dataSourceUuid(docRef.getUuid())
                            .content("Some Content - " + UUID.randomUUID().toString() + contentSearchTerm)
                            .assignTo("Some Guy - " + UUID.randomUUID().toString())
                            .status(Status.OPEN_ESCALATED)
                            .build())
                    .peek(a -> this.createAndUpdateAnnotation(docRef.getUuid(), a)) // add to database
                    .collect(Collectors.toSet());
            annotationsBySearchTerm.put(contentSearchTerm, annotations);
        }

        final Collection<OffsetRange> pageOffsets = IntStream.range(0, NUMBER_PAGES_EXPECTED)
                .mapToObj(value -> new OffsetRange.Builder()
                        .length((long) SEARCH_PAGE_LIMIT)
                        .offset((long) (value * SEARCH_PAGE_LIMIT))
                        .build())
                .collect(Collectors.toList());

        annotationsBySearchTerm.forEach((contentSearchTerm, annotationsSet) -> {
            final ExpressionOperator expressionOperator = new ExpressionOperator.Builder(ExpressionOperator.Op.OR)
                    .addTerm(Annotation.CONTENT, ExpressionTerm.Condition.CONTAINS, contentSearchTerm)
                    .build();

            final Set<String> resultsSet = new HashSet<>();
            final Set<String> expectedAnnotationIds = annotationsSet.stream()
                    .map(Annotation::getId)
                    .collect(Collectors.toSet());

            pageOffsets.forEach(offsetRange -> {
                try {
                    final SearchRequest request = getValidSearchRequest(docRef, expressionOperator, offsetRange);

                    final Response response = queryClient.search(adminUser(), request);
                    assertEquals(HttpStatus.OK_200, response.getStatus());

                    final SearchResponse searchResponse = getFromBody(response, SearchResponse.class);

                    for (final Result result : searchResponse.getResults()) {
                        assertTrue(result instanceof FlatResult);

                        final FlatResult flatResult = (FlatResult) result;
                        flatResult.getValues().stream()
                                .map(objects -> objects.get(3))
                                .map(Object::toString)
                                .forEach(resultsSet::add);
                    }
                } catch (Exception e) {
                    fail(e.getLocalizedMessage());
                }
            });

            assertEquals(expectedAnnotationIds, resultsSet);
        });

        // Create doc ref, update doc ref entity, create & update annotations, search per page
        checkAuditLogs(2 + (2 * TOTAL_ANNOTATIONS) + (NUMBER_SEARCH_TERMS * NUMBER_PAGES_EXPECTED));
    }

    @Test
    public void testDestroy() {
        final QueryKey aQueryKey = new QueryKey(UUID.randomUUID().toString());
        final String unauthentiatedUsername = UUID.randomUUID().toString();

        final Response authenticatedDestroyResponse = queryClient.destroy(adminUser(), aQueryKey);
        assertEquals(HttpStatus.NOT_FOUND_404, authenticatedDestroyResponse.getStatus());

        final Response unauthenticatedDestroyResponse = queryClient.destroy(
                unauthenticatedUser(unauthentiatedUsername),
                aQueryKey);
        assertEquals(HttpStatus.UNAUTHORIZED_401, unauthenticatedDestroyResponse.getStatus());

        // Just the authenticated destroy
        checkAuditLogs(1);
    }

    protected SearchRequest getValidSearchRequest(final DocRef docRef,
                                                final ExpressionOperator expressionOperator,
                                                final OffsetRange offsetRange) {
        final String queryKey = UUID.randomUUID().toString();
        return new SearchRequest.Builder()
                .query(new Query.Builder()
                        .dataSource(docRef)
                        .expression(expressionOperator)
                        .build())
                .key(queryKey)
                .dateTimeLocale("en-gb")
                .incremental(true)
                .addResultRequests(new ResultRequest.Builder()
                        .fetch(ResultRequest.Fetch.ALL)
                        .resultStyle(ResultRequest.ResultStyle.FLAT)
                        .componentId("componentId")
                        .requestedRange(offsetRange)
                        .addMappings(new TableSettings.Builder()
                                .queryId(queryKey)
                                .extractValues(false)
                                .showDetail(false)
                                .addFields(new Field.Builder()
                                        .name(Annotation.ID)
                                        .expression("${" + Annotation.ID + "}")
                                        .build())
                                .addMaxResults(10)
                                .build())
                        .build())
                .build();
    }

    /**
     * Creates an annotation, then updates it to set all the fields from the given fully
     * populated annotation object. This is used by tests which assume that create/update work
     * and they just need to dump data into the database to test other functions.
     *
     * @param docRefUuid the docRefUuid of the index the annotation belongs to
     * @param annotation The annotation to create
     * @return The initial state of the annotation
     */
    private Annotation createAndUpdateAnnotation(final String docRefUuid, final Annotation annotation) {
        Annotation result = null;

        try {
            final Response createResponse = annotationsClient.create(adminUser(), docRefUuid, annotation.getId());
            assertEquals(HttpStatus.OK_200, createResponse.getStatus());

            final Response updateResponse = annotationsClient.update(adminUser(), docRefUuid, annotation.getId(), annotation);
            assertEquals(HttpStatus.OK_200, updateResponse.getStatus());
        } catch (Exception e) {
            fail(e.getLocalizedMessage());
        }

        return result;
    }
}
