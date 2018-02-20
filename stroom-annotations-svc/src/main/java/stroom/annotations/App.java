package stroom.annotations;

import com.bendb.dropwizard.jooq.JooqBundle;
import com.bendb.dropwizard.jooq.JooqFactory;
import io.dropwizard.Application;
import io.dropwizard.configuration.EnvironmentVariableSubstitutor;
import io.dropwizard.configuration.SubstitutingSourceProvider;
import io.dropwizard.db.DataSourceFactory;
import io.dropwizard.db.ManagedDataSource;
import io.dropwizard.flyway.FlywayBundle;
import io.dropwizard.flyway.FlywayFactory;
import io.dropwizard.setup.Bootstrap;
import io.dropwizard.setup.Environment;
import org.eclipse.jetty.servlets.CrossOriginFilter;
import org.flywaydb.core.Flyway;
import org.flywaydb.core.api.FlywayException;
import org.glassfish.hk2.api.TypeLiteral;
import org.glassfish.hk2.utilities.binding.AbstractBinder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
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
    private static final Logger LOGGER = LoggerFactory.getLogger(App.class);

    private final JooqBundle<Config> jooqBundle = new JooqBundle<Config>() {
        public DataSourceFactory getDataSourceFactory(Config configuration) {
            return configuration.getDataSourceFactory();
        }

        public JooqFactory getJooqFactory(Config configuration) {
            return configuration.getJooqFactory();
        }

    };

    private final FlywayBundle flywayBundle = new FlywayBundle<Config>() {
        public DataSourceFactory getDataSourceFactory(Config config) {
            return config.getDataSourceFactory();
        }


        public FlywayFactory getFlywayFactory(Config config) {
            return config.getFlywayFactory();
        }
    };

    private final AuditedJooqQueryBundle<Config,
                Annotation,
                AnnotationsDocRefEntity,
                AnnotationsDocRefServiceImpl> auditedQueryBundle =
            new AuditedJooqQueryBundle<>(Annotation.class,
                    jooqBundle,
                    AnnotationsDocRefEntity.class,
                    AnnotationsDocRefServiceImpl.class);

    public static void main(final String[] args) throws Exception {
        new App().run(args);
    }

    @Override
    public void run(final Config configuration,
                    final Environment environment) throws Exception {

        // We need the database before we need most other things
        migrate(configuration, environment);

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

    private static void migrate(Config config, Environment environment) {
        ManagedDataSource dataSource = config.getDataSourceFactory().build(environment.metrics(), "flywayDataSource");
        Flyway flyway = config.getFlywayFactory().build(dataSource);
        // We want to be resilient against the database not being available, so we'll keep trying to migrate if there's
        // an exception. This approach blocks the startup of the service until the database is available. The downside
        // of this is that the admin pages won't be available - any future dashboarding that wants to emit information
        // about the missing database won't be able to do so. The upside of this approach is that it's very simple
        // to implement from where we are now, i.e. we don't need to add service-wide code to handle a missing database
        // e.g. in JwkDao.init().
        boolean migrationComplete = false;
        int databaseRetryDelayMs = 5000;
        while(!migrationComplete){
            try {
                flyway.migrate();
                migrationComplete = true;
            } catch(FlywayException flywayException){
                LOGGER.error("Unable to migrate database! Will retry in {}ms.", databaseRetryDelayMs);
                try {
                    Thread.sleep(databaseRetryDelayMs);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }

            }
        }
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
