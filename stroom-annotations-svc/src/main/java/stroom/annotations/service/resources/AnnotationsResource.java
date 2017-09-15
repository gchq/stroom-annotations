package stroom.annotations.service.resources;

import com.codahale.metrics.annotation.Timed;
import org.jooq.DSLContext;
import stroom.annotations.service.model.AnnotationDTO;
import stroom.annotations.service.model.ResponseMsgDTO;
import stroom.db.annotations.tables.records.AnnotationsRecord;

import javax.validation.constraints.NotNull;
import javax.ws.rs.*;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import static stroom.db.annotations.Tables.ANNOTATIONS_;

@Path("/annotations/v1")
@Produces(MediaType.APPLICATION_JSON)
public class AnnotationsResource {

    public AnnotationsResource() {
    }

    @GET
    @Path("/welcome")
    @Produces({MediaType.TEXT_PLAIN})
    @Timed
    @NotNull
    public final Response welcome() {
        return Response.status(Response.Status.OK)
                .entity("Welcome to the annotations service")
                .build();
    }

    @GET
    @Path("/{id}")
    @Produces({MediaType.APPLICATION_JSON})
    @Timed
    @NotNull
    public final Response get(@Context @NotNull DSLContext database,
                              @PathParam("id") final String id) {
        final AnnotationsRecord result = database.selectFrom(ANNOTATIONS_)
                .where(ANNOTATIONS_.ID.equal(id))
                .fetchAny();

        try {
            Thread.sleep(1000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        if (null != result) {
            final AnnotationDTO annotationDTO = new AnnotationDTO.Builder()
                    .id(result.getId())
                    .content(result.getContent())
                    .build();

            return Response.status(Response.Status.OK)
                    .entity(annotationDTO)
                    .build();
        } else {
            return Response.status(Response.Status.NOT_FOUND)
                    .entity(ResponseMsgDTO.msg("No annotation found")
                            .recordsUpdated(0)
                            .build())
                    .build();
        }
    }

    @POST
    @Path("/{id}")
    @Produces({MediaType.APPLICATION_JSON})
    @Timed
    @NotNull
    public final Response create(@Context @NotNull DSLContext database,
                                 @PathParam("id") final String id) {

        final int result = database.insertInto(ANNOTATIONS_)
                .columns(ANNOTATIONS_.ID, ANNOTATIONS_.CONTENT)
                .values(id, AnnotationDTO.DEFAULT_ANNOTATION_CONTENT)
                .execute();

        if (result > 0) {
            return get(database, id);
        } else {
            return Response.status(Response.Status.NOT_FOUND)
                    .entity(ResponseMsgDTO.msg("No annotation created")
                            .recordsUpdated(0)
                            .build())
                    .build();
        }
    }

    @PUT
    @Path("/{id}")
    @Consumes({MediaType.TEXT_PLAIN})
    @Produces({MediaType.APPLICATION_JSON})
    @Timed
    @NotNull
    public final Response update(@Context @NotNull DSLContext database,
                                 @PathParam("id") final String id,
                                 final String body) {
        final int result = database.update(ANNOTATIONS_)
                .set(ANNOTATIONS_.CONTENT, body)
                .where(ANNOTATIONS_.ID.equal(id))
                .execute();

        if (result > 0) {
            return get(database, id);
        } else {
            return Response.status(Response.Status.NOT_FOUND)
                    .entity(ResponseMsgDTO.msg("No annotation updated")
                            .recordsUpdated(0)
                            .build())
                    .build();
        }
    }

    @DELETE
    @Path("/{id}")
    @Produces({MediaType.APPLICATION_JSON})
    @Timed
    @NotNull
    public final Response remove(@Context @NotNull DSLContext database,
                                 @PathParam("id") final String id) {
        final int result = database.deleteFrom(ANNOTATIONS_)
                .where(ANNOTATIONS_.ID.equal(id))
                .execute();

        if (result > 0) {
            return Response.status(Response.Status.OK)
                    .entity(ResponseMsgDTO.msg("Annotation deleted")
                            .recordsUpdated(result)
                            .build())
                    .build();
        } else {
            return Response.status(Response.Status.NOT_FOUND)
                    .entity(ResponseMsgDTO.msg("No annotation removed")
                            .recordsUpdated(0)
                            .build())
                    .build();
        }
    }
}
