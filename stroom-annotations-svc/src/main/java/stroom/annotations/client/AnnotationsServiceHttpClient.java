package stroom.annotations.client;

import org.eclipse.jetty.http.HttpStatus;
import stroom.annotations.model.Annotation;
import stroom.annotations.model.AnnotationHistory;
import stroom.annotations.model.ResponseMsgDTO;
import stroom.annotations.service.AnnotationsService;
import stroom.query.audit.client.QueryApiExceptionMapper;
import stroom.query.audit.security.ServiceUser;
import stroom.query.audit.service.QueryApiException;

import javax.ws.rs.core.GenericType;
import javax.ws.rs.core.Response;
import java.io.Closeable;
import java.io.IOException;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

public class AnnotationsServiceHttpClient implements AnnotationsService, Closeable {
    private final AnnotationsHttpClient httpClient;

    public AnnotationsServiceHttpClient(final String baseUrl) {
        this.httpClient = new AnnotationsHttpClient(baseUrl);
    }

    @Override
    public List<Annotation> search(final ServiceUser user,
                                   final String index,
                                   final String q,
                                   final Integer seekPosition) throws QueryApiException {
        final Response response = httpClient.search(user, index, q, seekPosition);

        if (response.getStatus() == HttpStatus.OK_200) {
            return response.readEntity(new GenericType<List<Annotation>>(){});
        } else {
            throw QueryApiExceptionMapper.create(response);
        }
    }

    @Override
    public Optional<Annotation> get(final ServiceUser user,
                                    final String index,
                                    final String id) throws QueryApiException {
        final Response response = httpClient.get(user, index, id);

        if (response.getStatus() == HttpStatus.OK_200) {
            return Optional.of(response.readEntity(Annotation.class));
        } else {
            throw QueryApiExceptionMapper.create(response);
        }
    }

    @Override
    public Optional<List<AnnotationHistory>> getHistory(final ServiceUser user,
                                                        final String index,
                                                        final String id) throws QueryApiException {
        final Response response = httpClient.getHistory(user, index, id);

        if (response.getStatus() == HttpStatus.OK_200) {
            return Optional.of(
                    response.readEntity(
                            new GenericType<List<AnnotationHistory>>(){}))
                    .filter(a -> a.size() > 0);
        } else {
            throw QueryApiExceptionMapper.create(response);
        }
    }

    @Override
    public Optional<Annotation> create(final ServiceUser user,
                                       final String index,
                                       final String id) throws QueryApiException {
        final Response response = httpClient.create(user, index, id);

        if (response.getStatus() == HttpStatus.OK_200) {
            return Optional.of(response.readEntity(Annotation.class));
        } else {
            throw QueryApiExceptionMapper.create(response);
        }
    }

    @Override
    public Optional<Annotation> update(final ServiceUser user,
                                       final String index,
                                       final String id,
                                       final Annotation annotation) throws QueryApiException {
        final Response response = httpClient.update(user, index, id, annotation);

        if (response.getStatus() == HttpStatus.OK_200) {
            return Optional.of(response.readEntity(Annotation.class));
        } else {
            throw QueryApiExceptionMapper.create(response);
        }
    }

    @Override
    public Optional<Boolean> remove(final ServiceUser user,
                                    final String index,
                                    final String id) throws QueryApiException {
        final Response response = httpClient.remove(user, index, id);

        if (response.getStatus() == HttpStatus.OK_200) {
            return Optional.of(response.readEntity(ResponseMsgDTO.class))
                    .map(obj -> true);
        } else {
            throw QueryApiExceptionMapper.create(response);
        }
    }

    @Override
    public void close() throws IOException {
        httpClient.close();
    }
}
