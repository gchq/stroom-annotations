package stroom.annotations.service.resources;

import com.codahale.metrics.annotation.Timed;
import io.dropwizard.validation.Validated;
import org.hibernate.validator.constraints.Length;
import org.jooq.DSLContext;
import stroom.annotations.service.model.*;

import javax.validation.constraints.NotNull;
import javax.ws.rs.*;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

@Path("/annotations/v1")
@Produces(MediaType.APPLICATION_JSON)
public interface AnnotationsResource {

    @GET
    @Path("/static/welcome")
    @Produces({MediaType.TEXT_PLAIN})
    @Timed
    @NotNull
    Response welcome();

    @GET
    @Path("/static/statusValues")
    @Produces({MediaType.APPLICATION_JSON})
    @Timed
    @NotNull
    Response statusValues();

    @GET
    @Path("/search")
    @Produces({MediaType.APPLICATION_JSON})
    @Timed
    @NotNull
    Response search(@Context @NotNull DSLContext database,
                    @QueryParam("q") String q,
                    @QueryParam("seekId") String seekId,
                    @QueryParam("seekLastUpdated") Long seekLastUpdated);

    @GET
    @Path("/single/{id}")
    @Produces({MediaType.APPLICATION_JSON})
    @Timed
    @NotNull
    Response get(@Context @NotNull DSLContext database,
                 @Validated
                 @PathParam("id")
                 @NotNull
                 @Length(min=AnnotationDTO.MIN_ID_LENGTH) String id);

    @GET
    @Path("/single/{id}/history")
    @Produces({MediaType.APPLICATION_JSON})
    @Timed
    @NotNull
    Response getHistory(@Context @NotNull DSLContext database,
                        @Validated
                        @PathParam("id")
                        @NotNull
                        @Length(min=AnnotationDTO.MIN_ID_LENGTH) String id);

    @POST
    @Path("/single/{id}")
    @Produces({MediaType.APPLICATION_JSON})
    @Timed
    @NotNull
    Response create(@Context @NotNull DSLContext database,
                    @Validated
                    @PathParam("id")
                    @NotNull
                    @Length(min=AnnotationDTO.MIN_ID_LENGTH) String id);

    @PUT
    @Path("/single/{id}")
    @Consumes({MediaType.APPLICATION_JSON})
    @Produces({MediaType.APPLICATION_JSON})
    @Timed
    @NotNull
    Response update(@Context @NotNull DSLContext database,
                    @Validated
                    @PathParam("id")
                    @NotNull
                    @Length(min=AnnotationDTO.MIN_ID_LENGTH) String id,
                    AnnotationDTO annotation);

    @DELETE
    @Path("/single/{id}")
    @Produces({MediaType.APPLICATION_JSON})
    @Timed
    @NotNull
    Response remove(@Context @NotNull DSLContext database,
                    @Validated
                    @PathParam("id")
                    @NotNull
                    @Length(min=AnnotationDTO.MIN_ID_LENGTH) String id);
}
