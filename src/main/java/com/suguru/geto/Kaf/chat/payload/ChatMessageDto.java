package com.suguru.geto.Kaf.chat.payload;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.NonNull;

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
}
