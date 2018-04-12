package stroom.annotations;

import com.google.inject.AbstractModule;
import com.google.inject.Guice;
import com.google.inject.Injector;
import com.google.inject.Module;
import com.google.inject.util.Modules;
import io.dropwizard.Application;
import io.dropwizard.configuration.EnvironmentVariableSubstitutor;
import io.dropwizard.configuration.SubstitutingSourceProvider;
import io.dropwizard.setup.Bootstrap;
import io.dropwizard.setup.Environment;
import org.eclipse.jetty.servlets.CrossOriginFilter;
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

    private Injector injector;

    private AuditedJooqQueryBundle<Config,
                    AnnotationsDocRefServiceImpl,
                    AnnotationsDocRefEntity,
                    Annotation> auditedQueryBundle;

    public static void main(final String[] args) throws Exception {
        new App().run(args);
    }

    @Override
    public void run(final Config configuration,
                    final Environment environment) {
        // And we want to configure authentication before the resources
        configureCors(environment);

        environment.jersey().register(injector.getInstance(AuditedAnnotationsResourceImpl.class));
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


    private Module getGuiceModule(final Config config) {
        return Modules.combine(new AbstractModule() {
            @Override
            protected void configure() {
                bind(AnnotationsService.class).to(AnnotationsServiceImpl.class);
            }
        }, auditedQueryBundle.getGuiceModule(config));
    }

    @Override
    public void initialize(final Bootstrap<Config> bootstrap) {
        super.initialize(bootstrap);

        auditedQueryBundle =
                new AuditedJooqQueryBundle<>((c) -> {
                    injector = Guice.createInjector(getGuiceModule(c));
                    return injector;
                },
                AnnotationsDocRefServiceImpl.class,
                AnnotationsDocRefEntity.class,
                Annotation.class);

        // This allows us to use templating in the YAML configuration.
        bootstrap.setConfigurationSourceProvider(new SubstitutingSourceProvider(
                bootstrap.getConfigurationSourceProvider(),
                new EnvironmentVariableSubstitutor(false)));

        bootstrap.addBundle(this.auditedQueryBundle);
    }
}
