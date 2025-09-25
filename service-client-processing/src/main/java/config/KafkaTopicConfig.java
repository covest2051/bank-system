package config;

import org.apache.kafka.clients.admin.AdminClientConfig;
import org.apache.kafka.clients.admin.NewTopic;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.core.KafkaAdmin;

import java.util.HashMap;
import java.util.Map;

@Configuration
public class KafkaTopicConfig {
    @Bean
    public KafkaAdmin kafkaAdmin() {
        Map<String, Object> configs = new HashMap<>();
        configs.put(AdminClientConfig.BOOTSTRAP_SERVERS_CONFIG, "localhost:9092");
        return new KafkaAdmin(configs);
    }

    @Bean
    public NewTopic clientProductsTopic() {
        return new NewTopic("client_products", 3, (short) 1);
    }

    @Bean
    public NewTopic clientCreditProductsTopic() {
        return new NewTopic("client_credit_products", 3, (short) 1);
    }

    @Bean
    public NewTopic clientCardsTopic() {
        return new NewTopic("client_cards", 3, (short) 1);
    }
}