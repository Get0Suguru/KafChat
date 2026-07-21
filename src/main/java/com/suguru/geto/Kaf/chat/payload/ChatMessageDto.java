package com.suguru.geto.Kaf.chat.payload;

import lombok.*;
import org.springframework.data.mongodb.core.index.Indexed;

import java.time.LocalDateTime;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class ChatMessageDto {

    @NonNull
    private String sender;
    @NonNull
    private String content;
    @NonNull
    private String groupName;

    private LocalDateTime sentAt;

    // Set only for a targeted mention (e.g. "/*kabadi* message").
    // Null/blank means a normal broadcast message.
    private String targetUser;

    private String messageId; // unique id for each message (to prevent kafka duplicates issue)
}