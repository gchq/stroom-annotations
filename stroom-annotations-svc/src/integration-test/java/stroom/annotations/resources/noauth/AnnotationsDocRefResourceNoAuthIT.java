package stroom.annotations.resources.noauth;

import org.junit.ClassRule;
import stroom.annotations.App;
import stroom.annotations.config.Config;
import stroom.annotations.model.AnnotationsDocRefEntity;
import stroom.query.audit.model.DocRefEntity;
import stroom.query.testing.DocRefResourceNoAuthIT;
import stroom.query.testing.DropwizardAppWithClientsRule;

import java.util.HashMap;
import java.util.Map;

import static io.dropwizard.testing.ResourceHelpers.resourceFilePath;

public class AnnotationsDocRefResourceNoAuthIT
        extends DocRefResourceNoAuthIT<AnnotationsDocRefEntity, Config> {

    @ClassRule
    public static final DropwizardAppWithClientsRule<Config> appRule =
            new DropwizardAppWithClientsRule<>(App.class, resourceFilePath("config_noauth.yml"));

    public AnnotationsDocRefResourceNoAuthIT() {
        super(AnnotationsDocRefEntity.class,
                appRule);
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
