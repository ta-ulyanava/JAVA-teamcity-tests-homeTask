package com.example.teamcity.api.enums;

import com.example.teamcity.api.models.BaseModel;
import com.example.teamcity.api.models.BuildType;
import com.example.teamcity.api.models.Project;
import com.example.teamcity.api.models.User;
import lombok.AllArgsConstructor;
import lombok.Getter;

@AllArgsConstructor
@Getter
public enum ApiEndpoint {
    BUILD_TYPES("/app/rest/buildTypes", BuildType.class, "buildType"),
    PROJECTS("/app/rest/projects", Project.class, "project"),
    USERS("/app/rest/users", User.class, "user");

    private final String url;
    private final Class<? extends BaseModel> modelClass;
    private final String jsonListKey;
}

