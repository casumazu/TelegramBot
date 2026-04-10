package com.example.SpringBot.service;

import com.example.SpringBot.model.User;
import com.example.SpringBot.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class UserServiceTest {

    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private UserService userService;

    private User testUser;

    @BeforeEach
    void setUp() {
        testUser = new User(12345L, "Test User");
    }

    @Test
    void findByChatId_WhenUserExists_ReturnsUser() {
        // Given
        when(userRepository.findByChatID(12345L)).thenReturn(Optional.of(testUser));

        // When
        Optional<User> foundUser = userService.findByChatId(12345L);

        // Then
        assertTrue(foundUser.isPresent());
        assertEquals("Test User", foundUser.get().getName());
        assertEquals(12345L, foundUser.get().getChatID());
        verify(userRepository, times(1)).findByChatID(12345L);
    }

    @Test
    void findByChatId_WhenUserNotExists_ReturnsEmpty() {
        // Given
        when(userRepository.findByChatID(99999L)).thenReturn(Optional.empty());

        // When
        Optional<User> foundUser = userService.findByChatId(99999L);

        // Then
        assertFalse(foundUser.isPresent());
        verify(userRepository, times(1)).findByChatID(99999L);
    }

    @Test
    void createOrUpdateUser_WhenNewUser_CreatesUser() {
        // Given
        when(userRepository.findByChatID(12345L)).thenReturn(Optional.empty());
        when(userRepository.save(any(User.class))).thenReturn(testUser);

        // When
        User createdUser = userService.createOrUpdateUser(12345L, "Test User");

        // Then
        assertNotNull(createdUser);
        assertEquals("Test User", createdUser.getName());
        verify(userRepository, times(1)).findByChatID(12345L);
        verify(userRepository, times(1)).save(any(User.class));
    }

    @Test
    void createOrUpdateUser_WhenExistingUser_UpdatesName() {
        // Given
        User existingUser = new User(12345L, "Old Name");
        when(userRepository.findByChatID(12345L)).thenReturn(Optional.of(existingUser));
        when(userRepository.save(any(User.class))).thenReturn(existingUser);

        // When
        User updatedUser = userService.createOrUpdateUser(12345L, "New Name");

        // Then
        assertNotNull(updatedUser);
        assertEquals("New Name", updatedUser.getName());
        verify(userRepository, times(1)).findByChatID(12345L);
        verify(userRepository, times(1)).save(existingUser);
    }

    @Test
    void existsByChatId_WhenUserExists_ReturnsTrue() {
        // Given
        when(userRepository.existsByChatID(12345L)).thenReturn(true);

        // When
        boolean exists = userService.existsByChatId(12345L);

        // Then
        assertTrue(exists);
        verify(userRepository, times(1)).existsByChatID(12345L);
    }

    @Test
    void existsByChatId_WhenUserNotExists_ReturnsFalse() {
        // Given
        when(userRepository.existsByChatID(99999L)).thenReturn(false);

        // When
        boolean exists = userService.existsByChatId(99999L);

        // Then
        assertFalse(exists);
        verify(userRepository, times(1)).existsByChatID(99999L);
    }
}
