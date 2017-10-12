package stroom.annotations.service.audit.functions;

import event.logging.Data;
import event.logging.Event;
import event.logging.ObjectOutcome;
import event.logging.Outcome;

import javax.ws.rs.core.Response;

public abstract class ApiCall {
    private final Response response;
    private final Exception exception;

    public ApiCall(final Response response,
                   final Exception exception) {
        this.response = response;
        this.exception = exception;
    }

    abstract void enrichEventDetail(Event.EventDetail eventDetail);

    protected ObjectOutcome getOutcomeForId(final String id) {
        final ObjectOutcome objectOutcome = new ObjectOutcome();

        final Outcome outcome = new Outcome();
        objectOutcome.setOutcome(outcome);

        outcome.getData().add(getDataForId(id));

        return objectOutcome;
    }

    protected Data getDataForId(final String id) {
        final Data idData = new Data();

        idData.setName("id");
        idData.setValue(id);

        return idData;
    }
}
