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
import org.eclipse.jetty.http.HttpStatus;
import stroom.annotations.hibernate.Annotation;
import stroom.annotations.hibernate.Status;
import stroom.annotations.model.ResponseMsgDTO;
import stroom.annotations.service.AnnotationsService;
import stroom.query.audit.security.ServiceUser;
import stroom.util.shared.QueryApiException;

import javax.inject.Inject;
import javax.ws.rs.core.Response;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class AuditedAnnotationsResourceImpl implements AnnotationsResource {

    private final AnnotationsService service;

    private final EventLoggingService eventLoggingService;

    static final String WELCOME_TEXT = "Welcome to the annotations service";

    @Inject
    public AuditedAnnotationsResourceImpl(final AnnotationsService service,
                                          final EventLoggingService eventLoggingService) {
        this.service = service;
        this.eventLoggingService = eventLoggingService;
    }

    @Override
    public Response welcome() throws QueryApiException {
        return Response.ok(WELCOME_TEXT)
                .build();
    }

    @Override
    public Response statusValues() throws QueryApiException {
        final Map<String, String> statusValues = Arrays.stream(Status.values())
                .collect(Collectors.toMap(Object::toString, Status::getDisplayText));

        return Response.ok(statusValues)
                .build();
    }

    @Override
    public Response search(final ServiceUser authenticatedServiceUser,
                           final String index,
                           final String q,
                           final Integer seekPosition) throws QueryApiException {
        Response response;
        Exception exception = null;
        
        try {
            final List<Annotation> annotations = service.search(authenticatedServiceUser, index, q, seekPosition);

            response = Response.ok(annotations).build();

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
            response =  service.get(authenticatedServiceUser, index, id)
                    .map(d -> Response.ok(d).build())
                    .orElse(Response.status(HttpStatus.NOT_FOUND_404).build());

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
            response =  service.getHistory(authenticatedServiceUser, index, id)
                    .map(d -> Response.ok(d).build())
                    .orElse(Response.status(HttpStatus.NOT_FOUND_404).build());

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
            response =  service.create(authenticatedServiceUser, index, id)
                    .map(d -> Response.ok(d).build())
                    .orElse(Response.status(HttpStatus.NOT_FOUND_404).build());

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
            response =  service.update(authenticatedServiceUser, index, id, annotation)
                    .map(d -> Response.ok(d).build())
                    .orElse(Response.status(HttpStatus.NOT_FOUND_404).build());

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
            response =  service.remove(authenticatedServiceUser, index, id)
                    .map(d -> Response
                            .ok(ResponseMsgDTO.msg("Annotation deleted")
                                    .recordsUpdated(d ? 1 : 0)
                                    .build())
                            .build())
                    .orElse(Response.status(HttpStatus.NOT_FOUND_404).build());

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
