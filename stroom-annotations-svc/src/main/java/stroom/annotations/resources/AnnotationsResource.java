package stroom.annotations.resources;

import com.codahale.metrics.annotation.Timed;
import io.dropwizard.auth.Auth;
import io.dropwizard.validation.Validated;
import org.hibernate.validator.constraints.Length;
import stroom.annotations.hibernate.Annotation;
import stroom.query.audit.security.ServiceUser;
import stroom.util.shared.QueryApiException;

import javax.validation.constraints.NotNull;
import javax.ws.rs.Consumes;
import javax.ws.rs.DELETE;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

@Path("/annotations/v1")
@Produces(MediaType.APPLICATION_JSON)
public interface AnnotationsResource {

    @GET
    @Path("/static/welcome")
    @Timed
    Response welcome() throws QueryApiException;

    @GET
    @Path("/static/statusValues")
    @Timed
    Response statusValues() throws QueryApiException;

    @GET
    @Path("/search/{index}")
    @Produces({MediaType.APPLICATION_JSON})
    @Timed
    Response search(@Auth @NotNull ServiceUser authenticatedServiceUser,
                    @Validated
                    @PathParam("index")
                    @NotNull
                    @Length(min= Annotation.MIN_ID_LENGTH) String index,
                    @QueryParam("q") String q,
                    @QueryParam("seekPosition") Integer seekPosition) throws QueryApiException;

    @GET
    @Path("/single/{index}/{id}")
    @Timed
    Response get(@Auth @NotNull ServiceUser authenticatedServiceUser,
                 @Validated
                 @PathParam("index")
                 @NotNull
                 @Length(min=Annotation.MIN_ID_LENGTH) String index,
                 @Validated
                 @PathParam("id")
                 @NotNull
                 @Length(min=Annotation.MIN_ID_LENGTH) String id) throws QueryApiException;

    @GET
    @Path("/single/{index}/{id}/history")
    @Timed
    Response getHistory(@Auth @NotNull ServiceUser authenticatedServiceUser,
                        @Validated
                        @PathParam("index")
                        @NotNull
                        @Length(min=Annotation.MIN_ID_LENGTH) String index,
                        @Validated
                        @PathParam("id")
                        @NotNull
                        @Length(min=Annotation.MIN_ID_LENGTH) String id) throws QueryApiException;

    @POST
    @Path("/single/{index}/{id}")
    @Timed
    Response create(@Auth @NotNull ServiceUser authenticatedServiceUser,
                    @Validated
                    @PathParam("index")
                    @NotNull
                    @Length(min=Annotation.MIN_ID_LENGTH) String index,
                    @Validated
                    @PathParam("id")
                    @NotNull
                    @Length(min=Annotation.MIN_ID_LENGTH) String id) throws QueryApiException;

    @PUT
    @Path("/single/{index}/{id}")
    @Consumes({MediaType.APPLICATION_JSON})
    @Timed
    Response update(@Auth @NotNull ServiceUser authenticatedServiceUser,
                    @Validated
                    @PathParam("index")
                    @NotNull
                    @Length(min=Annotation.MIN_ID_LENGTH) String index,
                    @Validated
                    @PathParam("id")
                    @NotNull
                    @Length(min=Annotation.MIN_ID_LENGTH) String id,
                    Annotation annotation) throws QueryApiException;

    @DELETE
    @Path("/single/{index}/{id}")
    @Timed
    Response remove(@Auth @NotNull ServiceUser authenticatedServiceUser,
                    @Validated
                    @PathParam("index")
                    @NotNull
                    @Length(min=Annotation.MIN_ID_LENGTH) String index,
                    @Validated
                    @PathParam("id")
                    @NotNull
                    @Length(min=Annotation.MIN_ID_LENGTH) String id) throws QueryApiException;
}
