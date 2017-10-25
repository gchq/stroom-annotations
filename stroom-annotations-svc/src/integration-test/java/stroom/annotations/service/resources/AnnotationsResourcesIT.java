package stroom.annotations.service.resources;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.mashape.unirest.http.HttpResponse;
import com.mashape.unirest.http.Unirest;
import com.mashape.unirest.http.exceptions.UnirestException;
import io.dropwizard.testing.junit.DropwizardAppRule;
import org.apache.http.HttpStatus;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.junit.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import stroom.annotations.service.App;
import stroom.annotations.service.Config;
import stroom.annotations.service.hibernate.Annotation;
import stroom.annotations.service.model.AnnotationDTO;
import stroom.annotations.service.model.AnnotationHistoryDTO;
import stroom.annotations.service.model.HistoryOperation;
import stroom.annotations.service.model.Status;
import stroom.datasource.api.v2.DataSource;
import stroom.datasource.api.v2.DataSourceField;
import stroom.query.api.v2.*;

import javax.ws.rs.core.MediaType;
import java.io.IOException;
import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import static io.dropwizard.testing.ResourceHelpers.resourceFilePath;
import static org.junit.Assert.*;

public class AnnotationsResourcesIT {
    private static final Logger LOGGER = LoggerFactory.getLogger(AnnotationsResourcesIT.class);

    @ClassRule
    public static final DropwizardAppRule<Config> appRule = new DropwizardAppRule<>(App.class, resourceFilePath("config.yml"));

    private static String annotationsUrl;

    private static String queryUrl;

    private static KafkaAuditTestConsumer kafkaTestConsumer;

    private static final com.fasterxml.jackson.databind.ObjectMapper jacksonObjectMapper =
            new com.fasterxml.jackson.databind.ObjectMapper();

    private static String getAnnotationUrl(final String id) {
        return String.format("%s/single/%s", annotationsUrl, id);
    }

    private static String getHistoryUrl(final String id) {
        return String.format("%s/single/%s/history", annotationsUrl, id);
    }

    private static String getSearchUrl() {
        return String.format("%s/search", annotationsUrl);
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
        queryUrl = "http://localhost:" + appPort + "/annotationsQuery/v1";

        kafkaTestConsumer = new KafkaAuditTestConsumer();

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

        kafkaTestConsumer.getRecords(1);
    }

    @AfterClass
    public static void afterClass() {
        kafkaTestConsumer.close();
    }

    @Before
    public void beforeTest() {
    }

    @After
    public void afterTest() {
        kafkaTestConsumer.commitSync();
    }

    private void checkAuditLogs(final int expected) {
        final List<ConsumerRecord<String, String>> records = kafkaTestConsumer.getRecords(expected);

        for (ConsumerRecord<String, String> record : records) {
            LOGGER.info(String.format("offset = %d, key = %s, value = %s%n", record.offset(), record.key(), record.value()));
        }

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
        final String id = UUID.randomUUID().toString();
        createAnnotation(id);

        // Create
        checkAuditLogs(1);
    }

    @Test
    public void testCreateUpdateAndGetAnnotation() {
        // Create some test data
        final int RECORDS_TO_CREATE = 10;
        final Map<String, AnnotationDTO> annotations = IntStream.range(0, RECORDS_TO_CREATE)
                .mapToObj(i -> UUID.randomUUID().toString())
                .map(uuid -> new AnnotationDTO.Builder().id(uuid)
                        .content(UUID.randomUUID().toString())
                        .assignTo(UUID.randomUUID().toString())
                        .status(Status.OPEN_ESCALATED)
                        .build())
                .peek(this::createAnnotation) // add to database
                .peek(this::updateAnnotation) // update with initial state
                .collect(Collectors.toMap(AnnotationDTO::getId, Function.identity()));

        // Try and fetch each annotation
        annotations.forEach((id, annotation) -> {
            final AnnotationDTO annotationResponse = getAnnotation(id);
            assertEquals(annotation.getContent(), annotationResponse.getContent());
        });

        // Records * create, update, get
        checkAuditLogs(3 * RECORDS_TO_CREATE);
    }

    @Test
    public void testDelete() throws UnirestException {
        final String id = UUID.randomUUID().toString();
        createAnnotation(id);
        getAnnotation(id);
        deleteAnnotation(id);
        HttpResponse<String> result = Unirest
                .get(getAnnotationUrl(id))
                .asString();
        assertEquals(HttpStatus.SC_NOT_FOUND, result.getStatus());

        // Create, Get, Delete, Get
        checkAuditLogs(4);
    }

    @Test
    public void testGetHistory() {
        final String id = UUID.randomUUID().toString();
        final int numberUpdates = 5;
        createAnnotation(id); // create initial empty annotation

        final List<AnnotationDTO> annotationHistory =
                IntStream.range(0, numberUpdates).mapToObj(i -> new AnnotationDTO.Builder()
                        .id(id)
                        .content(UUID.randomUUID().toString())
                        .assignTo(UUID.randomUUID().toString())
                        .status(Status.OPEN_ESCALATED)
                        .build())
                        .peek(this::updateAnnotation) // push update to database
                        .collect(Collectors.toList());

        deleteAnnotation(id);

        final List<AnnotationHistoryDTO> result = getHistory(id);
        assertEquals(numberUpdates + 2, result.size());

        assertEquals(HistoryOperation.CREATE, result.get(0).getOperation());
        IntStream.range(0, annotationHistory.size()).forEach(index -> {
            assertEquals(HistoryOperation.UPDATE, result.get(index + 1).getOperation());
            assertEquals(annotationHistory.get(index), result.get(index + 1).getAnnotation());
        });
        assertEquals(HistoryOperation.DELETE, result.get(6).getOperation());

        // Createm, X updates, delete, getHistory
        checkAuditLogs(3 + numberUpdates);
    }

    @Test
    public void testSearch() {
        // Generate an UUID we can embed into the content of some annotations so we can find them
        final int NUMBER_SEARCH_TERMS = 4;
        final int NUMBER_PAGES_EXPECTED = 3;
        final int ANNOTATIONS_PER_SEARCH_TERM = AnnotationsResourceImpl.SEARCH_PAGE_LIMIT * NUMBER_PAGES_EXPECTED;
        final int TOTAL_ANNOTATIONS = ANNOTATIONS_PER_SEARCH_TERM * NUMBER_SEARCH_TERMS;
        final List<String> searchTerms = IntStream.range(0, NUMBER_SEARCH_TERMS)
                .mapToObj(i -> UUID.randomUUID().toString())
                .collect(Collectors.toList());

        // Create some test data for each search term
        final Map<String, Set<AnnotationDTO>> annotationsBySearchTerm = new HashMap<>();
        for (final String searchTerm : searchTerms) {
            final Set<AnnotationDTO> annotations = IntStream.range(0, ANNOTATIONS_PER_SEARCH_TERM)
                    .mapToObj(i -> UUID.randomUUID().toString()) // Generate an ID
                    .map(uuid -> new AnnotationDTO.Builder().id(uuid)
                            .content(UUID.randomUUID().toString() + searchTerm)
                            .assignTo(UUID.randomUUID().toString())
                            .status(Status.OPEN_ESCALATED)
                            .build())
                    .peek(this::createAnnotation) // add to database
                    .peek(this::updateAnnotation) // update with initial state
                    .collect(Collectors.toSet());
            annotationsBySearchTerm.put(searchTerm, annotations);
        }

        annotationsBySearchTerm.forEach((searchTerm, annotationsSet) -> {
            final Set<AnnotationDTO> resultsSet = new HashSet<>();

            // Get all the expected pages
            List<AnnotationDTO> lastPage = null;
            for (int page = 0; page < NUMBER_PAGES_EXPECTED; page++) {
                lastPage = searchAnnotation(searchTerm, lastPage);

                // ensures all of these new results do not already appear in our gathered results
                for (final AnnotationDTO result: lastPage) {
                    assertFalse(resultsSet.contains(result));
                }

                resultsSet.addAll(lastPage);
            }

            assertEquals(annotationsSet, resultsSet);
        });

        checkAuditLogs((2 * TOTAL_ANNOTATIONS) + (NUMBER_SEARCH_TERMS * NUMBER_PAGES_EXPECTED));
    }


    @Test
    public void testQuerySearch() {
        // Generate an UUID we can embed into the content of some annotations so we can find them
        final int NUMBER_SEARCH_TERMS = 2;
        final int NUMBER_PAGES_EXPECTED = 3;
        final int ANNOTATIONS_PER_SEARCH_TERM = AnnotationsResourceImpl.SEARCH_PAGE_LIMIT * NUMBER_PAGES_EXPECTED;
        final int TOTAL_ANNOTATIONS = ANNOTATIONS_PER_SEARCH_TERM * NUMBER_SEARCH_TERMS;
        final List<String> contentSearchTerms = IntStream.range(0, NUMBER_SEARCH_TERMS)
                .mapToObj(i -> UUID.randomUUID().toString())
                .collect(Collectors.toList());

        // Create some test data for each search term
        final Map<String, Set<AnnotationDTO>> annotationsBySearchTerm = new HashMap<>();
        for (final String contentSearchTerm : contentSearchTerms) {
            final Set<AnnotationDTO> annotations = IntStream.range(0, ANNOTATIONS_PER_SEARCH_TERM)
                    .mapToObj(i -> UUID.randomUUID().toString()) // Generate an ID
                    .map(uuid -> new AnnotationDTO.Builder().id(uuid)
                            .content("Some Content - " + UUID.randomUUID().toString() + contentSearchTerm)
                            .assignTo("Some Guy - " + UUID.randomUUID().toString())
                            .status(Status.OPEN_ESCALATED)
                            .build())
                    .peek(this::createAnnotation) // add to database
                    .peek(this::updateAnnotation) // update with initial state
                    .collect(Collectors.toSet());
            annotationsBySearchTerm.put(contentSearchTerm, annotations);
        }

        final Collection<OffsetRange> pageOffsets = IntStream.range(0, NUMBER_PAGES_EXPECTED)
                .mapToObj(value -> new OffsetRange.Builder()
                        .length((long) AnnotationsResourceImpl.SEARCH_PAGE_LIMIT)
                        .offset((long) (value * AnnotationsResourceImpl.SEARCH_PAGE_LIMIT))
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
                    .map(AnnotationDTO::getId)
                    .collect(Collectors.toSet());

            pageOffsets.forEach(offsetRange -> {
                final SearchResponse searchResponse = querySearch(expressionOperator, offsetRange);

                for (final Result result : searchResponse.getResults()) {
                    assertTrue(result instanceof FlatResult);

                    final FlatResult flatResult = (FlatResult) result;
                    flatResult.getValues().stream()
                            .map(objects -> objects.get(3))
                            .map(Object::toString)
                            .forEach(resultsSet::add);
                }
            });

            LOGGER.debug("Pages of Annotation IDs");
            for (String s : resultsSet) {
                LOGGER.debug("\t" + s);
            }

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

    private SearchResponse querySearch(final ExpressionOperator expressionOperator,
                                       final OffsetRange offsetRange) {
        SearchResponse result = null;

        try {
            final String queryKey = UUID.randomUUID().toString();
            final SearchRequest request = new SearchRequest.Builder()
                    .query()
                        .dataSource("docRefName", UUID.randomUUID().toString(), "docRefType")
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
     * @param annotation The annotation to create
     * @return The initial state of the annotation
     */
    private AnnotationDTO createAnnotation(final AnnotationDTO annotation) {
        return createAnnotation(annotation.getId());
    }

    /**
     * Creates an annotation for the ID given.
     * @param id The ID to annotate
     * @return The initial state of the annotation
     */
    private AnnotationDTO createAnnotation(final String id) {
        AnnotationDTO result = null;
        try {
            HttpResponse<String> response = Unirest.post(getAnnotationUrl(id))
                    .asString();
            assertEquals(HttpStatus.SC_OK, response.getStatus());

            result = jacksonObjectMapper.readValue(response.getBody(), AnnotationDTO.class);
            assertEquals(id, result.getId());
            assertEquals(AnnotationDTO.DEFAULT_CONTENT, result.getContent());
            assertEquals(AnnotationDTO.DEFAULT_ASSIGNEE, result.getAssignTo());
            assertEquals(AnnotationDTO.DEFAULT_UPDATED_BY, result.getUpdatedBy());
            assertEquals(AnnotationDTO.DEFAULT_STATUS, result.getStatus());
        } catch (Exception e) {
            fail(e.getLocalizedMessage());
        }
        return result;
    }

    /**
     * Updates the annotation by PUTting the new annotation data.
     * @param annotation The annotation to update
     * @return The updated annotation returned from the service.
     */
    private AnnotationDTO updateAnnotation(final AnnotationDTO annotation) {
        AnnotationDTO result = null;

        try {
            // Set the content in an update
            HttpResponse<String> response = Unirest
                    .put(getAnnotationUrl(annotation.getId()))
                    .header("accept", "application/json")
                    .header("Content-Type", "application/json")
                    .body(annotation)
                    .asString();
            assertEquals(HttpStatus.SC_OK, response.getStatus());

            result = jacksonObjectMapper.readValue(response.getBody(), AnnotationDTO.class);
            assertEquals(annotation, result);
        } catch (Exception e) {
            fail(e.getLocalizedMessage());
        }
        return result;
    }

    /**
     * Searches for annotations that contain the queryTerm given
     * @param queryTerm The term to search for
     * @return The list of annotations found by the service.
     */
    private List<AnnotationDTO> searchAnnotation(final String queryTerm) {
        List<AnnotationDTO> result = null;

        try {
            final HttpResponse<String> response = Unirest
                    .get(getSearchUrl())
                    .queryString("q", queryTerm)
                    .asString();

            assertEquals(HttpStatus.SC_OK, response.getStatus());

            result = jacksonObjectMapper.readValue(response.getBody(), new TypeReference<List<AnnotationDTO>>(){});
        } catch (Exception e) {
            fail(e.getLocalizedMessage());
        }

        return result;
    }

    /**
     * Searches for annotations that contain the queryTerm given,
     * using pagination that picks up from after the last annotation in the given list
     * @param queryTerm The term to search for
     * @param lastPage The results so far, we want the next page
     * @return The list of annotations found by the service.
     */
    private List<AnnotationDTO> searchAnnotation(final String queryTerm,
                                                 final List<AnnotationDTO> lastPage) {
        if (null != lastPage) {
            assertTrue(lastPage.size() > 0);
            return searchAnnotation(queryTerm, lastPage.get(lastPage.size() - 1));
        } else {
            return searchAnnotation(queryTerm);
        }
    }

    /**
     * Searches for annotations that contain the queryTerm given,
     * using pagination that picks up from after the annotation given
     * @param queryTerm The term to search for
     * @param lastAnnotation The last annotation seen in previous results
     * @return The list of annotations found by the service.
     */
    private List<AnnotationDTO> searchAnnotation(final String queryTerm,
                                                 final AnnotationDTO lastAnnotation) {
        if (null != lastAnnotation) {
            return searchAnnotation(queryTerm, lastAnnotation.getId(), lastAnnotation.getLastUpdated());
        } else {
            return searchAnnotation(queryTerm);
        }
    }

    /**
     * Searches for annotations that contain the queryTerm given
     * @param queryTerm The term to search for
     * @param seekId The last ID seen in a paginated result
     * @param seekLastUpdated The lastUpdated value seen in the last paginated result
     * @return The list of annotations found by the service.
     */
    private List<AnnotationDTO> searchAnnotation(final String queryTerm,
                                                 final String seekId,
                                                 final Long seekLastUpdated) {
        List<AnnotationDTO> result = null;

        try {
            final HttpResponse<String> response = Unirest
                    .get(getSearchUrl())
                    .queryString("q", queryTerm)
                    .queryString("seekId", seekId)
                    .queryString("seekLastUpdated", seekLastUpdated)
                    .asString();

            assertEquals(HttpStatus.SC_OK, response.getStatus());

            result = jacksonObjectMapper.readValue(response.getBody(), new TypeReference<List<AnnotationDTO>>(){});
        } catch (Exception e) {
            fail(e.getLocalizedMessage());
        }

        return result;
    }

    /**
     * Given an ID, calls GET on the service to retrieve that specific annotation.
     * This function asserts that the annotation was found correctly.
     *
     * @param id The ID to find.
     * @return The current annotation for that ID from the service.
     */
    private AnnotationDTO getAnnotation(final String id) {
        AnnotationDTO result = null;

        try {
            final HttpResponse<String> response = Unirest
                    .get(getAnnotationUrl(id))
                    .asString();

            assertEquals(HttpStatus.SC_OK, response.getStatus());

            result = jacksonObjectMapper.readValue(response.getBody(), AnnotationDTO.class);

            assertEquals(id, result.getId());
        } catch (Exception e) {
            fail(e.getLocalizedMessage());
        }

        return result;
    }

    /**
     * Given an ID, attempts to delete the annotation
     * @param id The ID to delete
     */
    private void deleteAnnotation(final String id) {
        try {
            final HttpResponse<String> response = Unirest.delete(getAnnotationUrl(id)).asString();
            assertEquals(HttpStatus.SC_OK, response.getStatus());
        } catch (UnirestException e) {
            fail(e.getLocalizedMessage());
        }
    }

    /**
     * Retrieves the annotations history for a particular ID. This can be called for ID's that have been deleted.
     * @param id The ID to find the history for
     * @return The list of history items.
     */
    private List<AnnotationHistoryDTO> getHistory(final String id) {
        List<AnnotationHistoryDTO> result = null;

        try {
            final HttpResponse<String> response = Unirest
                    .get(getHistoryUrl(id))
                    .asString();

            assertEquals(HttpStatus.SC_OK, response.getStatus());

            result = jacksonObjectMapper.readValue(response.getBody(), new TypeReference<List<AnnotationHistoryDTO>>() {});
        } catch (Exception e) {
            fail(e.getLocalizedMessage());
        }

        return result;
    }
}
