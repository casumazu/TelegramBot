package com.example.SpringBot.service;

import com.example.SpringBot.model.User;
import com.example.SpringBot.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

@Slf4j
@Service
@RequiredArgsConstructor
public class UserService {

    private final UserRepository userRepository;

    @Transactional(readOnly = true)
    public Optional<User> findByChatId(Long chatId) {
        return userRepository.findByChatID(chatId);
    }

    @Transactional
    public User createOrUpdateUser(Long chatId, String name) {
        return userRepository.findByChatID(chatId)
                .map(existingUser -> {
                    existingUser.setName(name);
                    log.debug("Обновлен пользователь с chatId: {}", chatId);
                    return userRepository.save(existingUser);
                })
                .orElseGet(() -> {
                    User newUser = new User(chatId, name);
                    log.info("Создан новый пользователь с chatId: {}", chatId);
                    return userRepository.save(newUser);
                });
    }

    @Transactional(readOnly = true)
    public boolean existsByChatId(Long chatId) {
        return userRepository.existsByChatID(chatId);
    }
}
