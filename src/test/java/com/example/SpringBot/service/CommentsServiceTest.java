package com.example.SpringBot.service;

import com.example.SpringBot.model.Comments;
import com.example.SpringBot.model.User;
import com.example.SpringBot.repository.CommentsRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class CommentsServiceTest {

    @Mock
    private CommentsRepository commentsRepository;

    @InjectMocks
    private CommentsService commentsService;

    private User testUser;
    private Comments testComment;

    @BeforeEach
    void setUp() {
        testUser = new User(1L, "Test User");
        testComment = new Comments(testUser, "Отличный кофе!");
    }

    @Test
    void addComment_WhenValidMessage_SavesComment() {
        // Given
        when(commentsRepository.save(any(Comments.class))).thenReturn(testComment);

        // When
        Comments savedComment = commentsService.addComment(testUser, "Отличный кофе!");

        // Then
        assertNotNull(savedComment);
        assertEquals("Отличный кофе!", savedComment.getMessage());
        verify(commentsRepository, times(1)).save(any(Comments.class));
    }

    @Test
    void addComment_WhenTooShortMessage_ThrowsException() {
        // Given & When & Then
        assertThrows(IllegalArgumentException.class, () -> 
            commentsService.addComment(testUser, "Ок")
        );
        verify(commentsRepository, never()).save(any());
    }

    @Test
    void getAllComments_WhenCommentsExist_ReturnsAllComments() {
        // Given
        Comments comment2 = new Comments(testUser, "Вкусный латте");
        when(commentsRepository.findAll()).thenReturn(Arrays.asList(testComment, comment2));

        // When
        List<Comments> comments = commentsService.getAllComments();

        // Then
        assertEquals(2, comments.size());
        verify(commentsRepository, times(1)).findAll();
    }

    @Test
    void getAllComments_WhenNoComments_ReturnsEmptyList() {
        // Given
        when(commentsRepository.findAll()).thenReturn(Collections.emptyList());

        // When
        List<Comments> comments = commentsService.getAllComments();

        // Then
        assertTrue(comments.isEmpty());
        verify(commentsRepository, times(1)).findAll();
    }

    @Test
    void getUserComments_ReturnsUserCommentsOrderedByDate() {
        // Given
        when(commentsRepository.findByUserIdOrderByCreatedAtDesc(1L))
            .thenReturn(Arrays.asList(testComment));

        // When
        List<Comments> comments = commentsService.getUserComments(1L);

        // Then
        assertEquals(1, comments.size());
        assertEquals("Отличный кофе!", comments.get(0).getMessage());
        verify(commentsRepository, times(1)).findByUserIdOrderByCreatedAtDesc(1L);
    }

    @Test
    void getCommentsCount_ReturnsCorrectCount() {
        // Given
        when(commentsRepository.count()).thenReturn(5L);

        // When
        long count = commentsService.getCommentsCount();

        // Then
        assertEquals(5, count);
        verify(commentsRepository, times(1)).count();
    }
}
