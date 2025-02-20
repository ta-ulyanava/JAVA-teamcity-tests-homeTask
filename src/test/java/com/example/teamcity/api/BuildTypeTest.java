package com.example.teamcity.api;

import com.example.teamcity.api.enums.Endpoint;
import com.example.teamcity.api.models.BuildType;
import com.example.teamcity.api.models.Project;
import com.example.teamcity.api.models.User;
import com.example.teamcity.api.requests.checked.CheckedBase;
import com.example.teamcity.api.spec.Specifications;
import org.testng.annotations.Test;

import java.util.concurrent.atomic.AtomicReference;

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
        step("Create user", () -> {
          /*  var user=  User.builder()
                    .name(RandomData.getString())
                    .password(RandomData.getString())
                    .build();*/

            // Создаем шаблон-риквестер
            var requester = new CheckedBase<User>(Specifications.superUserAuth(), Endpoint.USERS);
            requester.create(user);
        });
        var project = generate(Project.class);
        //???
        AtomicReference<String> projectLocator= new AtomicReference<>("");
        step("Create Project by user", () -> {
            var requester = new CheckedBase<Project>(Specifications.authSpec(user), Endpoint.PROJECTS);
            projectLocator.set(requester.create(project).getId());

            //projectLocator.set(requester.create(project).getLocator());
        });

        var buildType = generate(BuildType.class);
        buildType.setProject(Project.builder().id(null).locator(projectLocator.get()).build());
        var requester = new CheckedBase<BuildType>(Specifications.authSpec(user), Endpoint.BUILD_TYPES);
        AtomicReference<String> buildTypeId = new AtomicReference<>("");
        step("Create BuildType for project by user", () -> {
                    buildTypeId.set(requester.create(buildType).getId());
                }
        );
        step("Check BuildType was created successfully with correct data", () -> {
                   var createdBuildType= requester.read( buildTypeId.get());
                   // будем ассертить софт ассертами
            softy.assertEquals(buildType.getName(), createdBuildType.getName(), "Build type name is not correct");
                }
        );
    }

    @Test(description = "User cannot create two build types with same id", groups = {"Negative", "CRUD"})
    public void userCreatesTwoBuildTypesWithTheSameIdTest() {

        step("Create user");
        step("Create Project");
        step("Create BuildType1");
        step("Create BuildType2 with same Id as BuildType1");
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
