package stroom.annotations.service.resources;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.ws.rs.core.Response;
import javax.ws.rs.ext.ExceptionMapper;
import javax.ws.rs.ext.Provider;

@Provider
public class AnnotationsExceptionMapper implements ExceptionMapper<AnnotationsException> {
    private static final Logger LOGGER = LoggerFactory.getLogger(AnnotationsExceptionMapper.class);

    @Override
    public Response toResponse(final AnnotationsException e) {
        LOGGER.warn("Exception seen on REST interface", e);

        return Response.serverError().entity(e.getLocalizedMessage()).build();
    }
}
