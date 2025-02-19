package com.example.teamcity.api.enums;

import com.example.teamcity.api.models.BaseModel;
import com.example.teamcity.api.models.BuildType;
import com.example.teamcity.api.models.Project;
import lombok.AllArgsConstructor;
import lombok.Getter;

// Сначала создаем поля, которые хотим хранить в Enum
// Enum это как константа: если у меня урл 1 , то ДТО обязательно1
// Enum это подтип констант и его называют большими буквами
@AllArgsConstructor
@Getter //Enum это константа, ни в коем случае нельзя сеттер
public enum Endpoint {
    //??? BuildType.Class
BUILD_TYPES("/api/rest/buildTypes", BuildType.class),
    PROJECT("/api/rest/projects",Project.class);

    private final String url;
    private final Class<? extends BaseModel> modelClass; // любой наследник BaseModel
}
