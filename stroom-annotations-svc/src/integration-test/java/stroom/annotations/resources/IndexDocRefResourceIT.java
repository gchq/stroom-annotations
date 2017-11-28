package stroom.annotations.resources;

import com.fasterxml.jackson.core.JsonProcessingException;
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
import stroom.annotations.hibernate.AnnotationIndex;
import stroom.query.audit.FifoLogbackAppender;

import java.io.IOException;
import java.util.List;
import java.util.UUID;

import static io.dropwizard.testing.ResourceHelpers.resourceFilePath;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;

public class IndexDocRefResourceIT {
    private static final Logger LOGGER = LoggerFactory.getLogger(IndexDocRefResourceIT.class);

    @ClassRule
    public static final DropwizardAppRule<Config> appRule = new DropwizardAppRule<>(App.class, resourceFilePath("config.yml"));

    private static String indexUrl;

    private static String createIndexUrl(final String uuid, final String name) {
        return String.format("%s/create/%s/%s", indexUrl, uuid, name);
    }

    private static String getIndexUrl(final String uuid) {
        return String.format("%s/%s", indexUrl, uuid);
    }

    private static String deleteIndexUrl(final String uuid) {
        return String.format("%s/delete/%s", indexUrl, uuid);
    }

    private static String renameIndexUrl(final String uuid, final String name) {
        return String.format("%s/rename/%s/%s", indexUrl, uuid, name);
    }

    private static String copyIndexUrl(final String originalUuid, final String copyUuid) {
        return String.format("%s/copy/%s/%s", indexUrl, originalUuid, copyUuid);
    }

    private static final com.fasterxml.jackson.databind.ObjectMapper jacksonObjectMapper =
            new com.fasterxml.jackson.databind.ObjectMapper();

    @BeforeClass
    public static void setupClass() {
        int appPort = appRule.getLocalPort();

        indexUrl = "http://localhost:" + appPort + "/docRefApi/v1";

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
    public void testCreateIndex() {
        final String uuid = UUID.randomUUID().toString();
        final String name = UUID.randomUUID().toString();
        createIndex(uuid, name);

        final AnnotationIndex created = getIndex(uuid);
        assertEquals(name, created.getName());

        // Create, get
        checkAuditLogs(2);
    }

    @Test
    public void testRenameIndex() {
        final String uuid = UUID.randomUUID().toString();
        final String name1 = UUID.randomUUID().toString();
        final String name2 = UUID.randomUUID().toString();
        createIndex(uuid, name1);
        renameIndex(uuid, name2);

        final AnnotationIndex renamed = getIndex(uuid);
        assertEquals(name2, renamed.getName());

        // Create, rename, get
        checkAuditLogs(3);
    }

    @Test
    public void testCopyIndex() {
        final String uuid1 = UUID.randomUUID().toString();
        final String uuid2 = UUID.randomUUID().toString();
        final String name = UUID.randomUUID().toString();
        createIndex(uuid1, name);
        copyIndex(uuid1, uuid2);

        final AnnotationIndex copied = getIndex(uuid2);
        assertEquals(name, copied.getName());

        // Create, copy, get
        checkAuditLogs(3);
    }

    @Test
    public void testDeleteIndex() {
        final String uuid = UUID.randomUUID().toString();
        final String name = UUID.randomUUID().toString();
        createIndex(uuid, name);

        deleteIndex(uuid);

        try {
            final HttpResponse<String> response = Unirest
                    .get(getIndexUrl(uuid))
                    .asString();

            assertEquals(HttpStatus.SC_NOT_FOUND, response.getStatus());
        } catch (UnirestException e) {
            fail(e.getLocalizedMessage());
        }

        // Create, delete, attempt get
        checkAuditLogs(3);
    }

    private AnnotationIndex createIndex(String uuid, String name) {
        AnnotationIndex result = null;
        try {
            HttpResponse<String> response = Unirest.post(createIndexUrl(uuid, name))
                    .asString();
            assertEquals(HttpStatus.SC_OK, response.getStatus());

            result = jacksonObjectMapper.readValue(response.getBody(), AnnotationIndex.class);
            assertEquals(uuid, result.getUuid());
            assertEquals(name, result.getName());
        } catch (Exception e) {
            fail(e.getLocalizedMessage());
        }
        return result;
    }

    /**
     * Updates the annotation by PUTting the new annotation data.
     * @param originalUuid The UUID to copy
     * @param copyUuid The UUID of the copy to create
     * @return The updated annotation returned from the service.
     */
    private AnnotationIndex copyIndex(final String originalUuid, final String copyUuid) {
        AnnotationIndex result = null;

        try {
            // Set the content in an update
            HttpResponse<String> response = Unirest
                    .post(copyIndexUrl(originalUuid, copyUuid))
                    .asString();
            assertEquals(HttpStatus.SC_OK, response.getStatus());

            result = jacksonObjectMapper.readValue(response.getBody(), AnnotationIndex.class);
            assertEquals(copyUuid, result.getUuid());
        } catch (Exception e) {
            fail(e.getLocalizedMessage());
        }
        return result;
    }

    /**
     * Updates the annotation by PUTting the new annotation data.
     * @param uuid The UUID to update
     * @param name The new name
     * @return The updated annotation returned from the service.
     */
    private AnnotationIndex renameIndex(final String uuid, final String name) {
        AnnotationIndex result = null;

        try {
            // Set the content in an update
            HttpResponse<String> response = Unirest
                    .put(renameIndexUrl(uuid, name))
                    .asString();
            assertEquals(HttpStatus.SC_OK, response.getStatus());

            result = jacksonObjectMapper.readValue(response.getBody(), AnnotationIndex.class);
            assertEquals(name, result.getName());
        } catch (Exception e) {
            fail(e.getLocalizedMessage());
        }
        return result;
    }

    /**
     * Given an ID, calls GET on the service to retrieve that specific annotation.
     * This function asserts that the annotation was found correctly.
     *
     * @param uuid The UUID to find.
     * @return The current annotation for that ID from the service.
     */
    private AnnotationIndex getIndex(final String uuid) {
        AnnotationIndex result = null;

        try {
            final HttpResponse<String> response = Unirest
                    .get(getIndexUrl(uuid))
                    .asString();

            assertEquals(HttpStatus.SC_OK, response.getStatus());

            result = jacksonObjectMapper.readValue(response.getBody(), AnnotationIndex.class);

            assertEquals(uuid, result.getUuid());
        } catch (Exception e) {
            fail(e.getLocalizedMessage());
        }

        return result;
    }

    /**
     * Given an ID, calls GET on the service to delete the index
     *
     * @param uuid The UUID to delete
     */
    private void deleteIndex(final String uuid) {

        try {
            final HttpResponse<String> response = Unirest
                    .delete(deleteIndexUrl(uuid))
                    .asString();

            assertEquals(HttpStatus.SC_NO_CONTENT, response.getStatus());
        } catch (Exception e) {
            fail(e.getLocalizedMessage());
        }
    }
}
