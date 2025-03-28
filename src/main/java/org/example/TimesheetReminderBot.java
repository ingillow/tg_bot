package org.example;

import org.example.domain.DBRestaurantRepository;
import org.example.domain.InMemoryRestaurantRepository;
import org.example.domain.MemoryUserRepository;
import org.example.domain.UserRepository;
import org.example.service.BotConfig;
import org.example.service.CommandHandler;
import org.example.service.MessageSender;
import org.example.service.ReminderService;
import org.telegram.telegrambots.bots.DefaultBotOptions;
import org.telegram.telegrambots.bots.TelegramLongPollingBot;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;

public class TimesheetReminderBot extends TelegramLongPollingBot implements MessageSender {
    private final CommandHandler commandHandler;
    private final ReminderService reminderService;

    public TimesheetReminderBot(DefaultBotOptions options,
                                UserRepository userRepository,
                                DBRestaurantRepository restaurantRepo) {
        super(options);
        this.reminderService = new ReminderService(userRepository, restaurantRepo, this);
        this.commandHandler = new CommandHandler(userRepository, this, restaurantRepo);
    }

    public TimesheetReminderBot() {
        this(new DefaultBotOptions(),
                new MemoryUserRepository(),
                new InMemoryRestaurantRepository());
    }

    @Override
    public String getBotUsername() {
        return BotConfig.getBotUsername();
    }

    @Override
    public String getBotToken() {
        return BotConfig.getBotToken();
    }

    @Override
    public void onUpdateReceived(Update update) {
        commandHandler.handle(update);
    }

    public ReminderService getReminderService() {
        return this.reminderService;
    }

    @Override
    public void sendMessage(Long chatId, String text) throws TelegramApiException {
        SendMessage sendMessage = new SendMessage();
        sendMessage.setChatId(chatId.toString());
        sendMessage.setText(text);
        execute(sendMessage);
    }
}