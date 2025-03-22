package com.example.teamcity.api;

import com.example.teamcity.api.constants.TestConstants;
import com.example.teamcity.api.enums.ApiEndpoint;
import com.example.teamcity.api.enums.Role;
import com.example.teamcity.api.generators.RandomData;
import com.example.teamcity.api.generators.domain.ProjectTestData;
import com.example.teamcity.api.generators.TestDataGenerator;
import com.example.teamcity.api.models.ParentProject;
import com.example.teamcity.api.models.Project;
import com.example.teamcity.api.models.User;
import com.example.teamcity.api.requests.CheckedRequest;
import com.example.teamcity.api.requests.UncheckedRequest;
import com.example.teamcity.api.requests.helpers.ProjectHelper;
import com.example.teamcity.api.requests.helpers.UserHelper;
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

import java.util.ArrayList;
import java.util.List;

import static com.example.teamcity.api.constants.TestConstants.SQL_INJECTION_PAYLOAD;
import static com.example.teamcity.api.constants.TestConstants.XSS_PAYLOAD;

@Test(groups = {"Regression"})
public class ProjectCrudTest extends BaseApiTest {


    @Test(description = "User should be able to create a project with the minimum required fields under Root project", groups = {"Positive", "CRUD"})
    public void userCreatesProjectWithMandatoryFieldsOnlyTest() {
        Project project = testData.getProject();
        Project createdProject = ProjectHelper.createProject(userCheckedRequest, project);
        EntityValidator.validateAllEntityFieldsIgnoring(project, createdProject, List.of("parentProject"), softy);
        softy.assertEquals(createdProject.getParentProject().getId(), TestConstants.ROOT_PROJECT_ID, "Parent project should be '_Root' when not specified");
        softy.assertAll();
    }

    // =================== PROJECT COPY SETTINGS TESTS (COPY_SETTINGS_TAG) =================== //
 //Bug in API: projectsIdsMap, buildTypesIdsMap, vcsRootsIdsMap, sourceProject should be copied but are not
    @Feature("Project Copy Settings")
    @Story("Copy Project Parameters")
    @Test(description = "User should be able to create a Project with copyAllAssociatedSettings set to true and verify copied settings", groups = {"Positive", "CRUD", "KnownBugs", "COPY_SETTINGS_TAG"})
    public void userCreatesProjectWithCopyAllAssociatedSettingsTrueTest() {
        var sourceProject = ProjectHelper.createProject(userCheckedRequest, testData.getProject());
        var newProject = TestDataGenerator.generate(Project.class, RandomData.getString(), RandomData.getString(), sourceProject.getParentProject(), true, sourceProject);
        var createdProject = ProjectHelper.createProject(userCheckedRequest, newProject);
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
    @Test(description = "User should be able to create a Project with copyAllAssociatedSettings set to false and verify fields are NOT copied", groups = {"Positive", "CRUD", "COPY_SETTINGS_TAG"})
    public void userCreatesProjectWithCopyAllAssociatedSettingsFalseTest() {
        var sourceProject = ProjectHelper.createProject(userCheckedRequest, testData.getProject());
        var newProject = TestDataGenerator.generate(Project.class, RandomData.getString(), RandomData.getString(), new ParentProject(TestConstants.ROOT_PROJECT_ID, null), false, sourceProject);
        var createdProject = ProjectHelper.createProject(userCheckedRequest, newProject);
        EntityValidator.validateAllEntityFieldsIgnoring(sourceProject, createdProject, List.of("id", "name", "parentProject", "copyAllAssociatedSettings", "sourceProject", "projectsIdsMap", "buildTypesIdsMap", "vcsRootsIdsMap"), softy);
        softy.assertNull(createdProject.getCopyAllAssociatedSettings(), "copyAllAssociatedSettings должен быть null");
        softy.assertNull(createdProject.getSourceProject(), "sourceProject должен быть null");
        softy.assertNull(createdProject.getProjectsIdsMap(), "projectsIdsMap должен быть null");
        softy.assertNull(createdProject.getBuildTypesIdsMap(), "buildTypesIdsMap должен быть null");
        softy.assertNull(createdProject.getVcsRootsIdsMap(), "vcsRootsIdsMap должен быть null");
        softy.assertAll();
    }


    // =================== PROJECT COPY SETTINGS TESTS (COPY_SETTINGS_TAG) =================== //

    // =================== NESTED AND SIBLING PROJECTS TESTS (PROJECT_HIERARCHY_TAG) =================== //
    @Test(description = "User should be able to create nested projects", groups = {"Positive", "CRUD", "CornerCase"})
    public void userCreatesNestedProjectsTest() {
        int projectCount = 20;
        List<Project> projects = ProjectTestData.nestedProjects(projectCount);
        List<Project> createdProjects = ProjectHelper.createNestedProjects(userCheckedRequest, projects);
        softy.assertEquals(createdProjects.size(), projectCount, "The number of created projects is incorrect");
        softy.assertEquals(createdProjects.get(0).getName(), projects.get(0).getName(), "First project name should match");
        softy.assertEquals(createdProjects.get(projectCount-1).getName(), projects.get(projectCount-1).getName(), "Last project name should match");
        ProjectHelper.assertLinearHierarchy(createdProjects, softy);
        softy.assertAll();
    }

    @Feature("Project Management")
    @Story("Creating sibling projects")
    @Test(description = "User should be able to create 20 sibling projects", groups = {"Positive", "CRUD", "CornerCase"})
    public void userCreatesSiblingProjectsTest() {
        var rootProject = ProjectHelper.createProject(userCheckedRequest, testData.getProject());
        int projectCount = 20;
        List<Project> projects = ProjectTestData.siblingProjects(rootProject.getId(), projectCount);
        List<Project> createdProjects = ProjectHelper.createSiblingProjects(userCheckedRequest, projects);
        softy.assertEquals(createdProjects.size(), projectCount, "The number of created projects is incorrect");
        ProjectHelper.assertSiblingHierarchy(createdProjects, rootProject.getId(), softy);
        softy.assertAll();
    }

    // =================== PROJECT ID VALIDATION TESTS (PROJECT_ID_VALIDATION_TAG) =================== //
    @Feature("Project ID Validation")
    @Story("Max Length ID")
    @Test(description = "User should be able to create a Project with an ID of maximum allowed length", groups = {"Positive", "CRUD", "PROJECT_ID_VALIDATION_TAG"})
    public void userCreatesProjectWithMaxLengthIdTest() {
        Project validProject = TestDataGenerator.generate(Project.class, RandomData.getString(225), RandomData.getString());
        Project createdProject = ProjectHelper.createProject(superUserCheckRequests, validProject);
        EntityValidator.validateAllEntityFieldsIgnoring(validProject, createdProject, List.of("parentProject"), softy);
        softy.assertAll();
    }

    @Feature("Project ID Validation")
    @Story("Min Length ID")
    @Test(description = "User should be able to create a Project with an ID of length 1", groups = {"Positive", "CRUD", "PROJECT_ID_VALIDATION_TAG"})
    public void userCreatesProjectWithOneCharacterIdTest() {
        String minLengthId = RandomData.getString(1);
        Project validProject = TestDataGenerator.generate(Project.class, minLengthId, RandomData.getString());
        Project createdProject = ProjectHelper.createProject(superUserCheckRequests, validProject);
        EntityValidator.validateAllEntityFieldsIgnoring(validProject, createdProject, List.of("parentProject"), softy);
        softy.assertAll();
    }

    @Feature("Project ID Validation")
    @Story("Underscore in ID")
    @Test(description = "User should be able to create a Project with an ID containing an underscore", groups = {"Positive", "CRUD", "PROJECT_ID_VALIDATION_TAG"})
    public void userCreatesProjectWithUnderscoreInIdTest() {
        String idWithUnderscore = RandomData.getString() + "_test";
        Project projectWithUnderscore = TestDataGenerator.generate(Project.class, idWithUnderscore, RandomData.getString());
        Project createdProject = ProjectHelper.createProject(superUserCheckRequests, projectWithUnderscore);
        EntityValidator.validateAllEntityFieldsIgnoring(projectWithUnderscore, createdProject, List.of("parentProject"), softy);
        softy.assertAll();
    }

    @Feature("Project ID Validation")
    @Story("Project ID Length Exceeded")
    @Test(description = "User should not be able to create a Project with an ID longer than 225 characters", groups = {"Negative", "CRUD", "KnownBugs", "CornerCase", "PROJECT_ID_VALIDATION_TAG"})
    public void userCannotCreateProjectWithTooLongIdTest() {
        var tooLongId = RandomData.getString(226);
        var invalidProject = TestDataGenerator.generate(List.of(), Project.class, tooLongId, RandomData.getString());
        var response = userUncheckedRequest.getRequest(ApiEndpoint.PROJECTS).create(invalidProject);
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
        var response = userUncheckedRequest.getRequest(ApiEndpoint.PROJECTS).create(invalidProject);
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
        var response = userUncheckedRequest.getRequest(ApiEndpoint.PROJECTS).create(invalidProject);
        response.then().spec(IncorrectDataSpecs.badRequestNonLatinLetter("Project", "ID", invalidId));
        softy.assertAll();
    }

    @Feature("Project ID Validation")
    @Story("Empty ID")
// Need to fix 500 error (Known Bugs)
    @Test(description = "User should not be able to create Project with empty id", groups = {"Negative", "CRUD", "KnownBugs", "PROJECT_ID_VALIDATION_TAG"})
    public void userCannotCreateProjectWithEmptyIdTest() {
        Project invalidProject = TestDataGenerator.generate(Project.class, "", RandomData.getString());
        Response response = userUncheckedRequest.getRequest(ApiEndpoint.PROJECTS).create(invalidProject);
        response.then().spec(IncorrectDataSpecs.badRequestEmptyField("Project", "ID"));
        softy.assertAll();
    }

    @Feature("Project ID Validation")
    @Story("ID with Space")
// Need to fix 500 error (Known Bugs)
    @Test(description = "User should not be able to create a Project with a space as ID", groups = {"Negative", "CRUD", "KnownBugs", "PROJECT_ID_VALIDATION_TAG"})
    public void userCannotCreateProjectWithSpaceAsIdTest() {
        Project invalidProject = TestDataGenerator.generate(Project.class, " ", RandomData.getString());
        Response response = userUncheckedRequest.getRequest(ApiEndpoint.PROJECTS).create(invalidProject);
        response.then().spec(IncorrectDataSpecs.badRequestEmptyField("Project", "ID"));
        softy.assertAll();
    }

    @Feature("Project ID Validation")
    @Story("Duplicate Project ID")
    @Test(description = "User should not be able to create a Project with an existing ID", groups = {"Negative", "CRUD", "PROJECT_ID_VALIDATION_TAG"})
    public void userCannotCreateProjectWithExistingIdTest() {
        Project existingProject = ProjectHelper.createProject(userCheckedRequest, testData.getProject());
        Project duplicateProject = TestDataGenerator.generate(List.of(existingProject), Project.class, existingProject.getId(), RandomData.getString());
        Response response = userUncheckedRequest.getRequest(ApiEndpoint.PROJECTS).create(duplicateProject);
        response.then().spec(IncorrectDataSpecs.badRequestDuplicatedField("Project", "ID", existingProject.getId()));
        softy.assertAll();
    }

    @Feature("Project ID Validation")
    @Story("Duplicate Project ID with Different Case")
    @Test(description = "User should not be able to create a Project with an existing ID in a different case", groups = {"Negative", "CRUD", "PROJECT_ID_VALIDATION_TAG"})
    public void userCannotCreateProjectWithExistingIdDifferentCaseTest() {
        Project existingProject = ProjectHelper.createProject(userCheckedRequest, testData.getProject());
        String duplicateId = existingProject.getId().toUpperCase();
        Project duplicateProject = TestDataGenerator.generate(List.of(), Project.class, duplicateId, RandomData.getString());
        Response response = userUncheckedRequest.getRequest(ApiEndpoint.PROJECTS).create(duplicateProject);
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
        Response response = userUncheckedRequest.getRequest(ApiEndpoint.PROJECTS).create(invalidProject);
        response.then().spec(IncorrectDataSpecs.badRequestWithIncorrectFieldFormat("Project", "ID", invalidId, String.valueOf(invalidId.charAt(0))));
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
    @Test(description = "User should not be able to create a Project with an ID starting with an underscore or a digit",
            groups = {"Negative", "CRUD", "KnownBugs", "PROJECT_ID_VALIDATION_TAG"},
            dataProvider = "invalidIdStartId")
    public void userCannotCreateProjectWithInvalidStartingCharacterIdTest(String invalidId) {
        Project invalidProject = TestDataGenerator.generate(List.of(), Project.class, invalidId, RandomData.getString());
        Response response = userUncheckedRequest.getRequest(ApiEndpoint.PROJECTS).create(invalidProject);
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
        Response response = userUncheckedRequest.getRequest(ApiEndpoint.PROJECTS).create(invalidProject);
        response.then().spec(IncorrectDataSpecs.badRequestUnsupportedCharacter("Project", "ID", invalidId, " "));
        softy.assertAll();
    }

    @Feature("Project ID Validation")
    @Story("Valid Project ID")
    @Test(description = "User should be able to create a Project with an ID containing Latin letters, digits, and underscores", groups = {"Positive", "CRUD", "PROJECT_ID_VALIDATION_TAG"})
    public void userCreatesProjectWithValidIdCharactersTest() {
        Project validProject = TestDataGenerator.generate(List.of(), Project.class, "valid_123_ID", RandomData.getString());
        Project createdProject = ProjectHelper.createProject(superUserCheckRequests, validProject);
        EntityValidator.validateAllEntityFieldsIgnoring(validProject, createdProject, List.of("parentProject"), softy);
        softy.assertAll();
    }

    // Need to fix 500 error (Known Bugs)
    @Feature("Project ID Validation")
    @Story("Empty Project ID")
    @Test(description = "User should not be able to create a Project with an empty ID String", groups = {"Negative", "CRUD", "PROJECT_ID_VALIDATION_TAG", "KnownBugs"})
    public void userCannotCreateProjectWithEmptyIdStringTest() {
        Project projectWithEmptyId = TestDataGenerator.generate(List.of(), Project.class, "", RandomData.getString());
        Response response = userUncheckedRequest.getRequest(ApiEndpoint.PROJECTS).create(projectWithEmptyId);
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
        var response = userUncheckedRequest.getRequest(ApiEndpoint.PROJECTS).create(invalidProject);
        response.then().spec(IncorrectDataSpecs.badRequestEmptyField("Project", "name"));
        softy.assertAll();
    }

    @Feature("Project Name Validation")
    @Story("Space in Project Name")
// Need to fix incorrect response from server (Known bugs)
    @Test(description = "User should not be able to create a Project with a space as name", groups = {"Negative", "CRUD", "KnownBugs", "PROJECT_NAME_VALIDATION_TAG"})
    public void userCannotCreateProjectWithSpaceAsNameTest() {
        Project invalidProject = TestDataGenerator.generate(Project.class, RandomData.getString(), " ");
        Response response = userUncheckedRequest.getRequest(ApiEndpoint.PROJECTS).create(invalidProject);
        response.then().spec(IncorrectDataSpecs.badRequestEmptyField("project", "name"));
        softy.assertAll();
    }

    @Feature("Project Name Validation")
    @Story("Special Characters in Project Name")
    @Test(description = "User should be able to create a Project with special characters in name", groups = {"Positive", "CRUD", "PROJECT_NAME_VALIDATION_TAG"})
    public void userCreatesProjectWithSpecialCharactersInNameTest() {
        Project project = TestDataGenerator.generate(Project.class, RandomData.getString(), TestConstants.SPECIAL_CHARACTERS);
        Project createdProject = ProjectHelper.createProject(superUserCheckRequests, project);
        EntityValidator.validateAllEntityFieldsIgnoring(project, createdProject, List.of("parentProject"), softy);
        softy.assertAll();
    }

    @Feature("Project Name Validation")
    @Story("Localized Project Name")
    @Test(description = "User should be able to create a Project with a localized name", groups = {"Positive", "CRUD", "PROJECT_NAME_VALIDATION_TAG"})
    public void userCreatesProjectWithLocalizedNameTest() {
        Project localizedProject = TestDataGenerator.generate(Project.class, RandomData.getString(), TestConstants.LOCALIZATION_CHARACTERS);
        Project createdProject = ProjectHelper.createProject(superUserCheckRequests, localizedProject);
        EntityValidator.validateAllEntityFieldsIgnoring(localizedProject, createdProject, List.of("parentProject"), softy);
        softy.assertAll();
    }

    @Feature("Project Name Validation")
    @Story("One Character Project Name")
    @Test(description = "User should be able to create a Project with a name of length 1", groups = {"Positive", "CRUD", "PROJECT_NAME_VALIDATION_TAG"})
    public void userCreatesProjectWithOneCharacterNameTest() {
        Project validProject = TestDataGenerator.generate(Project.class, RandomData.getString(), "A");
        Project createdProject = ProjectHelper.createProject(superUserCheckRequests, validProject);
        EntityValidator.validateAllEntityFieldsIgnoring(validProject, createdProject, List.of("parentProject"), softy);
        softy.assertAll();
    }

    @Feature("Project Name Validation")
    @Story("Maximum Length Project Name")
    @Test(description = "User should be able to create a Project with a name of 500 characters", groups = {"Positive", "CRUD", "CornerCase", "PROJECT_NAME_VALIDATION_TAG"})
    public void userCreatesProjectWith500LengthNameTest() {
        String maxLengthName = "A".repeat(500);
        Project validProject = TestDataGenerator.generate(Project.class, RandomData.getString(), maxLengthName);
        Project createdProject = ProjectHelper.createProject(superUserCheckRequests, validProject);
        EntityValidator.validateAllEntityFieldsIgnoring(validProject, createdProject, List.of("parentProject"), softy);
        softy.assertAll();
    }

    @Feature("Project Name Validation")
    @Story("Duplicate Name with Different Case")
    @Test(description = "User should not be able to create a Project with an existing name in a different case", groups = {"Negative", "CRUD", "PROJECT_NAME_VALIDATION_TAG"})
    public void userCannotCreateProjectWithExistingNameDifferentCaseTest() {
        Project existingProject = ProjectHelper.createProject(userCheckedRequest, testData.getProject());
        String duplicateName = existingProject.getName().toUpperCase();
        Project duplicateProject = TestDataGenerator.generate(Project.class, RandomData.getString(), duplicateName);
        Response response = userUncheckedRequest.getRequest(ApiEndpoint.PROJECTS).create(duplicateProject);
        response.then().spec(IncorrectDataSpecs.badRequestDuplicatedField("Project", "name", duplicateName));
        softy.assertAll();
    }

    @Feature("Project Name Validation")
    @Story("Duplicate Name")
    @Test(description = "User should not be able to create a Project with an existing name", groups = {"Negative", "CRUD", "PROJECT_NAME_VALIDATION_TAG"})
    public void userCannotCreateProjectWithExistingNameTest() {
        Project existingProject = ProjectHelper.createProject(userCheckedRequest, testData.getProject());
        Project duplicateProject = TestDataGenerator.generate(Project.class, RandomData.getString(), existingProject.getName());
        Response response = userUncheckedRequest.getRequest(ApiEndpoint.PROJECTS).create(duplicateProject);
        response.then().spec(IncorrectDataSpecs.badRequestDuplicatedField("Project", "name", existingProject.getName()));
        softy.assertAll();
    }

    @Feature("Project Name Validation")
    @Story("Digits Only Name")
    @Test(description = "User should be able to create a Project with a name consisting only of digits", groups = {"Positive", "CRUD", "PROJECT_NAME_VALIDATION_TAG"})
    public void userCreatesProjectWithDigitsOnlyNameTest() {
        Project validProject = TestDataGenerator.generate(Project.class, RandomData.getUniqueId(), RandomData.getDigits(6));
        Project createdProject = ProjectHelper.createProject(superUserCheckRequests, validProject);
        EntityValidator.validateAllEntityFieldsIgnoring(validProject, createdProject, List.of("parentProject"), softy);
        softy.assertAll();
    }

    @Feature("Project Name Validation")
    @Story("Spaces In Name")
    @Test(description = "User should be able to create a Project with spaces in the middle of the name", groups = {"Positive", "CRUD", "PROJECT_NAME_VALIDATION_TAG"})
    public void userCreatesProjectWithSpacesInNameTest() {
        String uniqueProjectName = RandomData.getUniqueName().substring(0, 5) + " " + RandomData.getUniqueName().substring(5);
        Project validProject = TestDataGenerator.generate(Project.class, RandomData.getUniqueId(), uniqueProjectName);
        Project createdProject = ProjectHelper.createProject(superUserCheckRequests, validProject);
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
        Response response = userUncheckedRequest.getRequest(ApiEndpoint.PROJECTS).create(invalidProject);
        response.then().spec(IncorrectDataSpecs.entityNotFoundByLocator("Project", "id", invalidProject.getId()));
        softy.assertAll();
    }

    @Feature("Parent Project Validation")
    @Story("Parent ID Conflict")
    @Test(description = "User should not be able to create a Project with the same ID as its parent ID", groups = {"Negative", "CRUD", "PARENT_VALIDATION_TAG"})
    public void userCannotCreateProjectWithSameParentIdTest() {
        String projectId = testData.getProject().getId();
        Project invalidProject = TestDataGenerator.generate(Project.class, projectId, RandomData.getUniqueId());
        invalidProject.setParentProject(new ParentProject(projectId, null));
        Response response = userUncheckedRequest.getRequest(ApiEndpoint.PROJECTS).create(invalidProject);
        response.then().spec(IncorrectDataSpecs.entityNotFoundByLocator("Project", "id", projectId));
        softy.assertAll();
    }

    @Feature("Parent Project Validation")
    @Story("Parent ID Empty")
    @Test(description = "User should not be able to create a Project if parent project locator is empty", groups = {"Negative", "CRUD", "PARENT_VALIDATION_TAG"})
    public void userCannotCreateProjectWithEmptyParentProjectLocatorTest() {
        Project invalidProject = TestDataGenerator.generate(Project.class);
        invalidProject.setParentProject(new ParentProject("", null));
        Response response = userUncheckedRequest.getRequest(ApiEndpoint.PROJECTS).create(invalidProject);
        response.then().spec(IncorrectDataSpecs.entityNotFoundByLocator("Project", "id", invalidProject.getId()));
        softy.assertAll();
    }

    // =================== PARENT PROJECT VALIDATION TESTS (PARENT_VALIDATION_TAG) =================== //

    // =================== AUTHORIZATIONS TESTS (AUTH_TAG) =================== //
    @Feature("Authorization")
    @Story("User without authentication should not create a project")
    @Test(description = "User should not be able to create a project without authentication", groups = {"Negative", "Auth"})
    public void userCannotCreateProjectWithoutAuthTest() {
        var unauthRequest = new UncheckedRequest(RequestSpecs.unauthSpec());
        var invalidProject = TestDataGenerator.generate(List.of(), Project.class, RandomData.getString(), RandomData.getString());
        var response = unauthRequest.getRequest(ApiEndpoint.PROJECTS).create(invalidProject);
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
        Project createdProject = ProjectHelper.createProject(superUserCheckRequests, projectWithXSS);
        EntityValidator.validateAllEntityFieldsIgnoring(projectWithXSS, createdProject, List.of("parentProject"), softy);
        softy.assertAll();
    }


    @Feature("Security")
    @Story("SQL Injection Prevention")
    @Test(description = "User should be able to create a Project with an SQL injection payload in name (payload stored as text)", groups = {"Positive", "Security", "CRUD", "SEC_TAG"})
    public void userCreatesProjectWithSQLInjectionTest() {
        Project projectWithSQL = TestDataGenerator.generate(Project.class, RandomData.getUniqueId(), SQL_INJECTION_PAYLOAD);
        Project createdProject = ProjectHelper.createProject(superUserCheckRequests, projectWithSQL);
        EntityValidator.validateAllEntityFieldsIgnoring(projectWithSQL, createdProject, List.of("parentProject"), softy);
        softy.assertAll();
    }

// =================== SECURITY TESTS (SEC_TAG) =================== //

//     =================== ROLE-BASED ACCESS TESTS (ROLE_TAG) =================== //
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
        Project createdProject = ProjectHelper.createProject(userCheckedRequest, testData.getProject());
        User userWithRole = testData.getUser();
        User updatedUser = UserHelper.updateUserRole(superUserCheckRequests, userWithRole, role, createdProject.getId());
        softy.assertNotNull(updatedUser);
        softy.assertEquals(updatedUser.getRoles().getRole().get(0).getRoleId(), role.getRoleName());
        Project nestedProject = TestDataGenerator.generate(Project.class, RandomData.getUniqueId(), "Nested Project with " + role.getRoleName() + " " + RandomData.getString(8));
        UncheckedRequest restrictedUserRequest = new UncheckedRequest(RequestSpecs.authSpec(updatedUser));
        Response response = restrictedUserRequest.getRequest(ApiEndpoint.PROJECTS).create(nestedProject);
        response.then().spec(AccessErrorSpecs.accessDenied());
        softy.assertAll();
    }

    @Feature("Access Control")
    @Story("Allowed Roles - Project Creation")
    @Test(description = "User with allowed roles should be able to create a project", groups = {"Positive", "AccessControl", "CRUD", "ROLE_TAG"})
    @DataProvider(name = "allowedRoles")
    public static Object[][] allowedRoles() {
        return new Object[][]{
                {Role.PROJECT_ADMIN},
                {Role.AGENT_MANAGER}
        };
    }

    @Test(dataProvider = "allowedRoles")
    public void userWithAllowedRoleCanCreateProjectTest(Role role) {
        Project createdProject = ProjectHelper.createProject(userCheckedRequest, testData.getProject());
        User userWithRole = testData.getUser();
        User updatedUser = UserHelper.updateUserRole(superUserCheckRequests, userWithRole, role, createdProject.getId());
        softy.assertNotNull(updatedUser);
        softy.assertEquals(updatedUser.getRoles().getRole().get(0).getRoleId(), role.getRoleName());
        Project nestedProject = TestDataGenerator.generate(Project.class, RandomData.getUniqueId(), "Nested Project with " + role.getRoleName() + " " + RandomData.getString(8));
        List<Project> nestedProjects = new ArrayList<>();
        nestedProjects.add(nestedProject);
        CheckedRequest requestForNestedProject = new CheckedRequest(RequestSpecs.authSpec(updatedUser));
        List<Project> createdNestedProjects = ProjectHelper.createNestedProjects(requestForNestedProject, nestedProjects);
        Project createdNestedProject = createdNestedProjects.get(0);
        softy.assertNotNull(createdNestedProject);
        softy.assertEquals(createdNestedProject.getName(), nestedProject.getName());
        EntityValidator.validateAllEntityFieldsIgnoring(nestedProject, createdNestedProject, List.of("parentProject"), softy);
        softy.assertAll();
    }











// =================== ROLE-BASED ACCESS TESTS (ROLE_TAG) =================== //

}







