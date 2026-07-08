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

@Document(collection = "chat_groups")
@Data
@AllArgsConstructor
@NoArgsConstructor
public class ChatGroup {


    @Id
    private String id;  // u pass null and mongo will do the job

    @Indexed(unique = true)  // enabling indexing of name +  making unique true
    @NonNull
    private String name;

    @DocumentReference(collection = "users")
    private List<User> users;

}
