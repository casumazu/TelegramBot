package com.example.SpringBot.service;

import com.example.SpringBot.config.BotConfig;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.bots.TelegramLongPollingBot;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;

@Component
public class TelegramBot extends TelegramLongPollingBot {

    final BotConfig config;

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
        if (update.hasMessage() && update.getMessage().hasText()) {
            String messageText = update.getMessage().getText();
            long chatID = update.getMessage().getChatId();

            switch (messageText) {
                case "/start":
                    startCommandReceived(chatID, update.getMessage().getChat().getFirstName());
                    break;

                default: sendMessages(chatID, "Команда не поддерживается");
            }
        }
    }

    private void startCommandReceived(long chatID, String name) {
        String answer = "Приветствую, " + name + " !";
        sendMessages(chatID, answer);
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
