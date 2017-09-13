package stroom.annotations.service;

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
import org.flywaydb.core.Flyway;
import stroom.annotations.service.health.AnnotationsHealthCheck;
import stroom.annotations.service.resources.AnnotationsResource;

import javax.sql.DataSource;

public class App extends Application<Config> {

    private final JooqBundle jooqBundle = new JooqBundle<Config>() {
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

    public static void main(String[] args) throws Exception {
        new App().run(args);
    }

    @Override
    public void run(Config configuration, Environment environment) throws Exception {
        final AnnotationsResource annotationsResource = new AnnotationsResource();
        final AnnotationsHealthCheck annotationsHealthCheck =
                new AnnotationsHealthCheck();

        environment.healthChecks().register("annotations", annotationsHealthCheck);
        environment.jersey().register(annotationsResource);

        migrate(configuration, environment);
    }

    @Override
    public void initialize(Bootstrap<Config> bootstrap) {
        super.initialize(bootstrap);

        // This allows us to use templating in the YAML configuration.
        bootstrap.setConfigurationSourceProvider(new SubstitutingSourceProvider(
                bootstrap.getConfigurationSourceProvider(),
                new EnvironmentVariableSubstitutor(false)));

        bootstrap.addBundle(this.jooqBundle);
        bootstrap.addBundle(this.flywayBundle);

    }

    private static final void migrate(Config config, Environment environment) {
        ManagedDataSource dataSource = config.getDataSourceFactory().build(environment.metrics(), "flywayDataSource");
        Flyway flyway = config.getFlywayFactory().build((DataSource) dataSource);
        flyway.migrate();
    }

}
