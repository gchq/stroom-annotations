package stroom.annotations.service.audit.functions;

import event.logging.Event;

import javax.ws.rs.core.Response;

public class ApiUpdate extends ApiCall {
    private String id;

    public ApiUpdate(final Response response,
                     final Exception exception,
                     final String id) {
        super(response, exception);
        this.id = id;
    }

    @Override
    public void enrichEventDetail(Event.EventDetail eventDetail) {
        eventDetail.setTypeId("UPDATE");
        eventDetail.setDescription("Update an new Annotation with a specific ID");

        final Event.EventDetail.Update update = new Event.EventDetail.Update();
        eventDetail.setUpdate(update);

        update.getData().add(getDataForId(id));
    }
}
