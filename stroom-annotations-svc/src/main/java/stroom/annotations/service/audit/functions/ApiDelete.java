package stroom.annotations.service.audit.functions;

import event.logging.Event;

import javax.ws.rs.core.Response;

public class ApiDelete extends ApiCall {
    private String id;

    public ApiDelete(final Response response,
                     final Exception exception,
                     final String id) {
        super(response, exception);
        this.id = id;
    }

    @Override
    public void enrichEventDetail(Event.EventDetail eventDetail) {
        eventDetail.setTypeId("REMOVE");
        eventDetail.setDescription("Remove an new Annotation with a specific ID");

        eventDetail.setDelete(getOutcomeForId(id));
    }
}
