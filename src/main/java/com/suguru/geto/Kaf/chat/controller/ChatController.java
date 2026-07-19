package com.suguru.geto.Kaf.chat.controller;

import com.suguru.geto.Kaf.chat.model.ChatGroup;
import com.suguru.geto.Kaf.chat.model.ChatMessage;
import com.suguru.geto.Kaf.chat.payload.ChatMessageDto;
import com.suguru.geto.Kaf.chat.repository.ChatGroupRepo;
import com.suguru.geto.Kaf.chat.repository.ChatMessageRepo;
import com.suguru.geto.Kaf.chat.service.ChatService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.messaging.handler.annotation.Header;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Controller;

import java.util.List;

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


    @MessageMapping("/chat.joinGroup")
    public void joinGroup(@Payload String groupName) {
        chatService.findOrCreateGroup(groupName);
    }


    // here @Payload is websocket world -> request body   || its not http request ->  its a pipeline (realtime communication)
    @MessageMapping("/chat.sendMessage")            // Websocket world -> post mapping
    public void sendMessage(@Payload ChatMessageDto messageDto) {


        ChatMessage message = chatService.saveMessage(messageDto);
        messageDto.setSentAt(message.getSentAt());
        log.debug("sending in group u subbed to || destination = {}", "/topic/group/" + messageDto.getGroupName());
        // to send message
        messagingTemplate.convertAndSend("/topic/group/" + messageDto.getGroupName(), messageDto);

    }
}
