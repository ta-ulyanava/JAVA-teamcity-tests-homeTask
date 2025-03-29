package com.example.teamcity.api.enums;

public enum WebRoute {
    GITHUB_REPO("https://github.com/ta-ulyanava/JAVA-teamcity-tests-homeTask.git"),
    PROJECT_PAGE("/project/%s"),
    BUILD_TYPE_PAGE("/buildConfiguration/%s"),
    CREATE_BUILD_TYPE_PAGE("/admin/createObjectMenu.html?projectId=%s&showMode=createBuildTypeMenu");

    private final String url;

    WebRoute(String url) {
        this.url = url;
    }

    public String getUrl() {
        return url;
    }
} 