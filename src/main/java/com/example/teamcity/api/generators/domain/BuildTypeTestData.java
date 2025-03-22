package com.example.teamcity.api.generators.domain;

import java.util.UUID;

public class BuildTypeTestData {
    public static String buildTypeName() {
        return "test_" + UUID.randomUUID().toString().substring(0, 8);
    }
}
