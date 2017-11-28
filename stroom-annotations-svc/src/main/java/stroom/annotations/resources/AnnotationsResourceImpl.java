package stroom.annotations.resources;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import stroom.annotations.hibernate.Annotation;
import stroom.annotations.hibernate.AnnotationHistory;
import stroom.annotations.hibernate.Status;
import stroom.annotations.model.*;
import stroom.annotations.service.AnnotationsService;

import javax.inject.Inject;
import javax.ws.rs.core.Response;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class AnnotationsResourceImpl implements AnnotationsResource {

    private static final Logger LOGGER = LoggerFactory.getLogger(AnnotationsResourceImpl.class);

    static final String WELCOME_TEXT = "Welcome to the annotations service";

    private final AnnotationsService service;

    @Inject
    public AnnotationsResourceImpl(final AnnotationsService service) {
        this.service = service;
    }

    public final Response welcome() throws AnnotationsException {
        return Response.ok(WELCOME_TEXT)
                .build();
    }

    public final Response statusValues() throws AnnotationsException {
        final Map<String, String> statusValues = Arrays.stream(Status.values())
                .collect(Collectors.toMap(Object::toString, Status::getDisplayText));

        return Response.ok(statusValues)
                .build();
    }

    public final Response search(final String index,
                                 final String q,
                                 final Integer seekPosition) throws AnnotationsException {
        final List<Annotation> annotations = service.search(index, q, seekPosition);

        return Response.ok(annotations)
                .build();
    }

    public final Response get(final String index,
                              final String id) throws AnnotationsException {
        final Annotation annotation = service.get(index, id);

        return Response.ok(annotation).build();
    }

    public final Response getHistory(final String index,
                                     final String id) throws AnnotationsException {
        final List<AnnotationHistory> results = service.getHistory(index, id);

        if (results.size() > 0) {
            return Response.ok(results)
                    .build();
        } else {
            return Response.status(Response.Status.NOT_FOUND)
                    .entity(ResponseMsgDTO.msg("No history found for annotation")
                            .recordsUpdated(0)
                            .build())
                    .build();
        }
    }

    public final Response create(final String index,
                                 final String id) throws AnnotationsException {
        final Annotation annotation = service.create(index, id);

        return Response.ok(annotation)
                .build();
    }

    public final Response update(final String index,
                                 final String id,
                                 final Annotation annotationUpdate) throws AnnotationsException {
        final Annotation annotation = service.update(index, id, annotationUpdate);

        return Response.ok(annotation)
                .build();
    }

    public final Response remove(final String index,
                                 final String id) throws AnnotationsException {
        service.remove(index, id);

        return Response
                .ok(ResponseMsgDTO.msg("Annotation deleted")
                        .recordsUpdated(1)
                        .build())
                .build();
    }
}
