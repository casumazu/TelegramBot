package com.example.SpringBot.repository;

import com.example.SpringBot.model.Comments;
import org.springframework.data.jpa.repository.JpaRepository;

public interface CommentsRepository extends JpaRepository<Comments, Long> {
}
