package com.example.teamcity.api.generators.domain;

import io.qameta.allure.Step;

import java.util.UUID;

/**
 * Utility class for generating test data related to Build Types.
 * <p>
 * Provides methods for generating unique Build Type names to avoid conflicts during testing.
 */
public class BuildTypeTestData {

    /**
     * Generates a unique Build Type name using a random UUID.
     *
     * @return a unique Build Type name in the format: test_<random_string>
     */
    @Step("Generate unique Build Type name")
    public static String buildTypeName() {
        return "test_" + UUID.randomUUID().toString().substring(0, 8);
    }
}
