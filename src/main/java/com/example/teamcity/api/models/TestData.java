package com.example.teamcity.api.models;

import lombok.Data;

/**
 * Этот класс будет создавать все нужные нам сущности для тестов сразу: юзера. проект, билдтайп
 */
@Data
public class TestData {
    private Project project;
    private User user;
    private BuildType buildType;

}
