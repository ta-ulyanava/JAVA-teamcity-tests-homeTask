package com.example.teamcity.api.models;

import com.example.teamcity.api.annotations.Random;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.apache.commons.lang3.RandomStringUtils;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
public class User extends BaseModel {
    private int id;

    @Random
    @Builder.Default
    private String username = generateRandomString();

    @Random
    @Builder.Default
    private String password = generateRandomString();

    private Roles roles;

    private static String generateRandomString() {
        return "test_" + RandomStringUtils.randomAlphanumeric(8);
    }
}
