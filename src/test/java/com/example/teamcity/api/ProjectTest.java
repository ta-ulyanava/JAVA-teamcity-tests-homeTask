package com.example.teamcity.api;

import com.example.teamcity.BaseTest;
import com.example.teamcity.api.controllers.ProjectController;
import com.example.teamcity.api.enums.ApiEndpoint;
import com.example.teamcity.api.enums.Role;
import com.example.teamcity.api.generators.RandomData;
import com.example.teamcity.api.generators.TestDataGenerator;
import com.example.teamcity.api.models.*;
import com.example.teamcity.api.responses.ResponseExtractor;
import com.example.teamcity.api.validation.EntityValidator;
import com.example.teamcity.api.spec.RequestSpecifications;
import com.example.teamcity.api.spec.ResponseSpecifications;
import io.restassured.response.Response;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

import java.util.List;

import static com.example.teamcity.api.generators.TestDataGenerator.generate;

@Test(groups = {"Regression"})
public class ProjectTest extends BaseTest {

    private ProjectController projectController;

    @BeforeMethod(alwaysRun = true)
    public void setup() {
        super.beforeTest();
        superUserCheckRequests.getRequest(ApiEndpoint.USERS).create(testData.getUser());
        projectController = new ProjectController(RequestSpecifications.authSpec(testData.getUser()));
    }

    @DataProvider(name = "invalidSpecialCharactersForId")
    public static Object[][] invalidSpecialCharactersForId() {
        return "!@#$%^&*()+-={}[]:\\".chars()
                .mapToObj(c -> new Object[]{String.valueOf((char) c)})
                .toArray(Object[][]::new);
    }

    @DataProvider
    public static Object[][] nonLatinIdProviderForId() {
        return new Object[][]{{"проект"}, {"项目"}, {"プロジェクト"}, {"مشروع"}, {"παράδειγμα"}, {"नमूना"}, {"בדיקה"}};
    }

    @DataProvider
    public static Object[][] invalidIdStartId() {
        return new Object[][]{
                {"_invalidId"},
                {"1invalidId"},
        };
    }


    @Test(description = "User should be able to create a project with the minimum required fields under Root project", groups = {"Positive", "CRUD"})
    public void userCreatesProjectWithMandatoryFieldsOnlyTest() {
        var response = projectController.createProject(testData.getProject());
        response.then().spec(ResponseSpecifications.checkSuccess()); // Ожидаем 200 OK
        var createdProject = ResponseExtractor.extractModel(response, Project.class);
        softy.assertEquals(createdProject.getParentProject().getId(), "_Root", "Parent project should be '_Root' when not specified");
        softy.assertAll();
    }

    //Bug in API
    @Test(description = "User should be able to create a Project with copyAllAssociatedSettings set to true and verify copied fields",
            groups = {"Positive", "CRUD", "KnownBugs"})
    public void userCreatesProjectWithCopyAllAssociatedSettingsTrueTest() {
        var sourceProject = generate(List.of(), Project.class, RandomData.getString(), RandomData.getString(), new ParentProject("_Root", null));
        var createdSourceProject = projectController.createAndReturnProject(sourceProject);
        var newProject = generate(List.of(), Project.class, RandomData.getString(), RandomData.getString(), new ParentProject("_Root", null), true, createdSourceProject);
        var createdProject = projectController.createAndReturnProject(newProject);
        softy.assertEquals(createdProject, newProject);
        softy.assertAll();
    }

    @Test(description = "User should be able to create a Project with copyAllAssociatedSettings set to false and verify fields are NOT copied", groups = {"Positive", "CRUD"})
    public void userCreatesProjectWithCopyAllAssociatedSettingsFalseTest() {
        var sourceProject = generate(List.of(), Project.class, RandomData.getString(), RandomData.getString(), new ParentProject("_Root", null));
        var createdSourceProject = projectController.createAndReturnProject(sourceProject);
        var newProject = generate(List.of(), Project.class, RandomData.getString(), RandomData.getString(), new ParentProject("_Root", null), false, createdSourceProject);
        var createdProject = projectController.createAndReturnProject(newProject);
        softy.assertEquals(createdProject, newProject);
        softy.assertAll();
    }
    // Need to fix bug
    @DataProvider(name = "invalidCopySettings")
    public static Object[][] invalidCopySettings() {
        return new Object[][]{
                {"123"},
                {"null"},
                {"\"yes\""},
                {"\"no\""},
                {"{}"}
        };
    }

    @Test(description = "User should not be able to create Project with invalid copyAllAssociatedSettings",
            groups = {"Negative", "CRUD", "KnownBugs"},
            dataProvider = "invalidCopySettings")
    public void userCannotCreateProjectWithInvalidCopySettingsTest(Object invalidValue) {
        String invalidProjectJson = """
            {
                "id": "%s",
                "name": "%s",
                "parentProject": { "id": "_Root" },
                "copyAllAssociatedSettings": %s
            }
            """.formatted(RandomData.getString(), RandomData.getString(), invalidValue);
        projectController.createInvalidProjectFromString(invalidProjectJson)
                .then().spec(ResponseSpecifications.checkInvalidCopySettings());
    }
    @DataProvider(name = "projectCreationScenarios")
    public static Object[][] projectCreationScenarios() {
        return new Object[][]{{true, 20}, {false, 20}};
    }

    @Test(description = "User should be able to create multiple nested or sibling projects",
            dataProvider = "projectCreationScenarios", groups = {"Positive", "CRUD", "CornerCase"})
    public void userCreatesMultipleProjectsTest(boolean isNested, int projectCount) {
        var rootProject = generate(List.of(), Project.class, RandomData.getString(), RandomData.getString(), new ParentProject("_Root", null));
        var createdRootProject = projectController.createAndReturnProject(rootProject);
        List<Project> projects = isNested
                ? projectController.createNestedProjects(createdRootProject.getId(), projectCount)
                : projectController.createSiblingProjects(createdRootProject.getId(), projectCount);

        softy.assertEquals(projects.size(), projectCount, "The number of created projects is incorrect");

        projects.forEach(project -> {
            if (isNested && projects.indexOf(project) > 0) {
                softy.assertEquals(
                        project.getParentProject().getId(),
                        projects.get(projects.indexOf(project) - 1).getId(),
                        "Parent project ID is incorrect for project " + project.getId()
                );
            } else {
                softy.assertEquals(
                        project.getParentProject().getId(),
                        createdRootProject.getId(),
                        "Parent project ID is incorrect for project " + project.getId()
                );
            }
        });

        softy.assertAll();
    }

    @Test(description = "User should not be able to create a Project with a non-existent parentProject locator",
            groups = {"Negative", "CRUD"})
    public void userCannotCreateProjectWithNonExistentParentProjectTest() {
        var invalidProject = generate(List.of(), Project.class, RandomData.getString(), RandomData.getString(), new ParentProject("non_existent_locator", null));
        var response = projectController.createInvalidProjectFromProject(invalidProject);
        response.then().spec(ResponseSpecifications.checkProjectNotFound("non_existent_locator")); // Ожидаем 404
        softy.assertAll();
    }


    @Test(description = "User should not be able to create a Project with the same ID as its parent ID", groups = {"Negative", "CRUD"})
    public void userCannotCreateProjectWithSameParentIdTest() {
        var projectId = testData.getProject().getId();
        var invalidProject = generate(List.of(), Project.class, projectId, RandomData.getString(), new ParentProject(projectId, null));
        var response = projectController.createInvalidProjectFromProject(invalidProject);
        response.then().spec(ResponseSpecifications.checkProjectNotFound(projectId));
        softy.assertAll();
    }
    /****/
    @Test(description = "User should be able to create a Project with a name of 500 characters", groups = {"Positive", "CRUD", "CornerCase"})
    public void userCreatesProjectWith500LengthNameTest() {
        var maxLengthName = "A".repeat(500);
        var validProject = generate(List.of(), Project.class, RandomData.getString(), maxLengthName);
        var response = projectController.createProject(validProject);
        response.then().spec(ResponseSpecifications.checkSuccess());
        softy.assertAll();
    }
    /****/

    @Test(description = "User should be able to create a Project with a name of length 1", groups = {"Positive", "CRUD"})
    public void userCreatesProjectWithOneCharacterNameTest() {
        var validProject = generate(List.of(), Project.class, RandomData.getString(), "A");
        var response = projectController.createProject(validProject);
        response.then().spec(ResponseSpecifications.checkSuccess());
        softy.assertAll();
    }


    @Test(description = "User should be able to create a Project with an ID of maximum allowed length", groups = {"Positive", "CRUD"})
    public void userCreatesProjectWithMaxLengthIdTest() {
        var maxLengthId = "A".repeat(225);
        var validProject = generate(List.of(), Project.class, maxLengthId, RandomData.getString());
        var response = projectController.createProject(validProject);
        response.then().spec(ResponseSpecifications.checkSuccess());
        softy.assertAll();
    }

    @Test(description = "User should be able to create a Project with an ID of length 1", groups = {"Positive", "CRUD"})
    public void userCreatesProjectWithOneCharacterIdTest() {
        var validProject = generate(List.of(), Project.class, "A", RandomData.getString());
        var response = projectController.createProject(validProject);
        response.then().spec(ResponseSpecifications.checkSuccess());
        softy.assertAll();
    }



    //To fix 500 (Internal Server Error).
    @Test(description = "User should not be able to create a Project with an ID longer than 225 characters",
            groups = {"Negative", "CRUD", "KnownBugs", "CornerCase"})
    public void userCannotCreateProjectWithTooLongIdTest() {
        var tooLongId = "A".repeat(226);
        var invalidProject = generate(List.of(), Project.class, tooLongId, RandomData.getString());
        var response = projectController.createInvalidProjectFromProject(invalidProject);
        response.then().spec(ResponseSpecifications.checkProjectIdTooLong(225));
        softy.assertAll();
    }



    @Test(description = "User should be able to create a Project with special characters in name",
            groups = {"Positive", "CRUD"})
    public void userCreatesProjectWithSpecialCharactersInNameTest() {
        var project = generate(List.of(), Project.class, RandomData.getString(), RandomData.getFullSpecialCharacterString());
        var response = projectController.createProject(project);
        response.then().spec(ResponseSpecifications.checkBadRequest());
        var createdProject = ResponseExtractor.extractModel(response, Project.class);
        EntityValidator.validateEntityFields(project, createdProject, softy);
        softy.assertAll();
    }

@Test(description = "User should be able to create a Project with a localized name",
        groups = {"Positive", "CRUD"})
public void userCreatesProjectWithLocalizedNameTest() {
    var localizedProject = generate(List.of(), Project.class, RandomData.getString(), RandomData.getFullLocalizationString());
    var response = projectController.createProject(localizedProject);
    response.then().spec(ResponseSpecifications.checkSuccess());
    var createdProject = ResponseExtractor.extractModel(response, Project.class);
    EntityValidator.validateEntityFields(localizedProject, createdProject, softy);
    softy.assertAll();
}

@Test(description = "User should be able to create a Project with an ID containing an underscore",
        groups = {"Positive", "CRUD"})
public void userCreatesProjectWithUnderscoreInIdTest() {
    var projectWithUnderscore = generate(List.of(), Project.class, RandomData.getString() + "_test", RandomData.getString());
    var response = projectController.createProject(projectWithUnderscore);
    response.then().spec(ResponseSpecifications.checkSuccess());
    var createdProject = ResponseExtractor.extractModel(response, Project.class);
    EntityValidator.validateEntityFields(projectWithUnderscore, createdProject, softy);
    softy.assertAll();
}


// Need to fix 500 server Error
    @Test(description = "User should not be able to create a Project with special characters in ID",
            groups = {"Negative", "CRUD", "KnownBugs"}, dataProvider = "invalidSpecialCharactersForId")
    public void userCannotCreateProjectWithEachSpecialCharacterInIdTest(String specialChar) {
        var invalidId = "test_" + specialChar;
        var invalidProject = TestDataGenerator.generate(List.of(), Project.class, invalidId, "ValidName");
        var response = projectController.createInvalidProjectFromProject(invalidProject);
        response.then().spec(ResponseSpecifications.checkInvalidProjectId());
        softy.assertAll();
    }


    // Need to fix 500 server Error
    @Test(description = "User should not be able to create a Project with a non-Latin ID",
            groups = {"Negative", "CRUD", "KnownBugs"}, dataProvider = "nonLatinIdProviderForId")
    public void userCannotCreateProjectWithNonLatinIdTest(String invalidId) {
        var invalidProject = TestDataGenerator.generate(List.of(), Project.class, invalidId, RandomData.getString());
        var response = projectController.createInvalidProjectFromProject(invalidProject);
        response.then().spec(ResponseSpecifications.checkInvalidProjectId());
        softy.assertAll();
    }


    @Test(description = "User should not be able to create Project with empty name", groups = {"Negative", "CRUD"})
    public void userCannotCreateProjectWithEmptyNameTest() {
        var invalidProject = TestDataGenerator.generate(List.of(), Project.class, RandomData.getString(), "");
        var response = projectController.createInvalidProjectFromProject(invalidProject);
        response.then().spec(ResponseSpecifications.checkBadRequest());
        softy.assertTrue(response.asString().contains("Project name cannot be empty"));
        softy.assertAll();
    }


    //Need to fix 500 error
    @Test(description = "User should not be able to create Project with empty id", groups = {"Negative", "CRUD", "KnownBugs"})
    public void userCannotCreateProjectWithEmptyIdTest() {
        var invalidProject = TestDataGenerator.generate(List.of(), Project.class, "", RandomData.getString());
        var response = projectController.createInvalidProjectFromProject(invalidProject);
        response.then().spec(ResponseSpecifications.checkBadRequest());
        softy.assertTrue(response.asString().contains("Project ID must not be empty."));
        softy.assertAll();
    }
    //Need to fix 500 error
    @Test(description = "User should not be able to create a Project with a space as ID", groups = {"Negative", "CRUD", "KnownBugs"})
    public void userCannotCreateProjectWithSpaceAsIdTest() {
        var invalidProject = TestDataGenerator.generate(List.of(), Project.class, " ", RandomData.getString());
        var response = projectController.createInvalidProjectFromProject(invalidProject);
        response.then().spec(ResponseSpecifications.checkBadRequest());
        softy.assertTrue(response.asString().contains("Project ID must not be empty"));
        softy.assertAll();
    }
    //Need to fix 500 error
    @Test(description = "User should not be able to create a Project with a space as name", groups = {"Negative", "CRUD", "KnownBugs"})
    public void userCannotCreateProjectWithSpaceAsNameTest() {
        var invalidProject = TestDataGenerator.generate(List.of(), Project.class, RandomData.getString(), " ");
        var response = projectController.createInvalidProjectFromProject(invalidProject);
        response.then().spec(ResponseSpecifications.checkBadRequest());
        softy.assertTrue(response.asString().contains("Given project name is empty"));
        softy.assertAll();
    }



    @Test(description = "User should not be able to create a Project with an existing ID", groups = {"Negative", "CRUD"})
    public void userCannotCreateProjectWithExistingIdTest() {
        var existingProject = projectController.createAndReturnProject(testData.getProject());
        var duplicateProject = TestDataGenerator.generate(List.of(existingProject), Project.class, existingProject.getId(), RandomData.getString());
        var response = projectController.createInvalidProjectFromProject(duplicateProject);
        response.then().spec(ResponseSpecifications.checkProjectWithIdAlreadyExists(existingProject.getId()));
        softy.assertAll();
    }

    @Test(description = "User should not be able to create a Project with an existing name", groups = {"Negative", "CRUD"})
    public void userCannotCreateProjectWithExistingNameTest() {
        var existingProject = projectController.createAndReturnProject(testData.getProject());
        var duplicateProject = TestDataGenerator.generate(List.of(existingProject), Project.class, RandomData.getString(), existingProject.getName());
        var response = projectController.createInvalidProjectFromProject(duplicateProject);
        response.then().spec(ResponseSpecifications.checkProjectWithNameAlreadyExists(existingProject.getName()));
        softy.assertAll();
    }

    @Test(description = "User should not be able to create a Project with an existing name in a different case", groups = {"Negative", "CRUD"})
    public void userCannotCreateProjectWithExistingNameDifferentCaseTest() {
        var existingProject = projectController.createAndReturnProject(testData.getProject());
        var duplicateName = existingProject.getName().toUpperCase();
        var duplicateProject = TestDataGenerator.generate(List.of(), Project.class, RandomData.getString(), duplicateName);
        var response = projectController.createInvalidProjectFromProject(duplicateProject);
        response.then().spec(ResponseSpecifications.checkProjectWithNameAlreadyExists(duplicateName));
        softy.assertAll();
    }

    @Test(description = "User should not be able to create a Project with an existing ID in a different case", groups = {"Negative", "CRUD"})
    public void userCannotCreateProjectWithExistingIdDifferentCaseTest() {
        var existingProject = projectController.createAndReturnProject(testData.getProject());
        var duplicateId = existingProject.getId().toUpperCase();
        var duplicateProject = TestDataGenerator.generate(List.of(), Project.class, duplicateId, RandomData.getString());
        var response = projectController.createInvalidProjectFromProject(duplicateProject);
        response.then().spec(ResponseSpecifications.checkProjectWithIdAlreadyExists(duplicateId));
        softy.assertAll();
    }



    //Need to fix 500 error
    @Test(description = "User should not be able to create a Project with an ID consisting only of digits", groups = {"Negative", "CRUD", "KnownBugs"})
    public void userCannotCreateProjectWithDigitsOnlyIdTest() {
        var invalidProject = TestDataGenerator.generate(List.of(), Project.class, "123456", RandomData.getString());
        var response = projectController.createInvalidProjectFromProject(invalidProject);
        response.then().spec(ResponseSpecifications.checkInvalidProjectId());
        softy.assertAll();
    }
    //Need to fix 500 error
    @Test(description = "User should not be able to create a Project with an ID starting with an underscore or a digit", groups = {"Negative", "CRUD", "KnownBugs"}, dataProvider = "invalidIdStartId")
    public void userCannotCreateProjectWithInvalidStartingCharacterIdTest(String invalidId) {
        var invalidProject = TestDataGenerator.generate(List.of(), Project.class, invalidId, RandomData.getString());
        var response = projectController.createInvalidProjectFromProject(invalidProject);
        response.then().spec(ResponseSpecifications.checkInvalidProjectId());
        softy.assertAll();
    }
    //Need to fix 500 error
    @Test(description = "User should not be able to create a Project with spaces in the middle of the ID", groups = {"Negative", "CRUD", "KnownBugs"})
    public void userCannotCreateProjectWithSpacesInIdTest() {
        var invalidProject = TestDataGenerator.generate(List.of(), Project.class, "invalid id", RandomData.getString());
        var response = projectController.createInvalidProjectFromProject(invalidProject);
        response.then().spec(ResponseSpecifications.checkInvalidProjectId());
        softy.assertAll();
    }

    @Test(description = "User should be able to create a Project with an ID containing Latin letters, digits, and underscores", groups = {"Positive", "CRUD"})
    public void userCreatesProjectWithValidIdCharactersTest() {
        var validProject = TestDataGenerator.generate(List.of(), Project.class, "valid_123_ID", RandomData.getString());
        var response = projectController.createProject(validProject);
        response.then().spec(ResponseSpecifications.checkBadRequest());
        var createdProject = ResponseExtractor.extractModel(response, Project.class);
        EntityValidator.validateEntityFields(validProject, createdProject, softy);
        softy.assertAll();
    }

    @Test(description = "User should be able to create a Project with a name consisting only of digits", groups = {"Positive", "CRUD"})
    public void userCreatesProjectWithDigitsOnlyNameTest() {
        var validProject = TestDataGenerator.generate(List.of(), Project.class, RandomData.getString(), "123456");
        var response = projectController.createProject(validProject);
        response.then().spec(ResponseSpecifications.checkSuccess());
        var createdProject = ResponseExtractor.extractModel(response, Project.class);
        EntityValidator.validateEntityFields(validProject, createdProject, softy);
        softy.assertAll();
    }

    @Test(description = "User should be able to create a Project with spaces in the middle of the name", groups = {"Positive", "CRUD"})
    public void userCreatesProjectWithSpacesInNameTest() {
        var validProject = TestDataGenerator.generate(List.of(), Project.class, RandomData.getString(), "valid name with spaces");
        var response = projectController.createProject(validProject);
        response.then().spec(ResponseSpecifications.checkBadRequest());
        var createdProject = ResponseExtractor.extractModel(response, Project.class);
        EntityValidator.validateEntityFields(validProject, createdProject, softy);
        softy.assertAll();
    }

    @Test(description = "User should be able to create a Project with an empty ID String", groups = {"Positive", "CRUD"})
    public void userCreatesProjectWithEmptyIdStringTest() {
        var projectWithEmptyId = TestDataGenerator.generate(List.of(), Project.class, "", RandomData.getString());
        var response = projectController.createProject(projectWithEmptyId);
        response.then().spec(ResponseSpecifications.checkSuccess());
        var createdProject = ResponseExtractor.extractModel(response, Project.class);
        EntityValidator.validateEntityFields(projectWithEmptyId, createdProject, softy);
        softy.assertAll();
    }

    @Test(description = "User should not be able to create a Project without specifying a name", groups = {"Negative", "CRUD"})
    public void userCannotCreateProjectWithoutNameTest() {
        var projectWithoutName = TestDataGenerator.generate(List.of(), Project.class, RandomData.getString(), null);
        var response = projectController.createInvalidProjectFromProject(projectWithoutName);
        response.then().spec(ResponseSpecifications.checkBadRequest());
        softy.assertAll();
    }

    @Test(description = "User should not be able to create a Project if parent project locator is not provided", groups = {"Negative", "CRUD"})
    public void userCannotCreateProjectWithoutParentProjectLocatorTest() {
        var invalidProject = TestDataGenerator.generate(List.of(), Project.class, RandomData.getString(), RandomData.getString(), new ParentProject(null, null));
        var response = projectController.createInvalidProjectFromProject(invalidProject);
        response.then().spec(ResponseSpecifications.checkBadRequest());
        softy.assertAll();
    }

    @Test(description = "User should not be able to create a Project if parent project locator is empty", groups = {"Negative", "CRUD"})
    public void userCannotCreateProjectWithEmptyParentProjectLocatorTest() {
        var invalidProject = TestDataGenerator.generate(List.of(), Project.class, RandomData.getString(), RandomData.getString(), new ParentProject("", null));
        var response = projectController.createInvalidProjectFromProject(invalidProject);
        response.then().spec(ResponseSpecifications.checkProjectNotFound(""));
        softy.assertAll();
    }

    @Test(description = "User should not be able to create a project without authentication", groups = {"Negative", "Auth"})
    public void userCannotCreateProjectWithoutAuthTest() {
        var unauthProjectController = new ProjectController(RequestSpecifications.unauthSpec());
        var invalidProject = TestDataGenerator.generate(List.of(), Project.class, RandomData.getString(), RandomData.getString());
        var response = unauthProjectController.createInvalidProjectFromProject(invalidProject);
        response.then().spec(ResponseSpecifications.checkUnauthorizedAccess());
        softy.assertAll();
    }

    @Test(description = "User should be able to create a Project with an XSS payload in name (payload stored as text)", groups = {"Positive", "Security"})
    public void userCreatesProjectWithXSSInNameTest() {
        var xssPayload = "<script>alert('XSSd')</script>";
        var projectWithXSS = TestDataGenerator.generate(List.of(), Project.class, RandomData.getString(), xssPayload);
        Response response = projectController.createProject(projectWithXSS);
        EntityValidator.validateFieldValueFromResponse(response, Project.class, Project::getName, xssPayload, softy);
        softy.assertAll();
    }

    @Test(description = "User should be able to create a Project with an SQL injection payload in name (payload stored as text)", groups = {"Positive", "Security"})
    public void userCreatesProjectWithSQLInjectionTest() {
        var sqlPayload = "'; DROP TABLE projects; --";
        var projectWithSQL = TestDataGenerator.generate(List.of(), Project.class, RandomData.getString(), sqlPayload);
        Response response = projectController.createProject(projectWithSQL);
        EntityValidator.validateFieldValueFromResponse(response, Project.class, Project::getName, sqlPayload, softy);
        softy.assertAll();
    }
    @DataProvider(name = "restrictedRoles")
    public static Object[][] restrictedRoles() {
        return new Object[][]{
                {Role.PROJECT_VIEWER},
                {Role.GUEST_ROLE},
                {Role.USER_ROLE},
                {Role.PROJECT_DEVELOPER},
                {Role.TOOLS_INTEGRATION}
        };
    }

    @Test(description = "User with restricted role should not be able to create a project",
            dataProvider = "restrictedRoles")
    public void userWithRestrictedRoleCannotCreateProjectTest(Role role) {
        var restrictedUser = generate(User.class);
        restrictedUser.setRoles(new Roles(List.of(new com.example.teamcity.api.models.Role(role.getRoleName(), "g"))));
        superUserCheckRequests.getRequest(ApiEndpoint.USERS).create(restrictedUser);
        var restrictedUserController = new ProjectController(RequestSpecifications.authSpec(restrictedUser));
        var newProject = generate(Project.class, RandomData.getString(), RandomData.getString(), new ParentProject("_Root", null));
        var response = restrictedUserController.createInvalidProjectFromProject(newProject);
        response.then().spec(ResponseSpecifications.checkForbiddenAccess());
        softy.assertAll();
    }

    @DataProvider(name = "allowedRoles")
    public static Object[][] allowedRoles() {
        return new Object[][]{
                {Role.PROJECT_ADMIN},
                {Role.AGENT_MANAGER}
        };
    }

    @Test(description = "User with allowed role should be able to create a project",
            dataProvider = "allowedRoles")
    public void userWithAllowedRoleCanCreateProjectTest(Role role) {
        var allowedUser = generate(User.class);
        var newProject = generate(Project.class, RandomData.getString(), RandomData.getString(), new ParentProject("_Root", null));
        var createdProject = projectController.createAndReturnProject(newProject);
        allowedUser.setRoles(new Roles(List.of(new com.example.teamcity.api.models.Role(role.getRoleName(), "p:" + createdProject.getId()))));
        superUserCheckRequests.getRequest(ApiEndpoint.USERS).create(allowedUser);
        var userController = new ProjectController(RequestSpecifications.authSpec(allowedUser));
        var response = userController.createProject(newProject);
        response.then().spec(ResponseSpecifications.checkBadRequest());
        Project createdResponseProject = ResponseExtractor.extractModel(response, Project.class);
        softy.assertEquals(createdResponseProject.getId(), newProject.getId(), "ID проекта не совпадает");
        softy.assertEquals(createdResponseProject.getName(), newProject.getName(), "Название проекта не совпадает");
        softy.assertAll();
    }



}







