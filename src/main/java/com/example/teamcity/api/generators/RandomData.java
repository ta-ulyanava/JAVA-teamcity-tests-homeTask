package com.example.teamcity.api.generators;

import io.qameta.allure.Step;
import org.apache.commons.lang3.RandomStringUtils;

import java.util.Random;

/**
 * Utility class for generating random test data such as strings, IDs, and names.
 */
public final class RandomData {

    private static final String TEST_PREFIX = "test_";
    private static final int MAX_LENGTH = 10;
    private static final Random RANDOM = new Random();

    private RandomData() {}

    /**
     * Generates a random alphanumeric string with a default prefix.
     *
     * @return random string with prefix, e.g., test_ab12XyZ89
     */
    @Step("Generate random string with default prefix")
    public static String getString() {
        return TEST_PREFIX + RandomStringUtils.randomAlphanumeric(MAX_LENGTH);
    }

    /**
     * Generates a random alphanumeric string with a specific length and prefix.
     *
     * @param length total length of the resulting string including prefix
     * @return random string with prefix of specified length
     */
    @Step("Generate random string with prefix of length {length}")
    public static String getString(int length) {
        return TEST_PREFIX + RandomStringUtils.randomAlphanumeric(Math.max(length - TEST_PREFIX.length(), 1));
    }

    /**
     * Generates a unique name using the current timestamp and random characters.
     *
     * @return unique name string
     */
    @Step("Generate unique name")
    public static String getUniqueName() {
        return TEST_PREFIX + System.currentTimeMillis() + RandomStringUtils.randomAlphanumeric(MAX_LENGTH);
    }

    /**
     * Generates a unique ID using the current timestamp and random characters.
     *
     * @return unique ID string
     */
    @Step("Generate unique ID")
    public static String getUniqueId() {
        return TEST_PREFIX + System.currentTimeMillis() + RandomStringUtils.randomAlphanumeric(MAX_LENGTH);
    }

    /**
     * Generates a string consisting of random digits with specified length.
     *
     * @param length number of digits
     * @return numeric string
     */
    @Step("Generate random digit string of length {length}")
    public static String getDigits(int length) {
        return RandomStringUtils.randomNumeric(length);
    }
}
