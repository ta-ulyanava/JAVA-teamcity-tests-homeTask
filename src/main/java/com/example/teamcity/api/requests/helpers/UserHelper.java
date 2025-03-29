package com.example.teamcity.api.requests.helpers;

import com.example.teamcity.api.enums.ApiEndpoint;
import com.example.teamcity.api.enums.Role;
import com.example.teamcity.api.generators.RandomData;
import com.example.teamcity.api.models.Roles;
import com.example.teamcity.api.models.User;
import com.example.teamcity.api.requests.CheckedRequest;
import com.example.teamcity.api.responses.ResponseExtractor;
import io.qameta.allure.Step;
import io.restassured.response.Response;

import java.util.List;

public final class UserHelper {

    private UserHelper() {}

    @Step("Create user with role {role} in project {projectId}")
    public static User createUserWithRole(CheckedRequest checkedRequest, User templateUser, Role role, String projectId) {
        User user = createBaseUser(templateUser);
        assignRole(user, role, projectId);
        return saveUser(checkedRequest, user);
    }

    @Step("Update user role to {role} in project {projectId}")
    public static User updateUserRole(CheckedRequest adminCheckedRequest, User existingUser, Role role, String projectId) {
        assignRole(existingUser, role, projectId);
        return updateUser(adminCheckedRequest, existingUser);
    }

    private static void assignRole(User user, Role role, String projectId) {
        String roleScope = "g".equals(projectId) ? "p:_Root" : "p:" + projectId;
        user.setRoles(new Roles(List.of(new com.example.teamcity.api.models.Role(role.getRoleName(), roleScope))));
    }

    private static User createBaseUser(User templateUser) {
        User user = new User();
        user.setUsername(RandomData.getUniqueName());

        String password = templateUser.getPassword();
        if (password == null || password.isEmpty()) {
            password = RandomData.getString(12);
        }
        user.setPassword(password);

        return user;
    }

    private static User saveUser(CheckedRequest checkedRequest, User user) {
        Response response = (Response) checkedRequest.getRequest(ApiEndpoint.USERS).create(user);
        User createdUser = ResponseExtractor.extractModel(response, User.class);
        createdUser.setPassword(user.getPassword());
        return createdUser;
    }

    private static User updateUser(CheckedRequest checkedRequest, User user) {
        String locator = "username:" + user.getUsername();
        User updatedUser = (User) checkedRequest.getRequest(ApiEndpoint.USERS).update(locator, user);
        updatedUser.setPassword(user.getPassword());
        return updatedUser;
    }
}
