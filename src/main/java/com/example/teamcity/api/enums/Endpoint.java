package com.example.teamcity.api.enums;

import com.example.teamcity.api.models.BaseModel;
import com.example.teamcity.api.models.BuildType;
import com.example.teamcity.api.models.Project;
import com.example.teamcity.api.models.User;
import lombok.AllArgsConstructor;
import lombok.Getter;

// Сначала создаем поля, которые хотим хранить в Enum
// Enum это как константа: если у меня урл 1, то ДТО обязательно тоже ДТО1
// Enum это подтип констант и его называют большими буквами
@AllArgsConstructor
@Getter //Enum это константа, ни в коем случае нельзя сеттер
/**
 * Этот Enum отвечает за хранение URL API-эндпоинтов для работы с TeamCity
 * -Каждое значение** (`BUILD_TYPE`, `PROJECT`, `USER`) **связывается с конкретным URL**.
 * Метод `getUrl()` из Lombok позволяет **получить URL для запроса**.
 */
public enum Endpoint {
    // BuildType.Class -> Ссылка на класс, а не на объект
    BUILD_TYPES("/api/rest/buildTypes", BuildType.class),
    PROJECTS("/api/rest/projects", Project.class),
    USERS("/api/rest/users", User.class);


    private final String url;
    //Эта конструкция используется в Generics (обобщенные типы)
    private final Class<? extends BaseModel> modelClass; // означает "любой класс, который наследует BaseModel
}
