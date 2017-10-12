package stroom.annotations.service.audit;

import io.dropwizard.lifecycle.Managed;

import java.util.function.Consumer;

public interface AuditExecutor extends Managed, Consumer<String> {
}
