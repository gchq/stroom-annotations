package stroom.annotations.service.audit;

import org.apache.kafka.clients.producer.*;
import stroom.annotations.service.Config;

import java.nio.charset.Charset;
import java.util.Properties;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import java.util.logging.Logger;

public class AuditServiceImpl implements AuditService {
    private static final Logger LOGGER = Logger.getLogger(AuditServiceImpl.class.getName());

    private volatile Producer<String, byte[]> producer = null;

    private final Config.KafkaConfig config;

    private static final Integer PARTITION = 0;
    private static final String KEY = "0";

    public AuditServiceImpl(final Config.KafkaConfig config) {
        this.config = config;
    }

    @Override
    public void userDidSearch(final String searchTerm) {
        sendLog(String.format("User Searched for Term %s", searchTerm));
    }

    private void sendLog(final String auditLog) {

        try {
            initProducer();
        } catch (Exception e) {
            final String msg = String.format("Error initialising kafka producer to %s, (%s)",
                    this.config,
                    e.getMessage());
            LOGGER.severe(msg);
            return;
        }

        final ProducerRecord<String, byte[]> record =
                new ProducerRecord<>(
                        config.getLoggingTopic(),
                        PARTITION,
                        System.currentTimeMillis(),
                        KEY,
                        auditLog.getBytes(Charset.defaultCharset()));

        Future<RecordMetadata> future = producer.send(record, (recordMetadata, exception) -> {
            LOGGER.info("Record sent to Kafka");
        });

        try {
            future.get();
        } catch (InterruptedException | ExecutionException e) {
            e.printStackTrace();
            LOGGER.warning("Could not flush logging kafka");
        }
    }

    private void initProducer() {
        if (null == this.producer) {
            // Build properties that can be used by the kafka producer
            final Properties props = new Properties();
            props.put(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, config.getBootstrapServers());
            props.put(ProducerConfig.ACKS_CONFIG, "all");
            props.put(ProducerConfig.RETRIES_CONFIG, 0);
            props.put(ProducerConfig.BATCH_SIZE_CONFIG, 16384);
            props.put(ProducerConfig.LINGER_MS_CONFIG, 1);
            props.put(ProducerConfig.BUFFER_MEMORY_CONFIG, 33554432);
            props.put(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG,
                    org.apache.kafka.common.serialization.StringSerializer.class.getName());
            props.put(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG,
                    org.apache.kafka.common.serialization.ByteArraySerializer.class.getName());

            producer = new KafkaProducer<>(props);
        }
    }
}
