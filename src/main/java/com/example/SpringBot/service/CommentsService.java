package com.example.SpringBot.service;

import com.example.SpringBot.model.Comments;
import com.example.SpringBot.model.User;
import com.example.SpringBot.repository.CommentsRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class CommentsService {

    private static final int MIN_FEEDBACK_LENGTH = 3;

    private final CommentsRepository commentsRepository;

    @Transactional
    public Comments addComment(User user, String message) {
        if (message.length() < MIN_FEEDBACK_LENGTH) {
            throw new IllegalArgumentException("Отзыв слишком короткий (минимум " + MIN_FEEDBACK_LENGTH + " символа)");
        }
        
        Comments comment = new Comments(user, message);
        Comments savedComment = commentsRepository.save(comment);
        log.info("Добавлен отзыв от пользователя {} (ID: {})", user.getName(), user.getId());
        return savedComment;
    }

    @Transactional(readOnly = true)
    public List<Comments> getAllComments() {
        return commentsRepository.findAll();
    }

    @Transactional(readOnly = true)
    public List<Comments> getUserComments(Long userId) {
        return commentsRepository.findByUserIdOrderByCreatedAtDesc(userId);
    }

    @Transactional(readOnly = true)
    public long getCommentsCount() {
        return commentsRepository.count();
    }
}
