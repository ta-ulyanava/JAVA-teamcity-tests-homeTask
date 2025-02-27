package com.example.teamcity.api.spec;

import com.example.teamcity.api.config.Config;
import com.example.teamcity.api.models.User;
import io.restassured.builder.RequestSpecBuilder;
import io.restassured.filter.log.RequestLoggingFilter;
import io.restassured.filter.log.ResponseLoggingFilter;
import io.restassured.http.ContentType;
import io.restassured.specification.RequestSpecification;

import java.util.List;
// Спека нужна для параметризации параметров, которые задаются в зависимости от конкретного запроса
// Какие нам нужны спецификации? Пользователь долджен быть аутенцифицированным неаутнетифицированным и тп
// Хранит ли спецификация какието занчения? Совет -да, хранить токены
// И это тоже синглтон
//Нам необходимо использовать спецификацию для входа,
// то есть это явное прописание username, пароля в запросе, хоста и явно как-то endpoint.


public class Specifications {
    private static Specifications spec;

/* Какие спецификации и реквесты нужны?
    // Как минимум базовая спека кот содержит фильтры, контент тайпы
    // Используем билдер, реализованный по паттерну строитель в библиотеке Rest assured

    //Начнем создание rest-assured спецификаций в файле Specifications.java.
// Для того, чтобы создать спецификацию в rest-assured, нам понадобится RequestSpecBuilder
// Он реализует паттерн проектирования строитель и его суть в том, что мы создаем специальный класс,
// который является строителем и в него накапливаем какую-то информацию,
// потом явно вызываем метод Build и только после этого нам возвращается объект с этими свойствами.
// Поэтому, на самом деле, мы здесь используем паттерн проектирования.*/

    /**
     * Метод создает базовую спецификацию с помощью RequestSpecBuilder - класса Rest Assured
     */
    private static RequestSpecBuilder reqBuilder() {
        // создаем сам билдер
        RequestSpecBuilder reqBuilder = new RequestSpecBuilder();
        //Устанавливает базовый URL с помощью метода setBaseUri
        reqBuilder.setBaseUri("https://" + Config.getProperty("host"));
        // складываем в билдер значения, задавая формат отправляемых и принимаемых данных
        reqBuilder.setContentType(ContentType.JSON);
        reqBuilder.setAccept(ContentType.JSON);
        //Добавляет логирование запросов и ответов:
        reqBuilder.addFilters(List.of(new RequestLoggingFilter(), new ResponseLoggingFilter()));
        return reqBuilder;
    }

    /**
     * Метод создаёт спецификацию без аутентификации
     *
     */
//    Просто вызывает reqBuilder(), создаёт RequestSpecification без авторизации.
//    Используется для публичных API-запросов
//    Фраза "Метод создаёт спецификацию без аутентификации" означает, что этот метод (unauthSpec())
//    возвращает настройки для запросов, в которых отсутствует информация о логине и пароле.
//    Вызывает reqBuilder(), который:
//    Устанавливает базовый URL.
//    Добавляет Content-Type: JSON (формат данных).
//    Включает логирование запросов и ответов.

  /*  public static RequestSpecification unauthSpec() {
        var requestBuilder=reqBuilder();
        return reqBuilder()
// Вызывает .build(), что создаёт готовую спецификацию.
                .build();
    }*/
    public static RequestSpecification unauthSpec() {
        return new RequestSpecBuilder()
                .setBaseUri("http://" + Config.getProperty("host"))  // Используем HTTP вместо HTTPS
                .setContentType(ContentType.JSON)
                .setAccept(ContentType.JSON)
                .setRelaxedHTTPSValidation()  // Отключаем проверку сертификатов
                .addFilters(List.of(new RequestLoggingFilter(), new ResponseLoggingFilter()))
                .build();
    }


    /*public RequestSpecification authSpec(User user) {
        //Создаёт объект BasicAuthScheme (это класс для Basic Authentication)
        BasicAuthScheme basicAuthScheme = new BasicAuthScheme();
        //Устанавливает логин и пароль пользователя
        basicAuthScheme.setUserName(user.getUser());
        basicAuthScheme.setPassword(user.getPassword());
        //Передаёт эту аутентификацию в reqBuilder()
        return reqBuilder()
                .setAuth(basicAuthScheme)
                .build();
    }*/
public static RequestSpecification authSpec(User user){
    var requestBuilder=reqBuilder();
    // ???
    requestBuilder.setBaseUri("http://%s:%s@%s".formatted(user.getUsername(), user.getPassword(), Config.getProperty("host")));
    return requestBuilder.build();
}

public static RequestSpecification superUserAuthSpec(){
    var requestBuilder=reqBuilder();
    requestBuilder.setBaseUri("http://:%s@%s".formatted(Config.getProperty("superUserToken"), Config.getProperty("host")));
    return requestBuilder.build();
}

}
