package com.example.SpringBot.repository;

import com.example.SpringBot.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository<User, Long> {

    Optional<User> findByChatID(Long chatID);
    
    boolean existsByChatID(Long chatID);
}