package com.example.teamcity.api;

import com.example.teamcity.api.constants.TestConstants;
import com.example.teamcity.api.enums.ApiEndpoint;
import com.example.teamcity.api.generators.RandomData;
import com.example.teamcity.api.generators.TestDataGenerator;
import com.example.teamcity.api.generators.domain.ProjectTestData;
import com.example.teamcity.api.models.ParentProject;
import com.example.teamcity.api.models.Project;
import com.example.teamcity.api.requests.checked.CheckedBase;
import com.example.teamcity.api.requests.helpers.ProjectHelper;
import com.example.teamcity.api.responses.ResponseExtractor;
import com.example.teamcity.api.spec.responce.IncorrectDataSpecs;
import com.example.teamcity.api.validation.SearchValidator;
import io.qameta.allure.Feature;
import io.qameta.allure.Story;
import io.restassured.response.Response;
import org.testng.annotations.Test;

import java.util.ArrayList;
import java.util.List;
@Feature("Project Search")
@Test(groups = {"Regression", "Search"})
public class ProjectSearchTest extends BaseApiTest {

    // =================== TODO TESTS FOR PROJECT NAME SEARCH (should return 1 project) =================== //

    // =================== SEARCH BY NAME TESTS (PROJECT_SEARCH_NAME_TAG) =================== //
  // =================== LOCATOR-BASED SEARCH =================== //
    @Story("User should be able to find a project by its exact name")
    @Test(description = "User should be able to find a project by its exact name", groups = {"Positive", "PROJECT_SEARCH_NAME_TAG", "LOCATOR_BASED_SEARCH"})
    public void userShouldBeAbleToFindProjectByNameTest() {
        String projectName = RandomData.getUniqueName();
        Project createdProject = ProjectHelper.createProject(userCheckedRequest, TestDataGenerator.generate(Project.class, RandomData.getUniqueId(), projectName));
        Project foundProject = ProjectHelper.findProjectByLocator(userCheckedRequest, "name", createdProject.getName());
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
        Project createdProject = ProjectHelper.createProject(userCheckedRequest, TestDataGenerator.generate(Project.class, RandomData.getUniqueId(), multiWordName));
        Project foundProject = ProjectHelper.findProjectByLocator(userCheckedRequest, "name", createdProject.getName());
        SearchValidator.validateSearchResult(createdProject, foundProject, "Project", "name", List.of("parentProject"), softy);
    }

    @Story("Search by Name with Special Characters")
    @Test(description = "User should be able to find a project by name containing special characters", groups = {"Positive", "PROJECT_SEARCH_NAME_TAG", "LOCATOR_BASED_SEARCH"})
    public void userShouldBeAbleToFindProjectByNameWithSpecialCharactersTest() {
        String specialCharName = TestConstants.SPECIAL_CHARACTERS + RandomData.getString();
        Project createdProject = ProjectHelper.createProject(userCheckedRequest, TestDataGenerator.generate(Project.class, RandomData.getUniqueId(), specialCharName));
        Project foundProject = ProjectHelper.findProjectByLocator(userCheckedRequest, "name", createdProject.getName());
        SearchValidator.validateSearchResult(createdProject, foundProject, "Project", "name", List.of("parentProject"), softy);
    }

    @Story("Search by maximum allowed name length")
    @Test(description = "User should be able to search for a project with the maximum allowed name length", groups = {"Positive", "PROJECT_SEARCH_NAME_TAG", "LOCATOR_BASED_SEARCH"})
    public void userShouldBeAbleToSearchProjectByMaxLengthNameTest() {
        String longName = RandomData.getString(500);
        Project createdProject = ProjectHelper.createProject(userCheckedRequest, TestDataGenerator.generate(Project.class, RandomData.getUniqueId(), longName));
        Project foundProject = ProjectHelper.findProjectByLocator(userCheckedRequest, "name", createdProject.getName());
        SearchValidator.validateSearchResult(createdProject, foundProject, "Project", "name", List.of("parentProject"), softy);
    }

    @Story("Search by single character name")
    @Test(description = "User should be able to find a project by a name consisting of a single character", groups = {"Positive", "PROJECT_SEARCH_NAME_TAG", "LOCATOR_BASED_SEARCH"})
    public void userShouldBeAbleToFindProjectBySingleCharacterNameTest() {
        String oneCharName = RandomData.getString(1);
        Project createdProject = ProjectHelper.createProject(userCheckedRequest, TestDataGenerator.generate(Project.class, RandomData.getUniqueId(), oneCharName));
        Project foundProject = ProjectHelper.findProjectByLocator(userCheckedRequest, "name", createdProject.getName());
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
        String upperCasedName = originalName.toUpperCase();
        ProjectHelper.createProject(userCheckedRequest, TestDataGenerator.generate(Project.class, RandomData.getUniqueId(), originalName));
        Response response = userUncheckedRequest.getRequest(ApiEndpoint.PROJECTS).findFirstEntityByLocatorQuery("name:" + upperCasedName);
        response.then().spec(IncorrectDataSpecs.emptyEntityListReturned("Project", "name", upperCasedName));
        softy.assertAll();
    }

    @Story("Search by localized name")
    @Test(description = "User should be able to find a project by its localized name", groups = {"Positive", "PROJECT_SEARCH_NAME_TAG", "LOCATOR_BASED_SEARCH"})
    public void userShouldBeAbleToFindProjectByLocalizedNameTest() {
        String localizedName = TestConstants.LOCALIZATION_CHARACTERS;
        Project createdProject = ProjectHelper.createProject(userCheckedRequest, TestDataGenerator.generate(Project.class, RandomData.getString(), localizedName));
        Project foundProject = ProjectHelper.findProjectByLocator(userCheckedRequest, "name", createdProject.getName());
        SearchValidator.validateSearchResult(createdProject, foundProject, "Project", "name", List.of("parentProject"), softy);
    }

    @Story("Search by digits-only name")
    @Test(description = "User should be able to find a project by a name consisting only of digits", groups = {"Positive", "PROJECT_SEARCH_NAME_TAG", "LOCATOR_BASED_SEARCH"})
    public void userShouldBeAbleToFindProjectByDigitsOnlyNameTest() {
        String digitsOnlyName = RandomData.getDigits(6);
        Project createdProject = ProjectHelper.createProject(userCheckedRequest, TestDataGenerator.generate(Project.class, RandomData.getUniqueId(), digitsOnlyName));
        Project foundProject = ProjectHelper.findProjectByLocator(userCheckedRequest, "name", createdProject.getName());
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
        Project createdProject = ProjectHelper.createProject(userCheckedRequest, TestDataGenerator.generate(Project.class, ProjectTestData.projectId(), nameWithTrailingSpace));
        Project foundProject = ProjectHelper.findProjectByLocator(userCheckedRequest, "name", createdProject.getName());
        SearchValidator.validateSearchResult(createdProject, foundProject, "Project", "name", List.of("parentProject"), softy);
    }

    @Story("Search by name with leading space")
    @Test(description = "User should be able to find a project by name that starts with a space", groups = {"Positive", "PROJECT_SEARCH_NAME_TAG", "LOCATOR_BASED_SEARCH"})
    public void userShouldBeAbleToFindProjectByNameWithLeadingSpaceTest() {
        String nameWithLeadingSpace = " " + RandomData.getUniqueName();
        Project createdProject = ProjectHelper.createProject(userCheckedRequest, TestDataGenerator.generate(Project.class, ProjectTestData.projectId(), nameWithLeadingSpace));
        Project foundProject = ProjectHelper.findProjectByLocator(userCheckedRequest, "name", createdProject.getName());
        SearchValidator.validateSearchResult(createdProject, foundProject, "Project", "name", List.of("parentProject"), softy);
    }

    @Story("Search by partially matching words")
    @Test(description = "User should not be able to find a project by partially matching name (e.g., using space instead of dash)", groups = {"Negative", "PROJECT_SEARCH_NAME_TAG", "LOCATOR_BASED_SEARCH"})
    public void userShouldNotBeAbleToFindProjectByPartiallyMatchingWordsTest() {
        String actualProjectName = "Test-Project-" + RandomData.getString();
        String searchQuery = actualProjectName.replace("-", " ");
        Response response = userUncheckedRequest.getRequest(ApiEndpoint.PROJECTS).findFirstEntityByLocatorQuery("name:" + searchQuery);
        response.then().spec(IncorrectDataSpecs.emptyEntityListReturned("Project", "name", searchQuery));
        softy.assertAll();
    }

    @Story("Search by name for deeply nested project")
    @Test(description = "User should be able to find a project by name when it's deeply nested in hierarchy", groups = {"Positive", "PROJECT_SEARCH_NAME_TAG", "LOCATOR_BASED_SEARCH"})
    public void userShouldBeAbleToFindDeeplyNestedProjectByNameTest() {
        String nestedProjectName = "DeepNested_" + RandomData.getString();
        List<Project> nestedProjects = ProjectTestData.nestedProjects(20);
        Project deepestProject = nestedProjects.get(nestedProjects.size() - 1);
        deepestProject.setName(nestedProjectName);
        nestedProjects.get(0).setParentProject(null);
        ProjectHelper.createNestedProjects(userCheckedRequest, nestedProjects);
        Project foundProject = ProjectHelper.findProjectByLocator(userCheckedRequest, "name", nestedProjectName);
        SearchValidator.validateSearchResult(deepestProject, foundProject, "Project", "name", List.of("parentProject"), softy);
        softy.assertAll();
    }

    //  Bug in API, part search is not implemented as defined in API doc
    @Story("Search with pagination using count and start parameters")
    @Test(description = "User should be able to find exactly 2 projects when using count=2 and start=0", groups = {"Positive", "PROJECT_SEARCH_NAME_TAG", "LOCATOR_BASED_SEARCH"})
    public void userShouldBeAbleToFindTwoProjectsWithCountAndStartTest() {
        String namePrefix = "PaginationTest_";
        List<Project> projectsToCreate = ProjectTestData.createProjectsWithPrefixAndNumericSuffix(3, namePrefix + RandomData.getString(5));
        List<Project> savedProjects = ProjectHelper.createProjects(userCheckedRequest, projectsToCreate);
        List<Project> foundProjects = ProjectHelper.findProjectsByLocatorWithPagination(userCheckedRequest, "name:" + namePrefix, 2, 0);
        softy.assertEquals(foundProjects.size(), 2, "Expected exactly 2 projects");
        SearchValidator.validateSearchResults(List.of(savedProjects.get(0), savedProjects.get(1)), foundProjects, "Project", "name", List.of("parentProject"), softy);
        softy.assertAll();
    }

    //  Bug in API, part search is not implemented as defined in API doc
    @Story("Search with pagination using count and start parameters")
    @Test(description = "User should be able to find the second project when using count=1 and start=1", groups = {"Positive", "PROJECT_SEARCH_NAME_TAG", "LOCATOR_BASED_SEARCH"})
    public void userShouldBeAbleToFindSecondProjectWithCountAndStartTest() {
        String namePrefix = "PaginationTest_";
        List<Project> projectsToCreate = ProjectTestData.createProjectsWithPrefixAndNumericSuffix(3, namePrefix + RandomData.getString(5));
        List<Project> savedProjects = ProjectHelper.createProjects(userCheckedRequest, projectsToCreate);
        List<Project> foundProjects = ProjectHelper.findProjectsByLocatorWithPagination(userCheckedRequest, "name:" + namePrefix, 1, 1);
        softy.assertEquals(foundProjects.size(), 1, "Expected exactly 1 project");
        softy.assertEquals(foundProjects.get(0).getId(), savedProjects.get(1).getId(), "Expected the second project to be returned");
        SearchValidator.validateSearchResult(savedProjects.get(1), foundProjects.get(0), "Project", "name", List.of("parentProject"), softy);
        softy.assertAll();
    }
    @Story("Search with pagination using count and start parameters")
    @Test(description = "User should get an empty list when using count=0 and start=0", groups = {"Positive", "PROJECT_SEARCH_NAME_TAG", "LOCATOR_BASED_SEARCH"})
    public void userShouldGetEmptyListWithCountAndStartTest() {
        String namePrefix = "PaginationTest_";
        List<Project> projectsToCreate = ProjectTestData.createProjectsWithPrefixAndNumericSuffix(3, namePrefix + RandomData.getString(5));
        ProjectHelper.createProjects(userCheckedRequest, projectsToCreate);
        List<Project> foundProjects = ProjectHelper.findProjectsByLocatorWithPagination(userCheckedRequest, "name:" + namePrefix, 0, 0);
        softy.assertEquals(foundProjects.size(), 0, "Expected an empty list but received non-empty list");
        softy.assertAll();
    }












    // =================== LOCATOR-BASED SEARCH =================== //
    // =================== SEARCH BY NAME TESTS (PROJECT_SEARCH_TAG) =================== //
}

// =================== MISSING LOCATOR-BASED SEARCH TESTS =================== //

// ✅ Positive cases:


// TODO: Поиск с count, превышающим общее количество сущностей → убедиться, что возвращаются все без ошибки

// TODO: Поиск с count=0 → убедиться, что возвращается пустой список, но без ошибки

// TODO: Проверка метода readEntitiesQueryWithPagination — возвращает все сущности в пределах лимита

// TODO: Проверка метода readEntitiesQueryWithPagination(limit, offset) — корректная работа пагинации

// 🚫 Negative cases:

// TODO: Поиск по name:nonExistingValue через findEntitiesByLocatorQueryWithPagination → убедиться, что возвращается пустой список

// TODO: Поиск по name: (пустое значение) через findEntitiesByLocatorQueryWithPagination → убедиться, что возвращается пустой список

// TODO: Поиск с count=-1 и start=-1 → убедиться, что возвращается ошибка валидации (400 Bad Request)

// TODO: Поиск с name: " " (один пробел) через findEntitiesByLocatorQueryWithPagination → убедиться, что результат пустой

// TODO: Поиск без параметра locator вовсе (т.е., ?locator=) → убедиться, что API возвращает ошибку или пустой результат

// ⚠️ Tech gap:

// TODO: Задействовать findEntitiesByLocatorQueryWithPagination и readEntitiesQueryWithPagination в тестах (сейчас везде используется только findFirstEntityByLocatorQuery)

// =================== END OF MISSING LOCATOR-BASED SEARCH TESTS =================== //


