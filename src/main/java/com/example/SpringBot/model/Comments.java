package com.example.SpringBot.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Comments {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private long id;

    @ManyToOne(fetch = FetchType.LAZY, cascade = CascadeType.ALL)  // Добавление cascade = CascadeType.ALL
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    private String message;

    public Comments(User user, String message) {
        this.user = user;
        this.message = message;
    }
}
