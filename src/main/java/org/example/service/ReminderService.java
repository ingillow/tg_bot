package org.example.service;

import org.example.domain.DBRestaurantRepository;
import org.example.domain.UserRepository;

import java.util.List;

public class ReminderService {
    private final UserRepository userRepository;
    private final DBRestaurantRepository restaurantRepository;
    private final MessageSender messageSender;

    public ReminderService(UserRepository userRepository,
                           DBRestaurantRepository restaurantRepository,
                           MessageSender messageSender) {
        this.userRepository = userRepository;
        this.restaurantRepository = restaurantRepository;
        this.messageSender = messageSender;
    }

    public void sendRemindersToAllUsers() {
        List<Long> chatIds = userRepository.findAllChatIds();
        System.out.println("Найдено пользователей для напоминаний: " + chatIds.size());

        if (chatIds.isEmpty()) {
            System.out.println("Нет активных пользователей для напоминаний!");
        }

        chatIds.forEach(chatId -> {
            try {
                String phone = userRepository.findPhoneByChatId(chatId);
                System.out.println("Отправка напоминания для: " + chatId + " (телефон: " + phone + ")");

                List<String> restaurants = restaurantRepository.getRestaurantsByUserId(chatId);
                String message = buildReminderMessage(restaurants);
                messageSender.sendMessage(chatId, message);

                System.out.println("Напоминание отправлено для: " + chatId);
            } catch (Exception e) {
                System.err.println("Ошибка для " + chatId + ": " + e.getMessage());
            }
        });
    }

    private String buildReminderMessage(List<String> restaurants) {
        if (restaurants.isEmpty()) {
            return "⏰ Напоминание: сегодня последний день сдачи табеля!";
        }

        StringBuilder sb = new StringBuilder();
        sb.append("⏰ Напоминание: сегодня последний день сдачи табеля!\n\n");
        sb.append("🍽 Ваши рестораны:\n");

        for (int i = 0; i < restaurants.size(); i++) {
            sb.append(i + 1).append(". ").append(restaurants.get(i)).append("\n");
        }

        sb.append("\nНе забудьте сдать табель по всем точкам!");
        return sb.toString();
    }
}