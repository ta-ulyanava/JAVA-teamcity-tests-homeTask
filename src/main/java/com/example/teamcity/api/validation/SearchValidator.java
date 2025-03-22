package com.example.teamcity.api.validation;

import org.testng.asserts.SoftAssert;

import java.lang.reflect.Field;
import java.util.List;

public class SearchValidator {
    public static <T> void validateSearchResults(List<T> expectedList, List<T> actualList, String entityType, String identifierField, List<String> ignoredFields, SoftAssert softAssert) {
        softAssert.assertEquals(actualList.size(), expectedList.size(), "Number of found " + entityType + "s doesn't match expected");
        for (int i = 0; i < expectedList.size(); i++) {
            validateSearchResult(expectedList.get(i), actualList.get(i), entityType, identifierField, ignoredFields, softAssert);
        }
    }

    public static <T> void validateSearchResult(T expected, T actual, String entityType, String identifierField, List<String> ignoredFields, SoftAssert softAssert) {
        try {
            Field field = expected.getClass().getDeclaredField(identifierField);
            field.setAccessible(true);
            Object value = field.get(expected);

            softAssert.assertNotNull(actual, entityType + " with " + identifierField + " '" + value + "' was not found");
            EntityValidator.validateAllEntityFieldsIgnoring(expected, actual, ignoredFields, softAssert);
            softAssert.assertAll();

        } catch (NoSuchFieldException | IllegalAccessException e) {
            throw new RuntimeException("Failed to validate search result: " + e.getMessage(), e);
        }
    }
}
