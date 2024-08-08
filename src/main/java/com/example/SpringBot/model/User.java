package com.example.SpringBot.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;


@Data
@AllArgsConstructor
@NoArgsConstructor // Добавляем эту аннотацию
@Entity
@Table(name = "users")
public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private Long chatID;
    private String name;

    public User(Long chatID, String name) {
        this.chatID = chatID;
        this.name = name;
    }

    // Геттеры и сеттеры
}