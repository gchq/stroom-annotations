package stroom.annotations.service.audit.functions;

import event.logging.Event;

import javax.ws.rs.core.Response;

public class ApiGetHistory extends ApiCall {
    private String id;

    public ApiGetHistory(final Response response,
                         final Exception exception,
                         final String id) {
        super(response, exception);
        this.id = id;
    }

    @Override
    public void enrichEventDetail(Event.EventDetail eventDetail) {
        eventDetail.setTypeId("GET_HISTORY");
        eventDetail.setDescription("Get the history of a specific Annotation by ID");

        eventDetail.setView(getOutcomeForId(id));
    }
}
