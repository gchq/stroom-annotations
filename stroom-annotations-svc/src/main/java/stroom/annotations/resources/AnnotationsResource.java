package stroom.annotations.resources;

import com.codahale.metrics.annotation.Timed;
import io.dropwizard.validation.Validated;
import org.hibernate.validator.constraints.Length;
import stroom.annotations.model.AnnotationDTO;

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
    Response welcome() throws AnnotationsException;

    @GET
    @Path("/static/statusValues")
    @Timed
    Response statusValues() throws AnnotationsException;

    @GET
    @Path("/search/{index}")
    @Produces({MediaType.APPLICATION_JSON})
    @Timed
    Response search(@Validated
                    @PathParam("index")
                    @NotNull
                    @Length(min=AnnotationDTO.MIN_ID_LENGTH) String index,
                    @QueryParam("q") String q,
                    @QueryParam("seekPosition") Integer seekPosition) throws AnnotationsException;

    @GET
    @Path("/single/{index}/{id}")
    @Timed
    Response get(@Validated
                 @PathParam("index")
                 @NotNull
                 @Length(min=AnnotationDTO.MIN_ID_LENGTH) String index,
                 @Validated
                 @PathParam("id")
                 @NotNull
                 @Length(min=AnnotationDTO.MIN_ID_LENGTH) String id) throws AnnotationsException;

    @GET
    @Path("/single/{index}/{id}/history")
    @Timed
    Response getHistory(@Validated
                        @PathParam("index")
                        @NotNull
                        @Length(min=AnnotationDTO.MIN_ID_LENGTH) String index,
                        @Validated
                        @PathParam("id")
                        @NotNull
                        @Length(min=AnnotationDTO.MIN_ID_LENGTH) String id) throws AnnotationsException;

    @POST
    @Path("/single/{index}/{id}")
    @Timed
    Response create(@Validated
                    @PathParam("index")
                    @NotNull
                    @Length(min=AnnotationDTO.MIN_ID_LENGTH) String index,
                    @Validated
                    @PathParam("id")
                    @NotNull
                    @Length(min=AnnotationDTO.MIN_ID_LENGTH) String id) throws AnnotationsException;

    @PUT
    @Path("/single/{index}/{id}")
    @Consumes({MediaType.APPLICATION_JSON})
    @Timed
    Response update(@Validated
                    @PathParam("index")
                    @NotNull
                    @Length(min=AnnotationDTO.MIN_ID_LENGTH) String index,
                    @Validated
                    @PathParam("id")
                    @NotNull
                    @Length(min=AnnotationDTO.MIN_ID_LENGTH) String id,
                    AnnotationDTO annotation) throws AnnotationsException;

    @DELETE
    @Path("/single/{index}/{id}")
    @Timed
    Response remove(@Validated
                    @PathParam("index")
                    @NotNull
                    @Length(min=AnnotationDTO.MIN_ID_LENGTH) String index,
                    @Validated
                    @PathParam("id")
                    @NotNull
                    @Length(min=AnnotationDTO.MIN_ID_LENGTH) String id) throws AnnotationsException;
}
