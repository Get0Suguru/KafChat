package com.suguru.geto.Kaf.chat.repository;

import com.suguru.geto.Kaf.chat.model.User;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface UserRepo extends MongoRepository<User, String> {

    User findByUsername(String username);
}
