package com.example.SpringBot.repository;

import com.example.SpringBot.model.Comments;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface CommentsRepository extends JpaRepository<Comments, Long> {
    
    List<Comments> findByUserIdOrderByCreatedAtDesc(Long userId);
    
    long count();
}
