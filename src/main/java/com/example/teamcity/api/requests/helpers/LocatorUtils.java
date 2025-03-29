package com.example.teamcity.api.requests.helpers;

import io.qameta.allure.Step;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;

/**
 * Utility class for encoding TeamCity locator parameters.
 * <p>
 * Provides methods for safe URL encoding of complex locator strings used in REST API queries.
 */
public final class LocatorUtils {

    private LocatorUtils() {}

    /**
     * Encodes the given locator string using UTF-8 encoding.
     *
     * @param locator raw locator string (e.g. name:Project_123)
     * @return encoded string suitable for use in URLs
     */
    @Step("Encode locator string: {locator}")
    public static String encode(String locator) {
        return URLEncoder.encode(locator, StandardCharsets.UTF_8);
    }
}
