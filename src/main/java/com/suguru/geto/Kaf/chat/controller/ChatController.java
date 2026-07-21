package com.suguru.geto.Kaf.chat.controller;

import com.suguru.geto.Kaf.chat.model.ChatGroup;
import com.suguru.geto.Kaf.chat.model.ChatMessage;
import com.suguru.geto.Kaf.chat.payload.ChatMessageDto;
import com.suguru.geto.Kaf.chat.repository.ChatGroupRepo;
import com.suguru.geto.Kaf.chat.repository.ChatMessageRepo;
import com.suguru.geto.Kaf.chat.service.ChatService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.messaging.handler.annotation.Header;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Controller;

import java.util.List;
import java.util.UUID;

@Controller
@Slf4j
public class ChatController {

    @Autowired
    private ChatService chatService;

    @Autowired
    private ChatMessageRepo chatMessageRepo;

    @Autowired
    private ChatGroupRepo chatGroupRepo;
    @Autowired
    private SimpMessagingTemplate messagingTemplate;

    @Autowired
    private KafkaTemplate<String, ChatMessageDto> kafkaTemplate;

    @Value("${kafka.topic.chat-messages}")
    private String chatTopic;


    @MessageMapping("/chat.joinGroup")
    public void joinGroup(@Payload String groupName, @Header("simpSessionId") String sessionId) {

        log.error("join group hit. group name={}, sessionId={}", groupName, sessionId);

        ChatGroup group = chatService.findOrCreateGroup(groupName); // Create group here
        log.info("found/created group with name {}", group.getName());

        // No response needed since this is just to ensure group creation
        List<ChatMessage> messages = chatMessageRepo.findByGroup(chatGroupRepo.findByName(groupName));
        log.info("prev history of msgs || count = {}", messages.size());

        // converting to client friendly pojo & json object
        for (ChatMessage message : messages) {
            ChatMessageDto messageDto = new ChatMessageDto();
            messageDto.setContent(message.getContent());
            messageDto.setSender(message.getSender());
            messageDto.setSentAt(message.getSentAt());
            messageDto.setGroupName(message.getGroup().getName());

            messagingTemplate.convertAndSendToUser(sessionId,"/queue/group/" + groupName, messageDto);
        }
    }


    // here @Payload is websocket world -> request body   || its not http request ->  its a pipeline (realtime communication)
    @MessageMapping("/chat.sendMessage")            // Websocket world -> post mapping
    public void sendMessage(@Payload ChatMessageDto messageDto) {

        String targetUser = messageDto.getTargetUser();

        // todo x1 -> after kafka its a flaw -> its not over kafka (means only one instance will get it)

        // todo x3 -> thats a great check how -> without kafka the instance change won't able to deliever private msg
//        but in same instance  u can -> great 
        if (targetUser != null && !targetUser.isBlank()) {
            // Ephemeral by design: never touches the DB. If nobody's listening
            // right now, it's gone — same guarantee as the broker itself has
            // no memory of undelivered messages. No history endpoint, no
            // group fetch, nothing will ever surface this later.
            messageDto.setSentAt(java.time.LocalDateTime.now());

            String destination = "/queue/group/" + messageDto.getGroupName();
            log.debug("targeted send (not persisted) || user={}, destination={}", targetUser, destination);
            messagingTemplate.convertAndSendToUser(targetUser, destination, messageDto);
            messagingTemplate.convertAndSendToUser(messageDto.getSender(), destination, messageDto); // echo to sender
        } else {
//            ChatMessage message = chatService.saveMessage(messageDto);
//            messageDto.setSentAt(message.getSentAt());
//            log.debug("broadcasting to group || destination = {}", "/topic/group/" + messageDto.getGroupName());
//            messagingTemplate.convertAndSend("/topic/group/" + messageDto.getGroupName(), messageDto);
            log.debug("publishing to kafka || topic={}, key={}", chatTopic, messageDto.getGroupName());

            // producing to kafka topic
            messageDto.setMessageId(UUID.randomUUID().toString());
            kafkaTemplate.send(chatTopic, messageDto.getGroupName(), messageDto);
            // we are replacing the above lines with kafkaTemplate.send(chatTopic, messageDto.getGroupName(), messageDto);
            // the msg now first go though kafka layer and front that it'll picked again to save anall that

            // format of method is (topic, key, value)

        }
    }
}