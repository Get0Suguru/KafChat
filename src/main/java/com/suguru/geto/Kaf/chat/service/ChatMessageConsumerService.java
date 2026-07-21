package com.suguru.geto.Kaf.chat.service;

import com.suguru.geto.Kaf.chat.model.ChatMessage;
import com.suguru.geto.Kaf.chat.payload.ChatMessageDto;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;

@Component
@Slf4j
public class ChatMessageConsumerService {

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

        // tem making the send to client above save
        messageDto.setSentAt(LocalDateTime.now());

        messagingTemplate.convertAndSend("/topic/group/" + messageDto.getGroupName(), messageDto);


        // todo x2 -> this makes the message slow appear  in chat (due to save hitting db for checking messageId)
        ChatMessage saved = chatService.saveMessage(messageDto);


//        messagingTemplate.convertAndSend("/topic/group/" + messageDto.getGroupName(), messageDto);
    }
}