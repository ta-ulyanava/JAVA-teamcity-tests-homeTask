package com.example.teamcity.api.ui.pages;
import com.codeborne.selenide.SelenideElement;
import com.codeborne.selenide.Selenide;
import com.example.teamcity.api.ui.elements.ProjectElement;

import static com.codeborne.selenide.Selenide.$;

public class ProjectPage extends BasePage {
    private static final String PROJECT_URL = "/project/%s";
    public SelenideElement title = $("span[class='ProjectPageHeader__title--ih']");

    public static ProjectPage open(String projectId) {
        return Selenide.open(PROJECT_URL.formatted(projectId), ProjectPage.class);
    }

}
