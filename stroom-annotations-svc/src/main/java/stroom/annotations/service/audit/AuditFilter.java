package stroom.annotations.service.audit;

import javax.inject.Inject;
import javax.ws.rs.container.ContainerRequestContext;
import javax.ws.rs.container.ContainerRequestFilter;
import javax.ws.rs.ext.Provider;
import java.io.IOException;
import java.util.logging.Logger;

@Provider
public class AuditFilter implements ContainerRequestFilter {
    private static final Logger LOGGER = Logger.getLogger(AuditFilter.class.getName());

    @Inject
    private AuditService auditService;

    @Override
    public void filter(final ContainerRequestContext requestContext) throws IOException {
        LOGGER.info("Auditing Filter Hit" + auditService);
    }
}
