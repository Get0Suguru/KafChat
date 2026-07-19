package com.suguru.geto.Kaf.chat.config;

import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.mongodb.core.MongoTemplate;

//@Configuration
public class DatabaseInitializer {

    @Bean
    CommandLineRunner initDatabase(MongoTemplate mongoTemplate) {
        return args -> {
            // This drops the entire database on every startup
            mongoTemplate.getDb().drop();
            System.out.println("MongoDB database dropped and recreated successfully!");
        };
    }
}
