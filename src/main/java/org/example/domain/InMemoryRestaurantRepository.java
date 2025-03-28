package org.example.domain;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

// Это хардкодный вариант интеграции БД с ботом
// глобально можно было бы из таблички забирать номер телефона ТУ + все его рестораны
public class InMemoryRestaurantRepository implements DBRestaurantRepository {

    private static final Map<Long, List<String>> RESTAURANTS = new ConcurrentHashMap<>();

    static {
        RESTAURANTS.put(5218620376L, Arrays.asList(
                "Белый Аист",
                "Европа ФК",
                "Аллея героев"
        ));
    }

    @Override
    public List<String> getRestaurantsByUserId(Long userId) {
        return RESTAURANTS.getOrDefault(userId, Collections.emptyList());
    }
}
