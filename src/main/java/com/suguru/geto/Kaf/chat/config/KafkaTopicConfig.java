package com.suguru.geto.Kaf.chat.config;

import org.apache.kafka.clients.admin.NewTopic;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.config.TopicBuilder;

@Configuration
public class KafkaTopicConfig {

    @Value("${kafka.topic.chat-messages}")
    private String topicName;

    @Bean
    public NewTopic chatMessagesTopic() {
        return TopicBuilder.name(topicName)
                .partitions(3)   // >1 on purpose as with 1 you can't tell fan-out from luck
                .replicas(1)     // fine for a single local broker
                .build();
    }
}