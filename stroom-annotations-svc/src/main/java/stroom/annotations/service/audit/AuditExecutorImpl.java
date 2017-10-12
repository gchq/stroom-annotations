package stroom.annotations.service.audit;

import event.logging.impl.DefaultEventSerializer;
import event.logging.impl.EventSerializer;

import javax.inject.Inject;
import javax.inject.Singleton;
import java.util.Deque;
import java.util.concurrent.ConcurrentLinkedDeque;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.logging.Logger;

@Singleton
public class AuditExecutorImpl implements AuditExecutor {

    private static final Logger LOGGER = Logger.getLogger(AuditExecutor.class.getName());

    private final ScheduledExecutorService service = Executors.newScheduledThreadPool(2);

    private final Deque<String> events;

    private final EventSerializer eventSerializer = new DefaultEventSerializer();

    private KafkaService kafkaService;

    @Inject
    public AuditExecutorImpl(final KafkaService kafkaService) {
        this.events = new ConcurrentLinkedDeque<>();
        this.kafkaService = kafkaService;
    }

    @Override
    public void start() throws Exception {
        LOGGER.info("Starting Audit Executor");
        service.scheduleAtFixedRate(() -> {
            while (events.size() > 0) {
                final String event = events.pop();
                kafkaService.log(event);
            }
        }, 1, 1, TimeUnit.SECONDS);
    }

    @Override
    public void stop() throws Exception {
        LOGGER.info("Shutting Down Audit Executor");
        service.shutdown();
    }

    @Override
    public void accept(String apiCall) {
        events.push(apiCall);
    }
}
