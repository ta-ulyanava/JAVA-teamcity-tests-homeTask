package com.example.teamcity.ui.pages;

import com.codeborne.selenide.Selenide;
import com.codeborne.selenide.SelenideElement;
import io.qameta.allure.Step;

import static com.codeborne.selenide.Selenide.$;

/**
 * Page object for the TeamCity Project page.
 */
public class ProjectPage extends BasePage {

    private static final String PROJECT_URL = "/project/%s";

    public SelenideElement title = $("span[class='ProjectPageHeader__title--ih']");

    /**
     * Opens the Project page by project ID.
     *
     * @param projectId ID of the project to open
     * @return initialized ProjectPage
     */
    @Step("Open Project page for project ID '{projectId}'")
    public static ProjectPage open(String projectId) {
        return Selenide.open(PROJECT_URL.formatted(projectId), ProjectPage.class);
    }
}
