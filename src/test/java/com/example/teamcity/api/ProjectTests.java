package com.example.teamcity.api;

import com.example.teamcity.BaseTest;
import com.example.teamcity.api.controllers.ProjectController;
import com.example.teamcity.api.enums.Endpoint;
import com.example.teamcity.api.generators.RandomData;
import com.example.teamcity.api.generators.TestDataGenerator;
import com.example.teamcity.api.models.ParentProject;
import com.example.teamcity.api.models.Project;
import com.example.teamcity.api.responses.*;
import com.example.teamcity.api.spec.Specifications;
import io.restassured.response.Response;
import org.apache.http.HttpStatus;
import org.hamcrest.Matchers;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

import java.util.Arrays;
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
    public Object[][] nonLatinIdProviderForId() {
        return new Object[][]{{"проект"}, {"项目"}, {"プロジェクト"}, {"مشروع"}, {"παράδειγμα"}, {"नमूना"}, {"בדיקה"}};
    }

    @DataProvider
    public Object[][] invalidIdStartId() {
        return new Object[][]{
                {"_invalidId"},
                {"1invalidId"},
        };
    }

    @Test(description = "User should be able to create a project with the minimum required fields", groups = {"Positive", "CRUD"})
    public void userCreatesProjectWithMandatoryFieldsOnlyTest() {
        Response response = projectController.createProject(testData.getProject());
        ResponseLogger.logIfError(response);
        ResponseValidator.checkSuccessStatus(response, HttpStatus.SC_OK);
        Project createdProject = ResponseExtractor.extractModel(response, Project.class);
        TestValidator.validateEntityFields(testData.getProject(), createdProject, softy);
        softy.assertAll();
    }

    @Test(description = "User should be able to create Project with copyAllAssociatedSettings set to true", groups = {"Positive", "CRUD"})
    public void userCreatesProjectWithCopyAllAssociatedSettingsTrueTest() {
        var projectWithCopyAll = generate(Arrays.asList(testData.getProject()), Project.class, testData.getProject().getId(), testData.getProject().getName(), null, true);
        Response response = projectController.createProject(projectWithCopyAll);
        ResponseLogger.logIfError(response);
        ResponseValidator.checkSuccessStatus(response, HttpStatus.SC_OK);
        Project createdProject = ResponseExtractor.extractModel(response, Project.class);
        TestValidator.validateEntityFields(projectWithCopyAll, createdProject, softy);
        softy.assertAll();
    }


    @Test(description = "User should be able to create Project with copyAllAssociatedSettings set to false", groups = {"Positive", "CRUD"})
    public void userCreatesProjectWithCopyAllAssociatedSettingsFalseTest() {
        var projectWithCopyAll = generate(Arrays.asList(testData.getProject()), Project.class, testData.getProject().getId(), testData.getProject().getName(), null, false);
        Response response = projectController.createProject(projectWithCopyAll);
        ResponseLogger.logIfError(response);
        ResponseValidator.checkSuccessStatus(response, HttpStatus.SC_OK);
        Project createdProject = ResponseExtractor.extractModel(response, Project.class);
        TestValidator.validateEntityFields(projectWithCopyAll, createdProject, softy);
        softy.assertAll();
    }

    @Test(description = "User should be able to create a max amount of nested projects", groups = {"Positive", "CRUD", "CornerCase"})
    public void userCreatesMaxAmountNestedProjectsTest() {
        projectController.createProject(testData.getProject());
        int maxNestedProjects = 10;
        var nestedProjects = projectController.createNestedProjects(testData.getProject().getId(), maxNestedProjects);
        softy.assertEquals(nestedProjects.size(), maxNestedProjects, "The number of nested projects is incorrect");
        for (int i = 1; i < nestedProjects.size(); i++) {
            var parentProject = projectController.getProjectById(nestedProjects.get(i).getParentProject().getId());
            softy.assertEquals(parentProject.getId(), nestedProjects.get(i - 1).getId(), "Parent project ID is incorrect for project " + nestedProjects.get(i).getId());
        }
        softy.assertAll();
    }


    @Test(description = "User should be able to create a project in Root and nest 20 projects inside it", groups = {"Positive", "CRUD", "CornerCase"})
    public void userCreatesProjectInRootWith20NestedProjectsTest() {
        var rootProject = generate(List.of(), Project.class, RandomData.getString(), RandomData.getString(), new ParentProject("_Root", null));
        Response rootProjectResponse = projectController.createProject(rootProject);
        ResponseLogger.logIfError(rootProjectResponse);
        ResponseValidator.checkSuccessStatus(rootProjectResponse, HttpStatus.SC_OK);

        int nestedProjectsCount = 20;
        var nestedProjects = projectController.createNestedProjects(rootProject.getId(), nestedProjectsCount);

        softy.assertEquals(nestedProjects.size(), nestedProjectsCount, "The number of nested projects is incorrect");

        for (int i = 1; i < nestedProjects.size(); i++) {
            var parentProject = projectController.getProjectById(nestedProjects.get(i).getParentProject().getId());
            softy.assertEquals(parentProject.getId(), nestedProjects.get(i - 1).getId(), "Parent project ID is incorrect for project " + nestedProjects.get(i).getId());
        }

        var lastNestedProject = nestedProjects.get(nestedProjects.size() - 1);
        var createdLastNestedProject = projectController.getProjectById(lastNestedProject.getId());
        softy.assertEquals(createdLastNestedProject.getParentProject().getId(), nestedProjects.get(nestedProjects.size() - 2).getId(), "Parent project ID is incorrect for the last nested project");

        softy.assertAll();
    }


    @Test(description = "User should be able to create 20 sibling projects under the same parent", groups = {"Positive", "CRUD", "CornerCase"})
    public void userCreates20SiblingProjectsTest() {
        projectController.createProject(testData.getProject());
        int siblingProjectsCount = 20;
        var siblingProjects = projectController.createSiblingProjects(testData.getProject().getId(), siblingProjectsCount);

        softy.assertEquals(siblingProjects.size(), siblingProjectsCount, "The number of created sibling projects is incorrect");

        siblingProjects.forEach(project ->
                softy.assertEquals(project.getParentProject().getId(), testData.getProject().getId(),
                        "Parent project ID is incorrect for project " + project.getId())
        );

        softy.assertAll();
    }

    @Test(description = "User should not be able to create a Project with a non-existent parentProject locator", groups = {"Negative", "CRUD"})
    public void userCannotCreateProjectWithNonExistentParentProjectTest() {
        var response = projectController.createInvalidProject(generate(List.of(), Project.class, RandomData.getString(), RandomData.getString(), new ParentProject("non_existent_locator", null)));

        ResponseValidator.checkErrorAndBody(response, HttpStatus.SC_NOT_FOUND,
                "Project cannot be found by external id 'non_existent_locator'");
        softy.assertAll();
    }


    @Test(description = "User should not be able to create a Project with the same ID as its parent ID", groups = {"Negative", "CRUD"})
    public void userCannotCreateProjectWithSameParentIdTest() {
        var invalidProject = TestDataGenerator.generate(List.of(), Project.class, testData.getProject().getId(), RandomData.getString(), new ParentProject(testData.getProject().getId(), null));

        var response = projectController.createInvalidProject(invalidProject);

        ResponseValidator.checkErrorAndBody(response, HttpStatus.SC_NOT_FOUND,
                "Project cannot be found by external id '%s'".formatted(testData.getProject().getId()));
        softy.assertAll();
    }


    @Test(description = "User should be able to create a Project with a name of 500 characters", groups = {"Positive", "CRUD", "CornerCase"})
    public void userCreatesProjectWith500LengthNameTest() {
        var maxLengthName = "A".repeat(500);
        var validProject = TestDataGenerator.generate(List.of(), Project.class, RandomData.getString(), maxLengthName);
        Response response = projectController.createProject(validProject);
        ResponseValidator.checkSuccessStatus(response, HttpStatus.SC_OK);
        Project createdProject = ResponseExtractor.extractModel(response, Project.class);
        softy.assertEquals(createdProject.getName(), validProject.getName(), "Project name is incorrect");
        softy.assertAll();
    }

    @Test(description = "User should be able to create a Project with an ID of maximum allowed length", groups = {"Positive", "CRUD"})
    public void userCreatesProjectWithMaxLengthIdTest() {
        var maxLengthId = "A".repeat(225);
        var validProject = TestDataGenerator.generate(List.of(), Project.class, maxLengthId, RandomData.getString());
        Response response = projectController.createProject(validProject);
        ResponseLogger.logIfError(response);
        ResponseValidator.checkSuccessStatus(response, HttpStatus.SC_OK);
        Project createdProject = ResponseExtractor.extractModel(response, Project.class);
        TestValidator.validateEntityFields(validProject, createdProject, softy);
        softy.assertAll();
    }

    //To fix 500 (Internal Server Error).
    @Test(description = "User should not be able to create a Project with an ID longer than 225 characters", groups = {"Negative", "CRUD", "KnownBugs", "CornerCase"})
    public void userCannotCreateProjectWithTooLongIdTest() {
        var tooLongId = "A".repeat(226);
        var invalidProject = TestDataGenerator.generate(List.of(), Project.class, tooLongId, RandomData.getString());
        var response = projectController.createInvalidProject(invalidProject);
        ResponseValidator.checkErrorAndBody(response, HttpStatus.SC_BAD_REQUEST,
                "Project ID \"" + tooLongId + "\" is invalid: it is 226 characters long while the maximum length is 225.");
    }


    @Test(description = "User should be able to create a Project with an ID of length 1", groups = {"Positive", "CRUD"})
    public void userCreatesProjectWithOneCharacterIdTest() {
        var validProject = TestDataGenerator.generate(List.of(), Project.class, "A", RandomData.getString());

        Response response = projectController.createProject(validProject);
        ResponseValidator.checkSuccessStatus(response, HttpStatus.SC_OK);

        Project createdProject = ResponseExtractor.extractModel(response, Project.class);
        TestValidator.validateEntityFields(validProject, createdProject, softy);

        softy.assertAll();
    }


    @Test(description = "User should be able to create a Project with a name of length 1", groups = {"Positive", "CRUD"})
    public void userCreatesProjectWithOneCharacterNameTest() {
        var validProject = TestDataGenerator.generate(List.of(), Project.class, RandomData.getString(), "A");
        Response response = projectController.createProject(validProject);
        ResponseValidator.checkSuccessStatus(response, HttpStatus.SC_OK);
        Project createdProject = ResponseExtractor.extractModel(response, Project.class);
        TestValidator.validateEntityFields(validProject, createdProject, softy);
        softy.assertAll();
    }


    @Test(description = "User should be able to create a Project with special characters in name", groups = {"Positive", "CRUD"})
    public void userCreatesProjectWithSpecialCharactersInNameTest() {
        var project = TestDataGenerator.generate(List.of(), Project.class, RandomData.getString(), RandomData.getFullSpecialCharacterString());
        Response response = projectController.createProject(project);
        ResponseValidator.checkSuccessStatus(response, HttpStatus.SC_OK);
        Project createdProject = ResponseExtractor.extractModel(response, Project.class);
        TestValidator.validateEntityFields(project, createdProject, softy);
        softy.assertAll();
    }


    @Test(description = "User should be able to create a Project with an ID containing an underscore", groups = {"Positive", "CRUD"})
    public void userCreatesProjectWithUnderscoreInIdTest() {
        var validProject = TestDataGenerator.generate(List.of(), Project.class, "test_project_123", RandomData.getString());
        Response response = projectController.createProject(validProject);
        ResponseValidator.checkSuccessStatus(response, HttpStatus.SC_OK);
        Project createdProject = ResponseExtractor.extractModel(response, Project.class);
        TestValidator.validateEntityFields(validProject, createdProject, softy);
        softy.assertAll();
    }

    @Test(description = "User should be able to create a Project with a localized name", groups = {"Positive", "CRUD"})
    public void userCreatesProjectWithLocalizedNameTest() {
        var localizedProject = TestDataGenerator.generate(List.of(), Project.class, RandomData.getString(), RandomData.getFullLocalizationString());
        Response response = projectController.createProject(localizedProject);
        ResponseValidator.checkSuccessStatus(response, HttpStatus.SC_OK);
        Project createdProject = ResponseExtractor.extractModel(response, Project.class);
        TestValidator.validateEntityFields(localizedProject, createdProject, softy);
        softy.assertAll();
    }


    // Need to fix 500 server Error
    @Test(description = "User should not be able to create a Project with special characters in ID", groups = {"Negative", "CRUD", "KnownBugs"}, dataProvider = "invalidSpecialCharactersForId")
    public void userCannotCreateProjectWithEachSpecialCharacterInIdTest(String specialChar) {
        var invalidProject = TestDataGenerator.generate(List.of(), Project.class, "test_" + specialChar, "ValidName");
        var response = projectController.createInvalidProject(invalidProject);
        ResponseValidator.checkErrorAndBody(response, HttpStatus.SC_BAD_REQUEST,
                "Project ID \"test_" + specialChar + "\" is invalid",
                "ID should start with a latin letter and contain only latin letters, digits and underscores");
    }


    //Need to fix 500 error
    @Test(description = "User should not be able to create a Project with a non-Latin ID", groups = {"Negative", "CRUD", "KnownBugs"}, dataProvider = "nonLatinIdProviderForId")
    public void userCannotCreateProjectWithNonLatinIdTest(String invalidId) {
        var invalidProject = TestDataGenerator.generate(List.of(), Project.class, invalidId, RandomData.getString());
        var response = projectController.createInvalidProject(invalidProject);
        ResponseValidator.checkErrorAndBody(response, HttpStatus.SC_BAD_REQUEST,
                "Project ID \"test_" + invalidId + "\" is invalid: contains non-latin letter '" + invalidId +
                        "'. ID should start with a latin letter and contain only latin letters, digits and underscores (at most 225 characters).");
    }


    @Test(description = "User should not be able to create Project with empty name", groups = {"Negative", "CRUD"})
    public void userCannotCreateProjectWithEmptyNameTest() {
        var invalidProject = TestDataGenerator.generate(List.of(), Project.class, RandomData.getString(), "");
        Response response = projectController.createInvalidProject(invalidProject);
        ResponseValidator.checkErrorAndBody(response, HttpStatus.SC_BAD_REQUEST, "Project name cannot be empty");
    }

    //Need to fix 500 error
    @Test(description = "User should not be able to create Project with empty id", groups = {"Negative", "CRUD", "KnownBugs"})
    public void userCannotCreateProjectWithEmptyIdTest() {
        var invalidProject = TestDataGenerator.generate(List.of(), Project.class, "", RandomData.getString());
        Response response = projectController.createInvalidProject(invalidProject);
        ResponseValidator.checkErrorAndBody(response, HttpStatus.SC_INTERNAL_SERVER_ERROR, "Project ID cannot be empty");
    }

    //Need to fix 500 error
    @Test(description = "User should not be able to create a Project with a space as ID", groups = {"Negative", "CRUD", "KnownBugs"})
    public void userCannotCreateProjectWithSpaceAsIdTest() {
        var invalidProject = TestDataGenerator.generate(List.of(), Project.class, " ", RandomData.getString());
        Response response = projectController.createInvalidProject(invalidProject);
        ResponseValidator.checkErrorAndBody(response, HttpStatus.SC_BAD_REQUEST, "Project ID must not be empty");
    }


    // Need to fix bug: 500 server error
    @Test(description = "User should not be able to create a Project with a space as name", groups = {"Negative", "CRUD", "KnownBugs"})
    public void userCannotCreateProjectWithSpaceAsNameTest() {
        var invalidProject = TestDataGenerator.generate(List.of(), Project.class, RandomData.getString(), " ");
        var response = projectController.createInvalidProject(invalidProject);
        ResponseValidator.checkErrorAndBody(response, HttpStatus.SC_BAD_REQUEST, "Given project name is empty");
    }


    @Test(description = "User should not be able to create a Project with an existing ID", groups = {"Negative", "CRUD"})
    public void userCannotCreateProjectWithExistingIdTest() {
        projectController.createProject(testData.getProject());
        var duplicateProject = TestDataGenerator.generate(List.of(testData.getProject()), Project.class, testData.getProject().getId(), RandomData.getString());
        Response response = projectController.createInvalidProject(duplicateProject);
        ResponseValidator.checkErrorAndBody(response, HttpStatus.SC_BAD_REQUEST, "Project ID \"" + testData.getProject().getId() + "\" is already used by another project");
    }

    @Test(description = "User should not be able to create a Project with an existing name", groups = {"Negative", "CRUD"})
    public void userCannotCreateProjectWithExistingNameTest() {
        projectController.createProject(testData.getProject());
        var duplicateProject = TestDataGenerator.generate(List.of(testData.getProject()), Project.class, RandomData.getString(), testData.getProject().getName());
        Response response = projectController.createInvalidProject(duplicateProject);
        ResponseValidator.checkErrorAndBody(response, HttpStatus.SC_BAD_REQUEST, "Project with this name already exists: " + testData.getProject().getName());
    }

    @Test(description = "User should not be able to create a Project with an existing name in a different case", groups = {"Negative", "CRUD"})
    public void userCannotCreateProjectWithExistingNameDifferentCaseTest() {
        projectController.createProject(testData.getProject());
        var duplicateName = testData.getProject().getName().toUpperCase();
        var response = projectController.createInvalidProject(TestDataGenerator.generate(List.of(), Project.class, RandomData.getString(), duplicateName));
        ResponseValidator.checkErrorAndBody(response, HttpStatus.SC_BAD_REQUEST, "Project with this name already exists: " + duplicateName);
    }


    @Test(description = "User should not be able to create a Project with an existing ID in a different case", groups = {"Negative", "CRUD"})
    public void userCannotCreateProjectWithExistingIdDifferentCaseTest() {
        projectController.createProject(testData.getProject());
        var duplicateId = testData.getProject().getId().toUpperCase();
        var response = projectController.createInvalidProject(TestDataGenerator.generate(List.of(), Project.class, duplicateId, RandomData.getString()));
        ResponseValidator.checkErrorAndBody(response, HttpStatus.SC_BAD_REQUEST, "Project ID \"" + duplicateId + "\" is already used by another project");
    }


    //Need to fix 500 error
    @Test(description = "User should not be able to create a Project with an ID consisting only of digits", groups = {"Negative", "CRUD", "KnownBugs"})
    public void userCannotCreateProjectWithDigitsOnlyIdTest() {
        var invalidProject = TestDataGenerator.generate(List.of(), Project.class, "123456", RandomData.getString());
        var response = projectController.createInvalidProject(invalidProject);
        ResponseValidator.checkErrorAndBody(response, HttpStatus.SC_BAD_REQUEST,
                "Project ID \"123456\" is invalid",
                "ID should start with a latin letter and contain only latin letters, digits and underscores");
    }

    @Test(description = "User should be able to create a Project with a name consisting only of digits", groups = {"Positive", "CRUD"})
    public void userCreatesProjectWithDigitsOnlyNameTest() {
        var validProject = TestDataGenerator.generate(List.of(), Project.class, RandomData.getString(), "123456");
        Response response = projectController.createProject(validProject);
        ResponseValidator.checkSuccessStatus(response, HttpStatus.SC_OK);
        Project createdProject = ResponseExtractor.extractModel(response, Project.class);
        TestValidator.validateEntityFields(validProject, createdProject, softy);
        softy.assertAll();
    }


    //Need to fix 500 error
    @Test(description = "User should not be able to create a Project with an ID starting with an underscore or a digit", groups = {"Negative", "CRUD", "KnownBugs"}, dataProvider = "invalidIdStartId")
    public void userCannotCreateProjectWithInvalidStartingCharacterIdTest(String invalidId) {
        var invalidProject = TestDataGenerator.generate(List.of(), Project.class, invalidId, RandomData.getString());
        var response = projectController.createInvalidProject(invalidProject);
        ResponseValidator.checkErrorAndBody(response, HttpStatus.SC_BAD_REQUEST,
                "Project ID \"" + invalidId + "\" is invalid",
                "ID should start with a latin letter and contain only latin letters, digits and underscores");
    }
    //Need to fix 500 error

    @Test(description = "User should not be able to create a Project with spaces in the middle of the ID", groups = {"Negative", "CRUD", "KnownBugs"})
    public void userCannotCreateProjectWithSpacesInIdTest() {
        var invalidProject = TestDataGenerator.generate(List.of(), Project.class, "invalid id", RandomData.getString());
        var response = projectController.createInvalidProject(invalidProject);
        ResponseValidator.checkErrorAndBody(response, HttpStatus.SC_BAD_REQUEST,
                "Project ID \"invalid id\" is invalid",
                "ID should start with a latin letter and contain only latin letters, digits and underscores");
    }


    @Test(description = "User should be able to create a Project with spaces in the middle of the name", groups = {"Positive", "CRUD"})
    public void userCreatesProjectWithSpacesInNameTest() {
        var validProject = TestDataGenerator.generate(List.of(), Project.class, RandomData.getString(), "valid name with spaces");
        Response response = projectController.createProject(validProject);
        ResponseValidator.checkSuccessStatus(response, HttpStatus.SC_OK);
        Project createdProject = ResponseExtractor.extractModel(response, Project.class);
        TestValidator.validateEntityFields(validProject, createdProject, softy);
        softy.assertAll();
    }

    @Test(description = "User should be able to create a Project with an ID containing Latin letters, digits, and underscores", groups = {"Positive", "CRUD"})
    public void userCreatesProjectWithValidIdCharactersTest() {
        var validProject = TestDataGenerator.generate(List.of(), Project.class, "valid_123_ID", RandomData.getString());
        Response response = projectController.createProject(validProject);
        ResponseValidator.checkSuccessStatus(response, HttpStatus.SC_OK);
        Project createdProject = ResponseExtractor.extractModel(response, Project.class);
        TestValidator.validateEntityFields(validProject, createdProject, softy);
        softy.assertAll();
    }


    @Test(description = "User should be able to create a project without specifying an ID", groups = {"Positive", "CRUD"})
    public void userCreatesProjectWithoutIdFieldTest() {
        var projectName = RandomData.getString();
        var projectWithoutId = TestDataGenerator.generate(List.of(), Project.class, null, projectName);
        Response response = projectController.createProject(projectWithoutId);
        ResponseValidator.checkSuccessStatus(response, HttpStatus.SC_OK);
        Project createdProject = ResponseExtractor.extractModel(response, Project.class);
        softy.assertNotNull(createdProject.getId(), "Project ID should not be null");
        softy.assertFalse(createdProject.getId().isEmpty(), "Project ID should not be empty");
        softy.assertEquals(createdProject.getName(), projectName, "Project name is incorrect");
        softy.assertAll();
    }

    //Need to fix 500 error
    @Test(description = "User should be able to create a Project with an empty ID String", groups = {"Positive", "CRUD", "KnownBugs"})
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
        var response = projectController.createInvalidProject(projectWithoutName);
        ResponseValidator.checkErrorAndBody(response, HttpStatus.SC_BAD_REQUEST, "Project name cannot be empty");
    }


    @Test(description = "User should not be able to create a Project if parent project locator is not provided", groups = {"Negative", "CRUD"})
    public void userCannotCreateProjectWithoutParentProjectLocatorTest() {
        var invalidProject = TestDataGenerator.generate(List.of(), Project.class, RandomData.getString(), RandomData.getString(), new ParentProject(null, null));
        var response = projectController.createInvalidProject(invalidProject);
        ResponseValidator.checkErrorAndBody(response, HttpStatus.SC_BAD_REQUEST, "No project specified", "Either 'id', 'internalId' or 'locator' attribute should be present");
    }

    @Test(description = "User should not be able to create a Project if parent project locator is empty", groups = {"Negative", "CRUD"})
    public void userCannotCreateProjectWithEmptyParentProjectLocatorTest() {
        var invalidProject = TestDataGenerator.generate(List.of(), Project.class, RandomData.getString(), RandomData.getString(), new ParentProject("", null));
        var response = projectController.createInvalidProject(invalidProject);
        ResponseValidator.checkErrorAndBody(response, HttpStatus.SC_NOT_FOUND, "No project found by locator 'count:1,id:'", "Project cannot be found by external id ''");
    }


    @Test(description = "User should not be able to create a project without authentication", groups = {"Negative", "Auth"})
    public void userCannotCreateProjectWithoutAuthTest() {
        var unauthProjectController = new ProjectController(Specifications.unauthSpec());
        var invalidProject = TestDataGenerator.generate(List.of(), Project.class, RandomData.getString(), RandomData.getString());
        var response = unauthProjectController.createInvalidProject(invalidProject);
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


}



