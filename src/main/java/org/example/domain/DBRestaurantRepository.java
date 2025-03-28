package org.example.domain;

import java.util.List;
import java.util.Map;

public interface DBRestaurantRepository {
    List<String> getRestaurantsByUserId(Long userId);
}

