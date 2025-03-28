package org.example.domain;

import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public interface UserRepository {
    void saveUser(Long chatId, String phone);
    String findPhoneByChatId(Long chatId);
    List<Long> findAllChatIds();
}

