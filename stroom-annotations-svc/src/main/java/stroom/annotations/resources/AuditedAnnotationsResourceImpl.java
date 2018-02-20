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
import stroom.annotations.model.Annotation;
import stroom.annotations.model.AnnotationsDocRefEntity;
import stroom.annotations.model.Status;
import stroom.annotations.model.ResponseMsgDTO;
import stroom.annotations.service.AnnotationsService;
import stroom.query.api.v2.DocRef;
import stroom.query.audit.DocRefAuditWrapper;
import stroom.query.audit.authorisation.AuthorisationService;
import stroom.query.audit.authorisation.DocumentPermission;
import stroom.query.audit.security.ServiceUser;
import stroom.query.audit.service.DocRefService;

import javax.inject.Inject;
import javax.ws.rs.core.Response;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class AuditedAnnotationsResourceImpl implements AnnotationsResource {

    private final AnnotationsService service;

    public static final String WELCOME_TEXT = "Welcome to the annotations service";

    private final EventLoggingService eventLoggingService;

    private final AuthorisationService authorisationService;

    private final DocRefService<AnnotationsDocRefEntity> docRefService;

    @Inject
    public AuditedAnnotationsResourceImpl(final AnnotationsService service,
                                          final AuthorisationService authorisationService,
                                          final EventLoggingService eventLoggingService,
                                          final DocRefService<AnnotationsDocRefEntity> docRefService) {
        this.service = service;
        this.eventLoggingService = eventLoggingService;
        this.authorisationService = authorisationService;
        this.docRefService = docRefService;
    }

    @Override
    public Response welcome() {
        return Response.ok(WELCOME_TEXT)
                .build();
    }

    @Override
    public Response statusValues(){
        final Map<String, String> statusValues = Arrays.stream(Status.values())
                .collect(Collectors.toMap(Object::toString, Status::getDisplayText));

        return Response.ok(statusValues)
                .build();
    }

    public static String SEARCH_ANNOTATIONS = "SEARCH_ANNOTATIONS";

    @Override
    public Response search(final ServiceUser user,
                           final String indexDocRefUuid,
                           final String q,
                           final Integer seekPosition) {
        return DocRefAuditWrapper.<AnnotationsDocRefEntity>withUser(user)
                .withDocRef(new DocRef.Builder()
                        .type(AnnotationsDocRefEntity.TYPE)
                        .uuid(indexDocRefUuid)
                        .build())
                .withDocRefEntity((docRef) -> docRefService.get(user, docRef.getUuid()))
                .withAuthSupplier((docRef) -> authorisationService.isAuthorised(user,
                        docRef,
                        DocumentPermission.READ))
                .withResponse(docRefEntity ->  {
                    final List<Annotation> as = service.search(user, docRefEntity.getUuid(), q, seekPosition);
                    return Response.ok(as).build();
                })
                .withPopulateAudit((eventDetail, response, exception) -> {
                    eventDetail.setTypeId(SEARCH_ANNOTATIONS);
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
                }).callAndAudit(eventLoggingService);
    }

    public static String GET_ANNOTATION = "GET_ANNOTATION";

    @Override
    public Response get(final ServiceUser user,
                        final String indexDocRefUuid,
                        final String id) {
        return DocRefAuditWrapper.<AnnotationsDocRefEntity>withUser(user)
                .withDocRef(new DocRef.Builder()
                        .type(AnnotationsDocRefEntity.TYPE)
                        .uuid(indexDocRefUuid)
                        .build())
                .withDocRefEntity(docRef -> docRefService.get(user, docRef.getUuid()))
                .withAuthSupplier((docRef) -> authorisationService.isAuthorised(user,
                        docRef,
                        DocumentPermission.READ))
                .withResponse(docRefEntity -> service.get(user, docRefEntity.getUuid(), id)
                        .map(d -> Response.ok(d).build())
                        .orElse(Response.status(HttpStatus.NOT_FOUND_404).build()))
                .withPopulateAudit((eventDetail, response, exception) -> {
                    eventDetail.setTypeId(GET_ANNOTATION);
                    eventDetail.setDescription("Get a specific Annotation by ID");

                    eventDetail.setView(getOutcomeForId(id));
                    eventDetail.getView().getOutcome().setSuccess(null != exception);
                }).callAndAudit(eventLoggingService);
    }

    public static String GET_ANNOTATION_HISTORY = "GET_ANNOTATION_HISTORY";

    @Override
    public Response getHistory(final ServiceUser user,
                               final String indexDocRefUuid,
                               final String id) {
        return DocRefAuditWrapper.<AnnotationsDocRefEntity>withUser(user)
                .withDocRef(new DocRef.Builder()
                        .type(AnnotationsDocRefEntity.TYPE)
                        .uuid(indexDocRefUuid)
                        .build())
                .withDocRefEntity(docRef -> docRefService.get(user, docRef.getUuid()))
                .withAuthSupplier(docRef -> authorisationService.isAuthorised(user,
                        docRef,
                        DocumentPermission.READ))
                .withResponse(docRefEntity -> service.getHistory(user, docRefEntity.getUuid(), id)
                        .map(d -> Response.ok(d).build())
                        .orElse(Response.status(HttpStatus.NOT_FOUND_404).build()))
                .withPopulateAudit((eventDetail, response, exception) -> {
                    eventDetail.setTypeId(GET_ANNOTATION_HISTORY);
                    eventDetail.setDescription("Get the history of a specific Annotation by ID");

                    eventDetail.setView(getOutcomeForId(id));
                    eventDetail.getView().getOutcome().setSuccess(null != exception);
                }).callAndAudit(eventLoggingService);
    }

    public static String CREATE_ANNOTATION = "CREATE_ANNOTATION";

    @Override
    public Response create(final ServiceUser user,
                           final String indexDocRefUuid,
                           final String id) {
        return DocRefAuditWrapper.<AnnotationsDocRefEntity>withUser(user)
                .withDocRef(new DocRef.Builder()
                        .type(AnnotationsDocRefEntity.TYPE)
                        .uuid(indexDocRefUuid)
                        .build())
                .withDocRefEntity(docRef -> docRefService.get(user, docRef.getUuid()))
                .withAuthSupplier(docRef -> authorisationService.isAuthorised(user,
                        docRef,
                        DocumentPermission.UPDATE))
                .withResponse(docRefEntity ->  service.create(user, indexDocRefUuid, id)
                        .map(d -> Response.ok(d).build())
                        .orElse(Response.status(HttpStatus.NOT_FOUND_404).build()))
                .withPopulateAudit((eventDetail, response, exception) -> {
                    eventDetail.setTypeId(CREATE_ANNOTATION);
                    eventDetail.setDescription("Create a new Annotation by a specific ID");

                    eventDetail.setCreate(getOutcomeForId(id));
                    eventDetail.getCreate().getOutcome().setSuccess(null != exception);
                }).callAndAudit(eventLoggingService);
    }

    public static String UPDATE_ANNOTATION = "UPDATE_ANNOTATION";

    @Override
    public Response update(final ServiceUser user,
                           final String indexDocRefUuid,
                           final String id,
                           final Annotation annotation) {
        return DocRefAuditWrapper.<AnnotationsDocRefEntity>withUser(user)
                .withDocRef(new DocRef.Builder()
                        .type(AnnotationsDocRefEntity.TYPE)
                        .uuid(indexDocRefUuid)
                        .build())
                .withDocRefEntity(docRef -> docRefService.get(user, docRef.getUuid()))
                .withAuthSupplier(docRef -> authorisationService.isAuthorised(user,
                        docRef,
                        DocumentPermission.UPDATE))
                .withResponse(docRefEntity -> service.update(user, indexDocRefUuid, id, annotation)
                        .map(d -> Response.ok(d).build())
                        .orElse(Response.status(HttpStatus.NOT_FOUND_404).build()))
                .withPopulateAudit((eventDetail, response, exception) -> {
                    eventDetail.setTypeId(UPDATE_ANNOTATION);
                    eventDetail.setDescription("Update an new Annotation with a specific ID");

                    final Event.EventDetail.Update update = new Event.EventDetail.Update();
                    eventDetail.setUpdate(update);

                    final Outcome outcome = new Outcome();
                    update.setOutcome(outcome);

                    outcome.setSuccess(null != exception);

                    update.getData().add(getDataForId(id));
                }).callAndAudit(eventLoggingService);
    }

    public static String REMOVE_ANNOTATION = "REMOVE_ANNOTATION";

    @Override
    public Response remove(final ServiceUser user,
                           final String indexDocRefUuid,
                           final String id) {
        return DocRefAuditWrapper.<AnnotationsDocRefEntity>withUser(user)
                .withDocRef(new DocRef.Builder()
                        .type(AnnotationsDocRefEntity.TYPE)
                        .uuid(indexDocRefUuid)
                        .build())
                .withDocRefEntity(docRef -> docRefService.get(user, docRef.getUuid()))
                .withAuthSupplier(docRef -> authorisationService.isAuthorised(user,
                        docRef,
                        DocumentPermission.DELETE))
                .withResponse(docRefEntity -> service.remove(user, indexDocRefUuid, id)
                        .map(d -> Response
                                .ok(ResponseMsgDTO.msg("Annotation deleted")
                                        .recordsUpdated(d ? 1 : 0)
                                        .build())
                                .build())
                        .orElse(Response.status(HttpStatus.NOT_FOUND_404).build()))
                .withPopulateAudit((eventDetail, response, exception) -> {
                    eventDetail.setTypeId(REMOVE_ANNOTATION);
                    eventDetail.setDescription("Remove an new Annotation with a specific ID");

                    eventDetail.setDelete(getOutcomeForId(id));
                    eventDetail.getDelete().getOutcome().setSuccess(null != exception);
                }).callAndAudit(eventLoggingService);
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
