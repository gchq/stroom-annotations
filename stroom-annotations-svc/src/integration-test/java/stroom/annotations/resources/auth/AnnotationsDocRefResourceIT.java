package stroom.annotations.resources.auth;

import com.github.tomakehurst.wiremock.core.WireMockConfiguration;
import org.junit.ClassRule;
import stroom.annotations.App;
import stroom.annotations.config.Config;
import stroom.annotations.hibernate.AnnotationsDocRefEntity;
import stroom.query.audit.service.DocRefEntity;
import stroom.query.testing.DocRefResourceIT;
import stroom.query.testing.DropwizardAppWithClientsRule;
import stroom.query.testing.StroomAuthenticationRule;

import java.util.HashMap;
import java.util.Map;

import static io.dropwizard.testing.ResourceHelpers.resourceFilePath;

public class AnnotationsDocRefResourceIT
        extends DocRefResourceIT<AnnotationsDocRefEntity, Config> {

    @ClassRule
    public static final DropwizardAppWithClientsRule<Config> appRule =
            new DropwizardAppWithClientsRule<>(App.class, resourceFilePath("config_auth.yml"));

    @ClassRule
    public static StroomAuthenticationRule authRule =
            new StroomAuthenticationRule(WireMockConfiguration.options().port(10080), AnnotationsDocRefEntity.TYPE);

    public AnnotationsDocRefResourceIT() {
        super(AnnotationsDocRefEntity.class,
                appRule,
                authRule);
    }

    @Override
    protected AnnotationsDocRefEntity createPopulatedEntity() {
        return new AnnotationsDocRefEntity.Builder().build();
    }

    @Override
    protected Map<String, String> exportValues(AnnotationsDocRefEntity docRefEntity) {
        final Map<String, String> values = new HashMap<>();
        values.put(DocRefEntity.NAME, docRefEntity.getName());
        return values;
    }
}
