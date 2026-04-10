package com.example.SpringBot.service;

import com.example.SpringBot.config.BotConfig;
import com.example.SpringBot.model.Comments;
import com.example.SpringBot.model.Drink;
import com.example.SpringBot.model.User;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.bots.TelegramLongPollingBot;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.InlineKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.ReplyKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.InlineKeyboardButton;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.KeyboardRow;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

@Slf4j
@Component
public class TelegramBot extends TelegramLongPollingBot {

    private final BotConfig config;
    private final UserService userService;
    private final CommentsService commentsService;

    // Хранение состояния пользователей (для сбора отзывов)
    private final Map<Long, UserState> userStates = new ConcurrentHashMap<>();

    public TelegramBot(BotConfig config, UserService userService, CommentsService commentsService) {
        this.config = config;
        this.userService = userService;
        this.commentsService = commentsService;
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
        if (update.hasCallbackQuery()) {
            handleCallbackQuery(update);
            return;
        }

        if (!update.hasMessage() || !update.getMessage().hasText()) {
            return;
        }

        try {
            String messageText = update.getMessage().getText();
            long chatId = update.getMessage().getChatId();
            String firstName = update.getMessage().getChat().getFirstName();

            // Регистрируем или обновляем пользователя
            User user = userService.createOrUpdateUser(chatId, firstName);

            // Обрабатываем команды
            if (userStates.containsKey(chatId)) {
                handleStateMessage(chatId, user, messageText);
            } else {
                handleCommand(chatId, user, messageText);
            }
        } catch (Exception e) {
            log.error("Ошибка при обработке сообщения от пользователя", e);
            sendTextMessage(update.getMessage().getChatId(),
                    "Произошла ошибка. Пожалуйста, попробуйте еще раз.");
        }
    }

    private void handleCallbackQuery(Update update) {
        try {
            String callbackData = update.getCallbackQuery().getData();
            long chatId = update.getCallbackQuery().getMessage().getChatId();

            if (callbackData.startsWith("drink:")) {
                String drinkName = callbackData.substring("drink:".length());
                showDrinkDetails(chatId, drinkName);
            } else if (callbackData.startsWith("drinks_category:")) {
                String category = callbackData.substring("drinks_category:".length());
                sendDrinksByCategory(chatId, category);
            } else if ("drinks_back".equals(callbackData)) {
                sendDrinksMenu(chatId);
            }
        } catch (Exception e) {
            log.error("Ошибка при обработке callback query: {}", e.getMessage(), e);
        }
    }

    private void handleCommand(long chatId, User user, String command) {
        switch (command) {
            case "/start" -> sendStartMessage(chatId, user.getName());
            case "Напитки ☕" -> sendDrinksMenu(chatId);
            case "Добавить отзыв 😊" -> startFeedbackFlow(chatId);
            case "Посмотреть отзывы" -> showComments(chatId);
            default -> sendTextMessage(chatId, "Неизвестная команда. Используйте меню ниже.");
        }
    }

    private void handleStateMessage(long chatId, User user, String messageText) {
        UserState state = userStates.get(chatId);
        
        if (state == UserState.AWAITING_FEEDBACK) {
            processFeedback(chatId, user, messageText);
        } else {
            log.warn("Неизвестное состояние пользователя {}: {}", chatId, state);
            userStates.remove(chatId);
            sendTextMessage(chatId, "Произошла ошибка. Пожалуйста, начните сначала.");
        }
    }

    private void startFeedbackFlow(long chatId) {
        userStates.put(chatId, UserState.AWAITING_FEEDBACK);
        sendTextMessage(chatId, "Напишите ваш отзыв о нашей кофейне:");
    }

    private void processFeedback(long chatId, User user, String messageText) {
        try {
            commentsService.addComment(user, messageText);
            sendTextMessage(chatId, "Спасибо за ваш отзыв! 😊");
            log.info("Пользователь {} (chatId: {}) добавил отзыв", user.getName(), chatId);
        } catch (IllegalArgumentException e) {
            sendTextMessage(chatId, e.getMessage());
            return;
        } finally {
            userStates.remove(chatId);
        }
    }

    private void sendStartMessage(long chatId, String userName) {
        SendMessage message = new SendMessage();
        message.setChatId(String.valueOf(chatId));
        message.setText(String.format("Привет, %s! ☕\nДобро пожаловать в бот кофейни самообслуживания!", userName));
        message.setReplyMarkup(createKeyboard());

        executeMessage(message);
    }

    private ReplyKeyboardMarkup createKeyboard() {
        ReplyKeyboardMarkup keyboardMarkup = new ReplyKeyboardMarkup();
        keyboardMarkup.setSelective(true);
        keyboardMarkup.setResizeKeyboard(true);
        keyboardMarkup.setOneTimeKeyboard(false);

        List<KeyboardRow> keyboard = new ArrayList<>();

        KeyboardRow row1 = new KeyboardRow();
        row1.add("Напитки ☕");
        row1.add("Добавить отзыв 😊");

        KeyboardRow row2 = new KeyboardRow();
        row2.add("Посмотреть отзывы");

        keyboard.add(row1);
        keyboard.add(row2);

        keyboardMarkup.setKeyboard(keyboard);
        return keyboardMarkup;
    }

    private void showComments(long chatId) {
        List<Comments> comments = commentsService.getAllComments();
        
        if (comments.isEmpty()) {
            sendTextMessage(chatId, "Отзывов пока нет. Будьте первым! 😊");
            return;
        }

        StringBuilder response = new StringBuilder("📝 *Отзывы наших гостей:*\n\n");
        for (int i = 0; i < Math.min(comments.size(), 10); i++) {
            Comments comment = comments.get(i);
            response.append("• ").append(comment.getMessage()).append("\n\n");
        }
        
        if (comments.size() > 10) {
            response.append("\n_... и еще ").append(comments.size() - 10).append(" отзывов_");
        }

        sendTextMessage(chatId, response.toString());
    }

    private void sendDrinksMenu(long chatId) {
        SendMessage message = new SendMessage();
        message.setChatId(String.valueOf(chatId));
        message.setText("☕ *Меню напитков*\n\nВыберите категорию:");
        message.enableMarkdown(true);
        message.setReplyMarkup(createDrinksCategoryKeyboard());
        executeMessage(message);
    }

    private InlineKeyboardMarkup createDrinksCategoryKeyboard() {
        InlineKeyboardMarkup markup = new InlineKeyboardMarkup();
        List<List<InlineKeyboardButton>> keyboard = new ArrayList<>();

        // Кнопки категорий
        List<InlineKeyboardButton> row1 = new ArrayList<>();
        InlineKeyboardButton largeBtn = new InlineKeyboardButton();
        largeBtn.setText("☕ Большие (350 мл)");
        largeBtn.setCallbackData("drinks_category:LARGE");
        row1.add(largeBtn);

        List<InlineKeyboardButton> row2 = new ArrayList<>();
        InlineKeyboardButton smallBtn = new InlineKeyboardButton();
        smallBtn.setText("🥤 Маленькие (200 мл)");
        smallBtn.setCallbackData("drinks_category:SMALL");
        row2.add(smallBtn);

        keyboard.add(row1);
        keyboard.add(row2);
        markup.setKeyboard(keyboard);
        return markup;
    }

    private void sendDrinksByCategory(long chatId, String category) {
        Drink.DrinkSize size = Drink.DrinkSize.valueOf(category);
        List<Drink> drinks = Arrays.stream(Drink.values())
                .filter(d -> d.getSize() == size)
                .collect(Collectors.toList());

        SendMessage message = new SendMessage();
        message.setChatId(String.valueOf(chatId));
        message.setText(String.format("%s напитки (%d мл):", size.getLabel(), size.getVolume()));
        message.enableMarkdown(true);
        message.setReplyMarkup(createDrinksKeyboard(drinks));
        executeMessage(message);
    }

    private InlineKeyboardMarkup createDrinksKeyboard(List<Drink> drinks) {
        InlineKeyboardMarkup markup = new InlineKeyboardMarkup();
        List<List<InlineKeyboardButton>> keyboard = new ArrayList<>();

        for (Drink drink : drinks) {
            List<InlineKeyboardButton> row = new ArrayList<>();
            InlineKeyboardButton button = new InlineKeyboardButton();
            button.setText(String.format("%s — %s", drink.getDisplayName(), drink.getFormattedPrice()));
            button.setCallbackData("drink:" + drink.name());
            row.add(button);
            keyboard.add(row);
        }

        // Кнопка "Назад"
        List<InlineKeyboardButton> backRow = new ArrayList<>();
        InlineKeyboardButton backBtn = new InlineKeyboardButton();
        backBtn.setText("◀ Назад к категориям");
        backBtn.setCallbackData("drinks_back");
        backRow.add(backBtn);
        keyboard.add(backRow);

        markup.setKeyboard(keyboard);
        return markup;
    }

    private void showDrinkDetails(long chatId, String drinkName) {
        try {
            Drink drink = Drink.valueOf(drinkName);

            String details = String.format(
                    "☕ *%s*\n\n" +
                    "📏 Объём: %d мл\n" +
                    "💰 Цена: %s\n\n" +
                    "📝 _%s_",
                    drink.getDisplayName(),
                    drink.getSize().getVolume(),
                    drink.getFormattedPrice(),
                    drink.getDescription()
            );

            // Клавиатура с кнопкой "Назад"
            InlineKeyboardMarkup markup = new InlineKeyboardMarkup();
            List<List<InlineKeyboardButton>> keyboard = new ArrayList<>();
            List<InlineKeyboardButton> backRow = new ArrayList<>();
            InlineKeyboardButton backBtn = new InlineKeyboardButton();
            backBtn.setText("◀ Назад к напиткам");
            backBtn.setCallbackData("drinks_category:" + drink.getSize().name());
            backRow.add(backBtn);
            keyboard.add(backRow);
            markup.setKeyboard(keyboard);

            SendMessage message = new SendMessage();
            message.setChatId(String.valueOf(chatId));
            message.setText(details);
            message.enableMarkdown(true);
            message.setReplyMarkup(markup);
            executeMessage(message);
        } catch (IllegalArgumentException e) {
            sendTextMessage(chatId, "Напиток не найден.");
        }
    }

    private void sendTextMessage(long chatId, String text) {
        SendMessage message = new SendMessage();
        message.setChatId(String.valueOf(chatId));
        message.setText(text);
        message.enableMarkdown(true);
        executeMessage(message);
    }

    private void executeMessage(SendMessage message) {
        try {
            execute(message);
        } catch (TelegramApiException e) {
            log.error("Ошибка при отправке сообщения: {}", e.getMessage(), e);
        }
    }

    /**
     * Состояния пользователя для многошаговых диалогов
     */
    public enum UserState {
        AWAITING_FEEDBACK
    }
}
