package com.example.teamcity.api;
import com.example.teamcity.api.constants.TestConstants;
import com.example.teamcity.api.enums.ApiEndpoint;
import com.example.teamcity.api.enums.Role;
import com.example.teamcity.api.generators.RandomData;
import com.example.teamcity.api.generators.TestData;
import com.example.teamcity.api.generators.TestDataGenerator;
import com.example.teamcity.api.models.ParentProject;
import com.example.teamcity.api.models.Project;
import com.example.teamcity.api.models.User;
import com.example.teamcity.api.requests.CheckedRequest;
import com.example.teamcity.api.requests.UncheckedRequest;
import com.example.teamcity.api.responses.ResponseExtractor;
import com.example.teamcity.api.spec.request.RequestSpecs;
import com.example.teamcity.api.spec.responce.AccessErrorSpecs;
import com.example.teamcity.api.spec.responce.IncorrectDataSpecs;
import com.example.teamcity.api.validation.EntityValidator;
import io.qameta.allure.Feature;
import io.qameta.allure.Story;
import io.restassured.response.Response;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

import java.util.List;

import static com.example.teamcity.api.constants.TestConstants.SQL_INJECTION_PAYLOAD;
import static com.example.teamcity.api.constants.TestConstants.XSS_PAYLOAD;

@Test(groups = {"Regression"})
public class ProjectCrudTest extends BaseApiTest {



    @Test(description = "User should be able to create a project with the minimum required fields under Root project",
            groups = {"Positive", "CRUD"})
    public void userCreatesProjectWithMandatoryFieldsOnlyTest() {
        final String ROOT_PROJECT_ID = "_Root";
        Project project = testData.getProject();
        Project createdProject = createProjectAndExtractModel(project);
        EntityValidator.validateAllEntityFieldsIgnoring(project, createdProject, List.of("parentProject"), softy);
        softy.assertEquals(createdProject.getParentProject().getId(), ROOT_PROJECT_ID, "Parent project should be '_Root' when not specified");
        softy.assertAll();
    }

// =================== PROJECT COPY SETTINGS TESTS (COPY_SETTINGS_TAG) =================== //
// Bug in API: projectsIdsMap, buildTypesIdsMap, vcsRootsIdsMap, sourceProject should be copied but are not
@Feature("Project Copy Settings")
@Story("Copy Project Parameters")
@Test(description = "User should be able to create a Project with copyAllAssociatedSettings set to true and verify copied settings",
        groups = {"Positive", "CRUD", "KnownBugs", "COPY_SETTINGS_TAG"})
public void userCreatesProjectWithCopyAllAssociatedSettingsTrueTest() {
    var sourceProject = createProjectAndExtractModel(testData.getProject());
    var newProject = TestDataGenerator.generate(Project.class, RandomData.getString(), RandomData.getString(),sourceProject.getParentProject(), true, sourceProject);
    var createdProject = createProjectAndExtractModel(newProject);

    // Баг в API: settings не копируются
    EntityValidator.validateAllEntityFieldsIgnoring(sourceProject, createdProject, List.of("id", "name"), softy);
    softy.assertEquals(createdProject.getProjectsIdsMap(), sourceProject.getProjectsIdsMap(), "projectsIdsMap не был скопирован");
    softy.assertEquals(createdProject.getBuildTypesIdsMap(), sourceProject.getBuildTypesIdsMap(), "buildTypesIdsMap не был скопирован");
    softy.assertEquals(createdProject.getVcsRootsIdsMap(), sourceProject.getVcsRootsIdsMap(), "vcsRootsIdsMap не был скопирован");
    softy.assertEquals(createdProject.getSourceProject(), sourceProject, "sourceProject не был скопирован");
    softy.assertAll();
}

    @Feature("Project Copy Settings")
    @Story("Copy Settings Disabled")
    @Test(description = "User should be able to create a Project with copyAllAssociatedSettings set to false and verify fields are NOT copied",
            groups = {"Positive", "CRUD", "COPY_SETTINGS_TAG"})
    public void userCreatesProjectWithCopyAllAssociatedSettingsFalseTest() {
        var sourceProject = createProjectAndExtractModel(testData.getProject());
        var newProject = TestDataGenerator.generate(Project.class, RandomData.getString(), RandomData.getString(), new ParentProject("_Root", null), false, sourceProject);
        var createdProject = createProjectAndExtractModel(newProject);
        EntityValidator.validateAllEntityFieldsIgnoring(sourceProject, createdProject,
                List.of("id", "name", "parentProject", "copyAllAssociatedSettings", "sourceProject",
                        "projectsIdsMap", "buildTypesIdsMap", "vcsRootsIdsMap"), softy);
        softy.assertNull(createdProject.getCopyAllAssociatedSettings(), "copyAllAssociatedSettings должен быть null");
        softy.assertNull(createdProject.getSourceProject(), "sourceProject должен быть null");
        softy.assertNull(createdProject.getProjectsIdsMap(), "projectsIdsMap должен быть null");
        softy.assertNull(createdProject.getBuildTypesIdsMap(), "buildTypesIdsMap должен быть null");
        softy.assertNull(createdProject.getVcsRootsIdsMap(), "vcsRootsIdsMap должен быть null");
        softy.assertAll();
    }

    // =================== PROJECT COPY SETTINGS TESTS (COPY_SETTINGS_TAG) =================== //

// =================== NESTED AND SIBLING PROJECTS TESTS (PROJECT_HIERARCHY_TAG) =================== //
    @Feature("Project Management")
    @Story("Creating nested projects")
    @Test(description = "User should be able to create 20 nested projects",
            groups = {"Positive", "CRUD", "CornerCase"})
    public void userCreatesNestedProjectsTest() {
        var rootProject = createProjectAndExtractModel(testData.getProject());
        int projectCount = 20;
        List<Project> projects = TestData.nestedProjects(projectCount);
        projects.forEach(this::createProjectAndExtractModel);
        softy.assertEquals(projects.size(), projectCount, "The number of created projects is incorrect");
        projects.forEach(project -> softy.assertEquals(project.getParentProject().getId(),
                projects.indexOf(project) > 0 ? projects.get(projects.indexOf(project) - 1).getId() : rootProject.getId(),
                "Parent project ID is incorrect for project " + project.getId()));
        softy.assertAll();
    }
    @Feature("Project Management")
    @Story("Creating sibling projects")
    @Test(description = "User should be able to create 20 sibling projects",
            groups = {"Positive", "CRUD", "CornerCase"})
    public void userCreatesSiblingProjectsTest() {
        var rootProject = createProjectAndExtractModel(testData.getProject());
        int projectCount = 20;
        List<Project> projects = TestData.siblingProjects(rootProject.getId(), projectCount);
        projects.forEach(this::createProjectAndExtractModel);
        softy.assertEquals(projects.size(), projectCount, "The number of created projects is incorrect");
        projects.forEach(project -> softy.assertEquals(project.getParentProject().getId(), rootProject.getId(), "Parent project ID is incorrect for project " + project.getId()));
        softy.assertAll();
    }

// =================== PROJECT ID VALIDATION TESTS (PROJECT_ID_VALIDATION_TAG) =================== //
@Feature("Project ID Validation")
@Story("Max Length ID")
@Test(description = "User should be able to create a Project with an ID of maximum allowed length", groups = {"Positive", "CRUD", "PROJECT_ID_VALIDATION_TAG"})
public void userCreatesProjectWithMaxLengthIdTest() {
    Project validProject = TestDataGenerator.generate(Project.class, RandomData.getString(225), RandomData.getString());
    Project createdProject = ResponseExtractor.extractModel(new CheckedRequest(RequestSpecs.superUserAuthSpec()).getRequest(ApiEndpoint.PROJECTS).create(validProject), Project.class);
    EntityValidator.validateAllEntityFieldsIgnoring(validProject, createdProject, List.of("parentProject"), softy);
    softy.assertAll();
}

    @Feature("Project ID Validation")
    @Story("Min Length ID")
    @Test(description = "User should be able to create a Project with an ID of length 1", groups = {"Positive", "CRUD", "PROJECT_ID_VALIDATION_TAG"})
    public void userCreatesProjectWithOneCharacterIdTest() {
        String minLengthId = RandomData.getString(1);
        Project validProject = TestDataGenerator.generate(Project.class, minLengthId, RandomData.getString());
        Project createdProject = ResponseExtractor.extractModel(new CheckedRequest(RequestSpecs.superUserAuthSpec()).getRequest(ApiEndpoint.PROJECTS).create(validProject), Project.class);
        EntityValidator.validateAllEntityFieldsIgnoring(validProject, createdProject, List.of("parentProject"), softy);
        softy.assertAll();
    }
    @Feature("Project ID Validation")
    @Story("Underscore in ID")
    @Test(description = "User should be able to create a Project with an ID containing an underscore", groups = {"Positive", "CRUD", "PROJECT_ID_VALIDATION_TAG"})
    public void userCreatesProjectWithUnderscoreInIdTest() {
        String idWithUnderscore = RandomData.getString() + "_test";
        Project projectWithUnderscore = TestDataGenerator.generate(Project.class, idWithUnderscore, RandomData.getString());
        Response response = new CheckedRequest(RequestSpecs.superUserAuthSpec()).getRequest(ApiEndpoint.PROJECTS).create(projectWithUnderscore);
        Project createdProject = ResponseExtractor.extractModel(response, Project.class);
        EntityValidator.validateAllEntityFieldsIgnoring(projectWithUnderscore, createdProject, List.of("parentProject"), softy);
        softy.assertAll();
    }

    @Feature("Project ID Validation")
    @Story("Project ID Length Exceeded")
    @Test(description = "User should not be able to create a Project with an ID longer than 225 characters", groups = {"Negative","CRUD","KnownBugs","CornerCase","PROJECT_ID_VALIDATION_TAG"})
    public void userCannotCreateProjectWithTooLongIdTest() {
        var tooLongId = RandomData.getString(226);
        var invalidProject = TestDataGenerator.generate(List.of(), Project.class, tooLongId, RandomData.getString());
        var response = new UncheckedRequest(RequestSpecs.superUserAuthSpec()).getRequest(ApiEndpoint.PROJECTS).create(invalidProject);
        response.then().spec(IncorrectDataSpecs.badRequestFieldTooLong("Project", "ID", tooLongId, 225));
        softy.assertAll();
    }

    @Feature("Project ID Validation")
    @Story("Special Characters in Project ID")
    @DataProvider(name = "invalidSpecialCharactersForId")
    public static Object[][] invalidSpecialCharactersForId() {
        return "!@#$%^&*()+-={}[]:\\".chars()
                .mapToObj(c -> new Object[]{String.valueOf((char) c)})
                .toArray(Object[][]::new);
    }
    // Need to fix 500 error (Known Bugs)
    @Test(description = "User should not be able to create a Project with special characters in ID", groups = {"Negative", "CRUD", "KnownBugs", "PROJECT_ID_VALIDATION_TAG"}, dataProvider = "invalidSpecialCharactersForId")
    public void userCannotCreateProjectWithEachSpecialCharacterInIdTest(String specialChar) {
        var invalidId = "test_" + specialChar;
        var invalidProject = TestDataGenerator.generate(List.of(), Project.class, invalidId, RandomData.getString());
        var response = new UncheckedRequest(RequestSpecs.superUserAuthSpec()).getRequest(ApiEndpoint.PROJECTS).create(invalidProject);
        response.then().spec(IncorrectDataSpecs.badRequestUnsupportedCharacter("Project", "ID", invalidId, specialChar));
        softy.assertAll();
    }

    @Feature("Project ID Validation")
    @Story("Non-Latin Characters in Project ID")
    @DataProvider
    public static Object[][] nonLatinIdProviderForId() {
        return new Object[][]{{"проект"}, {"项目"}, {"プロジェクト"}, {"مشروع"}, {"παράδειγμα"}, {"नमूना"}, {"בדיקה"}};
    }
    // Need to fix 500 error (Known Bugs)
    @Test(description = "User should not be able to create a Project with a non-Latin ID",
            groups = {"Negative", "CRUD", "KnownBugs", "PROJECT_ID_VALIDATION_TAG"}, dataProvider = "nonLatinIdProviderForId")
    public void userCannotCreateProjectWithNonLatinIdTest(String invalidId) {
        var invalidProject = TestDataGenerator.generate(List.of(), Project.class, invalidId, RandomData.getString());
        var response = new UncheckedRequest(RequestSpecs.superUserAuthSpec()).getRequest(ApiEndpoint.PROJECTS).create(invalidProject);
        response.then().spec(IncorrectDataSpecs.badRequestNonLatinLetter("Project","ID", invalidId));
        softy.assertAll();
    }

@Feature("Project ID Validation")
@Story("Empty ID")
// Need to fix 500 error (Known Bugs)
@Test(description = "User should not be able to create Project with empty id", groups = {"Negative", "CRUD", "KnownBugs", "PROJECT_ID_VALIDATION_TAG"})
public void userCannotCreateProjectWithEmptyIdTest() {
    Project invalidProject = TestDataGenerator.generate(Project.class, "", RandomData.getString());
    Response response = new UncheckedRequest(RequestSpecs.superUserAuthSpec()).getRequest(ApiEndpoint.PROJECTS).create(invalidProject);
    response.then().spec(IncorrectDataSpecs.badRequestEmptyField("Project", "ID"));
    softy.assertAll();
}

    @Feature("Project ID Validation")
    @Story("ID with Space")
// Need to fix 500 error (Known Bugs)
    @Test(description = "User should not be able to create a Project with a space as ID", groups = {"Negative", "CRUD", "KnownBugs", "PROJECT_ID_VALIDATION_TAG"})
    public void userCannotCreateProjectWithSpaceAsIdTest() {
        Project invalidProject = TestDataGenerator.generate(Project.class, " ", RandomData.getString());
        Response response = new UncheckedRequest(RequestSpecs.superUserAuthSpec()).getRequest(ApiEndpoint.PROJECTS).create(invalidProject);
        response.then().spec(IncorrectDataSpecs.badRequestEmptyField("Project", "ID"));
        softy.assertAll();
    }
    @Feature("Project ID Validation")
    @Story("Duplicate Project ID")
    @Test(description = "User should not be able to create a Project with an existing ID", groups = {"Negative", "CRUD", "PROJECT_ID_VALIDATION_TAG"})
    public void userCannotCreateProjectWithExistingIdTest() {
        Project existingProject = createProjectAndExtractModel(testData.getProject());
        Project duplicateProject = TestDataGenerator.generate(List.of(existingProject), Project.class, existingProject.getId(), RandomData.getString());
        Response response = new UncheckedRequest(RequestSpecs.superUserAuthSpec()).getRequest(ApiEndpoint.PROJECTS).create(duplicateProject);
        response.then().spec(IncorrectDataSpecs.badRequestDuplicatedField("Project","ID", existingProject.getId() ));
        softy.assertAll();
    }

    @Feature("Project ID Validation")
    @Story("Duplicate Project ID with Different Case")
    @Test(description = "User should not be able to create a Project with an existing ID in a different case", groups = {"Negative", "CRUD", "PROJECT_ID_VALIDATION_TAG"})
    public void userCannotCreateProjectWithExistingIdDifferentCaseTest() {
        Project existingProject = createProjectAndExtractModel(testData.getProject());
        String duplicateId = existingProject.getId().toUpperCase();
        Project duplicateProject = TestDataGenerator.generate(List.of(), Project.class, duplicateId, RandomData.getString());
        Response response = new UncheckedRequest(RequestSpecs.superUserAuthSpec()).getRequest(ApiEndpoint.PROJECTS).create(duplicateProject);
        response.then().spec(IncorrectDataSpecs.badRequestDuplicatedField("Project", "ID", duplicateId));
        softy.assertAll();
    }

    // Need to fix 500 error (Known Bugs)
    @Feature("Project ID Validation")
    @Story("Invalid Project ID")
    @Test(description = "User should not be able to create a Project with an ID consisting only of digits",
            groups = {"Negative", "CRUD", "KnownBugs", "PROJECT_ID_VALIDATION_TAG"})
    public void userCannotCreateProjectWithDigitsOnlyIdTest() {
        String invalidId = RandomData.getDigits(6);
        Project invalidProject = TestDataGenerator.generate(List.of(), Project.class, invalidId, RandomData.getString());
        Response response = new UncheckedRequest(RequestSpecs.superUserAuthSpec()).getRequest(ApiEndpoint.PROJECTS).create(invalidProject);
        response.then().spec(IncorrectDataSpecs.badRequestWithIncorrectFieldFormat("Project", "ID", invalidId , String.valueOf(invalidId.charAt(0))));
        softy.assertAll();
    }
    @Feature("Project ID Validation")
    @Story("Invalid Starting Character in Project ID")
    @DataProvider
    public static Object[][] invalidIdStartId() {
        String randomString = RandomData.getString(9);
        return new Object[][]{
                {RandomData.getDigits(1) + randomString},
                {"_" + randomString}
        };
    }

    // Need to fix 500 error (Known Bugs)
    @Feature("Project ID Validation")
    @Story("Invalid Starting Character in Project ID")
    @Test(description = "User should not be able to create a Project with an ID starting with an underscore or a digit",
            groups = {"Negative", "CRUD", "KnownBugs", "PROJECT_ID_VALIDATION_TAG"},
            dataProvider = "invalidIdStartId")
    public void userCannotCreateProjectWithInvalidStartingCharacterIdTest(String invalidId) {
        Project invalidProject = TestDataGenerator.generate(List.of(), Project.class, invalidId, RandomData.getString());
        Response response = new UncheckedRequest(RequestSpecs.superUserAuthSpec()).getRequest(ApiEndpoint.PROJECTS).create(invalidProject);
        response.then().spec(IncorrectDataSpecs.badRequestWithIncorrectFieldFormat("Project", "ID", invalidId, String.valueOf(invalidId.charAt(0))));
        softy.assertAll();
    }

    // Need to fix 500 error (Known Bugs)
    @Feature("Project ID Validation")
    @Story("Spaces in Project ID")
    @Test(description = "User should not be able to create a Project with spaces in the middle of the ID", groups = {"Negative", "CRUD", "KnownBugs", "PROJECT_ID_VALIDATION_TAG"})
    public void userCannotCreateProjectWithSpacesInIdTest() {
        String invalidId = RandomData.getString(5) + " " + RandomData.getString(5);
        Project invalidProject = TestDataGenerator.generate(List.of(), Project.class, invalidId, RandomData.getString());
        Response response = new UncheckedRequest(RequestSpecs.superUserAuthSpec()).getRequest(ApiEndpoint.PROJECTS).create(invalidProject);
        response.then().spec(IncorrectDataSpecs.badRequestUnsupportedCharacter("Project", "ID", invalidId, " "));
        softy.assertAll();
    }
    @Feature("Project ID Validation")
    @Story("Valid Project ID")
    @Test(description = "User should be able to create a Project with an ID containing Latin letters, digits, and underscores", groups = {"Positive", "CRUD", "PROJECT_ID_VALIDATION_TAG"})
    public void userCreatesProjectWithValidIdCharactersTest() {
        Project validProject = TestDataGenerator.generate(List.of(), Project.class, "valid_123_ID", RandomData.getString());
        Response response = new CheckedRequest(RequestSpecs.superUserAuthSpec()).getRequest(ApiEndpoint.PROJECTS).create(validProject);
        Project createdProject = ResponseExtractor.extractModel(response, Project.class);
        EntityValidator.validateAllEntityFieldsIgnoring(validProject, createdProject, List.of("parentProject"), softy);
        softy.assertAll();
    }
    // Need to fix 500 error (Known Bugs)
    @Feature("Project ID Validation")
    @Story("Empty Project ID")
    @Test(description = "User should not be able to create a Project with an empty ID String", groups = {"Negative", "CRUD", "PROJECT_ID_VALIDATION_TAG", "KnownBugs"})
    public void userCannotCreateProjectWithEmptyIdStringTest() {
        Project projectWithEmptyId = TestDataGenerator.generate(List.of(), Project.class, "", RandomData.getString());
        Response response = new UncheckedRequest(RequestSpecs.superUserAuthSpec()).getRequest(ApiEndpoint.PROJECTS).create(projectWithEmptyId);
        response.then().spec(IncorrectDataSpecs.badRequestEmptyField("Project", "ID"));
        softy.assertAll();
    }

// =================== PROJECT ID VALIDATION TESTS (PROJECT_ID_VALIDATION_TAG) =================== //

// =================== PROJECT NAME VALIDATION TESTS (PROJECT_NAME_VALIDATION_TAG) =================== //
@Feature("Project Name Validation")
@Story("Empty Project Name")
@Test(description = "User should not be able to create Project with empty name", groups = {"Negative", "CRUD", "PROJECT_NAME_VALIDATION_TAG"})
public void userCannotCreateProjectWithEmptyNameTest() {
    var invalidProject = TestDataGenerator.generate(List.of(), Project.class, RandomData.getString(), "");
    var response = new UncheckedRequest(RequestSpecs.superUserAuthSpec()).getRequest(ApiEndpoint.PROJECTS).create(invalidProject);
    response.then().spec(IncorrectDataSpecs.badRequestEmptyField("Project", "name"));
    softy.assertAll();
}

    @Feature("Project Name Validation")
@Story("Space in Project Name")
// Need to fix incorrect response from server (Known bugs)
@Test(description = "User should not be able to create a Project with a space as name", groups = {"Negative", "CRUD", "KnownBugs", "PROJECT_NAME_VALIDATION_TAG"})
public void userCannotCreateProjectWithSpaceAsNameTest() {
    Project invalidProject = TestDataGenerator.generate(Project.class, RandomData.getString(), " ");
    Response response = new UncheckedRequest(RequestSpecs.superUserAuthSpec()).getRequest(ApiEndpoint.PROJECTS).create(invalidProject);
    response.then().spec(IncorrectDataSpecs.badRequestEmptyField("project","name"));
    softy.assertAll();
}
    @Feature("Project Name Validation")
    @Story("Special Characters in Project Name")
    @Test(description = "User should be able to create a Project with special characters in name", groups = {"Positive", "CRUD", "PROJECT_NAME_VALIDATION_TAG"})
    public void userCreatesProjectWithSpecialCharactersInNameTest() {
        Project project = TestDataGenerator.generate(Project.class, RandomData.getString(), TestConstants.SPECIAL_CHARACTERS);
        Response response = new CheckedRequest(RequestSpecs.superUserAuthSpec()).getRequest(ApiEndpoint.PROJECTS).create(project);
        Project createdProject = ResponseExtractor.extractModel(response, Project.class);
        EntityValidator.validateAllEntityFieldsIgnoring(project, createdProject, List.of("parentProject"), softy);
        softy.assertAll();
    }

    @Feature("Project Name Validation")
    @Story("Localized Project Name")
    @Test(description = "User should be able to create a Project with a localized name", groups = {"Positive", "CRUD", "PROJECT_NAME_VALIDATION_TAG"})
    public void userCreatesProjectWithLocalizedNameTest() {
        Project localizedProject = TestDataGenerator.generate(Project.class, RandomData.getString(), TestConstants.LOCALIZATION_CHARACTERS);
        Response response = new CheckedRequest(RequestSpecs.superUserAuthSpec()).getRequest(ApiEndpoint.PROJECTS).create(localizedProject);
        Project createdProject = ResponseExtractor.extractModel(response, Project.class);
        EntityValidator.validateAllEntityFieldsIgnoring(localizedProject, createdProject, List.of("parentProject"), softy);
        softy.assertAll();
    }
    @Feature("Project Name Validation")
    @Story("One Character Project Name")
    @Test(description = "User should be able to create a Project with a name of length 1", groups = {"Positive", "CRUD", "PROJECT_NAME_VALIDATION_TAG"})
    public void userCreatesProjectWithOneCharacterNameTest() {
        Project validProject = TestDataGenerator.generate(Project.class, RandomData.getString(), "A");
        Response response = new CheckedRequest(RequestSpecs.superUserAuthSpec()).getRequest(ApiEndpoint.PROJECTS).create(validProject);
        Project createdProject = ResponseExtractor.extractModel(response, Project.class);
        EntityValidator.validateAllEntityFieldsIgnoring(validProject, createdProject, List.of("parentProject"), softy);
        softy.assertAll();
    }

    @Feature("Project Name Validation")
    @Story("Maximum Length Project Name")
    @Test(description = "User should be able to create a Project with a name of 500 characters", groups = {"Positive", "CRUD", "CornerCase", "PROJECT_NAME_VALIDATION_TAG"})
    public void userCreatesProjectWith500LengthNameTest() {
        String maxLengthName = "A".repeat(500);
        Project validProject = TestDataGenerator.generate(Project.class, RandomData.getString(), maxLengthName);
        Response response = new CheckedRequest(RequestSpecs.superUserAuthSpec()).getRequest(ApiEndpoint.PROJECTS).create(validProject);
        Project createdProject = ResponseExtractor.extractModel(response, Project.class);
        EntityValidator.validateAllEntityFieldsIgnoring(validProject, createdProject, List.of("parentProject"), softy);
        softy.assertAll();
    }
    @Feature("Project Name Validation")
    @Story("Duplicate Name with Different Case")
    @Test(description = "User should not be able to create a Project with an existing name in a different case", groups = {"Negative", "CRUD", "PROJECT_NAME_VALIDATION_TAG"})
    public void userCannotCreateProjectWithExistingNameDifferentCaseTest() {
        Project existingProject = createProjectAndExtractModel(testData.getProject());
        String duplicateName = existingProject.getName().toUpperCase();
        Project duplicateProject = TestDataGenerator.generate(Project.class, RandomData.getString(), duplicateName);
        Response response = new UncheckedRequest(RequestSpecs.superUserAuthSpec()).getRequest(ApiEndpoint.PROJECTS).create(duplicateProject);
        response.then().spec(IncorrectDataSpecs.badRequestDuplicatedField("Project","name",duplicateName));
        softy.assertAll();
    }

    @Feature("Project Name Validation")
    @Story("Duplicate Name")
    @Test(description = "User should not be able to create a Project with an existing name", groups = {"Negative", "CRUD", "PROJECT_NAME_VALIDATION_TAG"})
    public void userCannotCreateProjectWithExistingNameTest() {
        Project existingProject = createProjectAndExtractModel(testData.getProject());
        Project duplicateProject = TestDataGenerator.generate(Project.class, RandomData.getString(), existingProject.getName());
        Response response = new UncheckedRequest(RequestSpecs.superUserAuthSpec()).getRequest(ApiEndpoint.PROJECTS).create(duplicateProject);
        response.then().spec(IncorrectDataSpecs.badRequestDuplicatedField("Project", "name", existingProject.getName()));
        softy.assertAll();
    }

    @Feature("Project Name Validation")
    @Story("Digits Only Name")
    @Test(description = "User should be able to create a Project with a name consisting only of digits", groups = {"Positive", "CRUD", "PROJECT_NAME_VALIDATION_TAG"})
    public void userCreatesProjectWithDigitsOnlyNameTest() {
        Project validProject = TestDataGenerator.generate(Project.class, RandomData.getUniqueId(), RandomData.getDigits(6));
        Response response = new CheckedRequest(RequestSpecs.superUserAuthSpec()).getRequest(ApiEndpoint.PROJECTS).create(validProject);
        Project createdProject = ResponseExtractor.extractModel(response, Project.class);
        EntityValidator.validateAllEntityFieldsIgnoring(validProject, createdProject, List.of("parentProject"), softy);
        softy.assertAll();
    }

    @Feature("Project Name Validation")
    @Story("Spaces In Name")
    @Test(description = "User should be able to create a Project with spaces in the middle of the name", groups = {"Positive", "CRUD", "PROJECT_NAME_VALIDATION_TAG"})
    public void userCreatesProjectWithSpacesInNameTest() {
        String uniqueProjectName = RandomData.getUniqueName().substring(0, 5) + " " + RandomData.getUniqueName().substring(5);
        Project validProject = TestDataGenerator.generate(Project.class, RandomData.getUniqueId(), uniqueProjectName);
        Response response = new CheckedRequest(RequestSpecs.superUserAuthSpec()).getRequest(ApiEndpoint.PROJECTS).create(validProject);
        Project createdProject = ResponseExtractor.extractModel(response, Project.class);
        EntityValidator.validateAllEntityFieldsIgnoring(validProject, createdProject, List.of("parentProject"), softy);
        softy.assertAll();
    }

// =================== PROJECT NAME VALIDATION TESTS (PROJECT_NAME_VALIDATION_TAG) =================== //

// =================== PARENT PROJECT VALIDATION TESTS (PARENT_VALIDATION_TAG) =================== //
    @Feature("Parent Project Validation")
    @Story("Non-Existent Parent Project")
    @Test(description = "User should not be able to create a Project with a non-existent parentProject locator", groups = {"Negative", "CRUD", "PARENT_VALIDATION_TAG"})
    public void userCannotCreateProjectWithNonExistentParentProjectTest() {
        Project invalidProject = TestDataGenerator.generate(Project.class);
        invalidProject.setParentProject(new ParentProject("non_existent_locator", null));
        Response response = new UncheckedRequest(RequestSpecs.superUserAuthSpec()).getRequest(ApiEndpoint.PROJECTS).create(invalidProject);
        response.then().spec(IncorrectDataSpecs.entityNotFoundByLocator("Project", "id", invalidProject.getId()));
        softy.assertAll();
    }

    @Feature("Parent Project Validation")
    @Story("Parent ID Conflict")
    @Test(description = "User should not be able to create a Project with the same ID as its parent ID",
            groups = {"Negative", "CRUD", "PARENT_VALIDATION_TAG"})
    public void userCannotCreateProjectWithSameParentIdTest() {
        String projectId = testData.getProject().getId();
        Project invalidProject = TestDataGenerator.generate(Project.class, projectId, RandomData.getUniqueId());
        invalidProject.setParentProject(new ParentProject(projectId, null));
        Response response = new UncheckedRequest(RequestSpecs.superUserAuthSpec()).getRequest(ApiEndpoint.PROJECTS).create(invalidProject);
        response.then().spec(IncorrectDataSpecs.entityNotFoundByLocator("Project", "id", projectId));
        softy.assertAll();
    }
    @Feature("Parent Project Validation")
    @Story("Parent ID Empty")
    @Test(description = "User should not be able to create a Project if parent project locator is empty", groups = {"Negative", "CRUD", "PARENT_VALIDATION_TAG"})
    public void userCannotCreateProjectWithEmptyParentProjectLocatorTest() {
        Project invalidProject = TestDataGenerator.generate(Project.class);
        invalidProject.setParentProject(new ParentProject("", null));
        UncheckedRequest request = new UncheckedRequest(RequestSpecs.superUserAuthSpec());
        Response response = request.getRequest(ApiEndpoint.PROJECTS).create(invalidProject);
        response.then().spec(IncorrectDataSpecs.entityNotFoundByLocator("Project", "id", invalidProject.getId()));
        softy.assertAll();
    }
    // =================== PARENT PROJECT VALIDATION TESTS (PARENT_VALIDATION_TAG) =================== //

    // =================== AUTHORIZATIONS TESTS (AUTH_TAG) =================== //
    @Feature("Authorization")
    @Story("User without authentication should not create a project")
    @Test(description = "User should not be able to create a project without authentication", groups = {"Negative", "Auth"})
    public void userCannotCreateProjectWithoutAuthTest() {
        Project invalidProject = TestDataGenerator.generate(Project.class);
        UncheckedRequest request = new UncheckedRequest(RequestSpecs.unauthSpec());
        Response response = request.getRequest(ApiEndpoint.PROJECTS).create(invalidProject);
        response.then().spec(AccessErrorSpecs.authenticationRequired());
        softy.assertAll();
    }
    // =================== AUTHORIZATIONS TESTS (AUTH_TAG) =================== //

    // =================== SECURITY TESTS (SEC_TAG) =================== //
    // Проверяем защиту от XSS и SQL-инъекций в имени проекта
    // Ожидаем, что сервер сохранит payload как текст, без исполнения скриптов
    // Для id такие проверки не нужны так как есть проверка на отсутствие спецсимволов

    @Feature("Security")
    @Story("XSS Injection Prevention")
    @Test(description = "User should be able to create a Project with an XSS payload in name (payload stored as text)", groups = {"Positive", "Security", "CRUD", "SEC_TAG"})
    public void userCreatesProjectWithXSSInNameTest() {
        Project projectWithXSS = TestDataGenerator.generate(Project.class, RandomData.getUniqueId(), XSS_PAYLOAD);
        CheckedRequest request = new CheckedRequest(RequestSpecs.superUserAuthSpec());
        Response response = request.getRequest(ApiEndpoint.PROJECTS).create(projectWithXSS);
        Project createdProject = ResponseExtractor.extractModel(response, Project.class);
        EntityValidator.validateAllEntityFieldsIgnoring(projectWithXSS, createdProject, List.of("parentProject"), softy);
        softy.assertAll();
    }

    @Feature("Security")
    @Story("SQL Injection Prevention")
    @Test(description = "User should be able to create a Project with an SQL injection payload in name (payload stored as text)", groups = {"Positive", "Security", "CRUD", "SEC_TAG"})
    public void userCreatesProjectWithSQLInjectionTest() {
        Project projectWithSQL = TestDataGenerator.generate(Project.class, RandomData.getUniqueId(), SQL_INJECTION_PAYLOAD);
        CheckedRequest request = new CheckedRequest(RequestSpecs.superUserAuthSpec());
        Response response = request.getRequest(ApiEndpoint.PROJECTS).create(projectWithSQL);
        Project createdProject = ResponseExtractor.extractModel(response, Project.class);
        EntityValidator.validateAllEntityFieldsIgnoring(projectWithSQL, createdProject, List.of("parentProject"), softy);
        softy.assertAll();
    }
// =================== SECURITY TESTS (SEC_TAG) =================== //

// =================== ROLE-BASED ACCESS TESTS (ROLE_TAG) =================== //
@Feature("Access Control")
@Story("Restricted Roles - No Project Creation")
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

    @Test(description = "User with restricted role should not be able to create a project", dataProvider = "restrictedRoles", groups = {"Negative", "CRUD", "ROLE_TAG"})
    public void userWithRestrictedRoleCannotCreateProjectTest(Role role) {
        User restrictedUser = createUserWithRole(role, "g");
        Project projectToCreate = testData.getProject();
        UncheckedRequest restrictedUserRequest = new UncheckedRequest(RequestSpecs.authSpec(restrictedUser));
        Response response = restrictedUserRequest.getRequest(ApiEndpoint.PROJECTS).create(projectToCreate);
        response.then().spec(AccessErrorSpecs.accessDenied());
        softy.assertAll();
    }
    @Feature("Access Control")
    @Story("Allowed Roles - Project Creation")
    @DataProvider(name = "allowedRoles")
    public static Object[][] allowedRoles() {
        return new Object[][]{
                {Role.PROJECT_ADMIN},
                {Role.AGENT_MANAGER}
        };
    }
    @Test(description = "User with allowed role should be able to create a project", dataProvider = "allowedRoles", groups = {"Positive", "CRUD", "ROLE_TAG"})
    public void userWithAllowedRoleCanCreateProjectTest(Role role) {
        Project scopeProject = createProjectAndExtractModel(testData.getProject());
        User roleUser = createUserWithRole(role, scopeProject.getId());
        Project newProject = TestDataGenerator.generate(Project.class);
        CheckedRequest roleUserRequest = new CheckedRequest(RequestSpecs.authSpec(roleUser));
        Response response = roleUserRequest.getRequest(ApiEndpoint.PROJECTS).create(newProject);
        Project createdProject = ResponseExtractor.extractModel(response, Project.class);
        EntityValidator.validateAllEntityFieldsIgnoring(newProject, createdProject, List.of("parentProject"), softy);
        softy.assertAll();
    }
// =================== ROLE-BASED ACCESS TESTS (ROLE_TAG) =================== //

}







