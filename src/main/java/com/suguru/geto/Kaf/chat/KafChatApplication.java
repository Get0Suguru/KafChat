package com.suguru.geto.Kaf.chat;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.data.mongodb.repository.config.EnableMongoRepositories;

@SpringBootApplication
@EnableMongoRepositories(basePackages = "com.suguru.geto.Kaf.chat.repository")
public class KafChatApplication {

	public static void main(String[] args) {
		SpringApplication.run(KafChatApplication.class, args);
	}

}
