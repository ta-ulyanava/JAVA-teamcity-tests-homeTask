package com.example.teamcity.api;

import com.example.teamcity.api.models.User;
import com.example.teamcity.api.spec.Specifications;
import io.restassured.RestAssured;
import org.testng.annotations.Test;

import java.util.List;

public class DummyTest extends BaseApiTest{
    @Test
    public void userShouldBeAbleToGetAllProjects(){
        RestAssured
                .given()
//Метод .spec() используется в Rest Assured для добавления спецификации запроса (RequestSpecification).
                .spec(Specifications.getSpec() //– получаем уникальный экземпляр Specifications (Singleton)
//                        Создаёт объект User с логином admin и паролем admin.
//                                Вызывает метод .authSpec(user), который добавляет Basic Auth.
//                                В итоге, Rest Assured отправляет запрос с заголовком авторизации
//                                (Authorization: Basic <base64-encoded admin:admin>).
//                        Как это работает?
//
//                                Получает объект User, у которого есть user (логин) и password.
//                                Создаёт объект BasicAuthScheme, который добавляет Basic Authentication.
//                                Вызывает reqBuilder() – базовую спецификацию (baseUri, Content-Type и т.д.).
//                                Добавляет .setAuth(basicAuthScheme), чтобы запрос автоматически включал логин и пароль.
//                                Возвращает готовую спецификацию RequestSpecification.
                        .authSpec(User.builder()
                                .user("admin").password("admin")
                                .build()))
                .when()
                .get("/app/rest/projects")
                .then()
                .statusCode(200);
    }
}
