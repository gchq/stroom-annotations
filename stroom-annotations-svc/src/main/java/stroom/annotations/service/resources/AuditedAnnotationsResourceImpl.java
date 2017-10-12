package stroom.annotations.service.resources;

import event.logging.Event;
import event.logging.EventLoggingService;
import org.jooq.DSLContext;
import stroom.annotations.service.audit.functions.*;
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
    public Response welcome() {
        return annotationsResource.welcome();
    }

    @Override
    public Response statusValues() {
        return annotationsResource.statusValues();
    }

    @Override
    public Response search(final DSLContext database,
                           final String q,
                           final String seekId,
                           final Long seekLastUpdated) {
        Response response = null;
        Exception exception = null;
        
        try {
            response = annotationsResource.search(database, q, seekId, seekLastUpdated);

            return response;
        } catch (Exception e) {
            exception = e;
            throw e;
        } finally {
            final Event event = eventLoggingService.createEvent();
            new ApiSearch(response, exception, q, seekId, seekLastUpdated)
                    .enrichEventDetail(event.getEventDetail());
            eventLoggingService.log(event);
        }
    }

    @Override
    public Response get(final DSLContext database,
                        final String id) {
        Response response = null;
        Exception exception = null;
        
        try {
            response =  annotationsResource.get(database, id);

            return response;
        } catch (Exception e) {
            exception = e;
            throw e;
        } finally {
            final Event event = eventLoggingService.createEvent();
            new ApiGet(response, exception, id)
                    .enrichEventDetail(event.getEventDetail());
            eventLoggingService.log(event);
        }
    }

    @Override
    public Response getHistory(final DSLContext database,
                               final String id) {
        Response response = null;
        Exception exception = null;

        try {
            response =  annotationsResource.getHistory(database, id);

            return response;
        } catch (Exception e) {
            exception = e;
            throw e;
        } finally {
            final Event event = eventLoggingService.createEvent();
            new ApiGetHistory(response, exception, id)
                    .enrichEventDetail(event.getEventDetail());
            eventLoggingService.log(event);
        }
    }

    @Override
    public Response create(final DSLContext database,
                           final String id) {
        Response response = null;
        Exception exception = null;

        try {
            response =  annotationsResource.create(database, id);

            return response;
        } catch (Exception e) {
            exception = e;
            throw e;
        } finally {
            final Event event = eventLoggingService.createEvent();
            new ApiCreate(response, exception, id)
                    .enrichEventDetail(event.getEventDetail());
            eventLoggingService.log(event);
        }
    }

    @Override
    public Response update(final DSLContext database,
                           final String id,
                           final AnnotationDTO annotation) {
        Response response = null;
        Exception exception = null;

        try {
            response =  annotationsResource.update(database, id, annotation);

            return response;
        } catch (Exception e) {
            exception = e;
            throw e;
        } finally {
            final Event event = eventLoggingService.createEvent();
            new ApiUpdate(response, exception, id)
                    .enrichEventDetail(event.getEventDetail());
            eventLoggingService.log(event);
        }
    }

    @Override
    public Response remove(final DSLContext database,
                           final String id) {
        Response response = null;
        Exception exception = null;

        try {
            response =  annotationsResource.remove(database, id);

            return response;
        } catch (Exception e) {
            exception = e;
            throw e;
        } finally {
            final Event event = eventLoggingService.createEvent();
            new ApiDelete(response, exception, id)
                    .enrichEventDetail(event.getEventDetail());
            eventLoggingService.log(event);
        }
    }
}
