package com.example.SpringBot.model;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class User {
    private long id;
    private Long chatID;
    private String name;

    public User(Long id, Long chatID, String name) {
        this.id = id;
        this.chatID = chatID;
        this.name = name;
    }
}
