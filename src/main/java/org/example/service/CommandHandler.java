package org.example.service;

import org.example.domain.DBRestaurantRepository;
import org.example.domain.UserRepository;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;

import java.util.List;

public class CommandHandler {
    private final UserRepository userRepository;
    private final DBRestaurantRepository restaurantRepository;
    private final MessageSender messageSender;

    public CommandHandler(UserRepository userRepository,
                          MessageSender messageSender,
                          DBRestaurantRepository restaurantRepository) {
        this.userRepository = userRepository;
        this.messageSender = messageSender;
        this.restaurantRepository = restaurantRepository;
    }



    public void handle(Update update) {
        try {
            if (!update.hasMessage() || !update.getMessage().hasText()) return;

            long chatId = update.getMessage().getChatId();
            String text = update.getMessage().getText();

            switch (text) {
                case "/start":
                    handleStart(chatId, update.getMessage().getChat().getFirstName());
                    break;
                case "/remind":
                    handleRemind(chatId);
                    break;
                case "/myrestaurants":
                    handleShowRestaurants(chatId);
                    break;
                case "/help":
                    handleHelp(chatId);
                    break;
                default:
                    if (text.matches("^\\+7\\d{10}$")) {
                        processPhoneNumber(chatId, text);
                    } else {
                        messageSender.sendMessage(chatId,
                                "❌ Неверный формат номера. Пример: +79123456789");
                    }
            }
        } catch (TelegramApiException e) {
            System.err.println("Ошибка обработки команды: " + e.getMessage());
        }

    }

    private void processPhoneNumber(Long chatId, String phoneNumber) throws TelegramApiException {
        userRepository.saveUser(chatId, phoneNumber);
        messageSender.sendMessage(chatId,
                "✅ Номер сохранен! Теперь вы будете получать напоминания.");
    }

    private void handleStart(Long chatId, String name) throws TelegramApiException {
        String answer = String.format(
                "Привет, %s! Я бот для напоминаний о сдаче табелей.%n%n" +
                        "Пожалуйста, отправьте ваш номер телефона в формате +7XXXXXXXXXX для регистрации.",
                name
        );
        messageSender.sendMessage(chatId, answer);
    }

    private void handleRemind(Long chatId) throws TelegramApiException {
        if (!isUserRegistered(chatId)) {
            messageSender.sendMessage(chatId, "Вы не зарегистрированы. Введите /start для начала работы.");
            return;
        }

        List<String> restaurants = restaurantRepository.getRestaurantsByUserId(chatId);
        String message;

        if (restaurants.isEmpty()) {
            message = "⏰ Напоминание: сегодня последний день сдачи табеля!";
        } else {
            message = buildRestaurantsMessage(restaurants);
        }

        messageSender.sendMessage(chatId, message);
    }


    private void handleHelp(Long chatId) throws TelegramApiException {
        String helpText = """
            Доступные команды:
            /start - начать работу с ботом
            /remind - получить напоминание о табеле
            /help - показать это сообщение
            /myrestaurants - показать список ресторанов""";
        messageSender.sendMessage(chatId, helpText);
    }

    private void handleDefault(Long chatId, String text) throws TelegramApiException {
        if (text.matches("^\\+7\\d{10}$")) {
            handlePhoneNumber(chatId, text);
        } else {
            messageSender.sendMessage(chatId, "Неизвестная команда. Введите /help для списка команд.");
        }
    }

    private void handlePhoneNumber(Long chatId, String phoneNumber) throws TelegramApiException {
        userRepository.saveUser(chatId, phoneNumber);
        messageSender.sendMessage(chatId, String.format(
                "✅ Спасибо! Ваш номер %s сохранен.%nТеперь вы будете получать напоминания о сдаче табелей.",
                phoneNumber
        ));
    }

    private boolean isUserRegistered(Long chatId) {
        return userRepository.findPhoneByChatId(chatId) != null;
    }

    private void handleShowRestaurants(Long chatId) throws TelegramApiException {
        if (!isUserRegistered(chatId)) {
            messageSender.sendMessage(chatId, "Начните чат с ботом используя /start");
            return;
        }

        List<String> restaurants = restaurantRepository.getRestaurantsByUserId(chatId);
        String message = buildRestaurantsMessage(restaurants);
        messageSender.sendMessage(chatId, message);
    }

    private String buildRestaurantsMessage(List<String> restaurants) {
        StringBuilder sb = new StringBuilder();
        sb.append("🍽 Список ваших рестораны:\n");

        for (int i = 0; i < restaurants.size(); i++) {
            sb.append(i + 1).append(". ").append(restaurants.get(i)).append("\n");
        }

        return sb.toString();
    }
}

