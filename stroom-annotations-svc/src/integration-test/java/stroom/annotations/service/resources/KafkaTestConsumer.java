package stroom.annotations.service.resources;

import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.clients.consumer.ConsumerRecords;
import org.apache.kafka.clients.consumer.KafkaConsumer;
import stroom.annotations.service.KafkaConfig;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Properties;

public class KafkaTestConsumer {

    private final KafkaConsumer<String, String> consumer;

    public KafkaTestConsumer(final KafkaConfig config) {
        Properties props = new Properties();
        props.put("bootstrap.servers", config.getBootstrapServers());
        props.put("group.id", "test");
        props.put("enable.auto.commit", "false");
        props.put("key.deserializer", org.apache.kafka.common.serialization.StringDeserializer.class.getName());
        props.put("value.deserializer", org.apache.kafka.common.serialization.StringDeserializer.class.getName());
        consumer = new KafkaConsumer<>(props);
        consumer.subscribe(Collections.singletonList(config.getLoggingTopic()));
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
