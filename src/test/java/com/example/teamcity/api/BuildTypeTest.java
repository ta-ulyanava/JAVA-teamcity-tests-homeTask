package com.example.teamcity.api;

import com.example.teamcity.api.enums.Endpoint;
import com.example.teamcity.api.models.BuildType;
import com.example.teamcity.api.models.Project;
import com.example.teamcity.api.models.User;
import com.example.teamcity.api.requests.CheckedRequests;
import com.example.teamcity.api.requests.UncheckedRequest;
import com.example.teamcity.api.requests.unchecked.UncheckedBase;
import com.example.teamcity.api.spec.Specifications;
import org.apache.http.HttpStatus;
import org.hamcrest.Matchers;
import org.testng.annotations.Test;

import java.util.Arrays;
import java.util.regex.Matcher;

import static com.example.teamcity.api.generators.TestDataGenerator.generate;
import static io.qameta.allure.Allure.step;

@Test(groups = {"Regression"})
public class BuildTypeTest extends BaseApiTest {
    // ?? как пользоваться группами в TestNg
    // ?? Где можно писать Теги TestNg
    @Test(description = "User should be able to create Build Type", groups = {"Positive", "CRUD"})
    public void userCreatesBuildTypeTest() {
        // Не используем захардкоженные данные а гениеруем все нужное сами!
        var user = generate(User.class);
        // Создаем юзера, отправляя запрос
        superUserCheckRequests.getRequest(Endpoint.USERS).create(user);
        var userCheckRequests = new CheckedRequests(Specifications.authSpec(user));

        var project = generate(Project.class);

        project = userCheckRequests.<Project>getRequest(Endpoint.PROJECTS).create(project);

        var buildType = generate(Arrays.asList(project), BuildType.class);

        userCheckRequests.getRequest(Endpoint.BUILD_TYPES).create(buildType);
        var createdBuildType = userCheckRequests.<BuildType>getRequest(Endpoint.BUILD_TYPES).read(buildType.getId());

        // будем ассертить софт ассертами
        softy.assertEquals(buildType.getName(), createdBuildType.getName(), "Build type name is not correct");


    }

    @Test(description = "User cannot create two build types with same id", groups = {"Negative", "CRUD"})
    public void userCreatesTwoBuildTypesWithTheSameIdTest() {
        //Создаем BuildType1
        var user = generate(User.class);
        superUserCheckRequests.getRequest(Endpoint.USERS).create(user);
        var userCheckRequests = new CheckedRequests(Specifications.authSpec(user));
        var project = generate(Project.class);
        project = userCheckRequests.<Project>getRequest(Endpoint.PROJECTS).create(project);
        var buildType1 = generate(Arrays.asList(project), BuildType.class);

        //BuildType 2
        var buildType2 = generate(Arrays.asList(project), BuildType.class,buildType1.getId());
        userCheckRequests.getRequest(Endpoint.BUILD_TYPES).create(buildType1);
        //Это негативный тест, поэтому мы не ожидаем никаких проверок и должны использовать Unchecked
        //        step("Create BuildType2 with same Id as BuildType1");
     new UncheckedBase(Specifications.authSpec(user),Endpoint.BUILD_TYPES)
             .create(buildType2)
                     .then().assertThat().statusCode(HttpStatus.SC_BAD_REQUEST)
                     .body(Matchers.containsString("The build configuration / template ID \"%s\" is already used by another configuration or template".formatted(buildType1.getId())));

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
