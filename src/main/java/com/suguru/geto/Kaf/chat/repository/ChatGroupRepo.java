package com.suguru.geto.Kaf.chat.repository;

import com.suguru.geto.Kaf.chat.model.ChatGroup;
import org.springframework.data.mongodb.repository.MongoRepository;

public interface ChatGroupRepo extends MongoRepository<ChatGroup, String> {

    ChatGroup findByName(String name);
}
