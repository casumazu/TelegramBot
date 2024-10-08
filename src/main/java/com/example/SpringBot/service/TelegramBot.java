package com.example.SpringBot.service;

import com.example.SpringBot.config.BotConfig;

import com.example.SpringBot.model.Comments;
import com.example.SpringBot.model.User;
import com.example.SpringBot.repository.CommentsRepository;
import com.example.SpringBot.repository.UserRepository;
import lombok.extern.slf4j.Slf4j;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.bots.TelegramLongPollingBot;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.Message;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.ReplyKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.KeyboardRow;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Slf4j
@Component
public class TelegramBot extends TelegramLongPollingBot {

    final BotConfig config;
    @Autowired
    private UserRepository userRepository;
    @Autowired
    private CommentsRepository commentsRepository;

    private final Map<Long, String> feedbackMap = new HashMap<>();

    public TelegramBot(BotConfig config) {
        this.config = config;
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
            User user = new User(chatID, update.getMessage().getChat().getFirstName());

            switch (messageText) {
                case "/start" -> sendMsg(message, chatID, user.getName());
                case "Напитки \u2615" -> sendMessages(chatID, drinksCoffee());
                case "Добавить отзыв \uD83D\uDE0A" -> {
                    sendMessages(chatID, "Напишите отзыв.");
                    feedbackMap.put(chatID, "awaiting_feedback");
                }
                case "Посмотреть отзывы" -> {
                    List<Comments> feedbacks = commentsRepository.findAll();
                    if (!feedbacks.isEmpty()) {
                        StringBuilder response = new StringBuilder("Отзывы:\n");
                        for (Comments feedback : feedbacks) {
                            response.append("- ").append(feedback.getMessage()).append("\n");
                        }
                        sendMessages(chatID, response.toString());
                    } else {
                        sendMessages(chatID, "Нет доступных отзывов.");
                    }
                }
                default -> {
                    if ((feedbackMap.containsKey(chatID))) { // для feedback
                        handleUserInput(user, chatID, messageText);
                    } else {
                        sendMessages(chatID, "Команда не поддерживается");
                    }
                }
            }
        }
    }

    private void handleUserInput(User user, long chatID, String messageText) {
        String feedbackState = feedbackMap.get(chatID);

        if (feedbackState != null && feedbackState.equals("awaiting_feedback")) {
            // Обрабатываем полученный отзыв
            if (messageText.length() > 2) {
                commentsRepository.save(new Comments(user, messageText));
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


    public void sendMsg (Message message, long chatID, String name) {
        SendMessage sendMessage = new SendMessage();
        sendMessage.enableMarkdown(true);
        userRepository.save(new User(chatID, name));
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
      //  createUserIfNotExists(chatID, name);
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
