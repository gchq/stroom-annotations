package stroom.annotations.resources;

import stroom.annotations.hibernate.Annotation;
import stroom.query.audit.SimpleJsonHttpClient;
import stroom.util.shared.QueryApiException;

import javax.ws.rs.core.Response;
import java.util.function.BiFunction;
import java.util.function.Function;

public class AnnotationsHttpClient implements AnnotationsResource {
    private final String baseUrl;
    private final String welcomeUrl;
    private final String statusValuesUrl;
    private final Function<String, String> searchUrl;
    private final BiFunction<String, String, String> singleUrl;
    private final BiFunction<String, String, String> getHistoryUrl;
    private final SimpleJsonHttpClient<QueryApiException> httpClient;

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
        this.httpClient = new SimpleJsonHttpClient<>(QueryApiException::new);
    }

    @Override
    public Response welcome() throws QueryApiException {
        return this.httpClient
                .get(this.welcomeUrl)
                .send();
    }

    @Override
    public Response statusValues() throws QueryApiException {
        return this.httpClient
                .get(this.statusValuesUrl)
                .send();
    }

    @Override
    public Response search(final String index,
                           final String q,
                           final Integer seekPosition) throws QueryApiException {
        return this.httpClient
                .get(this.searchUrl.apply(index))
                .queryParam("q", q)
                .queryParam("seekPosition", seekPosition)
                .send();
    }

    @Override
    public Response get(final String index,
                        final String id) throws QueryApiException {
        return this.httpClient
                .get(this.singleUrl.apply(index, id))
                .send();
    }

    @Override
    public Response getHistory(final String index,
                               final String id) throws QueryApiException {
        return this.httpClient
                .get(this.getHistoryUrl.apply(index, id))
                .send();
    }

    @Override
    public Response create(final String index,
                           final String id) throws QueryApiException {
        return this.httpClient
                .post(this.singleUrl.apply(index, id))
                .send();
    }

    @Override
    public Response update(final String index,
                           final String id,
                           final Annotation annotation) throws QueryApiException {
        return this.httpClient
                .put(this.singleUrl.apply(index, id))
                .body(annotation)
                .send();
    }

    @Override
    public Response remove(final String index,
                           final String id) throws QueryApiException {
        return this.httpClient
                .delete(this.singleUrl.apply(index, id))
                .send();
    }
}
