package com.example.teamcity.api;

import com.example.teamcity.api.enums.ApiEndpoint;
import com.example.teamcity.api.models.BuildType;
import com.example.teamcity.api.models.Project;
import com.example.teamcity.api.requests.CheckedRequest;
import com.example.teamcity.api.requests.unchecked.UncheckedBase;
import com.example.teamcity.api.spec.Specifications;
import org.apache.http.HttpStatus;
import org.hamcrest.Matchers;
import org.testng.annotations.Test;

import java.util.Arrays;

import static com.example.teamcity.api.generators.TestDataGenerator.generate;
import static io.qameta.allure.Allure.step;

@Test(groups = {"Regression"})
public class BuildTypeTest extends BaseApiTest {

    @Test(description = "User should be able to create Build Type", groups = {"Positive", "CRUD"})
    public void userCreatesBuildTypeTest() {
        superUserCheckRequests.getRequest(ApiEndpoint.USERS).create(testData.getUser());
        var userCheckRequests = new CheckedRequest(Specifications.authSpec(testData.getUser()));
        userCheckRequests.<Project>getRequest(ApiEndpoint.PROJECTS).create(testData.getProject());
        userCheckRequests.getRequest(ApiEndpoint.BUILD_TYPES).create(testData.getBuildType());
        var createdBuildType = userCheckRequests.<BuildType>getRequest(ApiEndpoint.BUILD_TYPES).read(testData.getBuildType().getId());
        softy.assertEquals(testData.getBuildType().getName(), createdBuildType.getName(), "Build type name is not correct");


    }

    @Test(description = "User cannot create two build types with same id", groups = {"Negative", "CRUD"})
    public void userCreatesTwoBuildTypesWithTheSameIdTest() {
        superUserCheckRequests.getRequest(ApiEndpoint.USERS).create(testData.getUser());
        var userCheckRequests = new CheckedRequest(Specifications.authSpec(testData.getUser()));
        userCheckRequests.<Project>getRequest(ApiEndpoint.PROJECTS).create(testData.getProject());
        var buildTypeWithSameId = generate(Arrays.asList(testData.getProject()), BuildType.class, testData.getBuildType().getId());
        userCheckRequests.getRequest(ApiEndpoint.BUILD_TYPES).create(testData.getBuildType());
        new UncheckedBase(Specifications.authSpec(testData.getUser()), ApiEndpoint.BUILD_TYPES)
                .create(buildTypeWithSameId)
                .then().assertThat().statusCode(HttpStatus.SC_BAD_REQUEST)
                .body(Matchers.containsString("The build configuration / template ID \"%s\" is already used by another configuration or template".formatted(testData.getBuildType().getId())));

        step("Check BuildType2 was not created with bad request code");
    }

    @Test(description = "Project Admin can create Build Type for their project", groups = {"Positive", "Roles"})
    public void projectAdminCreatesBuildTypeTest() {

        step("Create user");
        step("Create project");
        step("Grant user PROJECT_ADMIN role in project");
        step("Create BuildType for project by user(PROJECT_ADMIN)");
        step("Check BuildType was created successfully");
    }

    @Test(description = "Project Admin cannot create Build Type for not their project", groups = {"Negative", "Roles"})
    public void projectAdminCreatesBuildTypeForAnotherUserProjectTest() {

        step("Create user1");
        step("Create project1");
        step("Grant user1 PROJECT_ADMIN role in project1");

        step("Create user2");
        step("Create project2");
        step("Grant user2 PROJECT_ADMIN role in project2");

        step("Create BuildType for project1 by user2");
        step("Check BuildType was not created with forbidden code");
    }
}
