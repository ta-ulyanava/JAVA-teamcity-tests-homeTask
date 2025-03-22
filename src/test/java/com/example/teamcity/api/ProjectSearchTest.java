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
@Feature("Project Search")
@Test(groups = {"Regression", "Search"})
public class ProjectSearchTest extends BaseApiTest {

    // =================== SEARCH BY NAME TESTS (PROJECT_SEARCH_NAME_TAG) =================== //
  // =================== LOCATOR-BASED SEARCH =================== //
    @Story("User should be able to find a project by its exact name")
    @Test(description = "User should be able to find a project by its exact name", groups = {"Positive", "PROJECT_SEARCH_NAME_TAG", "LOCATOR_BASED_SEARCH"})
    public void userShouldBeAbleToFindProjectByNameTest() {
        String projectName = RandomData.getUniqueName();
        Project createdProject = createProjectAndExtractModel(TestDataGenerator.generate(Project.class, RandomData.getUniqueId(), projectName));
        Project foundProject = findSingleProjectByLocator("name", createdProject.getName());
        SearchValidator.validateSearchResult(createdProject, foundProject, "Project", "name", List.of("parentProject"), softy);
    }
    @Story("User should not be able to find a project by a non-existing name")
    @Test(description = "User should not be able to find a project by a non-existing name", groups = {"Negative", "PROJECT_SEARCH_NAME_TAG", "LOCATOR_BASED_SEARCH"})
    public void userShouldNotBeAbleToFindProjectByNonExistingNameTest() {
        String nonExistingProjectName = RandomData.getUniqueName();
        Response response = userUncheckedRequest.getRequest(ApiEndpoint.PROJECTS).findFirstEntityByLocatorQuery("name:" + nonExistingProjectName);
        response.then().spec(IncorrectDataSpecs.emptyEntityListReturned("Project", "name", nonExistingProjectName));
        softy.assertAll();
    }
    @Story("User should be able to find a project by multiple words in its name")
    @Test(description = "User should be able to find a project by its name containing multiple words", groups = {"Positive", "PROJECT_SEARCH_NAME_TAG", "LOCATOR_BASED_SEARCH"})
    public void userShouldBeAbleToFindProjectByMultiWordNameTest() {
        String multiWordName = "Test Project " + RandomData.getString();
        Project createdProject = createProjectAndExtractModel(TestDataGenerator.generate(Project.class, RandomData.getUniqueId(), multiWordName));
        Project foundProject = findSingleProjectByLocator("name", createdProject.getName());
        SearchValidator.validateSearchResult(createdProject, foundProject, "Project", "name", List.of("parentProject"), softy);
    }

    @Story("Search by Name with Special Characters")
    @Test(description = "User should be able to find a project by name containing special characters", groups = {"Positive", "PROJECT_SEARCH_NAME_TAG", "LOCATOR_BASED_SEARCH"})
    public void userShouldBeAbleToFindProjectByNameWithSpecialCharactersTest() {
        String specialCharName = TestConstants.SPECIAL_CHARACTERS + RandomData.getString();
        Project createdProject = createProjectAndExtractModel(TestDataGenerator.generate(Project.class, RandomData.getUniqueId(), specialCharName));
        Project foundProject = findSingleProjectByLocator("name", createdProject.getName());
        SearchValidator.validateSearchResult(createdProject, foundProject, "Project", "name", List.of("parentProject"), softy);
    }

    @Story("Search by maximum allowed name length")
    @Test(description = "User should be able to search for a project with the maximum allowed name length", groups = {"Positive", "PROJECT_SEARCH_NAME_TAG", "LOCATOR_BASED_SEARCH"})
    public void userShouldBeAbleToSearchProjectByMaxLengthNameTest() {
        String longName = RandomData.getString(500);
        Project createdProject = createProjectAndExtractModel(TestDataGenerator.generate(Project.class, RandomData.getUniqueId(), longName));
        Project foundProject = findSingleProjectByLocator("name", createdProject.getName());
        SearchValidator.validateSearchResult(createdProject, foundProject, "Project", "name", List.of("parentProject"), softy);
    }

    @Story("Search by single character name")
    @Test(description = "User should be able to find a project by a name consisting of a single character", groups = {"Positive", "PROJECT_SEARCH_NAME_TAG", "LOCATOR_BASED_SEARCH"})
    public void userShouldBeAbleToFindProjectBySingleCharacterNameTest() {
        String oneCharName = RandomData.getString(1);
        Project createdProject = createProjectAndExtractModel(TestDataGenerator.generate(Project.class, RandomData.getUniqueId(), oneCharName));
        Project foundProject = findSingleProjectByLocator("name", createdProject.getName());
        SearchValidator.validateSearchResult(createdProject, foundProject, "Project", "name", List.of("parentProject"), softy);
    }

    @Story("SQL Injection should not be executed")
    @Test(description = "User should not be able to find a project using SQL injection in name", groups = {"Negative", "Security", "PROJECT_SEARCH_NAME_TAG", "LOCATOR_BASED_SEARCH"})
    public void userShouldNotBeAbleToFindProjectUsingSqlInjectionInNameTest() {
        String sqlInjection = TestConstants.SQL_INJECTION_PAYLOAD;
        Response response = userUncheckedRequest.getRequest(ApiEndpoint.PROJECTS).findFirstEntityByLocatorQuery("name:" + sqlInjection);
        response.then().spec(IncorrectDataSpecs.emptyEntityListReturned("Project", "name", sqlInjection));
        softy.assertAll();
    }

    @Story("XSS payload should not be executed")
    @Test(description = "User should not be able to find a project using XSS injection in name", groups = {"Negative", "Security", "PROJECT_SEARCH_NAME_TAG", "LOCATOR_BASED_SEARCH"})
    public void userShouldNotBeAbleToFindProjectUsingXssInjectionInNameTest() {
        String xssPayload = TestConstants.XSS_PAYLOAD;
        Response response = userUncheckedRequest.getRequest(ApiEndpoint.PROJECTS).findFirstEntityByLocatorQuery("name:" + xssPayload);
        response.then().spec(IncorrectDataSpecs.emptyEntityListReturned("Project", "name", xssPayload));
        softy.assertAll();
    }

    @Story("Search should be case-sensitive")
    @Test(description = "User should not be able to find a project by name with different letter case", groups = {"Negative", "PROJECT_SEARCH_NAME_TAG", "LOCATOR_BASED_SEARCH"})
    public void userShouldNotBeAbleToFindProjectByNameWithDifferentLetterCaseTest() {
        String originalName = "testproject" + RandomData.getString();
        createProjectAndExtractModel(TestDataGenerator.generate(Project.class, RandomData.getUniqueId(), originalName));
        String upperCasedName = originalName.toUpperCase();
        Response response = userUncheckedRequest.getRequest(ApiEndpoint.PROJECTS).findFirstEntityByLocatorQuery("name:" + upperCasedName);
        response.then().spec(IncorrectDataSpecs.emptyEntityListReturned("Project", "name", upperCasedName));
        softy.assertAll();
    }

    @Story("Search by localized name")
    @Test(description = "User should be able to find a project by its localized name", groups = {"Positive", "PROJECT_SEARCH_NAME_TAG", "LOCATOR_BASED_SEARCH"})
    public void userShouldBeAbleToFindProjectByLocalizedNameTest() {
        String localizedName = TestConstants.LOCALIZATION_CHARACTERS;
        Project createdProject = createProjectAndExtractModel(TestDataGenerator.generate(Project.class, RandomData.getString(), localizedName));
        Project foundProject = findSingleProjectByLocator("name", createdProject.getName());
        SearchValidator.validateSearchResult(createdProject, foundProject, "Project", "name", List.of("parentProject"), softy);
    }

    @Story("Search by digits-only name")
    @Test(description = "User should be able to find a project by a name consisting only of digits", groups = {"Positive", "PROJECT_SEARCH_NAME_TAG", "LOCATOR_BASED_SEARCH"})
    public void userShouldBeAbleToFindProjectByDigitsOnlyNameTest() {
        String digitsOnlyName = RandomData.getDigits(6);
        Project createdProject = createProjectAndExtractModel(TestDataGenerator.generate(Project.class, RandomData.getUniqueId(), digitsOnlyName));
        Project foundProject = findSingleProjectByLocator("name", createdProject.getName());
        SearchValidator.validateSearchResult(createdProject, foundProject, "Project", "name", List.of("parentProject"), softy);
    }

    @Story("Search by empty name")
    @Test(description = "User should not be able to find a project using empty name as a search parameter", groups = {"Negative", "PROJECT_SEARCH_NAME_TAG", "LOCATOR_BASED_SEARCH"})
    public void userShouldNotBeAbleToFindProjectByEmptyNameTest() {
        Response response = userUncheckedRequest.getRequest(ApiEndpoint.PROJECTS).findFirstEntityByLocatorQuery("name:");
        response.then().spec(IncorrectDataSpecs.emptyEntityListReturned("Project", "name", ""));
        softy.assertAll();
    }

    @Story("Search by name with only space character")
    @Test(description = "User should not be able to find a project using a name with only a space", groups = {"Negative", "PROJECT_SEARCH_NAME_TAG", "LOCATOR_BASED_SEARCH"})
    public void userShouldNotBeAbleToFindProjectByNameWithSpaceOnlyTest() {
        String spaceName = " ";
        Response response = userUncheckedRequest.getRequest(ApiEndpoint.PROJECTS).findFirstEntityByLocatorQuery("name:" + spaceName);
        response.then().spec(IncorrectDataSpecs.emptyEntityListReturned("Project", "name", spaceName));
        softy.assertAll();
    }

    @Story("Search by name with trailing space")
    @Test(description = "User should be able to find a project by name that includes a trailing space", groups = {"Positive", "PROJECT_SEARCH_NAME_TAG", "LOCATOR_BASED_SEARCH"})
    public void userShouldBeAbleToFindProjectByNameWithTrailingSpaceTest() {
        String nameWithTrailingSpace = "Project_" + RandomData.getString() + " ";
        Project createdProject = createProjectAndExtractModel(TestDataGenerator.generate(Project.class, ProjectTestData.projectId(), nameWithTrailingSpace));
        Project foundProject = findSingleProjectByLocator("name", createdProject.getName());
        SearchValidator.validateSearchResult(createdProject, foundProject, "Project", "name", List.of("parentProject"), softy);
    }

    @Story("Search by name with leading space")
    @Test(description = "User should be able to find a project by name that starts with a space", groups = {"Positive", "PROJECT_SEARCH_NAME_TAG", "LOCATOR_BASED_SEARCH"})
    public void userShouldBeAbleToFindProjectByNameWithLeadingSpaceTest() {
        String nameWithLeadingSpace = " " + RandomData.getUniqueName();
        Project createdProject = createProjectAndExtractModel(TestDataGenerator.generate(Project.class, ProjectTestData.projectId(), nameWithLeadingSpace));
        Project foundProject = findSingleProjectByLocator("name", createdProject.getName());
        SearchValidator.validateSearchResult(createdProject, foundProject, "Project", "name", List.of("parentProject"), softy);
    }

    @Story("Search by partially matching words")
    @Test(description = "User should not be able to find a project by partially matching name (e.g., using space instead of dash)", groups = {"Negative", "PROJECT_SEARCH_NAME_TAG", "LOCATOR_BASED_SEARCH"})
    public void userShouldNotBeAbleToFindProjectByPartiallyMatchingWordsTest() {
        String actualProjectName = "Test-Project-" + RandomData.getString();
        createProjectAndExtractModel(TestDataGenerator.generate(Project.class, RandomData.getUniqueId(), actualProjectName));
        String searchQuery = actualProjectName.replace("-", " ");
        Response response = userUncheckedRequest.getRequest(ApiEndpoint.PROJECTS).findFirstEntityByLocatorQuery("name:" + searchQuery);
        response.then().spec(IncorrectDataSpecs.emptyEntityListReturned("Project", "name", searchQuery));
        softy.assertAll();
    }
    // =================== LOCATOR-BASED SEARCH =================== //
    // =================== SEARCH BY NAME TESTS (PROJECT_SEARCH_TAG) =================== //
}
// =================== MISSING TESTS FOR PROJECT NAME SEARCH =================== //

// ✅ Positive cases:
// PATH_BASED_SEARCH

// TODO: Проверка поиска по точному имени через /name:<имя> (возвращает один проект)

// TODO: Проверка поиска всех проектов с одинаковым именем через ?locator=name:<имя> (возвращает список)

// TODO: Проверка поиска по имени с ограничением count (например, count:1)

// TODO: Проверка пагинации: count + start (например, count:2, start:1)

// 🚫 Negative cases:

// TODO: Поиск без имени: /projects/name: (ожидается ошибка или пустой результат)

// TODO: Поиск по name: с пустым значением → ?locator=name: (ожидается пустой результат)

// TODO: Поиск с пробелами в начале/в конце параметра name → name:" project" / name:"project "

// TODO: Поиск имени с учётом регистра в ?locator=name: (убедиться, что чувствительность к регистру сохраняется)

// TODO: Проверка, что name:contains(...) / name:like(...) / name:substring(...) не поддерживаются (ожидается ошибка)

// =================== END OF MISSING TESTS =================== //
