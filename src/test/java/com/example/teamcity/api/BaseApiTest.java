package com.example.teamcity.api;

import com.example.teamcity.BaseTest;
import com.example.teamcity.api.enums.ApiEndpoint;
import com.example.teamcity.api.enums.Role;
import com.example.teamcity.api.generators.TestDataStorage;
import com.example.teamcity.api.models.AuthModules;
import com.example.teamcity.api.models.Project;
import com.example.teamcity.api.models.ServerAuthSettings;
import com.example.teamcity.api.models.User;
import com.example.teamcity.api.requests.CheckedRequest;
import com.example.teamcity.api.requests.ServerAuthRequest;
import com.example.teamcity.api.requests.UncheckedRequest;
import com.example.teamcity.api.requests.checked.CheckedBase;
import com.example.teamcity.api.helpers.ApiUserHelper;
import com.example.teamcity.api.responses.ResponseExtractor;
import com.example.teamcity.api.spec.request.RequestSpecs;
import io.qameta.allure.Step;
import io.restassured.response.Response;
import org.testng.annotations.AfterSuite;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.BeforeSuite;

import static com.example.teamcity.api.generators.TestDataGenerator.generate;

/**
 * Abstract base class for TeamCity API tests.
 * <p>
 * Handles request initialization, user setup, and authentication settings management.
 */
public abstract class BaseApiTest extends BaseTest {

    protected CheckedRequest userCheckedRequest;
    protected UncheckedRequest userUncheckedRequest;
    protected UncheckedRequest superUserUncheckedRequest = new UncheckedRequest(RequestSpecs.superUserAuthSpec());

    private final ServerAuthRequest serverAuthRequest = new ServerAuthRequest(RequestSpecs.superUserAuthSpec());
    private AuthModules authModules;
    private boolean perProjectPermissions;

    /**
     * Initializes test data and request objects before each test.
     * Creates a user for the test and sets up checked and unchecked requests with its credentials.
     */
    @BeforeMethod(alwaysRun = true)
    public void configureUserRequests() {
        super.beforeTest();
        Response response = superUserUncheckedRequest.getRequest(ApiEndpoint.USERS).create(testData.getUser());
        User createdUser = ResponseExtractor.extractModel(response, User.class);
        TestDataStorage.getInstance().addCreatedEntity(ApiEndpoint.USERS, String.valueOf(createdUser.getId()));

        userCheckedRequest = new CheckedRequest(RequestSpecs.authSpec(testData.getUser()));
        userUncheckedRequest = new UncheckedRequest(RequestSpecs.authSpec(testData.getUser()));
    }

    /**
     * Creates a new user using checked API and extracts the resulting user model.
     *
     * @param user user object to be created
     * @return created user model
     */
    @Step("Create user and extract model")
    protected User createUserAndExtractModel(User user) {
        Response response = (Response) userCheckedRequest.getRequest(ApiEndpoint.USERS).create(user);
        return ResponseExtractor.extractModel(response, User.class);
    }

    /**
     * Creates a new user and assigns a specific role in a given project.
     *
     * @param role      role to assign
     * @param projectId project ID for role scope
     * @return created user model
     */
    @Step("Create user with role '{role}' in project '{projectId}'")
    protected User createUserWithRole(Role role, String projectId) {
        return ApiUserHelper.createUserWithRole(userCheckedRequest, testData.getUser(), role, projectId);
    }

    /**
     * Gets a typed checked request object for project API.
     *
     * @return checked request for {@link Project}
     */
    protected CheckedBase<Project> getCheckedProjectRequest() {
        return userCheckedRequest.getRequest(ApiEndpoint.PROJECTS, Project.class);
    }

    /**
     * Enables per-project permission setting before any tests run and stores original state for cleanup.
     */
    @BeforeSuite(alwaysRun = true)
    public void setUpServerAuthSettings() {
        perProjectPermissions = serverAuthRequest.read().getPerProjectPermissions();
        authModules = generate(AuthModules.class);

        serverAuthRequest.update(ServerAuthSettings.builder()
                .perProjectPermissions(true)
                .modules(authModules)
                .build());
    }

    /**
     * Restores original server auth settings after all tests are complete.
     */
    @AfterSuite(alwaysRun = true)
    public void cleanUpServerAuthSettings() {
        serverAuthRequest.update(ServerAuthSettings.builder()
                .perProjectPermissions(perProjectPermissions)
                .modules(authModules)
                .build());
    }
}
