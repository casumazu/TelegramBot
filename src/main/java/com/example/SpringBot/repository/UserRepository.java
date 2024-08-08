package com.example.SpringBot.repository;

import com.example.SpringBot.model.User;
import org.springframework.data.jpa.repository.JpaRepository;

public interface UserRepository extends JpaRepository<User, Long> {

}