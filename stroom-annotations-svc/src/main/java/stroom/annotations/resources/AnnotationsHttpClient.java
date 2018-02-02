package stroom.annotations.resources;

import org.glassfish.jersey.client.ClientConfig;
import org.glassfish.jersey.client.ClientResponse;
import stroom.annotations.hibernate.Annotation;
import stroom.query.audit.security.ServiceUser;

import javax.ws.rs.client.Client;
import javax.ws.rs.client.ClientBuilder;
import javax.ws.rs.client.Entity;
import javax.ws.rs.core.Response;
import java.io.Closeable;
import java.io.IOException;
import java.util.function.BiFunction;
import java.util.function.Function;

public class AnnotationsHttpClient implements AnnotationsResource, Closeable {
    private final String baseUrl;
    private final String welcomeUrl;
    private final String statusValuesUrl;
    private final Function<String, String> searchUrl;
    private final BiFunction<String, String, String> singleUrl;
    private final BiFunction<String, String, String> getHistoryUrl;
    private final Client httpClient;

    public AnnotationsHttpClient(final String baseUrl) {
        this.baseUrl = baseUrl;
        this.welcomeUrl = String.format("%s/annotations/v1/static/welcome", baseUrl);
        this.statusValuesUrl = String.format("%s/annotations/v1/static/statusValues", baseUrl);
        this.searchUrl = (index) -> String.format("%s/annotations/v1/search/%s",
                this.baseUrl,
                index);
        this.getHistoryUrl = (index, id) -> String.format("%s/annotations/v1/single/%s/%s/history",
                this.baseUrl,
                index,
                id);

        this.singleUrl = (index, id) -> String.format("%s/annotations/v1/single/%s/%s",
                this.baseUrl,
                index,
                id);
        httpClient = ClientBuilder.newClient(new ClientConfig().register(ClientResponse.class));
    }

    @Override
    public Response welcome() {
        return httpClient
                .target(this.welcomeUrl)
                .request()
                .get();
    }

    @Override
    public Response statusValues() {
        return httpClient
                .target(this.statusValuesUrl)
                .request()
                .get();
    }

    @Override
    public Response search(final ServiceUser authenticatedServiceUser,
                           final String index,
                           final String q,
                           final Integer seekPosition) {
        return httpClient
                .target(this.searchUrl.apply(index))
                .queryParam("q", q)
                .queryParam("seekPosition", seekPosition)
                .request()
                .header("Authorization", "Bearer " + authenticatedServiceUser.getJwt())
                .get();
    }

    @Override
    public Response get(final ServiceUser authenticatedServiceUser,
                        final String index,
                        final String id) {
        return httpClient
                .target(this.singleUrl.apply(index, id))
                .request()
                .header("Authorization", "Bearer " + authenticatedServiceUser.getJwt())
                .get();
    }

    @Override
    public Response getHistory(final ServiceUser authenticatedServiceUser,
                               final String index,
                               final String id) {
        return httpClient
                .target(this.getHistoryUrl.apply(index, id))
                .request()
                .header("Authorization", "Bearer " + authenticatedServiceUser.getJwt())
                .get();
    }

    @Override
    public Response create(final ServiceUser authenticatedServiceUser,
                           final String index,
                           final String id) {
        return httpClient
                .target(this.singleUrl.apply(index, id))
                .request()
                .header("Authorization", "Bearer " + authenticatedServiceUser.getJwt())
                .post(Entity.json(""));
    }

    @Override
    public Response update(final ServiceUser authenticatedServiceUser,
                           final String index,
                           final String id,
                           final Annotation annotation) {
        return httpClient
                .target(this.singleUrl.apply(index, id))
                .request()
                .header("Authorization", "Bearer " + authenticatedServiceUser.getJwt())
                .put(Entity.json(annotation));
    }

    @Override
    public Response remove(final ServiceUser authenticatedServiceUser,
                           final String index,
                           final String id) {
        return httpClient
                .target(this.singleUrl.apply(index, id))
                .request()
                .header("Authorization", "Bearer " + authenticatedServiceUser.getJwt())
                .delete();
    }

    @Override
    public void close() throws IOException {
        httpClient.close();
    }
}
