package com.suguru.geto.Kaf.chat.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.NonNull;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.mapping.DocumentReference;

import java.util.List;

@Document(collection = "users")
@Data
@AllArgsConstructor
@NoArgsConstructor
public class User {


        @Id
        private String id;

        @Indexed(unique = true)
        private String username;

        @NonNull
        private String password;

        @DocumentReference(collection = "chat_groups")
        private List<ChatGroup> groups;

}
