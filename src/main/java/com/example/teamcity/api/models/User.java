package com.example.teamcity.api.models;

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

public class User extends BaseModel{
    private String user;
    private String password;
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
