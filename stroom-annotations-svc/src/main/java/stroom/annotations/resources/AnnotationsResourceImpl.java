package stroom.annotations.resources;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import stroom.annotations.hibernate.Annotation;
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
        final List<AnnotationDTO> dtos = service.search(index, q, seekPosition)
                .stream()
                .map(e -> new AnnotationDTO.Builder().entity(e).build())
                .collect(Collectors.toList());

        return Response.ok(dtos)
                .build();
    }

    public final Response get(final String index,
                              final String id) throws AnnotationsException {
        final AnnotationDTO annotationDTO = getDTO(index, id);

        return Response.ok(annotationDTO).build();
    }

    private AnnotationDTO getDTO(final String index, final String id) throws AnnotationsException {
        final Annotation annotation = service.get(index, id);

        return new AnnotationDTO.Builder().entity(annotation).build();
    }

    public final Response getHistory(final String index,
                                     final String id) throws AnnotationsException {
        final List<AnnotationHistoryDTO> results = service.getHistory(index, id)
                .stream()
                .map(e -> new AnnotationHistoryDTO.Builder().entity(e).build())
                .collect(Collectors.toList());

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

        final AnnotationDTO annotationDTO = new AnnotationDTO.Builder().entity(annotation).build();

        return Response.ok(annotationDTO)
                .build();
    }

    public final Response update(final String index,
                                 final String id,
                                 final AnnotationDTO annotationDTO) throws AnnotationsException {
        final Annotation annotationUpdate = new Annotation.Builder().dto(annotationDTO).build();
        final Annotation annotation = service.update(index, id, annotationUpdate);

        final AnnotationDTO resultDTO = new AnnotationDTO.Builder().entity(annotation).build();

        return Response.ok(resultDTO)
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
