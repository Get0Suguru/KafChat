package com.suguru.geto.Kaf.chat.repository;

import com.suguru.geto.Kaf.chat.model.User;
import org.springframework.data.mongodb.repository.MongoRepository;

public interface UserRepo extends MongoRepository<User, String> {

    User findByUsername(String username);
}
