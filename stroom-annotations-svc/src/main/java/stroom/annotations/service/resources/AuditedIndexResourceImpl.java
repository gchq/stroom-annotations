package stroom.annotations.service.resources;

import event.logging.Event;
import event.logging.EventLoggingService;

import javax.inject.Inject;
import javax.ws.rs.core.Request;
import javax.ws.rs.core.Response;

public class AuditedIndexResourceImpl implements IndexResource {
    private final IndexResource indexResource;

    private final EventLoggingService eventLoggingService;

    @Inject
    Request request;

    @Inject
    public AuditedIndexResourceImpl(final IndexResource indexResource,
                                    final EventLoggingService eventLoggingService) {
        this.indexResource = indexResource;
        this.eventLoggingService = eventLoggingService;
    }
    @Override
    public Response getIndexes() throws AnnotationsException {
        Response response;
        Exception exception = null;

        try {
            response = indexResource.getIndexes();

            return response;
        } finally {
            final Event event = eventLoggingService.createEvent();
            final Event.EventDetail eventDetail = event.getEventDetail();

            eventDetail.setTypeId("GET_INDEXES");
            eventDetail.setDescription("Get the list of indexes");

            eventLoggingService.log(event);
        }
    }
}
