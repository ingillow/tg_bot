package org.example.domain;

import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class MemoryUserRepository implements UserRepository {
    private final Map<Long, String> storage = new ConcurrentHashMap<>();

    @Override
    public void saveUser(Long chatId, String phone) {
        storage.put(chatId, phone);
    }

    @Override
    public String findPhoneByChatId(Long chatId) {
        return storage.get(chatId);
    }

    @Override
    public List<Long> findAllChatIds() {
        return List.copyOf(storage.keySet());
    }
}
