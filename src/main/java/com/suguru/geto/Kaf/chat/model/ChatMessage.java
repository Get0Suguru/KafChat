package com.suguru.geto.Kaf.chat.model;


import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.NonNull;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.DBRef;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.mapping.DocumentReference;

import java.time.LocalDateTime;

@Document(collection = "chat_messages")
@Data
@AllArgsConstructor
@NoArgsConstructor
public class ChatMessage {

    @Id
    @JsonIgnore
    private String id;

    @NonNull
    private String sender;

    @NonNull
    private String content;

    private LocalDateTime sentAt;

    @DocumentReference(collection = "chat_groups", lazy = true)
    @JsonIgnore
    private ChatGroup group;

    @DocumentReference(collection = "users", lazy = true)
    @JsonIgnore
    private User user;
}
