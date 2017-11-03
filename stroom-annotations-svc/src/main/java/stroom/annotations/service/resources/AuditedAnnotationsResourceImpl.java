package stroom.annotations.service.resources;

import event.logging.*;
import org.jooq.DSLContext;
import stroom.annotations.service.model.AnnotationDTO;

import javax.inject.Inject;
import javax.ws.rs.core.Response;

public class AuditedAnnotationsResourceImpl implements AnnotationsResource {

    private final AnnotationsResource annotationsResource;

    private final EventLoggingService eventLoggingService;

    @Inject
    public AuditedAnnotationsResourceImpl(final AnnotationsResource annotationsResource,
                                          final EventLoggingService eventLoggingService) {
        this.annotationsResource = annotationsResource;
        this.eventLoggingService = eventLoggingService;
    }

    @Override
    public Response welcome() throws AnnotationsException {
        return annotationsResource.welcome();
    }

    @Override
    public Response statusValues() throws AnnotationsException {
        return annotationsResource.statusValues();
    }

    @Override
    public Response search(final String q,
                           final String seekId,
                           final Long seekLastUpdated) throws AnnotationsException {
        Response response;
        Exception exception = null;
        
        try {
            response = annotationsResource.search(q, seekId, seekLastUpdated);

            return response;
        } finally {
            final Event event = eventLoggingService.createEvent();
            final Event.EventDetail eventDetail = event.getEventDetail();

            eventDetail.setTypeId("SEARCH");
            eventDetail.setDescription("Freetext search through Annotations");

            final Search search = new Search();
            eventDetail.setSearch(search);

            final Outcome outcome = new Outcome();
            outcome.setSuccess(null != exception);
            search.setOutcome(outcome);

            final Query query = new Query();
            search.setQuery(query);

            final Query.Advanced queryTerms = new Query.Advanced();
            query.setAdvanced(queryTerms);

            final Term qTerm = new Term();
            queryTerms.getAdvancedQueryItems().add(qTerm);
            qTerm.setName("q");
            qTerm.setValue(q);
            qTerm.setCondition(TermCondition.CONTAINS);

            if (null != seekId) {
                final Term seekIdTerm = new Term();
                queryTerms.getAdvancedQueryItems().add(seekIdTerm);
                seekIdTerm.setName("seekId");
                seekIdTerm.setValue(seekId);
                seekIdTerm.setCondition(TermCondition.GREATER_THAN);
            }

            if (null != seekLastUpdated) {
                final Term seekLastUpdatedTerm = new Term();
                queryTerms.getAdvancedQueryItems().add(seekLastUpdatedTerm);
                seekLastUpdatedTerm.setName("seekLastUpdated");
                seekLastUpdatedTerm.setValue(Long.toString(seekLastUpdated));
                seekLastUpdatedTerm.setCondition(TermCondition.GREATER_THAN);
            }

            eventLoggingService.log(event);
        }
    }

    @Override
    public Response get(final String id) throws AnnotationsException {
        Response response;
        Exception exception = null;
        
        try {
            response =  annotationsResource.get( id);

            return response;
        } finally {
            final Event event = eventLoggingService.createEvent();
            final Event.EventDetail eventDetail = event.getEventDetail();

            eventDetail.setTypeId("GET");
            eventDetail.setDescription("Get a specific Annotation by ID");

            eventDetail.setView(getOutcomeForId(id));
            eventDetail.getView().getOutcome().setSuccess(null != exception);

            eventLoggingService.log(event);
        }
    }

    @Override
    public Response getHistory(final String id) throws AnnotationsException {
        Response response;
        Exception exception = null;

        try {
            response =  annotationsResource.getHistory(id);

            return response;
        } finally {
            final Event event = eventLoggingService.createEvent();
            final Event.EventDetail eventDetail = event.getEventDetail();
            eventDetail.setTypeId("GET_HISTORY");
            eventDetail.setDescription("Get the history of a specific Annotation by ID");

            eventDetail.setView(getOutcomeForId(id));
            eventDetail.getView().getOutcome().setSuccess(null != exception);

            eventLoggingService.log(event);
        }
    }

    @Override
    public Response create(final String id) throws AnnotationsException {
        Response response;
        AnnotationsException exception = null;

        try {
            response =  annotationsResource.create(id);

            return response;
        } finally {
            final Event event = eventLoggingService.createEvent();
            final Event.EventDetail eventDetail = event.getEventDetail();

            eventDetail.setTypeId("CREATE");
            eventDetail.setDescription("Create a new Annotation by a specific ID");

            eventDetail.setCreate(getOutcomeForId(id));
            eventDetail.getCreate().getOutcome().setSuccess(null != exception);

            eventLoggingService.log(event);
        }
    }

    @Override
    public Response update(final String id,
                           final AnnotationDTO annotation) throws AnnotationsException {
        Response response;
        Exception exception = null;

        try {
            response =  annotationsResource.update(id, annotation);

            return response;
        } finally {
            final Event event = eventLoggingService.createEvent();
            final Event.EventDetail eventDetail = event.getEventDetail();

            eventDetail.setTypeId("UPDATE");
            eventDetail.setDescription("Update an new Annotation with a specific ID");

            final Event.EventDetail.Update update = new Event.EventDetail.Update();
            eventDetail.setUpdate(update);

            final Outcome outcome = new Outcome();
            update.setOutcome(outcome);

            outcome.setSuccess(null != exception);

            update.getData().add(getDataForId(id));

            eventLoggingService.log(event);
        }
    }

    @Override
    public Response remove(final String id) throws AnnotationsException {
        Response response;
        Exception exception = null;

        try {
            response =  annotationsResource.remove(id);

            return response;
        } finally {
            final Event event = eventLoggingService.createEvent();
            final Event.EventDetail eventDetail = event.getEventDetail();

            eventDetail.setTypeId("REMOVE");
            eventDetail.setDescription("Remove an new Annotation with a specific ID");

            eventDetail.setDelete(getOutcomeForId(id));
            eventDetail.getDelete().getOutcome().setSuccess(null != exception);

            eventLoggingService.log(event);
        }
    }

    private ObjectOutcome getOutcomeForId(final String id) {
        final ObjectOutcome objectOutcome = new ObjectOutcome();

        final Outcome outcome = new Outcome();
        objectOutcome.setOutcome(outcome);

        outcome.getData().add(getDataForId(id));

        return objectOutcome;
    }

    private Data getDataForId(final String id) {
        final Data idData = new Data();

        idData.setName("id");
        idData.setValue(id);

        return idData;
    }
}
