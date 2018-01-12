package stroom.annotations.resources;

import event.logging.Data;
import event.logging.Event;
import event.logging.EventLoggingService;
import event.logging.ObjectOutcome;
import event.logging.Outcome;
import event.logging.Query;
import event.logging.Search;
import event.logging.Term;
import event.logging.TermCondition;
import stroom.annotations.hibernate.Annotation;
import stroom.annotations.security.ServiceUser;
import stroom.util.shared.QueryApiException;

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
    public Response welcome() throws QueryApiException {
        return annotationsResource.welcome();
    }

    @Override
    public Response statusValues() throws QueryApiException {
        return annotationsResource.statusValues();
    }

    @Override
    public Response search(final ServiceUser authenticatedServiceUser,
                           final String index,
                           final String q,
                           final Integer seekPosition) throws QueryApiException {
        Response response;
        Exception exception = null;
        
        try {
            response = annotationsResource.search(authenticatedServiceUser, index, q, seekPosition);

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

            if (null != seekPosition) {
                final Term seekIdTerm = new Term();
                queryTerms.getAdvancedQueryItems().add(seekIdTerm);
                seekIdTerm.setName("seekPosition");
                seekIdTerm.setValue(Integer.toString(seekPosition));
                seekIdTerm.setCondition(TermCondition.GREATER_THAN);
            }

            eventLoggingService.log(event);
        }
    }

    @Override
    public Response get(final ServiceUser authenticatedServiceUser,
                        final String index,
                        final String id) throws QueryApiException {
        Response response;
        Exception exception = null;
        
        try {
            response =  annotationsResource.get(authenticatedServiceUser, index, id);

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
    public Response getHistory(final ServiceUser authenticatedServiceUser,
                               final String index,
                               final String id) throws QueryApiException {
        Response response;
        Exception exception = null;

        try {
            response =  annotationsResource.getHistory(authenticatedServiceUser, index, id);

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
    public Response create(final ServiceUser authenticatedServiceUser,
                           final String index,
                           final String id) throws QueryApiException {
        Response response;
        QueryApiException exception = null;

        try {
            response =  annotationsResource.create(authenticatedServiceUser, index, id);

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
    public Response update(final ServiceUser authenticatedServiceUser,
                           final String index,
                           final String id,
                           final Annotation annotation) throws QueryApiException {
        Response response;
        Exception exception = null;

        try {
            response =  annotationsResource.update(authenticatedServiceUser, index, id, annotation);

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
    public Response remove(final ServiceUser authenticatedServiceUser,
                           final String index,
                           final String id) throws QueryApiException {
        Response response;
        Exception exception = null;

        try {
            response =  annotationsResource.remove(authenticatedServiceUser, index, id);

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
