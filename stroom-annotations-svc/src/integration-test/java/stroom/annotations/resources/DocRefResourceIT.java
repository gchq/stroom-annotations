package stroom.annotations.resources;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.tomakehurst.wiremock.core.WireMockConfiguration;
import com.github.tomakehurst.wiremock.junit.WireMockClassRule;
import io.dropwizard.testing.junit.DropwizardAppRule;
import org.eclipse.jetty.http.HttpStatus;
import org.jose4j.jwk.JsonWebKey;
import org.jose4j.jwk.RsaJsonWebKey;
import org.jose4j.jwk.RsaJwkGenerator;
import org.jose4j.jws.AlgorithmIdentifiers;
import org.jose4j.jws.JsonWebSignature;
import org.jose4j.jwt.JwtClaims;
import org.jose4j.lang.JoseException;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.ClassRule;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import stroom.annotations.App;
import stroom.annotations.config.Config;
import stroom.annotations.hibernate.AnnotationsDocRefEntity;
import stroom.query.audit.client.DocRefResourceHttpClient;
import stroom.query.audit.logback.FifoLogbackAppender;
import stroom.query.audit.security.ServiceUser;

import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.util.List;
import java.util.UUID;

import static com.github.tomakehurst.wiremock.client.WireMock.aResponse;
import static com.github.tomakehurst.wiremock.client.WireMock.get;
import static com.github.tomakehurst.wiremock.client.WireMock.post;
import static com.github.tomakehurst.wiremock.client.WireMock.stubFor;
import static com.github.tomakehurst.wiremock.client.WireMock.urlEqualTo;
import static io.dropwizard.testing.ResourceHelpers.resourceFilePath;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;

public class DocRefResourceIT {
    private static final Logger LOGGER = LoggerFactory.getLogger(DocRefResourceIT.class);

    @ClassRule
    public static final DropwizardAppRule<Config> appRule = new DropwizardAppRule<>(App.class, resourceFilePath("config.yml"));

    @ClassRule
    public static WireMockClassRule wireMockRule = new WireMockClassRule(
            WireMockConfiguration.options().port(10080));

    private static final String PARENT_FOLDER_UUID = UUID.randomUUID().toString();
    private static final String LOCALHOST = "localhost";
    private static final ObjectMapper jacksonObjectMapper = new ObjectMapper();
    private static DocRefResourceHttpClient docRefClient;
    public static ServiceUser serviceUser;

    @BeforeClass
    public static void setupClass() {

        int appPort = appRule.getLocalPort();
        final String host = String.format("http://%s:%d", LOCALHOST, appPort);
        docRefClient = new DocRefResourceHttpClient(host);

        stubFor(post(urlEqualTo("/api/authorisation/v1/isAuthorised"))
                .willReturn(aResponse()
                        .withHeader("Content-Type", MediaType.TEXT_PLAIN)
                        .withBody("Mock approval for authorisation")
                        .withStatus(200)));

        RsaJsonWebKey jwk;
        try {
            String jwkId = UUID.randomUUID().toString();
            jwk = RsaJwkGenerator.generateJwk(2048);
            jwk.setKeyId(jwkId);
        } catch (Exception e) {
            fail(e.getLocalizedMessage());
            return;
        }

        stubFor(get(urlEqualTo("/testAuthService/publicKey"))
                .willReturn(aResponse()
                        .withHeader("Content-Type", MediaType.APPLICATION_JSON)
                        .withBody(jwk.toJson(JsonWebKey.OutputControlLevel.PUBLIC_ONLY))
                        .withStatus(200)));

        JwtClaims claims = new JwtClaims();
        claims.setIssuer("stroom");  // who creates the token and signs it
        claims.setSubject("testSubject"); // the subject/principal is whom the token is about

        final JsonWebSignature jws = new JsonWebSignature();
        jws.setPayload(claims.toJson());
        jws.setAlgorithmHeaderValue(AlgorithmIdentifiers.RSA_USING_SHA256);
        jws.setKey(jwk.getPrivateKey());
        jws.setDoKeyValidation(false);

        try {
            serviceUser = new ServiceUser.Builder()
                    .jwt(jws.getCompactSerialization())
                    .name("testSubject")
                    .build();
        } catch (JoseException e) {
            fail(e.getLocalizedMessage());
        }
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

        final AnnotationsDocRefEntity created = getIndex(uuid);
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

        final AnnotationsDocRefEntity renamed = getIndex(uuid);
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

        final AnnotationsDocRefEntity copied = getIndex(uuid2);
        assertEquals(name, copied.getName());

        // Create, copy, get
        checkAuditLogs(3);
    }

    @Test
    public void testDeleteIndex() throws Exception {
        final String uuid = UUID.randomUUID().toString();
        final String name = UUID.randomUUID().toString();
        createIndex(uuid, name);

        deleteIndex(uuid);

        final Response response = docRefClient.get(serviceUser, uuid);
        
        assertEquals(HttpStatus.NOT_FOUND_404, response.getStatus());

        // Create, delete, attempt get
        checkAuditLogs(3);
    }

    @Test
    public void testExportIndex() throws Exception {

    }

    @Test
    public void testImportIndex() throws Exception {

    }

    private AnnotationsDocRefEntity createIndex(String uuid, String name) {
        AnnotationsDocRefEntity result = null;
        try {
            final Response response = docRefClient.createDocument(serviceUser, uuid, name, PARENT_FOLDER_UUID);
            assertEquals(HttpStatus.OK_200, response.getStatus());

            result = jacksonObjectMapper.readValue(response.readEntity(String.class), AnnotationsDocRefEntity.class);
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
    private AnnotationsDocRefEntity copyIndex(final String originalUuid, final String copyUuid) {
        AnnotationsDocRefEntity result = null;

        try {
            final Response response = docRefClient.copyDocument(serviceUser, originalUuid, copyUuid, PARENT_FOLDER_UUID);
            assertEquals(HttpStatus.OK_200, response.getStatus());

            result = jacksonObjectMapper.readValue(response.readEntity(String.class), AnnotationsDocRefEntity.class);
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
    private AnnotationsDocRefEntity renameIndex(final String uuid, final String name) {
        AnnotationsDocRefEntity result = null;

        try {
            final Response response = docRefClient.renameDocument(serviceUser, uuid, name);
            assertEquals(HttpStatus.OK_200, response.getStatus());

            result = jacksonObjectMapper.readValue(response.readEntity(String.class), AnnotationsDocRefEntity.class);
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
    private AnnotationsDocRefEntity getIndex(final String uuid) {
        AnnotationsDocRefEntity result = null;

        try {
            final Response response = docRefClient.get(serviceUser, uuid);

            assertEquals(HttpStatus.OK_200, response.getStatus());

            result = jacksonObjectMapper.readValue(response.readEntity(String.class), AnnotationsDocRefEntity.class);

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
            final Response response = docRefClient.deleteDocument(serviceUser, uuid);

            assertEquals(HttpStatus.OK_200, response.getStatus());
        } catch (Exception e) {
            fail(e.getLocalizedMessage());
        }
    }
}
