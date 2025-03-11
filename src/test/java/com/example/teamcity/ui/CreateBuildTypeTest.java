package com.example.teamcity.ui;

import com.codeborne.selenide.Condition;
import com.example.teamcity.api.enums.Endpoint;
import com.example.teamcity.api.models.Project;
import com.example.teamcity.api.models.BuildType;
import com.example.teamcity.api.models.User;
import com.example.teamcity.api.requests.CheckedRequest;
import com.example.teamcity.api.requests.UncheckedRequest;
import com.example.teamcity.api.spec.Specifications;
import com.example.teamcity.api.ui.pages.ProjectPage;
import com.example.teamcity.api.ui.pages.BuildTypePage;
import com.example.teamcity.api.ui.pages.admin.CreateBuildTypePage;
import com.example.teamcity.api.enums.UrlConstant;
import com.example.teamcity.api.controllers.ProjectController;
import com.example.teamcity.api.responses.ResponseExtractor;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;
import io.restassured.response.Response;

import java.time.Duration;

import static com.codeborne.selenide.Selenide.$;
import static com.codeborne.selenide.Selenide.open;
import static io.qameta.allure.Allure.step;

@Test(groups = "Regression")
public class CreateBuildTypeTest extends BaseUiTest {

    private static final String REPO_URL = "https://github.com/AlexPshe/spring-core-for-qa";
    private ProjectController projectController;
    private UncheckedRequest uncheckedRequest;

    @BeforeMethod(alwaysRun = true)
    public void setup() {
        super.beforeTest();
        
        // Сначала логинимся как пользователь
        loginAs(testData.getUser());
        
        // Инициализируем контроллер проекта и создаем проект
        projectController = new ProjectController(Specifications.authSpec(testData.getUser()));
        projectController.createProject(testData.getProject());
        
        // Инициализируем UncheckedRequest для получения Response
        uncheckedRequest = new UncheckedRequest(Specifications.superUserAuthSpec());
    }

    @Test(description = "User should be able to create build type", groups = {"Positive"})
    public void userCreatesBuildType() {
        // Взаимодействие с UI
        step("Create Build Type via UI", () -> 
            CreateBuildTypePage.open(testData.getProject().getId())
                .createForm(REPO_URL)
                .setupBuildType(testData.getBuildType().getName())
        );

        // Проверка состояния API
        step("Verify Build Type creation via API", () -> {
            Response response = uncheckedRequest.getRequest(Endpoint.BUILD_TYPES)
                .read("name:" + testData.getBuildType().getName());
            var createdBuildType = ResponseExtractor.extractModel(response, BuildType.class);
            softy.assertNotNull(createdBuildType, "Build Type should be created");
            
            BuildTypePage.open(createdBuildType.getId())
                .getTitle()
                .shouldHave(Condition.exactText(testData.getBuildType().getName()));
        });

        softy.assertAll();
    }

    @Test(description = "User should not be able to create build type without name", groups = {"Negative"})
    public void userCannotCreateBuildTypeWithoutName() {
        // Взаимодействие с UI
        step("Try to create Build Type without name", () -> {
            CreateBuildTypePage.open(testData.getProject().getId())
                .createForm(REPO_URL)
                .setupBuildType("");  // Пустое имя
        });

        // Проверка состояния UI - должно появиться сообщение об ошибке
        step("Verify error message is displayed", () -> {
            $(".error").shouldHave(Condition.text("Build configuration name cannot be empty"));
        });

        // Проверка состояния API - Build Type не должен быть создан
        step("Verify Build Type was not created", () -> {
            Response response = uncheckedRequest.getRequest(Endpoint.BUILD_TYPES)
                .read("project:" + testData.getProject().getId());
            var buildType = ResponseExtractor.extractModel(response, BuildType.class);
            softy.assertNull(buildType, "Build Type should not be created");
        });

        softy.assertAll();
    }
}



