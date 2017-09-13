package stroom.annotations.service.health;

import com.codahale.metrics.health.HealthCheck;
import org.jooq.DSLContext;

public class AnnotationsHealthCheck extends HealthCheck {

    private final DSLContext dslContext;
    private final String validationQuery;

    public AnnotationsHealthCheck(DSLContext dslContext, String validationQuery) {
        this.dslContext = dslContext;
        this.validationQuery = validationQuery;
    }

    @Override
    protected Result check() throws Exception {
        dslContext.resultQuery(this.validationQuery).fetch();
        return Result.healthy();
    }
}
