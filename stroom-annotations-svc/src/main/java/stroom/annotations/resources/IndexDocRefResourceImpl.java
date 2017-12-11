package stroom.annotations.resources;

import stroom.annotations.hibernate.AnnotationIndex;
import stroom.annotations.service.IndexService;
import stroom.query.audit.DocRefResource;
import stroom.util.shared.QueryApiException;

import javax.inject.Inject;
import javax.ws.rs.core.Response;
import java.util.List;
import java.util.Map;

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
    public Response deleteDocument(final String uuid) throws QueryApiException {
        service.deleteDocument(uuid);
        return Response.noContent().build();
    }

    @Override
    public Response importDocument(final String uuid,
                                   final String name,
                                   final Boolean confirmed,
                                   final Map<String, String> dataMap) throws QueryApiException {
        final AnnotationIndex index = service.importDocument(uuid, name, confirmed, dataMap);
        return Response
                .ok(index)
                .build();
    }

    @Override
    public Response exportDocument(final String uuid) throws QueryApiException {
        final Map<String, String> doc = service.exportDocument(uuid);
        return Response
                .ok(doc)
                .build();
    }
}
