package com.example.teamcity.api.constants;

import java.util.List;

/**
 * Константы для тестов: специальные символы, локализация, payload'ы для тестирования безопасности.
 */
public final class TestConstants {
    private TestConstants() {}

    public static final String SPECIAL_CHARACTERS = "!@#$%^&*()_+-={}[]:\\";
    public static final String LOCALIZATION_CHARACTERS = "äöüßéèñçøπдёж漢字日本語한글";

    public static final String ROOT_PROJECT_ID = "_Root";

    public static final List<String> LOCALIZATION_STRINGS = List.of(
            "München", "北京", "Москва", "Español", "São Paulo", "東京", "فارسی", "नमस्ते", "한국어", "العربية"
    );

    // Тестовые payload'ы для XSS и SQL-инъекций
    public static final String XSS_PAYLOAD = "<script>alert('XSSd')</script>";
    public static final String SQL_INJECTION_PAYLOAD = "'; DROP TABLE projects; --";
}
