package stroom.annotations;

import io.dropwizard.Application;
import io.dropwizard.ConfiguredBundle;
import io.dropwizard.configuration.EnvironmentVariableSubstitutor;
import io.dropwizard.configuration.SubstitutingSourceProvider;
import io.dropwizard.db.DataSourceFactory;
import io.dropwizard.db.ManagedDataSource;
import io.dropwizard.flyway.FlywayBundle;
import io.dropwizard.flyway.FlywayFactory;
import io.dropwizard.hibernate.HibernateBundle;
import io.dropwizard.setup.Bootstrap;
import io.dropwizard.setup.Environment;
import org.eclipse.jetty.servlets.CrossOriginFilter;
import org.glassfish.hk2.api.TypeLiteral;
import org.glassfish.hk2.utilities.binding.AbstractBinder;
import stroom.annotations.config.Config;
import stroom.annotations.hibernate.Annotation;
import stroom.annotations.hibernate.AnnotationHistory;
import stroom.annotations.hibernate.AnnotationsDocRefEntity;
import stroom.annotations.resources.AuditedAnnotationsResourceImpl;
import stroom.annotations.service.AnnotationsDocRefServiceImpl;
import stroom.annotations.service.AnnotationsService;
import stroom.annotations.service.AnnotationsServiceImpl;
import stroom.query.audit.service.DocRefService;
import stroom.query.hibernate.AuditedCriteriaQueryBundle;

import javax.servlet.DispatcherType;
import javax.servlet.FilterRegistration;
import java.util.EnumSet;

public class App extends Application<Config> {

    // Wrap the flyway bundle so that we can call migrate in the bundles 'run'.
    // This allows the flyway migration to happen before the hibernate validation
    private final ConfiguredBundle<Config> flywayBundle = new ConfiguredBundle<Config>() {

        private final FlywayBundle<Config> wrappedBundle = new FlywayBundle<Config>() {
            public DataSourceFactory getDataSourceFactory(Config config) {
                return config.getDataSourceFactory();
            }

            public FlywayFactory getFlywayFactory(final Config config) {
                return config.getFlywayFactory();
            }
        };

        @Override
        public void run(final Config configuration, final Environment environment) throws Exception {
            wrappedBundle.run(environment);

            final ManagedDataSource dataSource = configuration.getDataSourceFactory()
                    .build(environment.metrics(), "flywayDataSource");
            configuration.getFlywayFactory()
                    .build(dataSource)
                    .migrate();
        }

        @Override
        public void initialize(Bootstrap<?> bootstrap) {
            wrappedBundle.initialize(bootstrap);
        }

    };

    private final AuditedCriteriaQueryBundle<Config,
            Annotation,
            AnnotationsDocRefEntity,
            AnnotationsDocRefServiceImpl> auditedQueryBundle =
            new AuditedCriteriaQueryBundle<>(Annotation.class,

                    new HibernateBundle<Config>(Annotation.class, AnnotationHistory.class, AnnotationsDocRefEntity.class) {
                        @Override
                        public DataSourceFactory getDataSourceFactory(Config configuration) {
                            return configuration.getDataSourceFactory();
                        }
                    },
                    AnnotationsDocRefEntity.class,
                    AnnotationsDocRefServiceImpl.class);

    public static void main(final String[] args) throws Exception {
        new App().run(args);
    }

    @Override
    public void run(final Config configuration,
                    final Environment environment) throws Exception {

        // And we want to configure authentication before the resources
        configureCors(environment);

        environment.jersey().register(new AbstractBinder() {
            @Override
            protected void configure() {
                bind(configuration).to(Config.class);
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

        bootstrap.addBundle(this.flywayBundle);
        bootstrap.addBundle(this.auditedQueryBundle);
    }
}
