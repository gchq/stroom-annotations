package stroom.annotations.resources;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.mashape.unirest.http.HttpResponse;
import com.mashape.unirest.http.Unirest;
import com.mashape.unirest.http.exceptions.UnirestException;
import io.dropwizard.testing.junit.DropwizardAppRule;
import org.apache.http.HttpStatus;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.ClassRule;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import stroom.annotations.App;
import stroom.annotations.Config;
import stroom.annotations.hibernate.Annotation;
import stroom.annotations.hibernate.AnnotationHistory;
import stroom.annotations.hibernate.HistoryOperation;
import stroom.annotations.hibernate.Status;
import stroom.datasource.api.v2.DataSource;
import stroom.datasource.api.v2.DataSourceField;
import stroom.query.api.v2.*;
import stroom.query.audit.FifoLogbackAppender;

import javax.ws.rs.core.MediaType;
import java.io.IOException;
import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import static io.dropwizard.testing.ResourceHelpers.resourceFilePath;
import static org.junit.Assert.*;
import static stroom.annotations.service.AnnotationsServiceImpl.SEARCH_PAGE_LIMIT;

public class AnnotationsResourcesIT {
    private static final Logger LOGGER = LoggerFactory.getLogger(AnnotationsResourcesIT.class);

    @ClassRule
    public static final DropwizardAppRule<Config> appRule = new DropwizardAppRule<>(App.class, resourceFilePath("config.yml"));

    private static String annotationsUrl;

    private static String queryUrl;

    private static final com.fasterxml.jackson.databind.ObjectMapper jacksonObjectMapper =
            new com.fasterxml.jackson.databind.ObjectMapper();

    private static String getAnnotationUrl(final String index, final String id) {
        return String.format("%s/single/%s/%s", annotationsUrl, index, id);
    }

    private static String getHistoryUrl(final String index, final String id) {
        return String.format("%s/single/%s/%s/history", annotationsUrl, index, id);
    }

    private static String getSearchUrl(final String index) {
        return String.format("%s/search/%s", annotationsUrl, index);
    }

    private static String getQueryDataSourceUrl() {
        return String.format("%s/dataSource", queryUrl);
    }

    private static String getQuerySearchUrl() {
        return String.format("%s/search", queryUrl);
    }

    private static String getQueryDestroyUrl() {
        return String.format("%s/destroy", queryUrl);
    }

    @BeforeClass
    public static void setupClass() {
        int appPort = appRule.getLocalPort();

        annotationsUrl = "http://localhost:" + appPort + "/annotations/v1";
        queryUrl = "http://localhost:" + appPort + "/queryApi/v1";

        Unirest.setObjectMapper(new com.mashape.unirest.http.ObjectMapper() {
            private com.fasterxml.jackson.databind.ObjectMapper jacksonObjectMapper
                    = new com.fasterxml.jackson.databind.ObjectMapper();

            public <T> T readValue(String value, Class<T> valueType) {
                try {
                    return jacksonObjectMapper.readValue(value, valueType);
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }
            }

            public String writeValue(Object value) {
                try {
                    return jacksonObjectMapper.writeValueAsString(value);
                } catch (JsonProcessingException e) {
                    throw new RuntimeException(e);
                }
            }
        });
    }

    @Before
    public void beforeTest() {
        FifoLogbackAppender.popLogs();
    }

    private void checkAuditLogs(final int expected) {
        final List<Object> records = FifoLogbackAppender.popLogs();

        LOGGER.info(String.format("Expected %d records, received %d", expected, records.size()));

        assertEquals(expected, records.size());
    }

    @Test
    public void testWelcome() throws UnirestException {
        final HttpResponse<String> response = Unirest
                .get(annotationsUrl + "/static/welcome")
                .asString();

        assertEquals(AnnotationsResourceImpl.WELCOME_TEXT, response.getBody());
    }

    @Test
    public void testStatusValues() throws UnirestException, IOException {
        final HttpResponse<String> response = Unirest
                .get(annotationsUrl + "/static/statusValues")
                .asString();

        final Map<String, String> responseStatusValues = jacksonObjectMapper.readValue(response.getBody(), new TypeReference<Map<String, String>>(){});
        final Map<String, String> statusValues = Arrays.stream(Status.values())
                .collect(Collectors.toMap(Object::toString, Status::getDisplayText));

        assertEquals(HttpStatus.SC_OK, response.getStatus());
        assertEquals(statusValues, responseStatusValues);
    }

    @Test
    public void testCreateAnnotation() throws UnirestException, IOException {
        final String index = UUID.randomUUID().toString();
        final String id = UUID.randomUUID().toString();
        createAnnotation(index, id);

        // Create
        checkAuditLogs(1);
    }

    @Test
    public void testCreateAndGetAnnotation() throws UnirestException, IOException {
        final String index = UUID.randomUUID().toString();
        final String id = UUID.randomUUID().toString();
        createAnnotation(index, id);

        final Annotation annotationResponse = getAnnotation(index, id);
        assertNotNull(annotationResponse);

        // Create
        checkAuditLogs(2);
    }

    @Test
    public void testCreateUpdateAndGetAnnotation() {
        // Create some test data
        final String index = UUID.randomUUID().toString();
        final int RECORDS_TO_CREATE = 10;
        final Map<String, Annotation> annotations = IntStream.range(0, RECORDS_TO_CREATE)
                .mapToObj(i -> UUID.randomUUID().toString())
                .map(uuid -> new Annotation.Builder().id(uuid)
                        .content(UUID.randomUUID().toString())
                        .assignTo(UUID.randomUUID().toString())
                        .status(Status.OPEN_ESCALATED)
                        .build())
                .peek(a -> this.createAnnotation(index, a)) // add to database
                .peek(a -> this.updateAnnotation(index, a)) // update with initial state
                .collect(Collectors.toMap(Annotation::getId, Function.identity()));

        // Try and fetch each annotation
        annotations.forEach((id, annotation) -> {
            final Annotation annotationResponse = getAnnotation(index, id);
            assertUserSetFieldsEqual(annotation, annotationResponse);
        });

        // Records * create, update, get
        checkAuditLogs(3 * RECORDS_TO_CREATE);
    }

    @Test
    public void testDelete() throws UnirestException {
        final String index = UUID.randomUUID().toString();
        final String id = UUID.randomUUID().toString();
        createAnnotation(index, id);
        getAnnotation(index, id);
        deleteAnnotation(index, id);
        HttpResponse<String> result = Unirest
                .get(getAnnotationUrl(index, id))
                .asString();
        assertEquals(HttpStatus.SC_NOT_FOUND, result.getStatus());

        // Create, Get, Delete, Get
        checkAuditLogs(4);
    }

    @Test
    public void testGetHistory() {
        final String index = UUID.randomUUID().toString();
        final String id = UUID.randomUUID().toString();
        final int numberUpdates = 5;
        createAnnotation(index, id); // create initial empty annotation

        final List<Annotation> annotationHistory =
                IntStream.range(0, numberUpdates).mapToObj(i -> new Annotation.Builder()
                        .id(id)
                        .content(UUID.randomUUID().toString())
                        .assignTo(UUID.randomUUID().toString())
                        .status(Status.OPEN_ESCALATED)
                        .build())
                        .peek(a -> this.updateAnnotation(index, a)) // push update to database
                        .collect(Collectors.toList());

        deleteAnnotation(index, id);

        final List<AnnotationHistory> result = getHistory(index, id);
        assertEquals(numberUpdates + 2, result.size());

        assertEquals(HistoryOperation.CREATE, result.get(0).getOperation());
        IntStream.range(0, annotationHistory.size()).forEach(i -> {
            assertEquals(HistoryOperation.UPDATE, result.get(i + 1).getOperation());
        });
        assertEquals(HistoryOperation.DELETE, result.get(6).getOperation());

        // Createm, X updates, delete, getHistory
        checkAuditLogs(3 + numberUpdates);
    }

    @Test
    public void testSearchSingle() {
        // Create some test data
        final String index = UUID.randomUUID().toString();
        final Annotation annotation = new Annotation.Builder().id(UUID.randomUUID().toString())
                        .content(UUID.randomUUID().toString())
                        .assignTo(UUID.randomUUID().toString())
                        .status(Status.OPEN_ESCALATED)
                        .build();
        createAnnotation(index, annotation);
        updateAnnotation(index, annotation);

        // Try and fetch each annotation
        final Annotation annotationResponse = getAnnotation(index, annotation.getId());
        assertEquals(annotation.getContent(), annotationResponse.getContent());

        final List<Annotation> page = searchAnnotation(index, annotation.getContent());
        assertEquals(1, page.size());
        assertUserSetFieldsEqual(annotation, page.get(0));

        // Records * create, update, get
        checkAuditLogs(4);
    }

    @Test
    public void testSearchPages() {
        // Generate an UUID we can embed into the content of some annotations so we can find them
        final String index = UUID.randomUUID().toString();
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
                            .dataSourceUuid(index)
                            .content(UUID.randomUUID().toString() + searchTerm)
                            .assignTo(UUID.randomUUID().toString())
                            .status(Status.OPEN_ESCALATED)
                            .build())
                    .peek(a -> this.createAnnotation(index, a)) // add to database
                    .peek(a -> this.updateAnnotation(index, a)) // update with initial state
                    .collect(Collectors.toSet());
            annotationsBySearchTerm.put(searchTerm, annotations);
        }

        // Print all the entries created
        LOGGER.info("Annotation ID's Created");
        annotationsBySearchTerm.entrySet().forEach(s -> {
            LOGGER.info(String.format("Search Term: %s", s.getKey()));

            s.getValue().forEach(annotationDTO -> {
                LOGGER.info(String.format("\t%s", annotationDTO.getId()));
            });
        });

        LOGGER.info("Annotation ID's Found");
        annotationsBySearchTerm.forEach((searchTerm, annotationsSet) -> {
            LOGGER.info(String.format("Search Term: %s", searchTerm));

            final Set<Annotation> resultsSet = new HashSet<>();

            // Get all the expected pages
            int seekPosition = 0;
            for (int page = 0; page < NUMBER_PAGES_EXPECTED; page++) {
                LOGGER.info(String.format("Page Found %d", page));

                List<Annotation> thisPage = searchAnnotation(index, searchTerm, seekPosition);

                // ensures all of these new results do not already appear in our gathered results
                for (final Annotation result: thisPage) {
                    LOGGER.info(String.format("\t%s", result.getId()));
                    assertFalse(resultsSet.contains(result));
                    seekPosition++;
                }

                resultsSet.addAll(thisPage);
            }

            assertEquals(annotationsSet, resultsSet);
        });

        checkAuditLogs((2 * TOTAL_ANNOTATIONS) + (NUMBER_SEARCH_TERMS * NUMBER_PAGES_EXPECTED));
    }


    @Test
    public void testQuerySearch() {
        // Generate an UUID we can embed into the content of some annotations so we can find them
        final String index = UUID.randomUUID().toString();
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
                            .dataSourceUuid(index)
                            .content("Some Content - " + UUID.randomUUID().toString() + contentSearchTerm)
                            .assignTo("Some Guy - " + UUID.randomUUID().toString())
                            .status(Status.OPEN_ESCALATED)
                            .build())
                    .peek(a -> this.createAnnotation(index, a)) // add to database
                    .peek(a -> this.updateAnnotation(index, a)) // update with initial state
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
                    .addTerm()
                        .field(Annotation.CONTENT)
                        .condition(ExpressionTerm.Condition.CONTAINS)
                        .value(contentSearchTerm)
                        .end()
                    .build();

            final Set<String> resultsSet = new HashSet<>();
            final Set<String> expectedAnnotationIds = annotationsSet.stream()
                    .map(Annotation::getId)
                    .collect(Collectors.toSet());

            pageOffsets.forEach(offsetRange -> {
                final SearchResponse searchResponse = querySearch(index, expressionOperator, offsetRange);

                for (final Result result : searchResponse.getResults()) {
                    assertTrue(result instanceof FlatResult);

                    final FlatResult flatResult = (FlatResult) result;
                    flatResult.getValues().stream()
                            .map(objects -> objects.get(3))
                            .map(Object::toString)
                            .forEach(resultsSet::add);
                }
            });

            assertEquals(expectedAnnotationIds, resultsSet);
        });

        checkAuditLogs((2 * TOTAL_ANNOTATIONS) + (NUMBER_SEARCH_TERMS * NUMBER_PAGES_EXPECTED));
    }

    @Test
    public void testGetDataSource() {
        final DataSource result = getDataSource();

        final Set<String> resultFieldNames = result.getFields().stream()
                .map(DataSourceField::getName)
                .collect(Collectors.toSet());

        assertTrue(resultFieldNames.contains(Annotation.ID));
        assertTrue(resultFieldNames.contains(Annotation.CONTENT));
        assertTrue(resultFieldNames.contains(Annotation.ASSIGN_TO));
        assertTrue(resultFieldNames.contains(Annotation.LAST_UPDATED));
        assertTrue(resultFieldNames.contains(Annotation.UPDATED_BY));
        assertTrue(resultFieldNames.contains(Annotation.STATUS));

        checkAuditLogs(1);
    }

    private DataSource getDataSource() {
        DataSource result = null;

        try {
            final HttpResponse<String> response = Unirest
                    .post(getQueryDataSourceUrl())
                    .header("accept", MediaType.APPLICATION_JSON)
                    .asString();

            assertEquals(HttpStatus.SC_OK, response.getStatus());

            result = jacksonObjectMapper.readValue(response.getBody(), DataSource.class);
        } catch (UnirestException | IOException e) {
            fail(e.getLocalizedMessage());
        }

        return result;
    }

    private SearchResponse querySearch(final String index,
                                       final ExpressionOperator expressionOperator,
                                       final OffsetRange offsetRange) {
        SearchResponse result = null;

        try {
            final String queryKey = UUID.randomUUID().toString();
            final SearchRequest request = new SearchRequest.Builder()
                    .query()
                        .dataSource("docRefName", index, "docRefType")
                        .expression(expressionOperator)
                        .end()
                    .key(queryKey)
                    .dateTimeLocale("en-gb")
                    .incremental(true)
                    .addResultRequest()
                        .fetch(ResultRequest.Fetch.ALL)
                        .resultStyle(ResultRequest.ResultStyle.FLAT)
                        .componentId("componentId")
                        .requestedRange(offsetRange)
                        .addMapping()
                            .queryId(queryKey)
                            .extractValues(false)
                            .showDetail(false)
                            .addField(Annotation.ID, "${" + Annotation.ID + "}").end()
                            .addMaxResults(10)
                            .end()
                        .end()
                    .build();

            final HttpResponse<String> response = Unirest
                    .post(getQuerySearchUrl())
                    .header("accept", "application/json")
                    .header("Content-Type", "application/json")
                    .body(request)
                    .asString();

            assertEquals(HttpStatus.SC_OK, response.getStatus());

            result = jacksonObjectMapper.readValue(response.getBody(), SearchResponse.class);
        } catch (UnirestException | IOException e) {
            fail(e.getLocalizedMessage());

        }

        return result;
    }

    /**
     * Various utility functions for making calls to the REST API
     */

    /**
     * Given an annotation DTO, creates an annotation for the ID. It makes no use of the
     * other property values in the annotation, a further update call would be required to set the other values.
     * @param index the name of the index the annotation belongs to
     * @param annotation The annotation to create
     * @return The initial state of the annotation
     */
    private Annotation createAnnotation(final String index, final Annotation annotation) {
        return createAnnotation(index, annotation.getId());
    }

    /**
     * Creates an annotation for the ID given.
     * @param index the name of the index the annotation belongs to
     * @param id The ID to annotate
     * @return The initial state of the annotation
     */
    private Annotation createAnnotation(final String index, final String id) {
        Annotation result = null;
        try {
            HttpResponse<String> response = Unirest.post(getAnnotationUrl(index, id))
                    .asString();
            assertEquals(HttpStatus.SC_OK, response.getStatus());

            result = jacksonObjectMapper.readValue(response.getBody(), Annotation.class);
            assertEquals(id, result.getId());
            assertEquals(Annotation.DEFAULT_CONTENT, result.getContent());
            assertEquals(Annotation.DEFAULT_ASSIGNEE, result.getAssignTo());
            assertEquals(Annotation.DEFAULT_UPDATED_BY, result.getUpdatedBy());
            assertEquals(Annotation.DEFAULT_STATUS, result.getStatus());
        } catch (Exception e) {
            fail(e.getLocalizedMessage());
        }
        return result;
    }

    /**
     * Updates the annotation by PUTting the new annotation data.
     * @param index the name of the index the annotation belongs to
     * @param annotation The annotation to update
     * @return The updated annotation returned from the service.
     */
    private Annotation updateAnnotation(final String index, final Annotation annotation) {
        Annotation result = null;

        try {
            // Set the content in an update
            HttpResponse<String> response = Unirest
                    .put(getAnnotationUrl(index, annotation.getId()))
                    .header("accept", "application/json")
                    .header("Content-Type", "application/json")
                    .body(annotation)
                    .asString();
            assertEquals(HttpStatus.SC_OK, response.getStatus());

            result = jacksonObjectMapper.readValue(response.getBody(), Annotation.class);
            assertUserSetFieldsEqual(annotation, result);
        } catch (Exception e) {
            fail(e.getLocalizedMessage());
        }
        return result;
    }

    private void assertUserSetFieldsEqual(final Annotation first, final Annotation second) {
        assertEquals(first.getId(), second.getId());
        assertEquals(first.getAssignTo(), second.getAssignTo());
        assertEquals(first.getContent(), second.getContent());
        assertEquals(first.getStatus(), second.getStatus());
    }

    /**
     * Searches for annotations that contain the queryTerm given
     * @param index the name of the index to search
     * @param queryTerm The term to search for
     * @return The list of annotations found by the service.
     */
    private List<Annotation> searchAnnotation(final String index, final String queryTerm) {
        List<Annotation> result = null;

        try {
            final HttpResponse<String> response = Unirest
                    .get(getSearchUrl(index))
                    .queryString("q", queryTerm)
                    .asString();

            assertEquals(HttpStatus.SC_OK, response.getStatus());

            result = jacksonObjectMapper.readValue(response.getBody(), new TypeReference<List<Annotation>>(){});
        } catch (Exception e) {
            fail(e.getLocalizedMessage());
        }

        return result;
    }

    /**
     * Searches for annotations that contain the queryTerm given,
     * using pagination that picks up from after the last annotation in the given list
     * @par
     * @param index the name of the index to search
     * @param queryTerm The term to search for
     * @param seekPosition Pagination continue
     * @return The list of annotations found by the service.
     */
    private List<Annotation> searchAnnotation(final String index,
                                                 final String queryTerm,
                                                 final Integer seekPosition) {
        List<Annotation> result = null;

        try {
            final HttpResponse<String> response = Unirest
                    .get(getSearchUrl(index))
                    .queryString("q", queryTerm)
                    .queryString("seekPosition", seekPosition)
                    .asString();

            assertEquals(HttpStatus.SC_OK, response.getStatus());

            result = jacksonObjectMapper.readValue(response.getBody(), new TypeReference<List<Annotation>>(){});
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
            final HttpResponse<String> response = Unirest
                    .get(getAnnotationUrl(index, id))
                    .asString();

            assertEquals(HttpStatus.SC_OK, response.getStatus());

            result = jacksonObjectMapper.readValue(response.getBody(), Annotation.class);

            assertEquals(id, result.getId());
        } catch (Exception e) {
            fail(e.getLocalizedMessage());
        }

        return result;
    }

    /**
     * Given an ID, attempts to delete the annotation
     * @param index the name of the index to search
     * @param id The ID to delete
     */
    private void deleteAnnotation(final String index, final String id) {
        try {
            final HttpResponse<String> response = Unirest.delete(getAnnotationUrl(index, id)).asString();
            assertEquals(HttpStatus.SC_OK, response.getStatus());
        } catch (UnirestException e) {
            fail(e.getLocalizedMessage());
        }
    }

    /**
     * Retrieves the annotations history for a particular ID. This can be called for ID's that have been deleted.
     * @param index the name of the index to search
     * @param id The ID to find the history for
     * @return The list of history items.
     */
    private List<AnnotationHistory> getHistory(final String index, final String id) {
        List<AnnotationHistory> result = null;

        try {
            final HttpResponse<String> response = Unirest
                    .get(getHistoryUrl(index, id))
                    .asString();

            assertEquals(HttpStatus.SC_OK, response.getStatus());

            result = jacksonObjectMapper.readValue(response.getBody(), new TypeReference<List<AnnotationHistory>>() {});
        } catch (Exception e) {
            fail(e.getLocalizedMessage());
        }

        return result;
    }
}
