package com.example.teamcity.api.validation;

import io.qameta.allure.Step;
import org.testng.asserts.SoftAssert;

import java.lang.reflect.Field;
import java.util.List;

/**
 * Utility class for validating search results from TeamCity API.
 * <p>
 * Provides assertions to verify that returned entities match expected ones, by comparing all or selected fields.
 */
public class SearchValidator {

    /**
     * Validates that all entities in the actual search result match the expected list by size and content.
     *
     * @param expectedList     list of expected entities
     * @param actualList       list of entities returned from API
     * @param entityType       type name of the entity (for logging)
     * @param identifierField  field name used to identify and match entities (e.g. "id" or "name")
     * @param ignoredFields    fields to skip during comparison
     * @param softAssert       SoftAssert instance to accumulate assertions
     * @param <T>              entity type
     */
    @Step("Validate search results for entity type: {entityType}")
    public static <T> void validateSearchResults(List<T> expectedList, List<T> actualList, String entityType, String identifierField, List<String> ignoredFields, SoftAssert softAssert) {
        softAssert.assertEquals(
                actualList.size(),
                expectedList.size(),
                String.format("Number of found %ss does not match expected. Expected: %d, Actual: %d", entityType, expectedList.size(), actualList.size())
        );
        for (int i = 0; i < expectedList.size(); i++) {
            validateSearchResult(expectedList.get(i), actualList.get(i), entityType, identifierField, ignoredFields, softAssert);
        }
    }

    /**
     * Validates a single expected entity against the actual entity returned by the API.
     *
     * @param expected         expected entity
     * @param actual           actual entity
     * @param entityType       type name of the entity (for logging)
     * @param identifierField  field used to locate the entity
     * @param ignoredFields    fields to skip during comparison
     * @param softAssert       SoftAssert instance
     * @param <T>              entity type
     */
    @Step("Validate single search result for entity type: {entityType}")
    public static <T> void validateSearchResult(T expected, T actual, String entityType, String identifierField, List<String> ignoredFields, SoftAssert softAssert) {
        try {
            Field field = expected.getClass().getDeclaredField(identifierField);
            field.setAccessible(true);
            Object expectedId = field.get(expected);

            softAssert.assertNotNull(
                    actual,
                    String.format("%s with %s '%s' was not found in the result list", entityType, identifierField, expectedId)
            );

            EntityValidator.validateAllEntityFieldsIgnoring(expected, actual, ignoredFields, softAssert);
            softAssert.assertAll();

        } catch (NoSuchFieldException | IllegalAccessException e) {
            throw new RuntimeException(String.format(
                    "Failed to validate %s by field '%s'. Reason: %s",
                    entityType, identifierField, e.getMessage()
            ), e);
        }
    }
}
