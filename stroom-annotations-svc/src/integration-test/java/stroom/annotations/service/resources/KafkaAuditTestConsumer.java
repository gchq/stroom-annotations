package stroom.annotations.service.resources;

import ch.qos.logback.classic.spi.ILoggingEvent;
import ch.qos.logback.core.Appender;
import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.clients.consumer.ConsumerRecords;
import org.apache.kafka.clients.consumer.KafkaConsumer;
import org.apache.kafka.clients.producer.ProducerConfig;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import stroom.annotations.service.audit.AnnotationsEventLoggingService;
import stroom.annotations.service.audit.KafkaLogbackAppender;
import stroom.annotations.service.audit.KafkaLogbackAppenderFactory;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Properties;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

public class KafkaAuditTestConsumer {

    private final KafkaConsumer<String, String> consumer;

    public KafkaAuditTestConsumer() {
        // Extract the kafka config by digging through the logger
        final Logger auditLogger = LoggerFactory.getLogger(AnnotationsEventLoggingService.AUDIT_LOGGER_NAME);

        assertTrue(auditLogger instanceof ch.qos.logback.classic.Logger);
        final ch.qos.logback.classic.Logger auditLoggerLB = (ch.qos.logback.classic.Logger) auditLogger;

        final Appender<ILoggingEvent> appender = auditLoggerLB.getAppender(KafkaLogbackAppenderFactory.APPENDER_NAME);
        assertNotNull(appender);
        assertTrue(appender instanceof KafkaLogbackAppender);
        KafkaLogbackAppender<?> kafkaLogbackAppender = (KafkaLogbackAppender<?>) appender;

        final String bootstrapServers = kafkaLogbackAppender.getProducerConfig().getProperty(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG);
        final String topic = kafkaLogbackAppender.getTopic();
        assertNotNull(bootstrapServers);
        assertNotNull(topic);

        // Construct properties for the consumer
        Properties props = new Properties();
        props.put(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, bootstrapServers);
        props.put(ConsumerConfig.GROUP_ID_CONFIG, "test");
        props.put(ConsumerConfig.ENABLE_AUTO_COMMIT_CONFIG, "false");
        props.put(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG,
                org.apache.kafka.common.serialization.StringDeserializer.class.getName());
        props.put(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG,
                org.apache.kafka.common.serialization.StringDeserializer.class.getName());

        // Construct the consumer and connect to the audit topic
        consumer = new KafkaConsumer<>(props);
        consumer.subscribe(Collections.singletonList(topic));
    }

    public List<ConsumerRecord<String, String>> getRecords(int expected) {
        List<ConsumerRecord<String, String>> records = new ArrayList<>();


        // This will be run at the end of tests, so it's a one shot
        int attempts = 5;
        while ((records.size() < expected) && (attempts > 0)) {
            final ConsumerRecords<String, String> pollRecords = consumer.poll(1000);
            pollRecords.forEach(records::add);

            attempts -= 1;
        }

        return records;
    }

    public void commitSync() {
        consumer.commitSync();
    }

    public void close() {
        this.consumer.close();
    }
}
