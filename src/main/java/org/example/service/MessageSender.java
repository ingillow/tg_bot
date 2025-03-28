package org.example.service;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;

public interface MessageSender {
    void sendMessage(Long chatId, String text) throws TelegramApiException;
}