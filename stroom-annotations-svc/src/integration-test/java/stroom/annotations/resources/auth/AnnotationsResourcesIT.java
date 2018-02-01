package stroom.annotations.resources.auth;

import com.github.tomakehurst.wiremock.core.WireMockConfiguration;
import org.eclipse.jetty.http.HttpStatus;
import org.junit.ClassRule;
import org.junit.Rule;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import stroom.annotations.App;
import stroom.annotations.config.Config;
import stroom.annotations.hibernate.Annotation;
import stroom.annotations.hibernate.AnnotationHistory;
import stroom.annotations.hibernate.AnnotationsDocRefEntity;
import stroom.annotations.hibernate.HistoryOperation;
import stroom.annotations.hibernate.Status;
import stroom.annotations.resources.AnnotationsHttpClient;
import stroom.annotations.resources.AuditedAnnotationsResourceImpl;
import stroom.query.api.v2.DocRef;
import stroom.query.audit.authorisation.DocumentPermission;
import stroom.query.audit.client.DocRefResourceHttpClient;
import stroom.query.audit.rest.AuditedDocRefResourceImpl;
import stroom.query.testing.DropwizardAppWithClientsRule;
import stroom.query.testing.FifoLogbackRule;
import stroom.query.testing.StroomAuthenticationRule;

import javax.ws.rs.core.GenericType;
import javax.ws.rs.core.Response;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import static io.dropwizard.testing.ResourceHelpers.resourceFilePath;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.fail;
import static stroom.annotations.service.AnnotationsServiceImpl.SEARCH_PAGE_LIMIT;
import static stroom.query.testing.FifoLogbackRule.containsAllOf;

public class AnnotationsResourcesIT {
    private static final Logger LOGGER = LoggerFactory.getLogger(AnnotationsResourcesIT.class);

    @ClassRule
    public static final DropwizardAppWithClientsRule<Config> appRule =
            new DropwizardAppWithClientsRule<>(App.class, resourceFilePath("config_auth.yml"));

    @ClassRule
    public static StroomAuthenticationRule authRule =
            new StroomAuthenticationRule(WireMockConfiguration.options().port(10080), AnnotationsDocRefEntity.TYPE);

    @Rule
    public FifoLogbackRule auditLogRule = new FifoLogbackRule();

    private final AnnotationsHttpClient annotationsClient;
    private final DocRefResourceHttpClient<AnnotationsDocRefEntity> docRefClient;

    public AnnotationsResourcesIT() {
        annotationsClient = appRule.getClient(AnnotationsHttpClient::new);
        docRefClient = appRule.getClient(DocRefResourceHttpClient::new);
    }

    @Test
    public void testWelcome() {
        final Response response = annotationsClient.welcome();

        assertEquals(AuditedAnnotationsResourceImpl.WELCOME_TEXT, response.readEntity(String.class));
    }

    @Test
    public void testStatusValues() {
        final Response response = annotationsClient.statusValues();

        final Map<String, String> responseStatusValues =
                response.readEntity(new GenericType<Map<String, String>>() {});
        final Map<String, String> statusValues = Arrays.stream(Status.values())
                .collect(Collectors.toMap(Object::toString, Status::getDisplayText));

        assertEquals(HttpStatus.OK_200, response.getStatus());
        assertEquals(statusValues, responseStatusValues);
    }

    @Test
    public void testCreateAnnotation() {
        final DocRef docRef = createDocument();
        final String annotationId = UUID.randomUUID().toString();

        final Response response = annotationsClient.create(authRule.adminUser(), docRef.getUuid(), annotationId);
        assertEquals(HttpStatus.OK_200, response.getStatus());

        final Annotation created = response.readEntity(Annotation.class);
        assertEquals(annotationId, created.getId());
        assertEquals(Annotation.DEFAULT_CONTENT, created.getContent());
        assertEquals(Annotation.DEFAULT_ASSIGNEE, created.getAssignTo());
        assertEquals(authRule.adminUser().getName(), created.getCreateUser());
        assertEquals(authRule.adminUser().getName(), created.getUpdateUser());
        assertEquals(Annotation.DEFAULT_STATUS, created.getStatus());

        // create document, create annotation
        auditLogRule.check().thereAreAtLeast(2)
                .containsOrdered(containsAllOf(AuditedDocRefResourceImpl.CREATE_DOC_REF, docRef.getUuid()))
                .containsOrdered(containsAllOf(AuditedAnnotationsResourceImpl.CREATE_ANNOTATION, annotationId));
    }

    @Test
    public void testCreateAndGetAnnotation() {
        final DocRef docRef = createDocument();
        final String annotationId = UUID.randomUUID().toString();

        final Response response = annotationsClient.create(authRule.adminUser(), docRef.getUuid(), annotationId);
        assertEquals(HttpStatus.OK_200, response.getStatus());

        final Annotation annotationResponse = getAnnotation(docRef.getUuid(), annotationId);
        assertNotNull(annotationResponse);

        // Create Document, create annotation, get annotation
        auditLogRule.check().thereAreAtLeast(3)
                .containsOrdered(containsAllOf(AuditedDocRefResourceImpl.CREATE_DOC_REF, docRef.getUuid()))
                .containsOrdered(containsAllOf(AuditedAnnotationsResourceImpl.CREATE_ANNOTATION, annotationId))
                .containsOrdered(containsAllOf(AuditedAnnotationsResourceImpl.GET_ANNOTATION, annotationId));
    }

    @Test
    public void testCreateUpdateAndGetMultipleAnnotations() {
        // Create some test data
        final DocRef docRef = createDocument();

        final int RECORDS_TO_CREATE = 10;
        final Map<String, Annotation> annotations = IntStream.range(0, RECORDS_TO_CREATE)
                .mapToObj(i -> UUID.randomUUID().toString())
                .map(uuid -> new Annotation.Builder().id(uuid)
                        .content(UUID.randomUUID().toString())
                        .assignTo(UUID.randomUUID().toString())
                        .status(Status.OPEN_ESCALATED)
                        .build())
                .peek(a -> this.createAndUpdateAnnotation(docRef, a)) // add to database
                .collect(Collectors.toMap(Annotation::getId, Function.identity()));

        // Try and fetch each annotation
        annotations.forEach((id, annotation) -> {
            final Annotation annotationResponse = getAnnotation(docRef.getUuid(), id);
            assertEquals(annotation.getId(), annotationResponse.getId());
            assertEquals(annotation.getAssignTo(), annotationResponse.getAssignTo());
            assertEquals(annotation.getContent(), annotationResponse.getContent());
            assertEquals(annotation.getStatus(), annotationResponse.getStatus());
        });

        // Create Document, Records * create, update, get
        final FifoLogbackRule.LogChecker logChecker = auditLogRule.check()
                .thereAreAtLeast(1 + (3 * RECORDS_TO_CREATE))
                .containsOrdered(containsAllOf(AuditedDocRefResourceImpl.CREATE_DOC_REF, docRef.getUuid()));

        annotations.values().forEach(a -> logChecker
                    .containsAnywhere(containsAllOf(AuditedAnnotationsResourceImpl.CREATE_ANNOTATION, a.getId()))
                    .containsAnywhere(containsAllOf(AuditedAnnotationsResourceImpl.UPDATE_ANNOTATION, a.getId()))
                    .containsAnywhere(containsAllOf(AuditedAnnotationsResourceImpl.GET_ANNOTATION, a.getId())));
    }

    @Test
    public void testDelete() {
        final DocRef docRef = createDocument();
        final String id = UUID.randomUUID().toString();

        final Response createResponse = annotationsClient.create(authRule.adminUser(), docRef.getUuid(), id);
        assertEquals(HttpStatus.OK_200, createResponse.getStatus());

        Response preDeleteGetResponse = annotationsClient.get(authRule.adminUser(), docRef.getUuid(), id);
        assertEquals(HttpStatus.OK_200, preDeleteGetResponse.getStatus());

        final Response deleteResponse = annotationsClient.remove(authRule.adminUser(), docRef.getUuid(), id);
        assertEquals(HttpStatus.OK_200, deleteResponse.getStatus());
        
        Response postDeleteGetResponse = annotationsClient.get(authRule.adminUser(), docRef.getUuid(), id);
        assertEquals(HttpStatus.NOT_FOUND_404, postDeleteGetResponse.getStatus());

        // create document, create annotation, pre-get, Delete, post-get
        auditLogRule.check()
                .thereAreAtLeast(5)
                .containsOrdered(containsAllOf(AuditedDocRefResourceImpl.CREATE_DOC_REF, docRef.getUuid()))
                .containsOrdered(containsAllOf(AuditedAnnotationsResourceImpl.CREATE_ANNOTATION, id))
                .containsOrdered(containsAllOf(AuditedAnnotationsResourceImpl.GET_ANNOTATION, id))
                .containsOrdered(containsAllOf(AuditedAnnotationsResourceImpl.REMOVE_ANNOTATION, id))
                .containsOrdered(containsAllOf(AuditedAnnotationsResourceImpl.GET_ANNOTATION, id));
    }

    @Test
    public void testGetHistory() {
        final DocRef docRef = createDocument();
        final String id = UUID.randomUUID().toString();

        final int numberUpdates = 5;
        final Response createResponse = annotationsClient.create(authRule.adminUser(), docRef.getUuid(), id);
        assertEquals(HttpStatus.OK_200, createResponse.getStatus());

        final List<Annotation> annotationHistory =
                IntStream.range(0, numberUpdates).mapToObj(i -> new Annotation.Builder()
                        .id(id)
                        .content(UUID.randomUUID().toString())
                        .assignTo(UUID.randomUUID().toString())
                        .status(Status.OPEN_ESCALATED)
                        .build())
                        .peek(a -> {
                            // Update the annotation with new randomised details
                            final Response updateResponse = annotationsClient.update(
                                    authRule.adminUser(),
                                    docRef.getUuid(),
                                    a.getId(),
                                    a);
                            assertEquals(HttpStatus.OK_200, updateResponse.getStatus());
                        }) // push update to database
                        .collect(Collectors.toList());

        final Response deleteResponse = annotationsClient.remove(authRule.adminUser(), docRef.getUuid(), id);
        assertEquals(HttpStatus.OK_200, deleteResponse.getStatus());

        // Get the history for this annotation
        final Response getHistoryResponse = annotationsClient.getHistory(authRule.adminUser(), docRef.getUuid(), id);
        assertEquals(HttpStatus.OK_200, getHistoryResponse.getStatus());

        final List<AnnotationHistory> history = getHistoryResponse.readEntity(new GenericType<List<AnnotationHistory>>() {});

        // Check that the history contains all the expected events
        assertEquals(numberUpdates + 2, history.size());

        assertEquals(HistoryOperation.CREATE, history.get(0).getOperation());
        IntStream.range(0, annotationHistory.size()).forEach(i -> {
            assertEquals(HistoryOperation.UPDATE, history.get(i + 1).getOperation());
        });
        assertEquals(HistoryOperation.DELETE, history.get(6).getOperation());

        // Check the audit log
        final FifoLogbackRule.LogChecker logChecker = auditLogRule.check()
                .thereAreAtLeast(4 + numberUpdates)
                .containsOrdered(containsAllOf(AuditedDocRefResourceImpl.CREATE_DOC_REF, docRef.getUuid()))
                .containsOrdered(containsAllOf(AuditedAnnotationsResourceImpl.CREATE_ANNOTATION, id));

        IntStream.range(0, numberUpdates)
                .forEach(i -> logChecker.containsOrdered(containsAllOf(AuditedAnnotationsResourceImpl.UPDATE_ANNOTATION, id)));

        logChecker.containsOrdered(containsAllOf(AuditedAnnotationsResourceImpl.REMOVE_ANNOTATION, id))
                .containsOrdered(containsAllOf(AuditedAnnotationsResourceImpl.GET_ANNOTATION_HISTORY, id));
    }

    @Test
    public void testSearchSingle() {
        // Create some test data
        final DocRef docRef = createDocument();

        final Annotation annotation = new Annotation.Builder().id(UUID.randomUUID().toString())
                        .content(UUID.randomUUID().toString())
                        .assignTo(UUID.randomUUID().toString())
                        .status(Status.OPEN_ESCALATED)
                        .build();
        createAndUpdateAnnotation(docRef, annotation);

        // Try and fetch the annotation
        final Annotation annotationResponse = getAnnotation(docRef.getUuid(), annotation.getId());
        assertEquals(annotation.getContent(), annotationResponse.getContent());

        final List<Annotation> page = searchAnnotation(docRef, annotation.getContent());
        assertEquals(1, page.size());
        final Annotation firstFound = page.get(0);

        assertEquals(annotation.getId(), firstFound.getId());
        assertEquals(annotation.getAssignTo(), firstFound.getAssignTo());
        assertEquals(annotation.getContent(), firstFound.getContent());
        assertEquals(annotation.getStatus(), firstFound.getStatus());

        auditLogRule.check().thereAreAtLeast(5)
                .containsOrdered(containsAllOf(AuditedDocRefResourceImpl.CREATE_DOC_REF, docRef.getUuid()))
                .containsOrdered(containsAllOf(AuditedAnnotationsResourceImpl.CREATE_ANNOTATION, annotation.getId()))
                .containsOrdered(containsAllOf(AuditedAnnotationsResourceImpl.UPDATE_ANNOTATION, annotation.getId()))
                .containsOrdered(containsAllOf(AuditedAnnotationsResourceImpl.GET_ANNOTATION, annotation.getId()))
                .containsOrdered(containsAllOf(AuditedAnnotationsResourceImpl.SEARCH_ANNOTATIONS, annotation.getContent()));
    }

    @Test
    public void testSearchPages() {
        // Generate an UUID we can embed into the content of some annotations so we can find them
        final DocRef docRef = createDocument();

        auditLogRule.check().thereAreAtLeast(1)
                .containsOrdered(containsAllOf(AuditedDocRefResourceImpl.CREATE_DOC_REF, docRef.getUuid()));

        final int NUMBER_SEARCH_TERMS = 3;
        final int NUMBER_PAGES_EXPECTED = 3;
        final int ANNOTATIONS_PER_SEARCH_TERM = SEARCH_PAGE_LIMIT * NUMBER_PAGES_EXPECTED;
        final int TOTAL_ANNOTATIONS = ANNOTATIONS_PER_SEARCH_TERM * NUMBER_SEARCH_TERMS;
        final List<String> searchTerms = IntStream.range(0, NUMBER_SEARCH_TERMS)
                .mapToObj(i -> UUID.randomUUID().toString())
                .collect(Collectors.toList());

        // Create some test data for each search term
        final Map<String, Set<Annotation>> annotationsBySearchTerm = new HashMap<>();
        for (final String searchTerm : searchTerms) {
            final Set<Annotation> annotations = IntStream.range(0, ANNOTATIONS_PER_SEARCH_TERM)
                    .mapToObj(i -> UUID.randomUUID().toString()) // Generate an ID
                    .map(uuid -> new Annotation.Builder().id(uuid)
                            .dataSourceUuid(docRef.getUuid())
                            .content(UUID.randomUUID().toString() + searchTerm)
                            .assignTo(UUID.randomUUID().toString())
                            .status(Status.OPEN_ESCALATED)
                            .build())
                    .peek(a -> this.createAndUpdateAnnotation(docRef, a)) // add to database
                    .collect(Collectors.toSet());
            annotationsBySearchTerm.put(searchTerm, annotations);
        }

        // Check for create/update audit logs for all annotations
        final FifoLogbackRule.LogChecker createUpdateAuditChecker =
                auditLogRule.check().thereAreAtLeast(2 * TOTAL_ANNOTATIONS);
        annotationsBySearchTerm.values().stream()
                .flatMap(Set::stream)
                .forEach(annotation -> {
                    createUpdateAuditChecker
                            .containsAnywhere(
                                    containsAllOf(
                                            AuditedAnnotationsResourceImpl.CREATE_ANNOTATION,
                                            annotation.getId()))
                            .containsAnywhere(
                                    containsAllOf(
                                            AuditedAnnotationsResourceImpl.UPDATE_ANNOTATION,
                                            annotation.getId()));
                });

        // Print all the entries created
        LOGGER.trace("Annotation ID's Created");
        annotationsBySearchTerm.entrySet().forEach(s -> {
            LOGGER.trace(String.format("Search Term: %s", s.getKey()));

            s.getValue().forEach(annotationDTO -> {
                LOGGER.trace(String.format("\t%s", annotationDTO.getId()));
            });
        });

        LOGGER.trace("Annotation ID's Found");
        annotationsBySearchTerm.forEach((searchTerm, annotationsSet) -> {
            LOGGER.trace(String.format("Search Term: %s", searchTerm));

            final Set<Annotation> resultsSet = new HashSet<>();

            // Get all the expected pages
            int seekPosition = 0;
            for (int page = 0; page < NUMBER_PAGES_EXPECTED; page++) {
                LOGGER.trace(String.format("Page Found %d", page));

                final List<Annotation> thisPage = searchAnnotation(docRef, searchTerm, seekPosition);

                // ensures all of these new results do not already appear in our gathered results
                for (final Annotation result: thisPage) {
                    LOGGER.trace(String.format("\t%s", result.getId()));
                    assertFalse(resultsSet.contains(result));
                    seekPosition++;
                }

                resultsSet.addAll(thisPage);
            }

            assertEquals(annotationsSet, resultsSet);
        });

        // Check all the audit logs exist for page searching
        final FifoLogbackRule.LogChecker pageSearchAuditChecker =
                auditLogRule.check().thereAreAtLeast(NUMBER_SEARCH_TERMS * NUMBER_PAGES_EXPECTED);
        annotationsBySearchTerm.forEach(
                (searchTerm, s) -> IntStream.range(0, NUMBER_PAGES_EXPECTED).forEach(
                        o -> pageSearchAuditChecker.containsOrdered(
                                containsAllOf(
                                        AuditedAnnotationsResourceImpl.SEARCH_ANNOTATIONS,
                                        searchTerm
                                )
                        )
                )
        );
    }

    /**
     * Utility function to randomly generate a new annotations index doc ref.
     * It assumes that the creation of documents works, the detail of that is tested in another suite of tests.
     * It will also give READ, UPDATE, DELETE permissions to the admin user.
     * @return The DocRef of the newly created annotations index.
     */
    private DocRef createDocument() {
        // Generate UUID's for the doc ref and it's parent folder
        final String parentFolderUuid = UUID.randomUUID().toString();
        final DocRef docRef = new DocRef.Builder()
                .uuid(UUID.randomUUID().toString())
                .type(AnnotationsDocRefEntity.TYPE)
                .name(UUID.randomUUID().toString())
                .build();

        // Create a doc ref to hang the search from
        authRule.giveFolderCreatePermission(authRule.adminUser(), parentFolderUuid);
        final Response createResponse = docRefClient.createDocument(authRule.adminUser(), docRef.getUuid(), docRef.getName(), parentFolderUuid);
        assertEquals(HttpStatus.OK_200, createResponse.getStatus());

        // Give the admin user complete permissions over the document
        authRule.giveDocumentPermission(authRule.adminUser(), docRef.getUuid(), DocumentPermission.UPDATE);
        authRule.giveDocumentPermission(authRule.adminUser(), docRef.getUuid(), DocumentPermission.READ);
        authRule.giveDocumentPermission(authRule.adminUser(), docRef.getUuid(), DocumentPermission.DELETE);

        return docRef;
    }

    /**
     * Creates an annotation, then updates it to set all the fields from the given fully
     * populated annotation object. This is used by tests which assume that create/update work
     * and they just need to dump data into the database to test other functions.
     *
     * @param docRef The Document of the index the annotation belongs to
     * @param annotation The annotation to create
     * @return The initial state of the annotation
     */
    private Annotation createAndUpdateAnnotation(final DocRef docRef, final Annotation annotation) {
        Annotation result = null;

        try {
            final Response createResponse = annotationsClient.create(authRule.adminUser(), docRef.getUuid(), annotation.getId());
            assertEquals(HttpStatus.OK_200, createResponse.getStatus());

            final Response updateResponse = annotationsClient.update(authRule.adminUser(), docRef.getUuid(), annotation.getId(), annotation);
            assertEquals(HttpStatus.OK_200, updateResponse.getStatus());
        } catch (Exception e) {
            fail(e.getLocalizedMessage());
        }

        return result;
    }

    /**
     * Searches for annotations that contain the queryTerm given
     * @param docRef The Document of the index to search
     * @param queryTerm The term to search for
     * @return The list of annotations found by the service.
     */
    private List<Annotation> searchAnnotation(final DocRef docRef, final String queryTerm) {
        return searchAnnotation(docRef, queryTerm, 0);
    }

    /**
     * Searches for annotations that contain the queryTerm given,
     * using pagination that picks up from after the last annotation in the given list
     *
     * @param docRef The Document of the index to search
     * @param queryTerm The term to search for
     * @param seekPosition Pagination continue
     * @return The list of annotations found by the service.
     */
    private List<Annotation> searchAnnotation(final DocRef docRef,
                                              final String queryTerm,
                                              final Integer seekPosition) {
        List<Annotation> result = null;

        try {
            final Response response = annotationsClient.search(authRule.adminUser(), docRef.getUuid(), queryTerm, seekPosition);
            assertEquals(HttpStatus.OK_200, response.getStatus());

            result = response.readEntity(new GenericType<List<Annotation>>(){});
        } catch (Exception e) {
            fail(e.getLocalizedMessage());
        }

        return result;
    }

    /**
     * Given an ID, calls GET on the service to retrieve that specific annotation.
     * This function asserts that the annotation was found correctly.
     *
     * @param index the name of the index to search
     * @param id The ID to find.
     * @return The current annotation for that ID from the service.
     */
    private Annotation getAnnotation(final String index, final String id) {
        Annotation result = null;

        try {
            final Response response = annotationsClient.get(authRule.adminUser(), index, id);

            assertEquals(HttpStatus.OK_200, response.getStatus());

            result = response.readEntity(Annotation.class);

            assertEquals(id, result.getId());
        } catch (Exception e) {
            fail(e.getLocalizedMessage());
        }

        return result;
    }
}
