package com.example.teamcity.ui.pages.admin;

import com.codeborne.selenide.Selenide;
import com.codeborne.selenide.SelenideElement;
import io.qameta.allure.Step;

import static com.codeborne.selenide.Selenide.$;

/**
 * Page object for the "Create Project" page in the TeamCity UI.
 * <p>
 * Supports form submission and project setup with build configuration.
 */
public class CreateProjectPage extends CreateBasePage {

    private static final String PROJECT_SHOW_MODE = "createProjectMenu";

    private final SelenideElement projectNameInput = $("#projectName");

    /**
     * Opens the create project page for the given parent project ID.
     *
     * @param projectId ID of the parent project
     * @return initialized CreateProjectPage
     */
    @Step("Open CreateProject page for project '{projectId}'")
    public static CreateProjectPage open(String projectId) {
        return Selenide.open(CREATE_URL.formatted(projectId, PROJECT_SHOW_MODE), CreateProjectPage.class);
    }

    /**
     * Submits the first step of the form by providing a VCS root URL.
     *
     * @param url repository URL
     * @return current page object
     */
    @Step("Submit VCS URL in create form: {url}")
    public CreateProjectPage createForm(String url) {
        baseCreateForm(url);
        return this;
    }

    /**
     * Fills in project and build configuration names and completes creation.
     *
     * @param projectName    name of the new project
     * @param buildTypeName  name of the new build configuration
     */
    @Step("Set project name '{projectName}' and build configuration name '{buildTypeName}'")
    public void setupProject(String projectName, String buildTypeName) {
        projectNameInput.val(projectName);
        buildTypeNameInput.val(buildTypeName);
        submitButton.click();
    }
}
