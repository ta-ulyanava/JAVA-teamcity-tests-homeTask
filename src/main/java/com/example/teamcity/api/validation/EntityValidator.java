package com.example.teamcity.api.validation;

import io.qameta.allure.Step;
import org.testng.asserts.SoftAssert;

import java.lang.reflect.Field;
import java.util.List;

/**
 * Utility for validating that all fields of two entity instances match.
 * <p>
 * Supports soft assertion comparison, and allows skipping selected fields.
 */
public class EntityValidator {

    /**
     * Compares all fields between expected and actual objects of the same type.
     * Fails softly using {@link SoftAssert}.
     *
     * @param expected    the expected object
     * @param actual      the actual object returned from API
     * @param softAssert  the SoftAssert instance to collect failures
     * @param <T>         entity type
     */
    @Step("Validate all fields of entity {expected.getClass().getSimpleName()}")
    public static <T> void validateAllEntityFields(T expected, T actual, SoftAssert softAssert) {
        validateAllEntityFieldsIgnoring(expected, actual, List.of(), softAssert);
    }

    /**
     * Compares all fields between expected and actual objects, except the ignored ones.
     * Fails softly using {@link SoftAssert}.
     *
     * @param expected      the expected object
     * @param actual        the actual object returned from API
     * @param ignoredFields list of field names to skip during comparison
     * @param softAssert    the SoftAssert instance to collect failures
     * @param <T>           entity type
     */
    @Step("Validate all fields of entity {expected.getClass().getSimpleName()}, ignoring fields: {ignoredFields}")
    public static <T> void validateAllEntityFieldsIgnoring(T expected, T actual, List<String> ignoredFields, SoftAssert softAssert) {
        for (Field field : expected.getClass().getDeclaredFields()) {
            field.setAccessible(true);
            if (ignoredFields.contains(field.getName())) continue;

            try {
                Object expectedValue = field.get(expected);
                Object actualValue = field.get(actual);
                softAssert.assertEquals(actualValue, expectedValue, "Field '" + field.getName() + "' does not match");
            } catch (IllegalAccessException e) {
                throw new RuntimeException(String.format(
                        "Failed to access field '%s' in class %s. Expected value: [%s], Actual value: [%s]",
                        field.getName(),
                        expected.getClass().getSimpleName(),
                        safeToString(field, expected),
                        safeToString(field, actual)
                ), e);
            }
        }
    }

    /**
     * Safely extracts string representation of a field value, or returns "unavailable" if inaccessible.
     *
     * @param field the field to extract from
     * @param obj   the object containing the field
     * @return string representation of the field value or "unavailable"
     */
    private static <T> String safeToString(Field field, T obj) {
        try {
            Object value = field.get(obj);
            return value != null ? value.toString() : "null";
        } catch (Exception e) {
            return "unavailable";
        }
    }
}
