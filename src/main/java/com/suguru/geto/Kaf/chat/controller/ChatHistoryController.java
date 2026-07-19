package com.suguru.geto.Kaf.chat.controller;

import com.suguru.geto.Kaf.chat.model.ChatGroup;
import com.suguru.geto.Kaf.chat.payload.ChatMessageDto;
import com.suguru.geto.Kaf.chat.repository.ChatGroupRepo;
import com.suguru.geto.Kaf.chat.repository.ChatMessageRepo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
public class ChatHistoryController {

    @Autowired
    private ChatGroupRepo chatGroupRepo;
    @Autowired
    private ChatMessageRepo chatMessageRepo;

    @GetMapping("/api/groups/{groupName}/messages")
    public List<ChatMessageDto> getHistory(@PathVariable String groupName) {
        ChatGroup group = chatGroupRepo.findByName(groupName);
        if (group == null) return List.of();

        return chatMessageRepo.findByGroup(group).stream()
                .map(m -> {
                    ChatMessageDto dto = new ChatMessageDto();
                    dto.setContent(m.getContent());
                    dto.setSender(m.getSender());
                    dto.setSentAt(m.getSentAt());
                    dto.setGroupName(m.getGroup().getName());
                    return dto;
                })
                .toList();
    }
}