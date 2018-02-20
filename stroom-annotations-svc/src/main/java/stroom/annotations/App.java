package stroom.annotations;

import io.dropwizard.Application;
import io.dropwizard.configuration.EnvironmentVariableSubstitutor;
import io.dropwizard.configuration.SubstitutingSourceProvider;
import io.dropwizard.setup.Bootstrap;
import io.dropwizard.setup.Environment;
import org.eclipse.jetty.servlets.CrossOriginFilter;
import org.glassfish.hk2.api.TypeLiteral;
import org.glassfish.hk2.utilities.binding.AbstractBinder;
import stroom.annotations.config.Config;
import stroom.annotations.model.Annotation;
import stroom.annotations.model.AnnotationsDocRefEntity;
import stroom.annotations.resources.AuditedAnnotationsResourceImpl;
import stroom.annotations.service.AnnotationsDocRefServiceImpl;
import stroom.annotations.service.AnnotationsService;
import stroom.annotations.service.AnnotationsServiceImpl;
import stroom.query.audit.service.DocRefService;
import stroom.query.jooq.AuditedJooqQueryBundle;

import javax.servlet.DispatcherType;
import javax.servlet.FilterRegistration;
import java.util.EnumSet;

public class App extends Application<Config> {

    private final AuditedJooqQueryBundle<Config,
                    AnnotationsDocRefServiceImpl,
                    AnnotationsDocRefEntity,
                    Annotation> auditedQueryBundle =
            new AuditedJooqQueryBundle<>(
                    AnnotationsDocRefServiceImpl.class,
                    AnnotationsDocRefEntity.class,
                    Annotation.class);

    public static void main(final String[] args) throws Exception {
        new App().run(args);
    }

    @Override
    public void run(final Config configuration,
                    final Environment environment) {

        // And we want to configure authentication before the resources
        configureCors(environment);

        environment.jersey().register(new AbstractBinder() {
            @Override
            protected void configure() {
                bind(AnnotationsServiceImpl.class).to(AnnotationsService.class);
                bind(AnnotationsDocRefServiceImpl.class).to(new TypeLiteral<DocRefService<AnnotationsDocRefEntity>>() {});
            }
        });
        environment.jersey().register(AuditedAnnotationsResourceImpl.class);
    }
    private static void configureCors(final Environment environment) {
        FilterRegistration.Dynamic cors = environment.servlets().addFilter("CORS", CrossOriginFilter.class);
        cors.addMappingForUrlPatterns(EnumSet.allOf(DispatcherType.class), true, new String[]{"/*"});
        cors.setInitParameter("allowedMethods", "GET,PUT,POST,DELETE,OPTIONS");
        cors.setInitParameter("allowedOrigins", "*");
        cors.setInitParameter("Access-Control-Allow-Origin", "*");
        cors.setInitParameter("allowedHeaders", "Content-Type,Authorization,X-Requested-With,Content-Length,Accept,Origin");
        cors.setInitParameter("allowCredentials", "true");
    }

    @Override
    public void initialize(final Bootstrap<Config> bootstrap) {
        super.initialize(bootstrap);

        // This allows us to use templating in the YAML configuration.
        bootstrap.setConfigurationSourceProvider(new SubstitutingSourceProvider(
                bootstrap.getConfigurationSourceProvider(),
                new EnvironmentVariableSubstitutor(false)));

        bootstrap.addBundle(this.auditedQueryBundle);
    }
}
