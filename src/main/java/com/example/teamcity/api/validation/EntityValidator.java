package com.example.teamcity.api.validation;

import org.testng.asserts.SoftAssert;
import java.lang.reflect.Field;
import java.util.List;

public class EntityValidator {

    public static <T> void validateAllEntityFields(T expected, T actual, SoftAssert softAssert) {
        validateAllEntityFieldsIgnoring(expected, actual, List.of(), softAssert);
    }

    public static <T> void validateAllEntityFieldsIgnoring(T expected, T actual, List<String> ignoredFields, SoftAssert softAssert) {
        for (Field field : expected.getClass().getDeclaredFields()) {
            field.setAccessible(true);
            if (ignoredFields.contains(field.getName())) continue;
            try {
                Object expectedValue = field.get(expected);
                Object actualValue = field.get(actual);
                softAssert.assertEquals(actualValue, expectedValue, "Поле " + field.getName() + " не совпадает");
            } catch (IllegalAccessException e) {
                throw new RuntimeException("Ошибка сравнения поля: " + field.getName(), e);
            }
        }
    }
}
