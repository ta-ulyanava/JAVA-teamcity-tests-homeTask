package com.example.teamcity.api.requests.helpers;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;

public final class LocatorUtils {

    private LocatorUtils() {}

    public static String encode(String locator) {
        return URLEncoder.encode(locator, StandardCharsets.UTF_8);
    }
}
