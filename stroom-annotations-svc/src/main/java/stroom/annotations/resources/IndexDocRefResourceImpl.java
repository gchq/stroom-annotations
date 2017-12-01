package stroom.annotations.resources;

import stroom.annotations.hibernate.AnnotationIndex;
import stroom.annotations.service.IndexService;
import stroom.query.audit.DocRefResource;
import stroom.util.shared.QueryApiException;

import javax.inject.Inject;
import javax.ws.rs.core.Response;
import java.util.List;

public class IndexDocRefResourceImpl implements DocRefResource {

    private IndexService service;

    @Inject
    public IndexDocRefResourceImpl(final IndexService service) {
        this.service = service;
    }


    @Override
    public Response getAll() throws QueryApiException {
        final List<AnnotationIndex> indexes = service.getAll();

        return Response
                .ok(indexes)
                .build();
    }

    @Override
    public Response get(final String uuid) throws QueryApiException {
        final AnnotationIndex index = service.get(uuid);

        return Response
                .ok(index)
                .build();
    }

    @Override
    public Response createDocument(final String uuid,
                                   final String name) throws QueryApiException {
        final AnnotationIndex index = service.create(uuid, name);

        return Response
                .ok(index)
                .build();
    }

    @Override
    public Response copyDocument(final String originalUuid,
                                 final String copyUuid) throws QueryApiException {
        final AnnotationIndex index = service.copyDocument(originalUuid, copyUuid);

        return Response
                .ok(index)
                .build();
    }

    @Override
    public Response documentMoved(String uuid) throws QueryApiException {
        final AnnotationIndex index = service.documentMoved(uuid);

        return Response
                .ok(index)
                .build();
    }

    @Override
    public Response documentRenamed(final String uuid,
                                    final String name) throws QueryApiException {
        final AnnotationIndex index = service.documentRenamed(uuid, name);

        return Response
                .ok(index)
                .build();
    }

    @Override
    public Response deleteDocument(String uuid) throws QueryApiException {
        service.deleteDocument(uuid);
        return Response.noContent().build();
    }
}
