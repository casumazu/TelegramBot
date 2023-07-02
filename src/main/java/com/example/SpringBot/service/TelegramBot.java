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
import org.telegram.telegrambots.meta.api.objects.Message;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.ReplyKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.KeyboardRow;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
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
        Message message = update.getMessage();
        if (update.hasMessage() && update.getMessage().hasText()) {
            String messageText = update.getMessage().getText();
            long chatID = update.getMessage().getChatId();

            switch (messageText) {
                case "/start" -> sendMsg(message, chatID, update.getMessage().getChat().getFirstName());
                case "Напитки \u2615" -> sendMessages(chatID, drinksCoffee());
                case "Добавить отзыв \uD83D\uDE0A" -> {
                    sendMessages(chatID, "Напишите отзыв.");
                    feedbackMap.put(chatID, "awaiting_feedback");
                }
                case "Посмотреть отзывы" -> {
                    List<String> feedbacks = getFeedbacks();
                    if (!feedbacks.isEmpty()) {
                        StringBuilder response = new StringBuilder("Отзывы:\n");
                        for (String feedback : feedbacks) {
                            response.append("- ").append(feedback).append("\n");
                        }
                        sendMessages(chatID, response.toString());
                    } else {
                        sendMessages(chatID, "Нет доступных отзывов.");
                    }
                }
                default -> {
                    if ((feedbackMap.containsKey(chatID))) { // для feedback
                        handleUserInput(chatID, messageText);
                    } else {
                        sendMessages(chatID, "Команда не поддерживается");
                    }
                }
            }
        }
    }

    private void handleUserInput(long chatID, String messageText) {
        String feedbackState = feedbackMap.get(chatID);

        if (feedbackState != null && feedbackState.equals("awaiting_feedback")) {
            // Обрабатываем полученный отзыв
            if (messageText.length() > 2) {
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


    public void sendMsg (Message message, long chatID, String name) {
        SendMessage sendMessage = new SendMessage();
        sendMessage.enableMarkdown(true);

        ReplyKeyboardMarkup replyKeyboardMarkup = new
                ReplyKeyboardMarkup();
        sendMessage.setReplyMarkup(replyKeyboardMarkup);
        replyKeyboardMarkup.setSelective(true);
        replyKeyboardMarkup.setResizeKeyboard(true);
        replyKeyboardMarkup.setOneTimeKeyboard(false);

        // список строк клавиатуры
        List<KeyboardRow> keyboard = new ArrayList<>();

        // Первая строчка клавиатуры
        KeyboardRow keyboardFirstRow = new KeyboardRow();
        keyboardFirstRow.add("Напитки \u2615");
        keyboardFirstRow.add("Добавить отзыв \uD83D\uDE0A");

        // Вторая строчка клавиатуры
        KeyboardRow keyboardSecondRow = new KeyboardRow();
        keyboardSecondRow.add("Посмотреть отзывы");


        keyboard.add(keyboardFirstRow);
        keyboard.add(keyboardSecondRow);

        replyKeyboardMarkup.setKeyboard(keyboard);

        sendMessage.setChatId(message.getChatId().toString());
        sendMessage.setReplyToMessageId(message.getMessageId());
        sendMessage.setText("Приветствую, " + name + " !");
        createUserIfNotExists(chatID, name);
        try {
            execute(sendMessage);
        } catch (TelegramApiException e) {
            e.printStackTrace();
        }
    }

    private String drinksCoffee() {
        return
                """
                        Доступные напитки:\s
                        Большие
                        Капучино 350мл - 130 руб\s
                        Латте 350мл - 130 руб
                        Моккачино 350мл - 130 руб

                        Маленькие
                        Раф Банановый 200 мл - 100 руб
                        Моккачино 200 мл - 100 руб
                        Латте 200 мл - 100 руб
                        Капучино 200 мл - 100 руб
                        Горячий шоколад 200 мл - 100 руб
                        Молочный шоколад 200 мл - 100 руб
                        Американо 200 мл - 100 руб
                        """;
    }

    private List<String> getFeedbacks() {
        String sqlQuery = "SELECT users.name, feedback.comment FROM feedback " +
                "LEFT JOIN users ON users.id = feedback.user_id";
        return jdbcTemplate.query(sqlQuery, (resultSet, rowNum) ->
                resultSet.getString("name") + ": " + resultSet.getString("comment"));
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
