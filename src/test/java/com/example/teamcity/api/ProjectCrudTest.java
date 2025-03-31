package com.example.teamcity.api;

import com.example.teamcity.api.constants.TestConstants;
import com.example.teamcity.api.enums.ApiEndpoint;
import com.example.teamcity.api.enums.Role;
import com.example.teamcity.api.generators.RandomData;
import com.example.teamcity.api.generators.TestDataGenerator;
import com.example.teamcity.api.generators.domain.ProjectTestData;
import com.example.teamcity.api.models.ParentProject;
import com.example.teamcity.api.models.Project;
import com.example.teamcity.api.models.User;
import com.example.teamcity.api.requests.UncheckedRequest;
import com.example.teamcity.api.spec.request.RequestSpecs;
import com.example.teamcity.api.spec.responce.AccessErrorSpecs;
import com.example.teamcity.api.spec.responce.IncorrectDataSpecs;
import com.example.teamcity.api.validation.EntityValidator;
import io.qameta.allure.Feature;
import io.qameta.allure.Issue;
import io.qameta.allure.Story;
import io.restassured.response.Response;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

import java.util.List;

import static com.example.teamcity.api.constants.TestConstants.SQL_INJECTION_PAYLOAD;
import static com.example.teamcity.api.constants.TestConstants.XSS_PAYLOAD;

@Test(groups = {"Regression"})
public class ProjectCrudTest extends BaseApiTest {

    @Feature("Projects creation")
    @Story("Create project with required fields only")
    @Test(description = "User should be able to create a project with the minimum required fields under Root project", groups = {"Positive", "CRUD"})
    public void userCreatesProjectWithMandatoryFieldsOnlyTest() {
        Project project = testData.getProject();
        Project createdProject = projectHelper.createProject(userCheckedRequest, project);
        EntityValidator.validateAllEntityFieldsIgnoring(project, createdProject, List.of("parentProject"), softy);
        softy.assertEquals(createdProject.getParentProject().getId(), TestConstants.ROOT_PROJECT_ID, "Parent project should be '_Root' when not specified");
        softy.assertAll();
    }

    // =================== PROJECT COPY SETTINGS TESTS (COPY_SETTINGS_TAG) =================== //
    @Feature("Project Copy Settings")
    @Story("Copy Project Parameters")
    @Issue("Bug in API: projectsIdsMap, buildTypesIdsMap, vcsRootsIdsMap, sourceProject should be copied but are not")
    @Test(description = "User should be able to create a Project with copyAllAssociatedSettings set to true and verify copied settings", groups = {"Positive", "CRUD", "KnownBugs", "COPY_SETTINGS_TAG"})
    public void userCreatesProjectWithCopyAllAssociatedSettingsTrueTest() {
        var sourceProject = projectHelper.createProject(userCheckedRequest, testData.getProject());
        var newProject = TestDataGenerator.generate(Project.class, RandomData.getString(), RandomData.getString(), sourceProject.getParentProject(), true, sourceProject);
        var createdProject = projectHelper.createProject(userCheckedRequest, newProject);
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
        var sourceProject = projectHelper.createProject(userCheckedRequest, testData.getProject());
        var newProject = TestDataGenerator.generate(Project.class, RandomData.getString(), RandomData.getString(), new ParentProject(TestConstants.ROOT_PROJECT_ID, null), false, sourceProject);
        var createdProject = projectHelper.createProject(userCheckedRequest, newProject);
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
        List<Project> createdProjects = projectHelper.createNestedProjects(userCheckedRequest, projects);
        softy.assertEquals(createdProjects.size(), projectCount, "The number of created projects is incorrect");
        softy.assertEquals(createdProjects.get(0).getName(), projects.get(0).getName(), "First project name should match");
        softy.assertEquals(createdProjects.get(projectCount - 1).getName(), projects.get(projectCount - 1).getName(), "Last project name should match");
        projectHelper.assertLinearHierarchy(createdProjects, softy);
        softy.assertAll();
    }

    @Feature("Project Management")
    @Story("Creating sibling projects")
    @Test(description = "User should be able to create 20 sibling projects", groups = {"Positive", "CRUD", "CornerCase"})
    public void userCreatesSiblingProjectsTest() {
        var rootProject = projectHelper.createProject(userCheckedRequest, testData.getProject());
        int projectCount = 20;
        List<Project> projects = ProjectTestData.siblingProjects(rootProject.getId(), projectCount);
        List<Project> createdProjects = projectHelper.createSiblingProjects(userCheckedRequest, projects);
        softy.assertEquals(createdProjects.size(), projectCount, "The number of created projects is incorrect");
        projectHelper.assertSiblingHierarchy(createdProjects, rootProject.getId(), softy);
        softy.assertAll();
    }

    // =================== PROJECT ID VALIDATION TESTS (PROJECT_ID_VALIDATION_TAG) =================== //
    @Feature("Project ID Validation")
    @Story("Max Length ID")
    @Test(description = "User should be able to create a Project with an ID of maximum allowed length", groups = {"Positive", "CRUD", "PROJECT_ID_VALIDATION_TAG"})
    public void userCreatesProjectWithMaxLengthIdTest() {
        Project validProject = TestDataGenerator.generate(Project.class, RandomData.getString(225), RandomData.getString());
        Project createdProject = projectHelper.createProject(superUserCheckRequests, validProject);
        EntityValidator.validateAllEntityFieldsIgnoring(validProject, createdProject, List.of("parentProject"), softy);
        softy.assertAll();
    }

    @Feature("Project ID Validation")
    @Story("Min Length ID")
    @Test(description = "User should be able to create a Project with an ID of length 1", groups = {"Positive", "CRUD", "PROJECT_ID_VALIDATION_TAG"})
    public void userCreatesProjectWithOneCharacterIdTest() {
        String minLengthId = RandomData.getString(1);
        Project validProject = TestDataGenerator.generate(Project.class, minLengthId, RandomData.getString());
        Project createdProject = projectHelper.createProject(superUserCheckRequests, validProject);
        EntityValidator.validateAllEntityFieldsIgnoring(validProject, createdProject, List.of("parentProject"), softy);
        softy.assertAll();
    }

    @Feature("Project ID Validation")
    @Story("Underscore in ID")
    @Test(description = "User should be able to create a Project with an ID containing an underscore", groups = {"Positive", "CRUD", "PROJECT_ID_VALIDATION_TAG"})
    public void userCreatesProjectWithUnderscoreInIdTest() {
        String idWithUnderscore = RandomData.getString() + "_test";
        Project projectWithUnderscore = TestDataGenerator.generate(Project.class, idWithUnderscore, RandomData.getString());
        Project createdProject = projectHelper.createProject(superUserCheckRequests, projectWithUnderscore);
        EntityValidator.validateAllEntityFieldsIgnoring(projectWithUnderscore, createdProject, List.of("parentProject"), softy);
        softy.assertAll();
    }

    @Feature("Project ID Validation")
    @Story("Project ID Length Exceeded")
    @Issue("Bug in API: 500 error is returned")
    @Test(description = "User should not be able to create a Project with an ID longer than 225 characters", groups = {"Negative", "CRUD", "KnownBugs", "CornerCase", "PROJECT_ID_VALIDATION_TAG"})
    public void userCannotCreateProjectWithTooLongIdTest() {
        var tooLongId = RandomData.getString(226);
        var invalidProject = TestDataGenerator.generate(List.of(), Project.class, tooLongId, RandomData.getString());
        var response = userUncheckedRequest.getRequest(ApiEndpoint.PROJECTS).create(invalidProject);
        response.then().spec(IncorrectDataSpecs.badRequestFieldTooLong("Project", "ID", tooLongId, 225));
        softy.assertAll();
    }


    @DataProvider(name = "invalidSpecialCharactersForId")
    public static Object[][] invalidSpecialCharactersForId() {
        return "!@#$%^&*()+-={}[]:\\".chars()
                .mapToObj(c -> new Object[]{String.valueOf((char) c)})
                .toArray(Object[][]::new);
    }

    @Feature("Project ID Validation")
    @Story("Special Characters in Project ID")
    @Issue("Bug in API: 500 error is returned")
    @Test(description = "User should not be able to create a Project with special characters in ID", groups = {"Negative", "CRUD", "KnownBugs", "PROJECT_ID_VALIDATION_TAG"}, dataProvider = "invalidSpecialCharactersForId")
    public void userCannotCreateProjectWithEachSpecialCharacterInIdTest(String specialChar) {
        var invalidId = "test_" + specialChar;
        var invalidProject = TestDataGenerator.generate(List.of(), Project.class, invalidId, RandomData.getString());
        var response = userUncheckedRequest.getRequest(ApiEndpoint.PROJECTS).create(invalidProject);
        response.then().spec(IncorrectDataSpecs.badRequestUnsupportedCharacter("Project", "ID", invalidId, specialChar));
        softy.assertAll();
    }


    @DataProvider
    public static Object[][] nonLatinIdProviderForId() {
        return new Object[][]{{"проект"}, {"项目"}, {"プロジェクト"}, {"مشروع"}, {"παράδειγμα"}, {"नमूना"}, {"בדיקה"}};
    }

    @Feature("Project ID Validation")
    @Story("Non-Latin Characters in Project ID")
    @Issue("Bug in API: 500 error is returned")
    @Test(description = "User should not be able to create a Project with a non-Latin ID", groups = {"Negative", "CRUD", "KnownBugs", "PROJECT_ID_VALIDATION_TAG"}, dataProvider = "nonLatinIdProviderForId")
    public void userCannotCreateProjectWithNonLatinIdTest(String invalidId) {
        var invalidProject = TestDataGenerator.generate(List.of(), Project.class, invalidId, RandomData.getString());
        var response = userUncheckedRequest.getRequest(ApiEndpoint.PROJECTS).create(invalidProject);
        response.then().spec(IncorrectDataSpecs.badRequestNonLatinLetter("Project", "ID", invalidId));
        softy.assertAll();
    }

    @Feature("Project ID Validation")
    @Story("Empty ID")
    @Issue("Bug in API: 500 error is returned")
    @Test(description = "User should not be able to create Project with empty id", groups = {"Negative", "CRUD", "KnownBugs", "PROJECT_ID_VALIDATION_TAG"})
    public void userCannotCreateProjectWithEmptyIdTest() {
        Project invalidProject = TestDataGenerator.generate(Project.class, "", RandomData.getString());
        Response response = userUncheckedRequest.getRequest(ApiEndpoint.PROJECTS).create(invalidProject);
        response.then().spec(IncorrectDataSpecs.badRequestEmptyField("Project", "ID"));
        softy.assertAll();
    }

    @Feature("Project ID Validation")
    @Story("ID with Space")
    @Issue("Bug in API: 500 error is returned")
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
        Project existingProject = projectHelper.createProject(userCheckedRequest, testData.getProject());
        Project duplicateProject = TestDataGenerator.generate(List.of(existingProject), Project.class, existingProject.getId(), RandomData.getString());
        Response response = userUncheckedRequest.getRequest(ApiEndpoint.PROJECTS).create(duplicateProject);
        response.then().spec(IncorrectDataSpecs.badRequestDuplicatedField("Project", "ID", existingProject.getId()));
        softy.assertAll();
    }

    @Feature("Project ID Validation")
    @Story("Duplicate Project ID with Different Case")
    @Test(description = "User should not be able to create a Project with an existing ID in a different case", groups = {"Negative", "CRUD", "PROJECT_ID_VALIDATION_TAG"})
    public void userCannotCreateProjectWithExistingIdDifferentCaseTest() {
        Project existingProject = projectHelper.createProject(userCheckedRequest, testData.getProject());
        String duplicateId = existingProject.getId().toUpperCase();
        Project duplicateProject = TestDataGenerator.generate(List.of(), Project.class, duplicateId, RandomData.getString());
        Response response = userUncheckedRequest.getRequest(ApiEndpoint.PROJECTS).create(duplicateProject);
        response.then().spec(IncorrectDataSpecs.badRequestDuplicatedField("Project", "ID", duplicateId));
        softy.assertAll();
    }


    @Feature("Project ID Validation")
    @Story("Invalid Project ID")
    @Issue("Bug in API: 500 error is returned")
    @Test(description = "User should not be able to create a Project with an ID consisting only of digits",
            groups = {"Negative", "CRUD", "KnownBugs", "PROJECT_ID_VALIDATION_TAG"})
    public void userCannotCreateProjectWithDigitsOnlyIdTest() {
        String invalidId = RandomData.getDigits(6);
        Project invalidProject = TestDataGenerator.generate(List.of(), Project.class, invalidId, RandomData.getString());
        Response response = userUncheckedRequest.getRequest(ApiEndpoint.PROJECTS).create(invalidProject);
        response.then().spec(IncorrectDataSpecs.badRequestWithIncorrectFieldFormat("Project", "ID", invalidId, String.valueOf(invalidId.charAt(0))));
        softy.assertAll();
    }


    @DataProvider
    public static Object[][] invalidIdStartId() {
        String randomString = RandomData.getString(9);
        return new Object[][]{
                {RandomData.getDigits(1) + randomString},
                {"_" + randomString}
        };
    }

    @Feature("Project ID Validation")
    @Story("Invalid Starting Character in Project ID")
    @Issue("Bug in API: 500 error is returned")
    @Test(description = "User should not be able to create a Project with an ID starting with an underscore or a digit",
            groups = {"Negative", "CRUD", "KnownBugs", "PROJECT_ID_VALIDATION_TAG"},
            dataProvider = "invalidIdStartId")
    public void userCannotCreateProjectWithInvalidStartingCharacterIdTest(String invalidId) {
        Project invalidProject = TestDataGenerator.generate(List.of(), Project.class, invalidId, RandomData.getString());
        Response response = userUncheckedRequest.getRequest(ApiEndpoint.PROJECTS).create(invalidProject);
        response.then().spec(IncorrectDataSpecs.badRequestWithIncorrectFieldFormat("Project", "ID", invalidId, String.valueOf(invalidId.charAt(0))));
        softy.assertAll();
    }

    @Feature("Project ID Validation")
    @Story("Spaces in Project ID")
    @Issue("Bug in API: 500 error is returned")
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
        Project createdProject = projectHelper.createProject(superUserCheckRequests, validProject);
        EntityValidator.validateAllEntityFieldsIgnoring(validProject, createdProject, List.of("parentProject"), softy);
        softy.assertAll();
    }


    @Feature("Project ID Validation")
    @Story("Empty Project ID")
    @Issue("Bug in API: 500 error is returned")
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
    @Issue("Bug in API: 500 error is returned")
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
        Project createdProject = projectHelper.createProject(superUserCheckRequests, project);
        EntityValidator.validateAllEntityFieldsIgnoring(project, createdProject, List.of("parentProject"), softy);
        softy.assertAll();
    }

    @Feature("Project Name Validation")
    @Story("Localized Project Name")
    @Test(description = "User should be able to create a Project with a localized name", groups = {"Positive", "CRUD", "PROJECT_NAME_VALIDATION_TAG"})
    public void userCreatesProjectWithLocalizedNameTest() {
        Project localizedProject = TestDataGenerator.generate(Project.class, RandomData.getString(), TestConstants.LOCALIZATION_CHARACTERS);
        Project createdProject = projectHelper.createProject(superUserCheckRequests, localizedProject);
        EntityValidator.validateAllEntityFieldsIgnoring(localizedProject, createdProject, List.of("parentProject"), softy);
        softy.assertAll();
    }

    @Feature("Project Name Validation")
    @Story("One Character Project Name")
    @Test(description = "User should be able to create a Project with a name of length 1", groups = {"Positive", "CRUD", "PROJECT_NAME_VALIDATION_TAG"})
    public void userCreatesProjectWithOneCharacterNameTest() {
        Project validProject = TestDataGenerator.generate(Project.class, RandomData.getString(), "A");
        Project createdProject = projectHelper.createProject(superUserCheckRequests, validProject);
        EntityValidator.validateAllEntityFieldsIgnoring(validProject, createdProject, List.of("parentProject"), softy);
        softy.assertAll();
    }

    @Feature("Project Name Validation")
    @Story("Maximum Length Project Name")
    @Test(description = "User should be able to create a Project with a name of 500 characters", groups = {"Positive", "CRUD", "CornerCase", "PROJECT_NAME_VALIDATION_TAG"})
    public void userCreatesProjectWith500LengthNameTest() {
        String maxLengthName = "A".repeat(500);
        Project validProject = TestDataGenerator.generate(Project.class, RandomData.getString(), maxLengthName);
        Project createdProject = projectHelper.createProject(superUserCheckRequests, validProject);
        EntityValidator.validateAllEntityFieldsIgnoring(validProject, createdProject, List.of("parentProject"), softy);
        softy.assertAll();
    }

    @Feature("Project Name Validation")
    @Story("Duplicate Name with Different Case")
    @Test(description = "User should not be able to create a Project with an existing name in a different case", groups = {"Negative", "CRUD", "PROJECT_NAME_VALIDATION_TAG"})
    public void userCannotCreateProjectWithExistingNameDifferentCaseTest() {
        Project existingProject = projectHelper.createProject(userCheckedRequest, testData.getProject());
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
        Project existingProject = projectHelper.createProject(userCheckedRequest, testData.getProject());
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
        Project createdProject = projectHelper.createProject(superUserCheckRequests, validProject);
        EntityValidator.validateAllEntityFieldsIgnoring(validProject, createdProject, List.of("parentProject"), softy);
        softy.assertAll();
    }

    @Feature("Project Name Validation")
    @Story("Spaces In Name")
    @Test(description = "User should be able to create a Project with spaces in the middle of the name", groups = {"Positive", "CRUD", "PROJECT_NAME_VALIDATION_TAG"})
    public void userCreatesProjectWithSpacesInNameTest() {
        String uniqueProjectName = RandomData.getUniqueName().substring(0, 5) + " " + RandomData.getUniqueName().substring(5);
        Project validProject = TestDataGenerator.generate(Project.class, RandomData.getUniqueId(), uniqueProjectName);
        Project createdProject = projectHelper.createProject(superUserCheckRequests, validProject);
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
        Project createdProject = projectHelper.createProject(superUserCheckRequests, projectWithXSS);
        EntityValidator.validateAllEntityFieldsIgnoring(projectWithXSS, createdProject, List.of("parentProject"), softy);
        softy.assertAll();
    }


    @Feature("Security")
    @Story("SQL Injection Prevention")
    @Test(description = "User should be able to create a Project with an SQL injection payload in name (payload stored as text)", groups = {"Positive", "Security", "CRUD", "SEC_TAG"})
    public void userCreatesProjectWithSQLInjectionTest() {
        Project projectWithSQL = TestDataGenerator.generate(Project.class, RandomData.getUniqueId(), SQL_INJECTION_PAYLOAD);
        Project createdProject = projectHelper.createProject(superUserCheckRequests, projectWithSQL);
        EntityValidator.validateAllEntityFieldsIgnoring(projectWithSQL, createdProject, List.of("parentProject"), softy);
        softy.assertAll();
    }

// =================== SECURITY TESTS (SEC_TAG) =================== //

    //     =================== ROLE-BASED ACCESS TESTS (ROLE_TAG) =================== //

    @DataProvider(name = "rolesWithRegularProjectAccess")
    public static Object[][] rolesWithRegularProjectAccess() {
        return new Object[][]{
                {Role.PROJECT_ADMIN},

        };
    }

    @Feature("Access Control")
    @Story("Project Creation Permissions")
    @Test(description = "User with allowed role should be able to create a regular project", dataProvider = "rolesWithRegularProjectAccess", groups = {"Positive", "CRUD", "ROLE_TAG"})
    public void userWithAllowedRoleCanCreateRegularProjectTest(Role role) {
        User updatedUser = userHelper.createUserWithRole(testData.getUser(), role, TestConstants.ROOT_PROJECT_ID);
        softy.assertNotNull(updatedUser, "User with role " + role.getRoleName() + " was not created");
        Project projectToCreate = TestDataGenerator.generate(Project.class, RandomData.getUniqueId(), "Regular-" + role.getRoleName() + "-" + RandomData.getString(8));
        Project actual = new UncheckedRequest(RequestSpecs.authSpec(updatedUser)).getRequest(ApiEndpoint.PROJECTS).create(projectToCreate).as(Project.class);
        EntityValidator.validateAllEntityFieldsIgnoring(projectToCreate, actual, List.of("parentProject"), softy);
        softy.assertEquals(actual.getParentProject().getId(), TestConstants.ROOT_PROJECT_ID, "Parent project should be '_Root'");
        softy.assertAll();
    }


    @DataProvider(name = "rolesWithoutRegularProjectAccess")
    public static Object[][] rolesWithoutRegularProjectAccess() {
        return new Object[][]{
                {Role.PROJECT_VIEWER},
                {Role.PROJECT_DEVELOPER},
                {Role.TOOLS_INTEGRATION},
                {Role.AGENT_MANAGER}
        };
    }

    @Feature("Access Control")
    @Story("Project Creation Permissions")
    @Test(description = "User with restricted role should not be able to create a regular project", dataProvider = "rolesWithoutRegularProjectAccess", groups = {"Negative", "CRUD", "ROLE_TAG"})
    public void userWithRestrictedRoleCannotCreateRegularProjectTest(Role role) {
        Project parentProject = projectHelper.createProject(userCheckedRequest, testData.getProject());
        User updatedUser = userHelper.createUserWithRole(testData.getUser(), role, parentProject.getId());
        softy.assertNotNull(updatedUser, "User with role " + role.getRoleName() + " was not created");
        Project nestedProject = TestDataGenerator.generate(Project.class, RandomData.getUniqueId(), "Nested-" + role.getRoleName() + "-" + RandomData.getString(8));
        UncheckedRequest restrictedUserRequest = new UncheckedRequest(RequestSpecs.authSpec(updatedUser));
        Response response = restrictedUserRequest.getRequest(ApiEndpoint.PROJECTS).create(nestedProject);
        response.then().spec(AccessErrorSpecs.accessDenied());
        softy.assertAll();
    }

    @DataProvider(name = "rolesWithNestedProjectAccess")
    public static Object[][] rolesWithNestedProjectAccess() {
        return new Object[][]{
                {Role.PROJECT_ADMIN}
        };
    }

    @Feature("Access Control")
    @Story("Project Creation Permissions")
    @Test(description = "User with allowed role should be able to create a nested project", dataProvider = "rolesWithNestedProjectAccess", groups = {"Positive", "CRUD", "ROLE_TAG"})
    public void userWithAllowedRoleCanCreateNestedProjectTest(Role role) {
        Project parent = projectHelper.createProject(userCheckedRequest, testData.getProject());
        User updatedUser = userHelper.createUserWithRole(testData.getUser(), role, parent.getId());
        Project nested = TestDataGenerator.generate(Project.class, RandomData.getUniqueId(), "Nested-" + role.getRoleName() + "-" + RandomData.getString(8));
        nested.setParentProject(new ParentProject(parent.getId(), null));
        Project created = new UncheckedRequest(RequestSpecs.authSpec(updatedUser)).getRequest(ApiEndpoint.PROJECTS).create(nested).as(Project.class);
        EntityValidator.validateAllEntityFieldsIgnoring(nested, created, List.of("parentProject"), softy);
        softy.assertEquals(created.getParentProject().getId(), parent.getId(), "Parent project ID should match");
        softy.assertAll();
    }

    @DataProvider(name = "rolesWithoutNestedProjectAccess")
    public static Object[][] rolesWithoutNestedProjectAccess() {
        return new Object[][]{
                {Role.AGENT_MANAGER},
                {Role.PROJECT_VIEWER},
        };
    }

    @Feature("Access Control")
    @Story("Project Creation Permissions")
    @Test(description = "User with restricted role should not be able to create a nested project", dataProvider = "rolesWithoutNestedProjectAccess", groups = {"Negative", "CRUD", "ROLE_TAG"})
    public void userWithRestrictedRoleCannotCreateNestedProjectTest(Role role) {
        Project parent = projectHelper.createProject(userCheckedRequest, testData.getProject());
        User updatedUser = userHelper.createUserWithRole(testData.getUser(), role, parent.getId());
        Project nested = TestDataGenerator.generate(Project.class, RandomData.getUniqueId(), "Nested-" + role.getRoleName() + "-" + RandomData.getString(8));
        nested.setParentProject(new ParentProject(parent.getId(), null));
        Response response = new UncheckedRequest(RequestSpecs.authSpec(updatedUser)).getRequest(ApiEndpoint.PROJECTS).create(nested);
        response.then().spec(AccessErrorSpecs.accessDenied());
        softy.assertAll();
    }


// =================== ROLE-BASED ACCESS TESTS (ROLE_TAG) =================== //

}







