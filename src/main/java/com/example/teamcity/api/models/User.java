package com.example.teamcity.api.models;

import com.example.teamcity.api.annotations.Random;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

// DTO class
// Аннотации ломбока
@Data // это шорткат для анотации toString,Equals, Getter
// t.e toString будет определяться на основе двух полей класса
// дает геттеры и сеттеры для НЕ final полей
@Builder // реализует паттерн билдер, позволяет гибко, в произвольном порядке, создавать поля объекта
// обычно джава требует строгий порядок
// билдер позволяет инициализировать не все поля
@AllArgsConstructor // конструктор со всеми аргументами
@NoArgsConstructor // конструктор без аргументов
@JsonIgnoreProperties(ignoreUnknown = true)// чтобы не падать когда приходят проперти, которые мы не описывали
public class User extends BaseModel{
    private int id;
    @Random
    private String username;
    @Random
    private String password;
    private Roles roles;


/* Т.к используем ломбок то можно закаментить
    public User(String user, String password){
        this.user=user;
        this.password=password;
    }
    public String getUser(){
        return user;
    }

    public String getPassword() {
        return password;
    }*/
}
