package org.example;

import com.google.common.collect.Sets;
import org.example.domain.DBRestaurantRepository;
import org.example.domain.InMemoryRestaurantRepository;
import org.example.domain.MemoryUserRepository;
import org.example.domain.UserRepository;
import org.example.service.ReminderService;
import org.quartz.*;
import org.quartz.impl.StdSchedulerFactory;
import org.telegram.telegrambots.bots.DefaultBotOptions;
import org.telegram.telegrambots.meta.TelegramBotsApi;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;
import org.telegram.telegrambots.updatesreceivers.DefaultBotSession;

public class MainApp {
    public static void main(String[] args) {
        try {
            UserRepository userRepository = new MemoryUserRepository();
            DBRestaurantRepository restaurantRepo = new InMemoryRestaurantRepository();

            TimesheetReminderBot bot = new TimesheetReminderBot(
                    new DefaultBotOptions(),
                    userRepository,
                    restaurantRepo
            );

            ReminderService reminderService = new ReminderService(
                    userRepository,
                    restaurantRepo,
                    bot
            );


            TelegramBotsApi botsApi = new TelegramBotsApi(DefaultBotSession.class);
            botsApi.registerBot(bot);

            System.out.println("Бот успешно запущен!");
            startScheduler(reminderService);

        } catch (TelegramApiException | SchedulerException e) {
            e.printStackTrace();
        }
    }

    private static void startScheduler(ReminderService reminderService) throws SchedulerException {
        JobDetail job = JobBuilder.newJob(ReminderJob.class)
                .withIdentity("reminderJob", "reminderGroup")
                .usingJobData(new JobDataMap() {{
                    put("reminderService", reminderService);
                }})
                .build();


        Trigger trigger = TriggerBuilder.newTrigger()
                .withIdentity("reminderTrigger", "reminderGroup")
                .withSchedule(CronScheduleBuilder.dailyAtHourAndMinute(17, 0) // Начинаем в 17:00
                        .withMisfireHandlingInstructionFireAndProceed())
                .endAt(DateBuilder.todayAt(18, 0, 0)) // Заканчиваем в 18:00
                .build();


        Trigger intervalTrigger = TriggerBuilder.newTrigger()
                .withIdentity("intervalReminderTrigger", "reminderGroup")
                .startAt(DateBuilder.todayAt(17, 0, 0)) // Начинаем в 17:00
                .endAt(DateBuilder.todayAt(18, 0, 0))  // Заканчиваем в 18:00
                .withSchedule(SimpleScheduleBuilder.simpleSchedule()
                        .withIntervalInMinutes(10)      // Интервал 10 минут
                        .repeatForever())
                .build();

        Scheduler scheduler = StdSchedulerFactory.getDefaultScheduler();
        scheduler.start();

        // Планируем оба триггера
        scheduler.scheduleJob(job, Sets.newHashSet(trigger, intervalTrigger), true);
    }

    public static class ReminderJob implements Job {
        @Override
        public void execute(JobExecutionContext context) {
            ReminderService service = (ReminderService) context.getJobDetail()
                    .getJobDataMap().get("reminderService");
            service.sendRemindersToAllUsers();
        }
    }
}
