package com.suguru.geto.Kaf.chat.repository;

import com.suguru.geto.Kaf.chat.model.ChatGroup;
import com.suguru.geto.Kaf.chat.model.ChatMessage;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ChatMessageRepo extends MongoRepository<ChatMessage, String> {
    List<ChatMessage> findByGroup(ChatGroup group);
}
