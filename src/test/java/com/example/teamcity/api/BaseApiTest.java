package com.example.teamcity.api;

import com.example.teamcity.BaseTest;
import com.example.teamcity.api.enums.ApiEndpoint;
import com.example.teamcity.api.enums.Role;
import com.example.teamcity.api.generators.TestDataStorage;
import com.example.teamcity.api.helpers.ApiProjectHelper;
import com.example.teamcity.api.helpers.ApiUserHelper;
import com.example.teamcity.api.models.AuthModules;
import com.example.teamcity.api.models.Project;
import com.example.teamcity.api.models.ServerAuthSettings;
import com.example.teamcity.api.models.User;
import com.example.teamcity.api.requests.CheckedRequest;
import com.example.teamcity.api.requests.ServerAuthRequest;
import com.example.teamcity.api.requests.UncheckedRequest;
import com.example.teamcity.api.requests.checked.CheckedBase;
import com.example.teamcity.api.responses.ResponseExtractor;
import com.example.teamcity.api.spec.request.RequestSpecs;
import io.qameta.allure.Step;
import io.restassured.response.Response;
import org.testng.annotations.AfterSuite;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.BeforeSuite;

import static com.example.teamcity.api.generators.TestDataGenerator.generate;

public abstract class BaseApiTest extends BaseTest {

    protected CheckedRequest userCheckedRequest;
    protected UncheckedRequest userUncheckedRequest;
    protected UncheckedRequest superUserUncheckedRequest = new UncheckedRequest(RequestSpecs.superUserAuthSpec());
    protected ApiProjectHelper projectHelper;
    protected ApiUserHelper userHelper;

    private final ServerAuthRequest serverAuthRequest = new ServerAuthRequest(RequestSpecs.superUserAuthSpec());
    private AuthModules authModules;
    private boolean perProjectPermissions;

    @BeforeMethod(alwaysRun = true)
    public void configureUserRequests() {
        super.beforeTest();
        Response response = superUserUncheckedRequest.getRequest(ApiEndpoint.USERS).create(testData.getUser());
        User createdUser = ResponseExtractor.extractModel(response, User.class);
        TestDataStorage.getInstance().addCreatedEntity(ApiEndpoint.USERS, String.valueOf(createdUser.getId()));

        userCheckedRequest = new CheckedRequest(RequestSpecs.authSpec(testData.getUser()));
        userUncheckedRequest = new UncheckedRequest(RequestSpecs.authSpec(testData.getUser()));
        projectHelper = new ApiProjectHelper();
        userHelper = new ApiUserHelper(userCheckedRequest);
    }

    @Step("Create user and extract model")
    protected User createUserAndExtractModel(User user) {
        Response response = (Response) userCheckedRequest.getRequest(ApiEndpoint.USERS).create(user);
        return ResponseExtractor.extractModel(response, User.class);
    }

    @Step("Create user with role '{role}' in project '{projectId}'")
    public User createUserWithRole(Role role, String projectId) {
        User user = testData.getUser();
        return userHelper.createUserWithRole(user, role, projectId);
    }


    protected CheckedBase<Project> getCheckedProjectRequest() {
        return userCheckedRequest.getRequest(ApiEndpoint.PROJECTS, Project.class);
    }

    @BeforeSuite(alwaysRun = true)
    public void setUpServerAuthSettings() {
        perProjectPermissions = serverAuthRequest.read().getPerProjectPermissions();
        authModules = generate(AuthModules.class);

        serverAuthRequest.update(ServerAuthSettings.builder()
                .perProjectPermissions(true)
                .modules(authModules)
                .build());
    }

    @AfterSuite(alwaysRun = true)
    public void cleanUpServerAuthSettings() {
        serverAuthRequest.update(ServerAuthSettings.builder()
                .perProjectPermissions(perProjectPermissions)
                .modules(authModules)
                .build());
    }
}
