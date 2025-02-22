package com.example.teamcity.api.generators;

import org.apache.commons.lang3.RandomStringUtils;

import java.util.List;
import java.util.Random;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

/**
 * Генерация случайных данных
 */
public final class RandomData {
    private static final String TEST_PREFIX = "test_";
    private static final int MAX_LENGTH = 10;
    private static final Random RANDOM = new Random();

    // 🔹 Строки для проверки спецсимволов и локализации
    private static final String SPECIAL_CHARACTERS = "!@#$%^&*()_+-={}[]:\\";
    private static final String LOCALIZATION_CHARACTERS = "äöüßéèñçøπдёж漢字日本語한글";

    private static final List<String> LOCALIZATION_STRINGS = List.of(
            "München", "北京", "Москва", "Español", "São Paulo", "東京", "فارسی", "नमस्ते", "한국어", "العربية"
    );

    private RandomData() {}

    // 🔹 Генерация случайной строки
    public static String getString() {
        return TEST_PREFIX + RandomStringUtils.randomAlphanumeric(MAX_LENGTH);
    }

    public static String getString(int length) {
        return TEST_PREFIX + RandomStringUtils.randomAlphanumeric(Math.max(length - TEST_PREFIX.length(), 1));
    }

    // 🔹 Возвращает полную строку спецсимволов
    public static String getFullSpecialCharacterString() {
        return TEST_PREFIX + SPECIAL_CHARACTERS;
    }

    // 🔹 Перебирает все спецсимволы по одному (для параметризованных тестов)
    public static List<String> getAllSpecialCharacters() {
        return SPECIAL_CHARACTERS.chars()
                .mapToObj(c -> String.valueOf((char) c))
                .collect(Collectors.toList());
    }

    // 🔹 Возвращает строку со всеми символами локализации
    public static String getFullLocalizationString() {
        return TEST_PREFIX + LOCALIZATION_CHARACTERS;
    }


    // 🔹 Перебирает все символы локализации по одному
    public static List<String> getAllLocalizationCharacters() {
        return LOCALIZATION_CHARACTERS.chars()
                .mapToObj(c -> String.valueOf((char) c))
                .collect(Collectors.toList());
    }

}
