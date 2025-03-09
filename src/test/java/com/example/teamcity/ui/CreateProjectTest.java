package com.example.teamcity.ui;

import com.codeborne.selenide.Condition;
import com.example.teamcity.api.enums.Endpoint;
import com.example.teamcity.api.models.Project;
import com.example.teamcity.api.ui.pages.LoginPage;
import com.example.teamcity.api.ui.pages.ProjectPage;
import com.example.teamcity.api.ui.pages.ProjectsPage;
import com.example.teamcity.api.ui.pages.admin.CreateProjectPage;
import lombok.val;
import org.testng.annotations.Test;

import static io.qameta.allure.Allure.step;

@Test(groups = "Regression")
public class CreateProjectTest extends BaseUiTest {
    private static final String REPO_URL = "https://github.com/AlexPshe/spring-core-for-qa";

    @Test(description = "User should be able to create project", groups = {"Positive"})
    public void UserCreatesProject() {
        // подготовка окружения
        loginAs(testData.getUser());
        // взаимодействие с UI
        CreateProjectPage.open("_Root")
                .createForm(REPO_URL)
                .setupProject(testData.getProject().getId(), testData.getBuildType().getName());
        // проверка состояния API
        // (корректность отправки данных с UI на API)
        var createdProject = superUserCheckRequests.<Project>getRequest(Endpoint.PROJECTS).read("name:" + testData.getProject().getName());
        softy.assertNotNull(createdProject);
        // проверка состояния UI
        // (корректность считывания данных и отображение данных на UI)
        ProjectPage.open(createdProject.getId())
                .title.shouldHave(Condition.exactText(testData.getProject().getName()));
        var isProjectExists = ProjectsPage.open().getProjects().stream()
                .anyMatch(project -> project
                        .getName()
                        .text()
                        .equals(testData.getProject().getName()));
        softy.assertTrue(isProjectExists);

    }

//    @Test(description = "User should not be able to create a project without a name", groups = {"Negative"})
//    public void userCreatesProjectWithoutName() {
//        // подготовка окружения
//        step("Login as user");
//        loginAs(testData.getUser());
//        // взаимодействие с UI
//        step("Open `Create Project Page` (http://localhost:8111/admin/createObjectMenu.html)");
//        step("Send all project parameters (repository URL)");
//        step("Click `Proceed`");
//        step("Set Project Name value is empty");
//        step("Click `Proceed`");
//
//        // проверка состояния API
//        // (корректность отправки данных с UI на API)
//        step("Check that number of projects did not change");
//        // проверка состояния UI
//        // (корректность считывания данных и отображение данных на UI)
//        step("Check that error appears `Project name must not be empty`");
//
//    }
}
