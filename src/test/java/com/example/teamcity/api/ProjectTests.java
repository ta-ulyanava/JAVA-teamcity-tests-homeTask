package com.example.teamcity.api;

import com.example.teamcity.BaseTest;
import com.example.teamcity.api.controllers.ProjectController;
import com.example.teamcity.api.enums.Endpoint;
import com.example.teamcity.api.generators.RandomData;
import com.example.teamcity.api.generators.TestDataGenerator;
import com.example.teamcity.api.models.ParentProject;
import com.example.teamcity.api.models.Project;
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
    public void userCreatesProjectWithMandatoryFieldsTest() {
        projectController.createProject(testData.getProject());
        var createdProject = projectController.getProjectById(testData.getProject().getId());
        softy.assertEquals(testData.getProject().getId(), createdProject.getId(), "Project id is not correct");
        softy.assertEquals(testData.getProject().getName(), createdProject.getName(), "Project name is not correct");
        softy.assertAll();
    }

    @Test(description = "User should be able to create Project with copyAllAssociatedSettings set to true", groups = {"Positive", "CRUD"})
    public void userCreatesProjectWithCopyAllAssociatedSettingsTest() {
        var projectWithCopyAll = generate(Arrays.asList(testData.getProject()), Project.class, testData.getProject().getId(), testData.getProject().getName(), null, true);
        projectController.createProject(projectWithCopyAll);
        var createdProject = projectController.getProjectById(projectWithCopyAll.getId());
        softy.assertEquals(projectWithCopyAll.getId(), createdProject.getId(), "Project ID does not match");
        softy.assertEquals(projectWithCopyAll.getName(), createdProject.getName(), "Project name does not match");
        softy.assertAll();
    }

    @Test(description = "User should be able to create a nested project", groups = {"Positive", "CRUD"})
    public void userCreatesNestedProjectTest() {
        projectController.createProject(testData.getProject());
        var nestedProjects = projectController.createNestedProjects(testData.getProject().getId(), 1);
        var secondProject = nestedProjects.get(0);
        var createdSecondProject = projectController.getProjectById(secondProject.getId());
        softy.assertEquals(createdSecondProject.getParentProject().getId(), testData.getProject().getId(), "Parent project ID is incorrect");
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
        projectController.createProject(rootProject);

        var nestedProjects = projectController.createNestedProjects(rootProject.getId(), 20);
        var lastNestedProject = nestedProjects.get(nestedProjects.size() - 1);
        var createdLastNestedProject = projectController.getProjectById(lastNestedProject.getId());

        softy.assertEquals(createdLastNestedProject.getParentProject().getId(), nestedProjects.get(nestedProjects.size() - 2).getId(), "Parent project ID is incorrect");
        softy.assertAll();
    }

    @Test(description = "User should be able to create 20 sibling projects under the same parent", groups = {"Positive", "CRUD", "CornerCase"})
    public void userCreates20SiblingProjectsTest() {
        projectController.createProject(testData.getProject());

        var siblingProjects = projectController.createSiblingProjects(testData.getProject().getId(), 20);

        softy.assertEquals(siblingProjects.size(), 20, "The number of created sibling projects is incorrect");

        siblingProjects.forEach(project -> {
            var createdProject = projectController.getProjectById(project.getId());
            softy.assertEquals(createdProject.getParentProject().getId(), testData.getProject().getId(),
                    "Parent project ID is incorrect for project " + project.getId());
        });

        softy.assertAll();
    }

    @Test(description = "User should not be able to create a Project with a non-existent parentProject locator", groups = {"Negative", "CRUD"})
    public void userCannotCreateProjectWithNonExistentParentProjectTest() {
        var response = projectController.createInvalidProject(generate(List.of(), Project.class, RandomData.getString(), RandomData.getString(), new ParentProject("non_existent_locator", null)));

        response.then().assertThat()
                .statusCode(HttpStatus.SC_NOT_FOUND)
                .body(Matchers.containsString("Project cannot be found by external id 'non_existent_locator'"));
    }

    @Test(description = "User should not be able to create a Project with the same ID as its parent ID", groups = {"Negative", "СRUD"})
    public void userCannotCreateProjectWithSameParentIdTest() {
        var invalidProject = TestDataGenerator.generate(List.of(), Project.class, testData.getProject().getId(), RandomData.getString(), new ParentProject(testData.getProject().getId(), null));

        var response = projectController.createInvalidProject(invalidProject);

        response.then().assertThat()
                .statusCode(HttpStatus.SC_NOT_FOUND)
                .body(Matchers.containsString("Project cannot be found by external id '%s'".formatted(testData.getProject().getId())));
    }


    @Test(description = "User should be able to create a Project with a name of 500 characters", groups = {"Positive", "CRUD", "CornerCase"})
    public void userCreatesProjectWith500LengthNameTest() {
        var maxLengthName = "A".repeat(500);
        var validProject = TestDataGenerator.generate(List.of(), Project.class, RandomData.getString(), maxLengthName);

        projectController.createProject(validProject);
        var createdProject = projectController.getProjectById(validProject.getId());

        softy.assertEquals(createdProject.getName(), validProject.getName(), "Project name is incorrect");
        softy.assertAll();
    }

    @Test(description = "User should be able to create a Project with an ID of maximum allowed length", groups = {"Positive", "CRUD"})
    public void userCreatesProjectWithMaxLengthIdTest() {
        var maxLengthId = "A".repeat(225);
        var validProject = TestDataGenerator.generate(List.of(), Project.class, maxLengthId, RandomData.getString());

        projectController.createProject(validProject);
        var createdProject = projectController.getProjectById(validProject.getId());

        softy.assertEquals(createdProject.getId(), validProject.getId(), "Project ID is incorrect");
        softy.assertAll();
    }
    //To fix 500 (Internal Server Error).
    @Test(description = "User should not be able to create a Project with an ID longer than 225 characters", groups = {"Negative", "CRUD", "KnownBugs", "CornerCase"})
    public void userCannotCreateProjectWithTooLongIdTest() {
        var tooLongId = "A".repeat(226);
        var invalidProject = TestDataGenerator.generate(List.of(), Project.class, tooLongId, RandomData.getString());

        var response = projectController.createInvalidProject(invalidProject);

        response.then().assertThat()
                .statusCode(HttpStatus.SC_BAD_REQUEST)
                .body(Matchers.containsString("Project ID \"%s\" is invalid: it is 226 characters long while the maximum length is 225.".formatted(tooLongId)));
    }



    @Test(description = "User should be able to create a Project with an ID of length 1", groups = {"Positive", "CRUD"})
    public void userCreatesProjectWithOneCharacterIdTest() {
        var validProject = TestDataGenerator.generate(List.of(), Project.class, "A", RandomData.getString());

        projectController.createProject(validProject);
        var createdProject = projectController.getProjectById(validProject.getId());

        softy.assertEquals(createdProject.getId(), validProject.getId(), "Project ID is incorrect");
        softy.assertEquals(createdProject.getName(), validProject.getName(), "Project name is incorrect");
        softy.assertAll();
    }

    @Test(description = "User should be able to create a Project with a name of length 1", groups = {"Positive", "CRUD"})
    public void userCreatesProjectWithOneCharacterNameTest() {
        var validProject = TestDataGenerator.generate(List.of(), Project.class, RandomData.getString(), "A");

        projectController.createProject(validProject);
        var createdProject = projectController.getProjectById(validProject.getId());

        softy.assertEquals(createdProject.getId(), validProject.getId(), "Project ID is incorrect");
        softy.assertEquals(createdProject.getName(), validProject.getName(), "Project name is incorrect");
        softy.assertAll();
    }


    @Test(description = "User should be able to create a Project with special characters in name", groups = {"Positive", "CRUD"})
    public void userCreatesProjectWithSpecialCharactersInNameTest() {
        var project = TestDataGenerator.generate(List.of(), Project.class, RandomData.getString(), RandomData.getFullSpecialCharacterString());
        projectController.createProject(project);
        var createdProject = projectController.getProjectById(project.getId());

        softy.assertEquals(createdProject.getId(), project.getId(), "Project ID is incorrect");
        softy.assertEquals(createdProject.getName(), project.getName(), "Project name is incorrect");
        softy.assertAll();
    }

    @Test(description = "User should be able to create a Project with an ID containing an underscore", groups = {"Positive", "CRUD"})
    public void userCreatesProjectWithUnderscoreInIdTest() {
        var validProject = TestDataGenerator.generate(List.of(), Project.class, "test_project_123", RandomData.getString());

        projectController.createProject(validProject);
        var createdProject = projectController.getProjectById(validProject.getId());

        softy.assertEquals(createdProject.getId(), validProject.getId(), "Project ID is incorrect");
        softy.assertEquals(createdProject.getName(), validProject.getName(), "Project name is incorrect");
        softy.assertAll();
    }

    @Test(description = "User should be able to create a Project with a localized name", groups = {"Positive", "CRUD"})
    public void userCreatesProjectWithLocalizedNameTest() {
        var localizedProject = TestDataGenerator.generate(List.of(), Project.class, RandomData.getString(), RandomData.getFullLocalizationString());
        projectController.createProject(localizedProject);
        var createdProject = projectController.getProjectById(localizedProject.getId());

        softy.assertEquals(createdProject.getName(), localizedProject.getName(), "Localized project name is incorrect");
        softy.assertAll();
    }

    // Need to fix 500 server Error
    @Test(description = "User should not be able to create a Project with special characters in ID",
            groups = {"Negative", "CRUD", "KnownBugs"},
            dataProvider = "invalidSpecialCharactersForId")
    public void userCannotCreateProjectWithEachSpecialCharacterInIdTest(String specialChar) {
        var invalidProject = TestDataGenerator.generate(List.of(), Project.class, "test_" + specialChar, "ValidName");

        var response = projectController.createInvalidProject(invalidProject);

        response.then().assertThat()
                .statusCode(HttpStatus.SC_BAD_REQUEST)
                .body(Matchers.containsString("Project ID \"test_%s\" is invalid".formatted(specialChar)))
                .body(Matchers.containsString("ID should start with a latin letter and contain only latin letters, digits and underscores"));
    }

    //Need to fix 500 error
    @Test(description = "User should not be able to create a Project with a non-Latin ID", groups = {"Negative", "CRUD", "KnownBugs"}, dataProvider = "nonLatinIdProviderForId")
    public void userCannotCreateProjectWithNonLatinIdTest(String invalidId) {
        var invalidProject = TestDataGenerator.generate(List.of(), Project.class, invalidId, RandomData.getString());
        var response = projectController.createInvalidProject(invalidProject);

        response.then().assertThat()
                .statusCode(HttpStatus.SC_BAD_REQUEST)
                .body(Matchers.containsString("Project ID \"test_%s\" is invalid: contains non-latin letter '%s'. ID should start with a latin letter and contain only latin letters, digits and underscores (at most 225 characters).".formatted(invalidId)));
    }


    @Test(description = "User should not be able to create Project with empty name", groups = {"Negative", "CRUD"})
    public void userCannotCreateProjectWithEmptyNameTest() {
        var invalidProject = TestDataGenerator.generate(List.of(), Project.class, RandomData.getString(), "");
        Response response = projectController.createInvalidProject(invalidProject);
        response.then().assertThat()
                .statusCode(HttpStatus.SC_BAD_REQUEST)
                .body(Matchers.containsString("Project name cannot be empty"));
    }
// Need to fix a bug 500 (Internal Server Error).
    @Test(description = "User should not be able to create Project with empty id", groups = {"Negative", "CRUD", "KnownBugs"})
    public void userCannotCreateProjectWithEmptyIdTest() {
        var invalidProject = TestDataGenerator.generate(List.of(), Project.class, "", RandomData.getString());
        Response response = projectController.createInvalidProject(invalidProject);
        response.then().assertThat()
                .statusCode(HttpStatus.SC_INTERNAL_SERVER_ERROR)
                .body(Matchers.containsString("Project ID cannot be empty"));
    }

    // Need to fix bug: 500 server error
    @Test(description = "User should not be able to create a Project with a space as ID", groups = {"Negative", "CRUD", "KnownBugs"})
    public void userCannotCreateProjectWithSpaceAsIdTest() {
        var invalidProject = TestDataGenerator.generate(List.of(), Project.class, " ", RandomData.getString());

        var response = projectController.createInvalidProject(invalidProject);

        response.then().assertThat()
                .statusCode(HttpStatus.SC_BAD_REQUEST)
                .body(Matchers.containsString("Project ID must not be empty"));
    }

    // Need to fix bug: 500 server error
    @Test(description = "User should not be able to create a Project with a space as name", groups = {"Negative", "CRUD", "KnownBugs"})
    public void userCannotCreateProjectWithSpaceAsNameTest() {
        var invalidProject = TestDataGenerator.generate(List.of(), Project.class, RandomData.getString(), " ");

        var response = projectController.createInvalidProject(invalidProject);

        response.then().assertThat()
                .statusCode(HttpStatus.SC_BAD_REQUEST)
                .body(Matchers.containsString("Given project name is empty"));
    }

    @Test(description = "User should not be able to create a Project with an existing ID", groups = {"Negative", "CRUD"})
    public void userCannotCreateProjectWithExistingIdTest() {
        projectController.createProject(testData.getProject());

        var duplicateProject = TestDataGenerator.generate(List.of(testData.getProject()), Project.class, testData.getProject().getId(), RandomData.getString());

        Response response = projectController.createInvalidProject(duplicateProject);

        response.then().assertThat()
                .statusCode(HttpStatus.SC_BAD_REQUEST)
                .body(Matchers.containsString("Project ID \"%s\" is already used by another project".formatted(testData.getProject().getId())));
    }

    @Test(description = "User should not be able to create a Project with an existing name", groups = {"Negative", "CRUD"})
    public void userCannotCreateProjectWithExistingNameTest() {
        projectController.createProject(testData.getProject());

        var duplicateProject = TestDataGenerator.generate(List.of(testData.getProject()), Project.class, RandomData.getString(), testData.getProject().getName());

        Response response = projectController.createInvalidProject(duplicateProject);

        response.then().assertThat()
                .statusCode(HttpStatus.SC_BAD_REQUEST)
                .body(Matchers.containsString("Project with this name already exists: %s".formatted(testData.getProject().getName())));
    }
    @Test(description = "User should not be able to create a Project with an existing name in a different case", groups = {"Negative", "CRUD"})
    public void userCannotCreateProjectWithExistingNameDifferentCaseTest() {
        projectController.createProject(testData.getProject());

        var duplicateName = testData.getProject().getName().toUpperCase();
        var response = projectController.createInvalidProject(TestDataGenerator.generate(List.of(), Project.class, RandomData.getString(), duplicateName));

        response.then().assertThat()
                .statusCode(HttpStatus.SC_BAD_REQUEST)
                .body(Matchers.containsString("Project with this name already exists: %s".formatted(duplicateName)));
    }


    @Test(description = "User should not be able to create a Project with an existing ID in a different case", groups = {"Negative", "CRUD"})
    public void userCannotCreateProjectWithExistingIdDifferentCaseTest() {
        projectController.createProject(testData.getProject());

        var duplicateId = testData.getProject().getId().toUpperCase();
        var response = projectController.createInvalidProject(TestDataGenerator.generate(List.of(), Project.class, duplicateId, RandomData.getString()));

        response.then().assertThat()
                .statusCode(HttpStatus.SC_BAD_REQUEST)
                .body(Matchers.containsString("Project ID \"%s\" is already used by another project".formatted(duplicateId)));
    }
    @Test(description = "User should not be able to create a Project with an ID consisting only of digits", groups = {"Negative", "CRUD", "KnownBugs"})
    public void userCannotCreateProjectWithDigitsOnlyIdTest() {
        var invalidProject = TestDataGenerator.generate(List.of(), Project.class, "123456", RandomData.getString());

        var response = projectController.createInvalidProject(invalidProject);

        response.then().assertThat()
                .statusCode(HttpStatus.SC_BAD_REQUEST)
                .body(Matchers.containsString("Project ID \"123456\" is invalid"))
                .body(Matchers.containsString("ID should start with a latin letter and contain only latin letters, digits and underscores"));
    }
    @Test(description = "User should be able to create a Project with a name consisting only of digits", groups = {"Positive", "Validation"})
    public void userCreatesProjectWithDigitsOnlyNameTest() {
        var validProject = TestDataGenerator.generate(List.of(), Project.class, RandomData.getString(), "123456");

        projectController.createProject(validProject);
        var createdProject = projectController.getProjectById(validProject.getId());

        softy.assertEquals(createdProject.getName(), validProject.getName(), "Project name is incorrect");
        softy.assertAll();
    }


    @Test(description = "User should not be able to create a Project with an ID starting with an underscore or a digit",
            groups = {"Negative", "CRUD", "KnownBugs"}, dataProvider = "invalidIdStartId")
    public void userCannotCreateProjectWithInvalidStartingCharacterIdTest(String invalidId) {
        var invalidProject = TestDataGenerator.generate(List.of(), Project.class, invalidId, RandomData.getString());

        var response = projectController.createInvalidProject(invalidProject);

        response.then().assertThat()
                .statusCode(HttpStatus.SC_BAD_REQUEST)
                .body(Matchers.containsString("Project ID \"%s\" is invalid".formatted(invalidId)))
                .body(Matchers.containsString("ID should start with a latin letter and contain only latin letters, digits and underscores"));
    }
    //Fix bug 500 (Internal Server Error).
    @Test(description = "User should not be able to create a Project with spaces in the middle of the ID",
            groups = {"Negative", "CRUD", "KnownBugs"})
    public void userCannotCreateProjectWithSpacesInIdTest() {
        var invalidProject = TestDataGenerator.generate(List.of(), Project.class, "invalid id", RandomData.getString());

        var response = projectController.createInvalidProject(invalidProject);

        response.then().assertThat()
                .statusCode(HttpStatus.SC_BAD_REQUEST)
                .body(Matchers.containsString("Project ID \"invalid id\" is invalid"))
                .body(Matchers.containsString("ID should start with a latin letter and contain only latin letters, digits and underscores"));
    }

    @Test(description = "User should be able to create a Project with spaces in the middle of the name",
            groups = {"Positive", "CRUD"})
    public void userCreatesProjectWithSpacesInNameTest() {
        var validProject = TestDataGenerator.generate(List.of(), Project.class, RandomData.getString(), "valid name with spaces");

        projectController.createProject(validProject);
        var createdProject = projectController.getProjectById(validProject.getId());

        softy.assertEquals(createdProject.getId(), validProject.getId(), "Project ID is incorrect");
        softy.assertEquals(createdProject.getName(), validProject.getName(), "Project name is incorrect");
        softy.assertAll();
    }
    @Test(description = "User should be able to create a Project with an ID containing Latin letters, digits, and underscores",
            groups = {"Positive", "CRUD"})
    public void userCreatesProjectWithValidIdCharactersTest() {
        var validProject = TestDataGenerator.generate(List.of(), Project.class, "valid_123_ID", RandomData.getString());

        projectController.createProject(validProject);
        var createdProject = projectController.getProjectById(validProject.getId());

        softy.assertEquals(createdProject.getId(), validProject.getId(), "Project ID is incorrect");
        softy.assertEquals(createdProject.getName(), validProject.getName(), "Project name is incorrect");
        softy.assertAll();
    }
    @Test(description = "User should not be able to create a Project without an ID",
            groups = {"Negative", "Validation"})
    public void userCannotCreateProjectWithoutIdTest() {
        var invalidProject = TestDataGenerator.generate(List.of(), Project.class, null, RandomData.getString());

        var response = projectController.createInvalidProject(invalidProject);

        response.then().assertThat()
                .statusCode(HttpStatus.SC_BAD_REQUEST)
                .body(Matchers.containsString("Project ID cannot be empty"));
    }
    @Test(description = "User should be able to create a Project with an empty ID",
            groups = {"Positive", "CRUD"})
    public void userCreatesProjectWithEmptyIdTest() {
        var projectWithEmptyId = TestDataGenerator.generate(List.of(), Project.class, "", RandomData.getString());

        projectController.createProject(projectWithEmptyId);
        var createdProject = projectController.getProjectById(projectWithEmptyId.getId());

        softy.assertEquals(createdProject.getId(), projectWithEmptyId.getId(), "Project ID is incorrect");
        softy.assertEquals(createdProject.getName(), projectWithEmptyId.getName(), "Project name is incorrect");
        softy.assertAll();
    }
    @Test(description = "User should be able to create a Project without specifying an ID", groups = {"Positive", "CRUD"})
    public void userCreatesProjectWithoutIdTest() {
        var projectName = RandomData.getString();
        var projectWithoutId = TestDataGenerator.generate(List.of(), Project.class, null, projectName);

        var response = projectController.createInvalidProject(projectWithoutId);

        response.then().assertThat()
                .statusCode(HttpStatus.SC_OK)
                .body("name", Matchers.equalTo(projectName));
    }


    @Test(description = "User should be able to create a Project without specifying a name", groups = {"Positive", "CRUD"})
    public void userCreatesProjectWithoutNameTest() {
        var projectId = RandomData.getString();
        var projectWithoutName = TestDataGenerator.generate(List.of(), Project.class, projectId, null);

        var response = projectController.createInvalidProject(projectWithoutName);

        response.then().assertThat()
                .statusCode(HttpStatus.SC_OK)
                .body("id", Matchers.equalTo(projectId))
                .body("name", Matchers.notNullValue()); // Проверяем, что имя не осталось пустым
    }




    @Test(description = "User should not be able to create a project without authentication", groups = {"Negative", "Auth"})
    public void userCannotCreateProjectWithoutAuthTest() {
        var unauthProjectController = new ProjectController(Specifications.unauthSpec());
        var invalidProject = TestDataGenerator.generate(List.of(), Project.class, RandomData.getString(), RandomData.getString());
        var response = unauthProjectController.createInvalidProject(invalidProject);

        response.then().assertThat()
                .statusCode(HttpStatus.SC_UNAUTHORIZED)
                .body(Matchers.containsString("Authentication required"));
    }

    @Test(description = "User should be able to create a Project with an XSS payload in name (payload stored as text)", groups = {"Positive", "Security"})
    public void userCreatesProjectWithXSSInNameTest() {
        var xssPayload = "<script>alert('XSS')</script>";
        var projectWithXSS = TestDataGenerator.generate(List.of(), Project.class, RandomData.getString(), xssPayload);

        projectController.createProject(projectWithXSS);
        var createdProject = projectController.getProjectById(projectWithXSS.getId());

        softy.assertEquals(createdProject.getName(), xssPayload, "XSS payload was modified or blocked");
        softy.assertAll();
    }

    @Test(description = "User should be able to create a Project with an SQL injection payload in name (payload stored as text)", groups = {"Positive", "Security"})
    public void userCreatesProjectWithSQLInjectionTest() {
        var sqlPayload = "'; DROP TABLE projects; --";
        var projectWithSQL = TestDataGenerator.generate(List.of(), Project.class, RandomData.getString(), sqlPayload);

        projectController.createProject(projectWithSQL);
        var createdProject = projectController.getProjectById(projectWithSQL.getId());

        softy.assertEquals(createdProject.getName(), sqlPayload, "SQL injection payload was modified or blocked");
        softy.assertAll();
    }

}



