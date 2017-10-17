package stroom.annotations.service;

import com.bendb.dropwizard.jooq.JooqBundle;
import com.bendb.dropwizard.jooq.JooqFactory;
import io.dropwizard.Application;
import io.dropwizard.configuration.EnvironmentVariableSubstitutor;
import io.dropwizard.configuration.SubstitutingSourceProvider;
import io.dropwizard.db.DataSourceFactory;
import io.dropwizard.db.ManagedDataSource;
import io.dropwizard.db.PooledDataSourceFactory;
import io.dropwizard.flyway.FlywayBundle;
import io.dropwizard.flyway.FlywayFactory;
import io.dropwizard.hibernate.HibernateBundle;
import io.dropwizard.setup.Bootstrap;
import io.dropwizard.setup.Environment;
import org.eclipse.jetty.servlets.CrossOriginFilter;
import org.flywaydb.core.Flyway;
import org.jooq.Configuration;
import org.jooq.DSLContext;
import org.jooq.SQLDialect;
import org.jooq.impl.DSL;
import stroom.annotations.service.audit.*;
import stroom.annotations.service.health.AnnotationsHealthCheck;
import stroom.annotations.service.hibernate.Annotation;
import stroom.annotations.service.resources.AuditedAnnotationsResourceImpl;
import stroom.annotations.service.resources.AuditedQueryResourceImpl;

import javax.servlet.DispatcherType;
import javax.servlet.FilterRegistration;
import javax.sql.DataSource;
import java.util.EnumSet;

public class App extends Application<Config> {
    private final JooqBundle jooqBundle = new JooqBundle<Config>() {
        public DataSourceFactory getDataSourceFactory(Config configuration) {
            return configuration.getDataSourceFactory();
        }

        public JooqFactory getJooqFactory(Config configuration) {
            return configuration.getJooqFactory();
        }

    };

    private final HibernateBundle<Config> hibernateBundle = new HibernateBundle<Config>(Annotation.class) {
        @Override
        public DataSourceFactory getDataSourceFactory(Config configuration) {
            return configuration.getDataSourceFactory();
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

        // Register DB health check
        final PooledDataSourceFactory dsf = jooqBundle.getDataSourceFactory(configuration);
        final Configuration jooqConfig = this.jooqBundle.getConfiguration();
        final SQLDialect dialect = configuration.getJooqFactory().getDialect().get();
        final ManagedDataSource dataSource = dsf
                .build(environment.metrics(), dialect.getName());
        final String validationQuery = dsf.getValidationQuery();
        final DSLContext dslContext = DSL.using(dataSource, dialect);

        environment.lifecycle().manage(dataSource);
        environment.healthChecks().register(dialect.getName(), new AnnotationsHealthCheck(dslContext, validationQuery));

        environment.jersey().register(
                new stroom.annotations.service.Module(hibernateBundle.getSessionFactory(),
                        configuration,
                        jooqConfig));
        environment.jersey().register(AuditedAnnotationsResourceImpl.class);
        environment.jersey().register(AuditedQueryResourceImpl.class);

        configureCors(environment);
        migrate(configuration, environment);
    }


    private static void configureCors(Environment environment) {
        FilterRegistration.Dynamic cors = environment.servlets().addFilter("CORS", CrossOriginFilter.class);
        cors.addMappingForUrlPatterns(EnumSet.allOf(DispatcherType.class), true, new String[]{"/*"});
        cors.setInitParameter("allowedMethods", "GET,PUT,POST,DELETE,OPTIONS");
        cors.setInitParameter("allowedOrigins", "*");
        cors.setInitParameter("Access-Control-Allow-Origin", "*");
        cors.setInitParameter("allowedHeaders", "Content-Type,Authorization,X-Requested-With,Content-Length,Accept,Origin");
        cors.setInitParameter("allowCredentials", "true");
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
        bootstrap.addBundle(this.hibernateBundle);
    }

    private static void migrate(Config config, Environment environment) {
        ManagedDataSource dataSource = config.getDataSourceFactory().build(environment.metrics(), "flywayDataSource");
        Flyway flyway = config.getFlywayFactory().build((DataSource) dataSource);
        flyway.migrate();
    }

}
