package com.suguru.geto.Kaf.chat.consumer;

import com.suguru.geto.Kaf.chat.model.ChatMessage;
import com.suguru.geto.Kaf.chat.payload.ChatMessageDto;
import com.suguru.geto.Kaf.chat.service.ChatService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Component;

@Component
@Slf4j
public class ChatMessageConsumer {

    @Autowired
    private ChatService chatService;
    @Autowired
    private SimpMessagingTemplate messagingTemplate;

    // again if u forgot -> we using single topic -> chat-messages  and key -> group name  so they land in same partition
    // making new topic per group is a bad idea -> they can go pretty high & again we want the same partition per group
    @KafkaListener(topics = "${kafka.topic.chat-messages}")
    public void consume(ChatMessageDto messageDto) {
        log.debug("consumed from kafka || group={}, sender={}", messageDto.getGroupName(), messageDto.getSender());
        // workflow is -> we brodcast to kafka topic from the controller as grepping message by /app (websocket)
        // then from kafka we get the message and brodcast to websocket tunnel of that instance -> save it in the database
        // then we send it to the client

        // making sure each instance of jvm running will be able to hold on info (with alone websockets horizontal scaling is not possible)

        ChatMessage saved = chatService.saveMessage(messageDto);
        messageDto.setSentAt(saved.getSentAt());

        messagingTemplate.convertAndSend("/topic/group/" + messageDto.getGroupName(), messageDto);
    }
}