package stroom.annotations.service.resources;

import com.codahale.metrics.annotation.Timed;
import org.jooq.DSLContext;
import stroom.annotations.service.model.AnnotationDTO;
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

        final AnnotationDTO annotationDTO = new AnnotationDTO.Builder()
                .id(result.getId())
                .content(result.getContent())
                .build();

        return Response.status(Response.Status.OK)
                .entity(annotationDTO)
                .build();
    }

    @POST
    @Path("/{id}")
    @Consumes({MediaType.TEXT_PLAIN})
    @Produces({MediaType.TEXT_PLAIN})
    @Timed
    @NotNull
    public final Response create(@Context @NotNull DSLContext database,
                                 @PathParam("id") final String id) {

        final int result = database.insertInto(ANNOTATIONS_)
                .columns(ANNOTATIONS_.ID)
                .values(id)
                .execute();

        return Response.status(Response.Status.OK)
                .entity(result)
                .build();
    }

    @PUT
    @Path("/{id}")
    @Consumes({MediaType.TEXT_PLAIN})
    @Produces({MediaType.TEXT_PLAIN})
    @Timed
    @NotNull
    public final Response update(@Context @NotNull DSLContext database,
                                 @PathParam("id") final String id,
                                 final String body) {
        final int result = database.update(ANNOTATIONS_)
                .set(ANNOTATIONS_.CONTENT, body)
                .where(ANNOTATIONS_.ID.equal(id))
                .execute();

        return Response.status(Response.Status.OK)
                .entity(result)
                .build();
    }

    @DELETE
    @Path("/{id}")
    @Timed
    @NotNull
    public final Response remove(@Context @NotNull DSLContext database,
                                 @PathParam("id") final String id) {
        final int result = database.deleteFrom(ANNOTATIONS_)
                .where(ANNOTATIONS_.ID.equal(id))
                .execute();

        return Response.status(Response.Status.OK)
                .entity(result)
                .build();
    }
}
