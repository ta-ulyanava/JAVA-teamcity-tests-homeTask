package com.example.teamcity.api.validation;

import java.util.List;
import java.util.function.Function;

public class EntityFinder {

    public static <T> T findById(List<T> entities, String id, Function<T, String> idExtractor) {
        return entities.stream()
                .filter(e -> idExtractor.apply(e).equals(id))
                .findFirst()
                .orElse(null);
    }

    public static <T> T findByName(List<T> entities, String name, Function<T, String> nameExtractor) {
        return entities.stream()
                .filter(e -> nameExtractor.apply(e).equals(name))
                .findFirst()
                .orElse(null);
    }
}
