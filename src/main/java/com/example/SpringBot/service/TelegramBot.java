package com.example.SpringBot.service;

import com.example.SpringBot.config.BotConfig;
import com.example.SpringBot.exception.UserNotFoundException;
import com.example.SpringBot.model.User;
import lombok.extern.slf4j.Slf4j;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.support.rowset.SqlRowSet;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.bots.TelegramLongPollingBot;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.Map;

@Slf4j

@Component
public class TelegramBot extends TelegramLongPollingBot {

    final BotConfig config;
    final JdbcTemplate jdbcTemplate;

    private final Map<Long, String> feedbackMap = new HashMap<>();

    public TelegramBot(BotConfig config, JdbcTemplate jdbcTemplate) {
        this.config = config;
        this.jdbcTemplate = jdbcTemplate;
    }

    @Override
    public String getBotUsername() {
        return config.getBotName();
    }

    @Override
    public String getBotToken() {
        return config.getToken();
    }

    @Override
    public void onUpdateReceived(Update update) {
        if (update.hasMessage() && update.getMessage().hasText()) {
            String messageText = update.getMessage().getText();
            long chatID = update.getMessage().getChatId();

            switch (messageText) {
                case "/start":
                    startCommandReceived(chatID, update.getMessage().getChat().getFirstName());
                    break;

                case "/drinks":
                    drinksCoffee(chatID);
                    break;
                case "/feedback":
                    sendMessages(chatID, "Напишите отзыв.");
                    feedbackMap.put(chatID, "awaiting_feedback");
                    break;
                default:
                    if ((feedbackMap.containsKey(chatID))) {
                        handleUserInput(chatID, messageText);
                    } else {
                        sendMessages(chatID, "Команда не поддерживается");
                    }

            }
        }
    }

    private void handleUserInput(long chatID, String messageText) {
        String feedbackState = feedbackMap.get(chatID);

        if (feedbackState != null && feedbackState.equals("awaiting_feedback")) {
            // Обрабатываем полученный отзыв
            if(messageText.length() > 2) {
                insertFeedback(chatID, messageText);
                sendMessages(chatID, "Отзыв добавлен");
            } else {
                sendMessages(chatID, "Отзыв слишком короткий");
            }
            // Удаляем состояние ожидания отзыва
            feedbackMap.remove(chatID);
        } else {
            sendMessages(chatID, "Команда не поддерживается");
        }
    }

    private void insertFeedback(long chatID, String feedback) {
        log.info("Получен запрос на создание отзыва от пользователя({})", chatID);
        String sqlQuery = "INSERT INTO feedback (user_id, comment) VALUES (?, ?)";
        jdbcTemplate.update(sqlQuery, getUserByChatID(chatID).getId(), feedback);
        log.info("Пользователь с идентификатором {} оставил отзыв", chatID);
    }

    private User getUserByChatID(long chatID) {
        String sqlQuery = "SELECT id, chatID, name FROM users WHERE chatID = ?";
        SqlRowSet userRows = jdbcTemplate.queryForRowSet(sqlQuery, chatID);
        if (userRows.next()) {
            return jdbcTemplate.queryForObject(sqlQuery, this::mapRowToUser, chatID);
        } else {
            log.info("User с идентификатором {} не найден.", chatID);
            throw new UserNotFoundException("Пользователь не найден");
        }
    }

    private void startCommandReceived(long chatID, String name) {
        String answer = "Приветствую, " + name + " ! \n" +
                "\n" +
                "Доступные команды:" +
                " \n    /start" +
                " \n    /drinks - напитки" +
                " \n    /feedback - добавить отзыв" +
                " \n " +
                " \n В разработке: " +
                " \n *   Акции" +
                " \n *   Отзывы";
        sendMessages(chatID, answer);
        createUserIfNotExists(chatID, name);
    }

    private void createUserIfNotExists(long UserID, String name) {
        log.info("Получен запрос на получение пользователя({})", UserID);
        String sqlQuery = "select chatID, name FROM users WHERE chatID = ?";
        SqlRowSet userRows = jdbcTemplate.queryForRowSet(sqlQuery, UserID);
        if (!userRows.next()) {
            jdbcTemplate.update("insert into users (name, chatID)" + "values (?, ?)", name, UserID);
            log.info("Пользователь {} с идентификатором {} добавлен в БД", name, UserID);
        }
    }

    private User mapRowToUser(ResultSet rs, int rowNum) throws SQLException {
        return new User(
                rs.getLong("id"),
                rs.getLong("chatID"),
                rs.getString("name")
        );
    }

    private void drinksCoffee(long chatId) {
        String answer =
                "Доступные напитки: \n" +
                        "Большие\n" +
                        "Капучино 350мл - 130 руб \n" +
                        "Латте 350мл - 130 руб\n" +
                        "Моккачино 350мл - 130 руб\n" +
                        "\n" +
                        "Маленькие\n" +
                        "Раф Банановый 200 мл - 100 руб\n" +
                        "Моккачино 200 мл - 100 руб\n" +
                        "Латте 200 мл - 100 руб\n" +
                        "Капучино 200 мл - 100 руб\n" +
                        "Горячий шоколад 200 мл - 100 руб\n" +
                        "Молочный шоколад 200 мл - 100 руб\n" +
                        "Американо 200 мл - 100 руб\n";
        sendMessages(chatId, answer);
    }

    private void sendMessages(long chatID, String sendText) {
        SendMessage message = new SendMessage();
        message.setChatId(String.valueOf(chatID));
        message.setText(sendText);

        try {
            execute(message);
        } catch (TelegramApiException e) {
            throw new RuntimeException(e);
        }
    }
}
