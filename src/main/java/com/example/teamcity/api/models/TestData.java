package com.example.teamcity.api.models;

import lombok.Data;

/**
 * Этот класс будет создавать все нужные нам сущности для тестов сразу: юзера. проект, билдтайп
 */
@Data
//Аннотация @Data автоматически генерирует:
//
//        @Getter – геттеры для всех полей
//        @Setter – сеттеры для всех полей (кроме final)
//        @ToString – метод toString()
//        @EqualsAndHashCode – методы equals() и hashCode()
//        @RequiredArgsConstructor – конструктор для final полей
public class TestData {
    private Project project;
    private User user;
    private BuildType buildType;
}
