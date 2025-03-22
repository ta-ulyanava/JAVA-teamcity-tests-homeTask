package com.example.teamcity.api;

import com.example.teamcity.api.constants.TestConstants;
import com.example.teamcity.api.enums.ApiEndpoint;
import com.example.teamcity.api.generators.RandomData;
import com.example.teamcity.api.generators.TestDataGenerator;
import com.example.teamcity.api.generators.domain.ProjectTestData;
import com.example.teamcity.api.models.Project;
import com.example.teamcity.api.spec.responce.IncorrectDataSpecs;
import com.example.teamcity.api.validation.SearchValidator;
import io.qameta.allure.Feature;
import io.qameta.allure.Story;
import io.restassured.response.Response;
import org.testng.annotations.Test;

import java.util.List;

@Test(groups = {"Regression", "Search"})
public class ProjectSearchTest extends BaseApiTest {

    // =================== SEARCH BY NAME TESTS (PROJECT_SEARCH_NAME_TAG) =================== //
    @Feature("Project Search")
    @Story("User should be able to find a project by its exact name")
    @Test(description = "User should be able to find a project by its exact name", groups = {"Positive", "PROJECT_SEARCH_NAME_TAG"})
    public void userShouldBeAbleToFindProjectByNameTest() {
        String projectName = RandomData.getUniqueName();
        Project createdProject = createProjectAndExtractModel(TestDataGenerator.generate(Project.class, RandomData.getUniqueId(), projectName));
        Project foundProject = findSingleProjectByLocator("name", createdProject.getName());
        SearchValidator.validateSearchResult(createdProject, foundProject, "Project", "name", List.of("parentProject"), softy);
    }


    @Feature("Project Search")
    @Story("User should not be able to find a project by a non-existing name")
    @Test(description = "User should not be able to find a project by a non-existing name", groups = {"Negative", "PROJECT_SEARCH_NAME_TAG"})
    public void userShouldNotBeAbleToFindProjectByNonExistingNameTest() {
        String nonExistingProjectName = RandomData.getUniqueName();
        Response response = userUncheckedRequest.getRequest(ApiEndpoint.PROJECTS).findSingleByLocator("name:" + nonExistingProjectName);
        response.then().spec(IncorrectDataSpecs.emptyEntityListReturned("Project", "name", nonExistingProjectName));
        softy.assertAll();
    }
    @Feature("Project Search")
    @Story("User should be able to find a project by multiple words in its name")
    @Test(description = "User should be able to find a project by its name containing multiple words", groups = {"Positive", "PROJECT_SEARCH_NAME_TAG"})
    public void userShouldBeAbleToFindProjectByMultiWordNameTest() {
        String multiWordName = "Test Project " + RandomData.getString();
        Project createdProject = createProjectAndExtractModel(TestDataGenerator.generate(Project.class, RandomData.getUniqueId(), multiWordName));
        Project foundProject = findSingleProjectByLocator("name", createdProject.getName());
        SearchValidator.validateSearchResult(createdProject, foundProject, "Project", "name", List.of("parentProject"), softy);
    }

    @Feature("Project Search")
    @Story("Search by Name with Special Characters")
    @Test(description = "User should be able to find a project by name containing special characters", groups = {"Positive", "PROJECT_SEARCH_NAME_TAG"})
    public void userShouldBeAbleToFindProjectByNameWithSpecialCharactersTest() {
        String specialCharName = TestConstants.SPECIAL_CHARACTERS + RandomData.getString();
        Project createdProject = createProjectAndExtractModel(TestDataGenerator.generate(Project.class, RandomData.getUniqueId(), specialCharName));
        Project foundProject = findSingleProjectByLocator("name", createdProject.getName());
        SearchValidator.validateSearchResult(createdProject, foundProject, "Project", "name", List.of("parentProject"), softy);
    }

    @Feature("Project Search")
    @Story("Search by maximum allowed name length")
    @Test(description = "User should be able to search for a project with the maximum allowed name length", groups = {"Positive", "PROJECT_SEARCH_NAME_TAG"})
    public void userShouldBeAbleToSearchProjectByMaxLengthNameTest() {
        String longName = RandomData.getString(500);
        Project createdProject = createProjectAndExtractModel(TestDataGenerator.generate(Project.class, RandomData.getUniqueId(), longName));
        Project foundProject = findSingleProjectByLocator("name", createdProject.getName());
        SearchValidator.validateSearchResult(createdProject, foundProject, "Project", "name", List.of("parentProject"), softy);
    }

    @Feature("Project Search")
    @Story("SQL Injection should not be executed")
    @Test(description = "User should not be able to find a project using SQL injection in name", groups = {"Negative", "Security", "PROJECT_SEARCH_NAME_TAG"})
    public void userShouldNotBeAbleToFindProjectUsingSqlInjectionInNameTest() {
        String sqlInjection = TestConstants.SQL_INJECTION_PAYLOAD;
        Response response = userUncheckedRequest.getRequest(ApiEndpoint.PROJECTS).findSingleByLocator("name:" + sqlInjection);
        response.then().spec(IncorrectDataSpecs.emptyEntityListReturned("Project", "name", sqlInjection));
        softy.assertAll();
    }

    @Feature("Project Search")
    @Story("XSS payload should not be executed")
    @Test(description = "User should not be able to find a project using XSS injection in name", groups = {"Negative", "Security", "PROJECT_SEARCH_NAME_TAG"})
    public void userShouldNotBeAbleToFindProjectUsingXssInjectionInNameTest() {
        String xssPayload = TestConstants.XSS_PAYLOAD;
        Response response = userUncheckedRequest.getRequest(ApiEndpoint.PROJECTS).findSingleByLocator("name:" + xssPayload);
        response.then().spec(IncorrectDataSpecs.emptyEntityListReturned("Project", "name", xssPayload));
        softy.assertAll();
    }

    @Feature("Project Search")
    @Story("Search should be case-sensitive")
    @Test(description = "User should not be able to find a project by name with different letter case", groups = {"Negative", "PROJECT_SEARCH_NAME_TAG"})
    public void userShouldNotBeAbleToFindProjectByNameWithDifferentLetterCaseTest() {
        String originalName = "testproject" + RandomData.getString();
        createProjectAndExtractModel(TestDataGenerator.generate(Project.class, RandomData.getUniqueId(), originalName));
        String upperCasedName = originalName.toUpperCase();
        Response response = userUncheckedRequest.getRequest(ApiEndpoint.PROJECTS).findSingleByLocator("name:" + upperCasedName);
        response.then().spec(IncorrectDataSpecs.emptyEntityListReturned("Project", "name", upperCasedName));
        softy.assertAll();
    }


    // =================== SEARCH BY NAME TESTS (PROJECT_SEARCH_TAG) =================== //
}
/**
 * Вот чеклист проверок поиска по имени, основанный на интерфейсе `SearchInterface<T>` и примерах на изображениях:
 *
 * ### **Положительные кейсы:**
 *
 * 2. **Поиск по подстроке**
 *    - Поиск по части имени → найден проект, содержащий эту подстроку.
 *    - `/app/rest/projects?locator=name:proj`
 *

 * 5. **Пустой запрос (все проекты)**
 *    - Отправка запроса без указания имени → возвращаются все проекты.
 *    - `/app/rest/projects`
 *

 *
 * 7. **Поиск с спецсимволами, не поддерживаемыми API**
 *    - Поиск проекта с `?`, `*`, `@` в имени → проверяем поведение (ошибка или экранирование).
 *    - `/app/rest/projects?locator=name:?InvalidProjectName`
 *

 *
 * 9. **Поиск по пустой строке в параметре**
 *    - Запрос `name:` без значения → проверяем, как API обрабатывает такой ввод.
 *    - `/app/rest/projects?locator=name:`
 *

 * 13. **Поиск по частично совпадающим словам**
 *    - Проверяем, если запрос "Test Project", а в системе "Test-Project", API должен вернуть результат или нет.
 *    - `/app/rest/projects?locator=name:Test Project`
 *
 * Этот чеклист покроет как функциональные, так и негативные сценарии, включая проверку безопасности.
 */