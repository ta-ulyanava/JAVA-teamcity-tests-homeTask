package com.example.teamcity.api;

import com.example.teamcity.BaseTest;
import com.example.teamcity.api.controllers.ProjectController;
import com.example.teamcity.api.enums.Endpoint;
import com.example.teamcity.api.generators.RandomData;
import com.example.teamcity.api.generators.TestDataGenerator;
import com.example.teamcity.api.models.*;
import com.example.teamcity.api.responses.*;
import com.example.teamcity.api.spec.Specifications;
import io.restassured.response.Response;
import org.apache.http.HttpStatus;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

import java.util.List;

import static com.example.teamcity.api.generators.TestDataGenerator.generate;

@Test(groups = {"Regression"})
public class ProjectTests extends BaseTest {

    private ProjectController projectController;

    @BeforeMethod(alwaysRun = true)
    public void setup() {
        super.beforeTest();
        superUserCheckRequests.getRequest(Endpoint.USERS).create(testData.getUser());
        projectController = new ProjectController(Specifications.authSpec(testData.getUser()));
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
        Project createdProject = projectController.createAndReturnProject(testData.getProject());
        projectController.validateCreatedProject(testData.getProject(), createdProject, softy);
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

     softy.assertNotNull(createdProject.getSourceProject(), "sourceProject должен присутствовать, но отсутствует!");
     softy.assertNotNull(createdProject.getProjectsIdsMap(), "projectsIdsMap должен быть скопирован");
     softy.assertNotNull(createdProject.getBuildTypesIdsMap(), "buildTypesIdsMap должен быть скопирован");
     softy.assertNotNull(createdProject.getVcsRootsIdsMap(), "vcsRootsIdsMap должен быть скопирован");

     softy.assertAll(); // Перемещаем сюда
 }



    @Test(description = "User should be able to create a Project with copyAllAssociatedSettings set to false and verify fields are NOT copied", groups = {"Positive", "CRUD"})
    public void userCreatesProjectWithCopyAllAssociatedSettingsFalseTest() {
        var sourceProject = generate(List.of(), Project.class, RandomData.getString(), RandomData.getString(), new ParentProject("_Root", null));
        var createdSourceProject = projectController.createAndReturnProject(sourceProject);
        var newProject = generate(List.of(), Project.class, RandomData.getString(), RandomData.getString(), new ParentProject("_Root", null), false, createdSourceProject);
        var createdProject = projectController.createAndReturnProject(newProject);
        softy.assertNull(createdProject.getProjectsIdsMap(), "projectsIdsMap should NOT be copied");
        softy.assertNull(createdProject.getBuildTypesIdsMap(), "buildTypesIdsMap should NOT be copied");
        softy.assertNull(createdProject.getVcsRootsIdsMap(), "vcsRootsIdsMap should NOT be copied");
        softy.assertNull(createdProject.getSourceProject(), "Source project should NOT be copied");
        TestValidator.validateEntityFields(newProject, createdProject, softy);
        softy.assertAll();
    }

// Need to fix bug
@DataProvider(name = "invalidCopySettings")
public static Object[][] invalidCopySettings() {
    return new Object[][] {
            {"123"},     // Число (не булевое)
            {"null"},    // null может быть ошибкой в API
            {"\"yes\""}, // Строка (не булевое)
            {"\"no\""},  // Строка (не булевое)
            {"{}"}       // Пустой объект (не булевое)
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
        Response response = projectController.createInvalidProjectFromString(invalidProjectJson);
        ResponseValidator.checkErrorStatus(response, HttpStatus.SC_BAD_REQUEST);
        softy.assertTrue(response.asString().contains("Cannot deserialize value of type `java.lang.Boolean`"),
                "Ошибка должна содержать сообщение о неверном типе copyAllAssociatedSettings");
        softy.assertAll();
    }


    @DataProvider(name = "projectCreationScenarios")
    public static Object[][] projectCreationScenarios() {
        return new Object[][]{
                {true, 20},  // Вложенные проекты, 20 штук
                {false, 20}  // Проекты на одном уровне, 20 штук
        };
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

        if (isNested) {
            for (int i = 1; i < projects.size(); i++) {
                var parentProject = projectController.getProjectById(projects.get(i).getParentProject().getId());
                TestValidator.validateEntityFields(projects.get(i - 1), parentProject, softy);
            }
        } else {
            projects.forEach(project ->
                    softy.assertEquals(project.getParentProject().getId(), createdRootProject.getId(),
                            "Parent project ID is incorrect for project " + project.getId())
            );
        }

        softy.assertAll();
    }


    @Test(description = "User should not be able to create a Project with a non-existent parentProject locator", groups = {"Negative", "CRUD"})
    public void userCannotCreateProjectWithNonExistentParentProjectTest() {
        var invalidProject = generate(List.of(), Project.class, RandomData.getString(), RandomData.getString(), new ParentProject("non_existent_locator", null));
        var response = projectController.createInvalidProjectFromProject(invalidProject);

        ResponseValidator.checkErrorAndBody(response, HttpStatus.SC_NOT_FOUND, "Project cannot be found by external id 'non_existent_locator'");
        softy.assertAll();
    }

    @Test(description = "User should not be able to create a Project with the same ID as its parent ID", groups = {"Negative", "CRUD"})
    public void userCannotCreateProjectWithSameParentIdTest() {
        var projectId = testData.getProject().getId();
        var invalidProject = generate(List.of(), Project.class, projectId, RandomData.getString(), new ParentProject(projectId, null));
        var response = projectController.createInvalidProjectFromProject(invalidProject);
        ResponseValidator.checkErrorAndBody(response, HttpStatus.SC_NOT_FOUND, "Project cannot be found by external id '%s'".formatted(projectId));
        softy.assertAll();
    }

    @Test(description = "User should be able to create a Project with a name of 500 characters",
            groups = {"Positive", "CRUD", "CornerCase"})
    public void userCreatesProjectWith500LengthNameTest() {
        var maxLengthName = "A".repeat(500);
        var validProject = generate(List.of(), Project.class, RandomData.getString(), maxLengthName);
        var response = projectController.createProject(validProject);
        ResponseValidator.checkSuccessStatus(response, HttpStatus.SC_OK);
        var createdProject = ResponseExtractor.extractModel(response, Project.class);
        TestValidator.validateEntityFields(validProject, createdProject, softy);
        softy.assertAll();
    }

    @Test(description = "User should be able to create a Project with a name of length 1",
            groups = {"Positive", "CRUD"})
    public void userCreatesProjectWithOneCharacterNameTest() {
        var validProject = generate(List.of(), Project.class, RandomData.getString(), "A");
        var response = projectController.createProject(validProject);
        ResponseValidator.checkSuccessStatus(response, HttpStatus.SC_OK);
        var createdProject = ResponseExtractor.extractModel(response, Project.class);
        TestValidator.validateEntityFields(validProject, createdProject, softy);
        softy.assertAll();
    }

    @Test(description = "User should be able to create a Project with an ID of maximum allowed length",
            groups = {"Positive", "CRUD"})
    public void userCreatesProjectWithMaxLengthIdTest() {
        var maxLengthId = "A".repeat(225);
        var validProject = generate(List.of(), Project.class, maxLengthId, RandomData.getString());
        var response = projectController.createProject(validProject);
        ResponseValidator.checkSuccessStatus(response, HttpStatus.SC_OK);
        var createdProject = ResponseExtractor.extractModel(response, Project.class);
        TestValidator.validateEntityFields(validProject, createdProject, softy);
        softy.assertAll();
    }

    @Test(description = "User should be able to create a Project with an ID of length 1",
            groups = {"Positive", "CRUD"})
    public void userCreatesProjectWithOneCharacterIdTest() {
        var validProject = generate(List.of(), Project.class, "A", RandomData.getString());
        var response = projectController.createProject(validProject);
        ResponseValidator.checkSuccessStatus(response, HttpStatus.SC_OK);
        var createdProject = ResponseExtractor.extractModel(response, Project.class);
        TestValidator.validateEntityFields(validProject, createdProject, softy);
        softy.assertAll();
    }

    //To fix 500 (Internal Server Error).
    @Test(description = "User should not be able to create a Project with an ID longer than 225 characters",
            groups = {"Negative", "CRUD", "KnownBugs", "CornerCase"})
    public void userCannotCreateProjectWithTooLongIdTest() {
        var tooLongId = "A".repeat(226);
        var invalidProject = generate(List.of(), Project.class, tooLongId, RandomData.getString());
        var response = projectController.createInvalidProjectFromProject(invalidProject);
        ResponseValidator.checkErrorAndBody(response, HttpStatus.SC_BAD_REQUEST,
                "Project ID \"%s\" is invalid: it is %d characters long while the maximum length is 225."
                        .formatted(tooLongId, tooLongId.length()));
        softy.assertAll();
    }

    @Test(description = "User should be able to create a Project with special characters in name",
            groups = {"Positive", "CRUD"})
    public void userCreatesProjectWithSpecialCharactersInNameTest() {
        var project = generate(List.of(), Project.class, RandomData.getString(), RandomData.getFullSpecialCharacterString());
        var response = projectController.createProject(project);
        ResponseValidator.checkSuccessStatus(response, HttpStatus.SC_OK);
        var createdProject = ResponseExtractor.extractModel(response, Project.class);
        TestValidator.validateEntityFields(project, createdProject, softy);
        softy.assertAll();
    }

    @Test(description = "User should be able to create a Project with a localized name",
            groups = {"Positive", "CRUD"})
    public void userCreatesProjectWithLocalizedNameTest() {
        var localizedProject = generate(List.of(), Project.class, RandomData.getString(), RandomData.getFullLocalizationString());
        var response = projectController.createProject(localizedProject);
        ResponseValidator.checkSuccessStatus(response, HttpStatus.SC_OK);
        var createdProject = ResponseExtractor.extractModel(response, Project.class);
        TestValidator.validateEntityFields(localizedProject, createdProject, softy);
        softy.assertAll();
    }


    @Test(description = "User should be able to create a Project with an ID containing an underscore",
            groups = {"Positive", "CRUD"})
    public void userCreatesProjectWithUnderscoreInIdTest() {
        var projectWithUnderscore = generate(List.of(), Project.class, RandomData.getString() + "_test", RandomData.getString());
        var response = projectController.createProject(projectWithUnderscore);
        ResponseValidator.checkSuccessStatus(response, HttpStatus.SC_OK);
        var createdProject = ResponseExtractor.extractModel(response, Project.class);
        TestValidator.validateEntityFields(projectWithUnderscore, createdProject, softy);
        softy.assertAll();
    }

    // Need to fix 500 server Error
    @Test(description = "User should not be able to create a Project with special characters in ID",
            groups = {"Negative", "CRUD", "KnownBugs"}, dataProvider = "invalidSpecialCharactersForId")
    public void userCannotCreateProjectWithEachSpecialCharacterInIdTest(String specialChar) {
        var invalidId="test_"+specialChar;
        var invalidProject=TestDataGenerator.generate(List.of(),Project.class,invalidId,"ValidName");
        var response=projectController.createInvalidProjectFromProject(invalidProject);
        ResponseValidator.checkErrorAndBody(response,HttpStatus.SC_BAD_REQUEST,
                "Project ID \"%s\" is invalid".formatted(invalidId),
                "ID should start with a latin letter and contain only latin letters, digits and underscores");
    }
    // Need to fix 500 server Error
    @Test(description = "User should not be able to create a Project with a non-Latin ID",
            groups = {"Negative", "CRUD", "KnownBugs"}, dataProvider = "nonLatinIdProviderForId")
    public void userCannotCreateProjectWithNonLatinIdTest(String invalidId) {
        var invalidProject=TestDataGenerator.generate(List.of(),Project.class,invalidId,RandomData.getString());
        var response=projectController.createInvalidProjectFromProject(invalidProject);
        ResponseValidator.checkErrorAndBody(response,HttpStatus.SC_BAD_REQUEST,
                "Project ID \"%s\" is invalid: contains non-latin letter '%s'.".formatted(invalidId,invalidId),
                "ID should start with a latin letter and contain only latin letters, digits and underscores (at most 225 characters).");
    }

    @Test(description = "User should not be able to create Project with empty name", groups = {"Negative", "CRUD"})
    public void userCannotCreateProjectWithEmptyNameTest() {
        var invalidProject=TestDataGenerator.generate(List.of(),Project.class,RandomData.getString(),"");
        var response=projectController.createInvalidProjectFromProject(invalidProject);
        ResponseValidator.checkErrorAndBody(response,HttpStatus.SC_BAD_REQUEST,"Project name cannot be empty");
    }

    //Need to fix 500 error
    @Test(description = "User should not be able to create Project with empty id", groups = {"Negative", "CRUD", "KnownBugs"})
    public void userCannotCreateProjectWithEmptyIdTest() {
        var invalidProject = TestDataGenerator.generate(List.of(), Project.class, "", RandomData.getString());
        var response = projectController.createInvalidProjectFromProject(invalidProject);
        ResponseValidator.checkErrorAndBody(response, HttpStatus.SC_BAD_REQUEST, "Project ID must not be empty.");
    }

    //Need to fix 500 error
    @Test(description = "User should not be able to create a Project with a space as ID", groups = {"Negative", "CRUD", "KnownBugs"})
    public void userCannotCreateProjectWithSpaceAsIdTest() {
        var invalidProject=TestDataGenerator.generate(List.of(),Project.class," ",RandomData.getString());
        var response=projectController.createInvalidProjectFromProject(invalidProject);
        ResponseValidator.checkErrorAndBody(response,HttpStatus.SC_BAD_REQUEST,"Project ID must not be empty");
    }
    //Need to fix 500 error
    @Test(description = "User should not be able to create a Project with a space as name", groups = {"Negative", "CRUD", "KnownBugs"})
    public void userCannotCreateProjectWithSpaceAsNameTest() {
        var invalidProject=TestDataGenerator.generate(List.of(),Project.class,RandomData.getString()," ");
        var response=projectController.createInvalidProjectFromProject(invalidProject);
        ResponseValidator.checkErrorAndBody(response,HttpStatus.SC_BAD_REQUEST,"Given project name is empty");
    }

    @Test(description = "User should not be able to create a Project with an existing ID", groups = {"Negative", "CRUD"})
    public void userCannotCreateProjectWithExistingIdTest() {
        var existingProject = projectController.createAndReturnProject(testData.getProject());
        var duplicateProject = TestDataGenerator.generate(List.of(existingProject), Project.class, existingProject.getId(), RandomData.getString());
        var response = projectController.createInvalidProjectFromProject(duplicateProject);
        ResponseValidator.checkErrorAndBody(response, HttpStatus.SC_BAD_REQUEST, "Project ID \"" + existingProject.getId() + "\" is already used by another project");
    }

    @Test(description = "User should not be able to create a Project with an existing name", groups = {"Negative", "CRUD"})
    public void userCannotCreateProjectWithExistingNameTest() {
        var existingProject = projectController.createAndReturnProject(testData.getProject());
        var duplicateProject = TestDataGenerator.generate(List.of(existingProject), Project.class, RandomData.getString(), existingProject.getName());
        var response = projectController.createInvalidProjectFromProject(duplicateProject);
        ResponseValidator.checkErrorAndBody(response, HttpStatus.SC_BAD_REQUEST, "Project with this name already exists: " + existingProject.getName());
    }

    @Test(description = "User should not be able to create a Project with an existing name in a different case", groups = {"Negative", "CRUD"})
    public void userCannotCreateProjectWithExistingNameDifferentCaseTest() {
        var existingProject = projectController.createAndReturnProject(testData.getProject());
        var duplicateName = existingProject.getName().toUpperCase();
        var duplicateProject = TestDataGenerator.generate(List.of(), Project.class, RandomData.getString(), duplicateName);
        var response = projectController.createInvalidProjectFromProject(duplicateProject);
        ResponseValidator.checkErrorAndBody(response, HttpStatus.SC_BAD_REQUEST, "Project with this name already exists: " + duplicateName);
    }

    @Test(description = "User should not be able to create a Project with an existing ID in a different case", groups = {"Negative", "CRUD"})
    public void userCannotCreateProjectWithExistingIdDifferentCaseTest() {
        var existingProject = projectController.createAndReturnProject(testData.getProject());
        var duplicateId = existingProject.getId().toUpperCase();
        var duplicateProject = TestDataGenerator.generate(List.of(), Project.class, duplicateId, RandomData.getString());
        var response = projectController.createInvalidProjectFromProject(duplicateProject);
        ResponseValidator.checkErrorAndBody(response, HttpStatus.SC_BAD_REQUEST, "Project ID \"" + duplicateId + "\" is already used by another project");
    }


//Need to fix 500 error
    @Test(description = "User should not be able to create a Project with an ID consisting only of digits", groups = {"Negative", "CRUD", "KnownBugs"})
    public void userCannotCreateProjectWithDigitsOnlyIdTest() {
        var invalidProject = TestDataGenerator.generate(List.of(), Project.class, "123456", RandomData.getString());
        var response = projectController.createInvalidProjectFromProject(invalidProject);
        ResponseValidator.checkErrorAndBody(response, HttpStatus.SC_BAD_REQUEST,
                "Project ID \"123456\" is invalid",
                "ID should start with a latin letter and contain only latin letters, digits and underscores");
    }

    //Need to fix 500 error
    @Test(description = "User should not be able to create a Project with an ID starting with an underscore or a digit", groups = {"Negative", "CRUD", "KnownBugs"}, dataProvider = "invalidIdStartId")
    public void userCannotCreateProjectWithInvalidStartingCharacterIdTest(String invalidId) {
        var invalidProject = TestDataGenerator.generate(List.of(), Project.class, invalidId, RandomData.getString());
        var response = projectController.createInvalidProjectFromProject(invalidProject);
        ResponseValidator.checkErrorAndBody(response, HttpStatus.SC_BAD_REQUEST,
                "Project ID \"" + invalidId + "\" is invalid",
                "ID should start with a latin letter and contain only latin letters, digits and underscores");
    }

    //Need to fix 500 error
    @Test(description = "User should not be able to create a Project with spaces in the middle of the ID", groups = {"Negative", "CRUD", "KnownBugs"})
    public void userCannotCreateProjectWithSpacesInIdTest() {
        var invalidProject = TestDataGenerator.generate(List.of(), Project.class, "invalid id", RandomData.getString());
        var response = projectController.createInvalidProjectFromProject(invalidProject);
        ResponseValidator.checkErrorAndBody(response, HttpStatus.SC_BAD_REQUEST,
                "Project ID \"invalid id\" is invalid",
                "ID should start with a latin letter and contain only latin letters, digits and underscores");
    }

    @Test(description = "User should be able to create a Project with an ID containing Latin letters, digits, and underscores", groups = {"Positive", "CRUD"})
    public void userCreatesProjectWithValidIdCharactersTest() {
        var validProject = TestDataGenerator.generate(List.of(), Project.class, "valid_123_ID", RandomData.getString());
        var response = projectController.createProject(validProject);
        ResponseValidator.checkSuccessStatus(response, HttpStatus.SC_OK);
        var createdProject = ResponseExtractor.extractModel(response, Project.class);
        TestValidator.validateEntityFields(validProject, createdProject, softy);
        softy.assertAll();
    }

    @Test(description = "User should be able to create a Project with a name consisting only of digits", groups = {"Positive", "CRUD"})
    public void userCreatesProjectWithDigitsOnlyNameTest() {
        var validProject = TestDataGenerator.generate(List.of(), Project.class, RandomData.getString(), "123456");
        var response = projectController.createProject(validProject);
        ResponseValidator.checkSuccessStatus(response, HttpStatus.SC_OK);
        var createdProject = ResponseExtractor.extractModel(response, Project.class);
        TestValidator.validateEntityFields(validProject, createdProject, softy);
        softy.assertAll();
    }

    @Test(description = "User should be able to create a Project with spaces in the middle of the name", groups = {"Positive", "CRUD"})
    public void userCreatesProjectWithSpacesInNameTest() {
        var validProject = TestDataGenerator.generate(List.of(), Project.class, RandomData.getString(), "valid name with spaces");
        var response = projectController.createProject(validProject);
        ResponseValidator.checkSuccessStatus(response, HttpStatus.SC_OK);
        var createdProject = ResponseExtractor.extractModel(response, Project.class);
        TestValidator.validateEntityFields(validProject, createdProject, softy);
        softy.assertAll();
    }


    @Test(description = "User should be able to create a Project with an empty ID String", groups = {"Positive", "CRUD"})
    public void userCreatesProjectWithEmptyIdStringTest() {
        var projectWithEmptyId = TestDataGenerator.generate(List.of(), Project.class, "", RandomData.getString());
        Response response = projectController.createProject(projectWithEmptyId);
        ResponseValidator.checkSuccessStatus(response, HttpStatus.SC_OK);
        Project createdProject = ResponseExtractor.extractModel(response, Project.class);
        TestValidator.validateEntityFields(projectWithEmptyId, createdProject, softy);
        softy.assertAll();
    }

    @Test(description = "User should not be able to create a Project without specifying a name", groups = {"Negative", "CRUD"})
    public void userCannotCreateProjectWithoutNameTest() {
        var projectWithoutName = TestDataGenerator.generate(List.of(), Project.class, RandomData.getString(), null);
        var response = projectController.createInvalidProjectFromProject(projectWithoutName);
        ResponseValidator.checkErrorAndBody(response, HttpStatus.SC_BAD_REQUEST, "Project name cannot be empty");
    }

    @Test(description = "User should not be able to create a Project if parent project locator is not provided", groups = {"Negative", "CRUD"})
    public void userCannotCreateProjectWithoutParentProjectLocatorTest() {
        var invalidProject = TestDataGenerator.generate(List.of(), Project.class, RandomData.getString(), RandomData.getString(), new ParentProject(null, null));
        var response = projectController.createInvalidProjectFromProject(invalidProject);
        ResponseValidator.checkErrorAndBody(response, HttpStatus.SC_BAD_REQUEST, "No project specified", "Either 'id', 'internalId' or 'locator' attribute should be present");
    }

    @Test(description = "User should not be able to create a Project if parent project locator is empty", groups = {"Negative", "CRUD"})
    public void userCannotCreateProjectWithEmptyParentProjectLocatorTest() {
        var invalidProject = TestDataGenerator.generate(List.of(), Project.class, RandomData.getString(), RandomData.getString(), new ParentProject("", null));
        var response = projectController.createInvalidProjectFromProject(invalidProject);
        ResponseValidator.checkErrorAndBody(response, HttpStatus.SC_NOT_FOUND, "No project found by locator 'count:1,id:'", "Project cannot be found by external id ''");
    }


    @Test(description = "User should not be able to create a project without authentication", groups = {"Negative", "Auth"})
    public void userCannotCreateProjectWithoutAuthTest() {
        var unauthProjectController = new ProjectController(Specifications.unauthSpec());
        var invalidProject = TestDataGenerator.generate(List.of(), Project.class, RandomData.getString(), RandomData.getString());
        var response = unauthProjectController.createInvalidProjectFromProject(invalidProject);
        ResponseValidator.checkErrorAndBody(response, HttpStatus.SC_UNAUTHORIZED, "Authentication required");
    }

    @Test(description = "User should be able to create a Project with an XSS payload in name (payload stored as text)", groups = {"Positive", "Security"})
    public void userCreatesProjectWithXSSInNameTest() {
        var xssPayload = "<script>alert('XSSd')</script>";
        var projectWithXSS = TestDataGenerator.generate(List.of(), Project.class, RandomData.getString(), xssPayload);
        Response response = projectController.createProject(projectWithXSS);
        TestValidator.validateFieldValueFromResponse(response, Project.class, Project::getName, xssPayload, softy);
        softy.assertAll();
    }

    @Test(description = "User should be able to create a Project with an SQL injection payload in name (payload stored as text)", groups = {"Positive", "Security"})
    public void userCreatesProjectWithSQLInjectionTest() {
        var sqlPayload = "'; DROP TABLE projects; --";
        var projectWithSQL = TestDataGenerator.generate(List.of(), Project.class, RandomData.getString(), sqlPayload);
        Response response = projectController.createProject(projectWithSQL);
        TestValidator.validateFieldValueFromResponse(response, Project.class, Project::getName, sqlPayload, softy);
        softy.assertAll();
    }


        @DataProvider(name = "restrictedRoles")
        public static Object[][] restrictedRoles() {
            return new Object[][]{
                    {"PROJECT_VIEWER"},
                    {"GUEST_ROLE"},
                    {"USER_ROLE"},
                    {"PROJECT_DEVELOPER"},
                    {"TOOLS_INTEGRATION"}
            };
        }


    @Test(description = "User with restricted role should not be able to create a project",
            dataProvider = "restrictedRoles")
    public void userWithRestrictedRoleCannotCreateProjectTest(String roleId) {
        var restrictedUser = generate(User.class);
        restrictedUser.setRoles(new Roles(List.of(new Role(roleId, "g"))));

        superUserCheckRequests.getRequest(Endpoint.USERS).create(restrictedUser);

        var restrictedUserController = new ProjectController(Specifications.authSpec(restrictedUser));
        var newProject = generate(Project.class, RandomData.getString(), RandomData.getString(), new ParentProject("_Root", null));

        var response = restrictedUserController.createInvalidProjectFromProject(newProject);

        ResponseValidator.checkErrorStatus(response, HttpStatus.SC_FORBIDDEN);
        softy.assertTrue(response.asString().contains("Access denied"), "Ошибка должна содержать 'Access denied'");
        softy.assertTrue(response.asString().contains("Create subproject"), "Ошибка должна содержать 'Create subproject'");

        softy.assertAll();
    }




        @DataProvider(name = "allowedRoles")
        public static Object[][] allowedRoles() {
            return new Object[][]{
                    {"PROJECT_ADMIN"},
                    {"AGENT_MANAGER"}
            };
        }

    @Test(description = "User with allowed role should be able to create a project",
            dataProvider = "allowedRoles")
    public void userWithAllowedRoleCanCreateProjectTest(String roleId) {
        var allowedUser = generate(User.class);
        allowedUser.setRoles(new Roles(List.of(new Role(roleId, "g"))));

        superUserCheckRequests.getRequest(Endpoint.USERS).create(allowedUser);

        var userController = new ProjectController(Specifications.authSpec(allowedUser));
        var newProject = generate(Project.class, RandomData.getString(), RandomData.getString(), new ParentProject("_Root", null));

        var response = userController.createProject(newProject);

        ResponseValidator.checkSuccessStatus(response, HttpStatus.SC_OK);
        TestValidator.validateFieldWithStatusCode(response, HttpStatus.SC_OK, Project.class, Project::getId, newProject.getId(), softy);
        TestValidator.validateFieldWithStatusCode(response, HttpStatus.SC_OK, Project.class, Project::getName, newProject.getName(), softy);

        softy.assertAll();
    }

    }







