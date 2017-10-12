package stroom.annotations.service.audit.functions;

import event.logging.Event;

import javax.ws.rs.core.Response;

public class ApiCreate extends ApiCall {
    private String id;

    public ApiCreate(final Response response,
                     final Exception exception,
                     final String id) {
        super(response, exception);
        this.id = id;
    }

    @Override
    public void enrichEventDetail(Event.EventDetail eventDetail) {
        eventDetail.setTypeId("CREATE");
        eventDetail.setDescription("Create a new Annotation by a specific ID");

        eventDetail.setCreate(getOutcomeForId(id));
    }
}
