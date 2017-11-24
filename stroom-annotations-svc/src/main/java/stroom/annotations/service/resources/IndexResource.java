package stroom.annotations.service.resources;

import com.codahale.metrics.annotation.Timed;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

@Path("/indexes/v1")
@Produces(MediaType.APPLICATION_JSON)
public interface IndexResource {

    @GET
    @Path("/")
    @Timed
    Response getIndexes() throws AnnotationsException;

}
