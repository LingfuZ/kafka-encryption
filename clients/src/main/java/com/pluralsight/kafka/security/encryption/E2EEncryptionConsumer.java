package com.pluralsight.kafka.security.encryption;

import org.apache.kafka.clients.CommonClientConfigs;
import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.clients.consumer.ConsumerRecords;
import org.apache.kafka.clients.consumer.KafkaConsumer;
import org.apache.kafka.common.config.SslConfigs;
import org.apache.kafka.common.serialization.StringDeserializer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.Duration;
import java.util.Collections;
import java.util.Properties;
import java.util.concurrent.ExecutionException;

public class E2EEncryptionConsumer {

    private static final Logger log = LoggerFactory.getLogger(E2EEncryptionConsumer.class);

    public static void main(String[] args) throws ExecutionException, InterruptedException {
        Properties props = new Properties();
        props.put(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, "broker-1:9191,broker-2:9192,broker-3:9193");
        props.put(ConsumerConfig.GROUP_ID_CONFIG, "e2e.consumer");

        props.put(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class.getName());
        props.put(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG, "de.saly.kafka.crypto.DecryptingDeserializer");
        props.put("crypto.wrapped_deserializer", StringDeserializer.class.getName());
        props.put("crypto.rsa.privatekey.filepath", "C:\\kafka_cluster\\security\\private.key");

        props.put(CommonClientConfigs.SECURITY_PROTOCOL_CONFIG, "SSL");

        props.put(SslConfigs.SSL_KEYSTORE_LOCATION_CONFIG, "C:\\kafka_cluster\\security\\keystore\\consumer.keystore.jks");
        props.put(SslConfigs.SSL_KEYSTORE_PASSWORD_CONFIG, "password");
        props.put(SslConfigs.SSL_KEY_PASSWORD_CONFIG, "password");

        props.put(SslConfigs.SSL_TRUSTSTORE_LOCATION_CONFIG, "C:\\kafka_cluster\\security\\keystore\\consumer.keystore.jks");
        props.put(SslConfigs.SSL_TRUSTSTORE_PASSWORD_CONFIG, "password");

        KafkaConsumer<String, String> consumer = new KafkaConsumer<>(props);

        Thread haltedHook = new Thread(consumer::close);
        Runtime.getRuntime().addShutdownHook(haltedHook);

        consumer.subscribe(Collections.singletonList("e2e-topic"));

        while (true) {
            ConsumerRecords<String, String> records = consumer.poll(Duration.ofMillis(100));
            
            records.forEach(record -> log.info("Consumed message: " + record.key() + ":" + record.value()));  
        }
    }
}
