package com.pluralsight.kafka.security.encryption;

import org.apache.kafka.clients.CommonClientConfigs;
import org.apache.kafka.clients.producer.*;
import org.apache.kafka.common.config.SslConfigs;
import org.apache.kafka.common.serialization.StringSerializer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Properties;
import java.util.UUID;
import java.util.concurrent.ExecutionException;

public class E2EEncryptionProducer {

    private static final Logger log = LoggerFactory.getLogger(E2EEncryptionProducer.class);

    public static void main(String[] args) throws ExecutionException, InterruptedException {
        Properties props = new Properties();
        props.put(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, "broker-1:9191,broker-2:9192,broker-3:9193");
        
        props.put(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, StringSerializer.class.getName());
        props.put(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, "de.saly.kafka.crypto.EncryptingSerializer");
        props.put("crypto.wrapped_serializer", StringSerializer.class.getName());
        props.put("crypto.rsa.publickey.filepath", "C:\\kafka_cluster\\security\\public.key");

        props.put(CommonClientConfigs.SECURITY_PROTOCOL_CONFIG, "SSL");
        props.put(SslConfigs.SSL_KEYSTORE_LOCATION_CONFIG, "C:\\kafka_cluster\\security\\keystore\\producer.keystore.jks");
        props.put(SslConfigs.SSL_KEYSTORE_PASSWORD_CONFIG, "password");
        props.put(SslConfigs.SSL_KEY_PASSWORD_CONFIG, "password");

        props.put(SslConfigs.SSL_TRUSTSTORE_LOCATION_CONFIG, "C:\\kafka_cluster\\security\\keystore\\producer.keystore.jks");
        props.put(SslConfigs.SSL_TRUSTSTORE_PASSWORD_CONFIG, "password");

        KafkaProducer<String, String> producer = new KafkaProducer<>(props);

        Thread haltedHook = new Thread(producer::close);
        Runtime.getRuntime().addShutdownHook(haltedHook);

        long i = 0;
        while(true) {
            String key = String.valueOf(i);
            String value = UUID.randomUUID().toString();

            ProducerRecord<String, String> producerRecord = 
                new ProducerRecord<>("e2e-topic", key, value);
            producer.send(producerRecord);
            log.info("Message sent: " + key + ":" + value);

            i++;
            Thread.sleep(2000);
        }
    }
}
