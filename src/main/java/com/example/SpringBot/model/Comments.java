package com.example.SpringBot.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Data
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "comments", indexes = {
    @Index(name = "idx_comment_user", columnList = "user_id")
})
public class Comments {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false, foreignKey = @ForeignKey(name = "fk_user_comments"))
    private User user;

    @NotBlank(message = "Текст отзыва не может быть пустым")
    @Size(min = 3, max = 500, message = "Длина отзыва должна быть от 3 до 500 символов")
    @Column(nullable = false, length = 500)
    private String message;

    @Column(name = "created_at")
    private LocalDateTime createdAt;

    public Comments(User user, String message) {
        this.user = user;
        this.message = message;
        this.createdAt = LocalDateTime.now();
    }
}
