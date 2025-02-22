package com.example.teamcity.api.enums;

import com.example.teamcity.api.models.BaseModel;
import com.example.teamcity.api.models.BuildType;
import com.example.teamcity.api.models.Project;
import com.example.teamcity.api.models.User;
import lombok.AllArgsConstructor;
import lombok.Getter;

@AllArgsConstructor
@Getter
/**
 * Этот Enum отвечает за хранение URL API-эндпоинтов для работы с TeamCity
 * -Каждое значение** (`BUILD_TYPE`, `PROJECT`, `USER`) **связывается с конкретным URL**.
 * Метод `getUrl()` из Lombok позволяет **получить URL для запроса**.
 */
public enum Endpoint {
    BUILD_TYPES("/app/rest/buildTypes", BuildType.class),
    PROJECTS("/app/rest/projects", Project.class),
    USERS("/app/rest/users", User.class);


    private final String url;
    private final Class<? extends BaseModel> modelClass;
}
