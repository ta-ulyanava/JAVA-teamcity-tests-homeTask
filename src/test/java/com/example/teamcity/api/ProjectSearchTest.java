package com.example.teamcity.api;

import com.example.teamcity.api.constants.TestConstants;
import com.example.teamcity.api.enums.ApiEndpoint;
import com.example.teamcity.api.generators.RandomData;
import com.example.teamcity.api.generators.TestData;
import com.example.teamcity.api.generators.TestDataGenerator;
import com.example.teamcity.api.models.Project;
import com.example.teamcity.api.spec.responce.IncorrectDataSpecs;
import com.example.teamcity.api.validation.EntityValidator;
import io.qameta.allure.Feature;
import io.qameta.allure.Story;
import io.restassured.response.Response;
import org.testng.annotations.Test;

import java.util.List;
import java.util.stream.Collectors;

public class ProjectSearchTest extends BaseApiTest {

    // =================== SEARCH BY NAME TESTS (PROJECT_SEARCH_TAG) =================== //
    @Feature("Project Search")
    @Story("User should be able to find a project by its exact name")
    @Test(description = "User should be able to find a project by its exact name", groups = {"Positive", "Search", "PROJECT_SEARCH_TAG"})
    public void userShouldBeAbleToFindProjectByNameTest() {
        Project createdProject = createProjectAndExtractModel(testData.getProject());
        Project foundProject = findSingleProjectByLocator("name", createdProject.getName());
        softy.assertNotNull(foundProject, "Project with name '" + createdProject.getName() + "' was not found");
        EntityValidator.validateAllEntityFieldsIgnoring(createdProject, foundProject, List.of("parentProject"), softy);
        softy.assertAll();
    }

    @Feature("Project Search")
    @Story("User should not be able to find a project by a non-existing name")
    @Test(description = "User should not be able to find a project by a non-existing name",
            groups = {"Negative", "Search", "PROJECT_SEARCH_TAG"})
    public void userShouldNotBeAbleToFindProjectByNonExistingNameTest() {
        String nonExistingProjectName = RandomData.getUniqueName();
        Response response = userUncheckedRequest.getRequest(ApiEndpoint.PROJECTS).findSingleByLocator("name:" + nonExistingProjectName);
        response.then().spec(IncorrectDataSpecs.emptyEntityListReturned("Project", "name", nonExistingProjectName));
        softy.assertAll();
    }

    @Feature("Project Search")
    @Story("User should be able to find a project by multiple words in its name")
    @Test(description = "User should be able to find a project by its name containing multiple words",
            groups = {"Positive", "Search", "PROJECT_SEARCH_TAG"})
    public void userShouldBeAbleToFindProjectByMultiWordNameTest() {
        String multiWordName = "Test Project " + RandomData.getString();
        String validId = TestData.projectId();
        Project project = TestDataGenerator.generate(Project.class, validId, multiWordName);
        Project createdProject = createProjectAndExtractModel(project);
        Project foundProject = findSingleProjectByLocator("name", createdProject.getName());
        softy.assertNotNull(foundProject, "Project with name '" + createdProject.getName() + "' was not found");
        EntityValidator.validateAllEntityFieldsIgnoring(createdProject, foundProject, List.of("parentProject"), softy);
        softy.assertAll();
    }
    @Feature("Project Search")
    @Story("Search by Name with Special Characters")
    @Test(description = "User should be able to find a project by name containing special characters",
            groups = {"Positive", "Search", "PROJECT_SEARCH_TAG"})
    public void userShouldBeAbleToFindProjectByNameWithSpecialCharactersTest() {
        String specialCharName = TestConstants.SPECIAL_CHARACTERS + RandomData.getString();
        String validId = TestData.projectId();
        Project project = TestDataGenerator.generate(Project.class, validId, specialCharName);
        Project createdProject = createProjectAndExtractModel(project);
        Project foundProject = findSingleProjectByLocator("name", createdProject.getName());
        softy.assertNotNull(foundProject, "Project with name '" + createdProject.getName() + "' was not found");
        EntityValidator.validateAllEntityFieldsIgnoring(createdProject, foundProject, List.of("parentProject"), softy);
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

 * 4. **Поиск с учетом спецсимволов**
 *    - Поиск проекта, содержащего спецсимволы (например, `-`, `_`, `.`) → найден проект.
 *    - `/app/rest/projects?locator=name:Test-Project_2025`
 *
 * 5. **Пустой запрос (все проекты)**
 *    - Отправка запроса без указания имени → возвращаются все проекты.
 *    - `/app/rest/projects`
 *
 * ### **Негативные кейсы:**
 * 6. **Поиск по несуществующему имени**
 *    - Поиск проекта с именем, которого нет в системе → ошибка `404` или пустой список.
 *    - `/app/rest/projects?locator=name:NonExistingProject`
 *
 * 7. **Поиск с спецсимволами, не поддерживаемыми API**
 *    - Поиск проекта с `?`, `*`, `@` в имени → проверяем поведение (ошибка или экранирование).
 *    - `/app/rest/projects?locator=name:?InvalidProjectName`
 *
 * 8. **Поиск с максимальной длиной строки**
 *    - Поиск по очень длинному имени (например, 255+ символов) → проверяем корректность обработки.
 *    - `/app/rest/projects?locator=name:VeryLongProjectNameWithMaxCharactersAllowed`
 *
 * 9. **Поиск по пустой строке в параметре**
 *    - Запрос `name:` без значения → проверяем, как API обрабатывает такой ввод.
 *    - `/app/rest/projects?locator=name:`
 *
 * 10. **Поиск по SQL-инъекции (безопасность)**
 *    - Проверяем, что API не выполняет SQL-запрос, а обрабатывает его как строку.
 *    - `/app/rest/projects?locator=name:1 OR 1=1`
 *
 * 11. **Поиск с XSS-инъекцией (безопасность)**
 *    - Проверяем, что API не исполняет переданный скрипт, а обрабатывает как текст.
 *    - `/app/rest/projects?locator=name:<script>alert(1)</script>`
 *
 * 12. **Поиск с различными регистрами букв**
 *    - Проверяем, учитывает ли поиск регистр букв (чувствительный или нет).
 *    - `/app/rest/projects?locator=name:TESTPROJECT`
 *
 * 13. **Поиск по частично совпадающим словам**
 *    - Проверяем, если запрос "Test Project", а в системе "Test-Project", API должен вернуть результат или нет.
 *    - `/app/rest/projects?locator=name:Test Project`
 *
 * Этот чеклист покроет как функциональные, так и негативные сценарии, включая проверку безопасности.
 */