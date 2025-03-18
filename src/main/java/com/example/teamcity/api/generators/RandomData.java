package com.example.teamcity.api.generators;

import org.apache.commons.lang3.RandomStringUtils;
import java.util.Random;

/**
 * Генерация случайных данных
 */
public final class RandomData {
    private static final String TEST_PREFIX = "test_";
    private static final int MAX_LENGTH = 10;
    private static final Random RANDOM = new Random();

    private RandomData() {}

    // Генерация случайной строки с префиксом
    public static String getString() {
        return TEST_PREFIX + RandomStringUtils.randomAlphanumeric(MAX_LENGTH);
    }

    public static String getString(int length) {
        return TEST_PREFIX + RandomStringUtils.randomAlphanumeric(Math.max(length - TEST_PREFIX.length(), 1));
    }

    // Генерация уникального имени
    public static String getUniqueName() {
        return TEST_PREFIX + System.currentTimeMillis() + RandomStringUtils.randomAlphanumeric(MAX_LENGTH);
    }

    // Генерация уникального ID
    public static String getUniqueId() {
        return TEST_PREFIX + System.currentTimeMillis() + RandomStringUtils.randomAlphanumeric(MAX_LENGTH);
    }
    public static String getDigits(int length) {
        return RandomStringUtils.randomNumeric(length);
    }

}
