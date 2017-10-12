package stroom.annotations.service.audit.functions;

import event.logging.Event;

import javax.ws.rs.core.Response;

public class ApiGet extends ApiCall {
    private String id;

    public ApiGet(final Response response,
                  final Exception exception,
                  final String id) {
        super(response, exception);
        this.id = id;
    }

    @Override
    public void enrichEventDetail(Event.EventDetail eventDetail) {
        eventDetail.setTypeId("GET");
        eventDetail.setDescription("Get a specific Annotation by ID");

        eventDetail.setView(getOutcomeForId(id));
    }
}
