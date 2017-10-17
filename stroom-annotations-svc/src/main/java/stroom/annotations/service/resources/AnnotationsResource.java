package stroom.annotations.service.resources;

import com.codahale.metrics.annotation.Timed;
import io.dropwizard.validation.Validated;
import org.hibernate.validator.constraints.Length;
import stroom.annotations.service.model.*;

import javax.validation.constraints.NotNull;
import javax.ws.rs.*;
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
    Response search(@QueryParam("q") String q,
                    @QueryParam("seekId") String seekId,
                    @QueryParam("seekLastUpdated") Long seekLastUpdated);

    @GET
    @Path("/single/{id}")
    @Produces({MediaType.APPLICATION_JSON})
    @Timed
    @NotNull
    Response get(@Validated
                 @PathParam("id")
                 @NotNull
                 @Length(min=AnnotationDTO.MIN_ID_LENGTH) String id);

    @GET
    @Path("/single/{id}/history")
    @Produces({MediaType.APPLICATION_JSON})
    @Timed
    @NotNull
    Response getHistory(@Validated
                        @PathParam("id")
                        @NotNull
                        @Length(min=AnnotationDTO.MIN_ID_LENGTH) String id);

    @POST
    @Path("/single/{id}")
    @Produces({MediaType.APPLICATION_JSON})
    @Timed
    @NotNull
    Response create(@Validated
                    @PathParam("id")
                    @NotNull
                    @Length(min=AnnotationDTO.MIN_ID_LENGTH) String id);

    @PUT
    @Path("/single/{id}")
    @Consumes({MediaType.APPLICATION_JSON})
    @Produces({MediaType.APPLICATION_JSON})
    @Timed
    @NotNull
    Response update(@Validated
                    @PathParam("id")
                    @NotNull
                    @Length(min=AnnotationDTO.MIN_ID_LENGTH) String id,
                    AnnotationDTO annotation);

    @DELETE
    @Path("/single/{id}")
    @Produces({MediaType.APPLICATION_JSON})
    @Timed
    @NotNull
    Response remove(@Validated
                    @PathParam("id")
                    @NotNull
                    @Length(min=AnnotationDTO.MIN_ID_LENGTH) String id);
}
