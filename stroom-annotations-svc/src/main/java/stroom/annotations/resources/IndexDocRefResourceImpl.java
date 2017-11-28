package stroom.annotations.resources;

import stroom.annotations.hibernate.AnnotationIndex;
import stroom.annotations.model.AnnotationIndexDTO;
import stroom.annotations.service.IndexService;
import stroom.query.audit.DocRefException;
import stroom.query.audit.DocRefResource;

import javax.inject.Inject;
import javax.ws.rs.core.Response;
import java.util.List;
import java.util.stream.Collectors;

public class IndexDocRefResourceImpl implements DocRefResource {

    private IndexService service;

    @Inject
    public IndexDocRefResourceImpl(final IndexService service) {
        this.service = service;
    }

    @Override
    public Response getAll() throws DocRefException {
        final List<AnnotationIndex> indexes = service.getAll();

        return Response
                .ok(indexes
                        .stream()
                        .map(e -> new AnnotationIndexDTO.Builder().entity(e).build())
                        .collect(Collectors.toList()))
                .build();
    }

    @Override
    public Response get(final String uuid) throws DocRefException {
        final AnnotationIndex index = service.get(uuid);

        return Response
                .ok(new AnnotationIndexDTO.Builder()
                        .entity(index)
                        .build())
                .build();
    }

    @Override
    public Response createDocument(final String uuid, final String name) throws DocRefException {
        final AnnotationIndex index = service.create(uuid, name);

        return Response
                .ok(new AnnotationIndexDTO.Builder()
                        .entity(index)
                        .build())
                .build();
    }

    @Override
    public Response copyDocument(final String originalUuid, final String copyUuid) throws DocRefException {
        final AnnotationIndex index = service.copyDocument(originalUuid, copyUuid);

        return Response
                .ok(new AnnotationIndexDTO.Builder()
                        .entity(index)
                        .build())
                .build();
    }

    @Override
    public Response documentMoved(final String uuid) throws DocRefException {
        final AnnotationIndex index = service.documentMoved(uuid);

        return Response
                .ok(new AnnotationIndexDTO.Builder()
                        .entity(index)
                        .build())
                .build();
    }

    @Override
    public Response documentRenamed(final String uuid, final String name) throws DocRefException {
        final AnnotationIndex index = service.documentRenamed(uuid, name);

        return Response
                .ok(new AnnotationIndexDTO.Builder()
                        .entity(index)
                        .build())
                .build();
    }

    @Override
    public Response deleteDocument(final String uuid) throws DocRefException {
        service.deleteDocument(uuid);
        return Response.noContent().build();
    }
}
