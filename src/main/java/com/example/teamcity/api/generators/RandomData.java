package com.example.teamcity.api.generators;

import org.apache.commons.lang3.RandomStringUtils;

import java.util.List;
import java.util.Random;

/**
 * Генерация случайных данных
 */
public final class RandomData {
    private static final String TEST_PREFIX = "test_";
    private static final int MAX_LENGTH = 10;
    private static final Random RANDOM = new Random();

    private static final String SPECIAL_CHARACTERS = "!@#$%^&*()_+-={}[]:\\";
    private static final String LOCALIZATION_CHARACTERS = "äöüßéèñçøπдёж漢字日本語한글";

    private static final List<String> LOCALIZATION_STRINGS = List.of(
            "München", "北京", "Москва", "Español", "São Paulo", "東京", "فارسی", "नमस्ते", "한국어", "العربية"
    );

    private RandomData() {
    }

    // Генерация случайной строки с префиксом
    public static String getString() {
        return TEST_PREFIX + RandomStringUtils.randomAlphanumeric(MAX_LENGTH);
    }

    // Генерация строки заданной длины
    public static String getString(int length) {
        return TEST_PREFIX + RandomStringUtils.randomAlphanumeric(Math.max(length - TEST_PREFIX.length(), 1));
    }

    // Генерация строки со специальными символами (возвращаем как было в старом классе)
    public static String getFullSpecialCharacterString() {
        return TEST_PREFIX + SPECIAL_CHARACTERS;
    }

    // Генерация строки с локализованными символами
    public static String getFullLocalizationString() {
        return TEST_PREFIX + LOCALIZATION_CHARACTERS;
    }

    // Генерация уникального имени с использованием времени
    public static String getUniqueName() {
        return TEST_PREFIX + System.currentTimeMillis() + RandomStringUtils.randomAlphanumeric(MAX_LENGTH);
    }

    // Генерация уникального ID с использованием времени
    public static String getUniqueId() {
        return TEST_PREFIX + System.currentTimeMillis() + RandomStringUtils.randomAlphanumeric(MAX_LENGTH);
    }
}
