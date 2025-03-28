package org.example.service;

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

public class BotConfig {
    private static final Properties props = new Properties();

    static {
        try (InputStream input = BotConfig.class.getClassLoader().getResourceAsStream("config.properties")) {
            if (input == null) {
                throw new RuntimeException("Не найден config.properties");
            }
            props.load(input);
        } catch (IOException ex) {
            throw new RuntimeException("Ошибка загрузки конфига", ex);
        }
    }

    public static String getBotToken() {
        return props.getProperty("bot.token");
    }

    public static String getBotUsername() {
        return props.getProperty("bot.username");
    }
}