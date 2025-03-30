package com.example.teamcity.api.helpers;

import com.example.teamcity.api.enums.ApiEndpoint;
import com.example.teamcity.api.enums.Role;
import com.example.teamcity.api.generators.RandomData;
import com.example.teamcity.api.models.Roles;
import com.example.teamcity.api.models.User;
import com.example.teamcity.api.requests.CheckedRequest;
import com.example.teamcity.api.requests.checked.CheckedBase;
import com.example.teamcity.api.responses.ResponseExtractor;
import io.qameta.allure.Step;
import io.restassured.response.Response;

import java.util.List;

public final class ApiUserHelper {
    private final CheckedRequest checkedRequest;

    public ApiUserHelper(CheckedRequest checkedRequest) {
        this.checkedRequest = checkedRequest;
    }

    @Step("Create user with role {role} in project {projectId}")
    public User createUserWithRole(User templateUser, Role role, String projectId) {
        User user = createBaseUser(templateUser);
        assignRole(user, role, projectId);
        return saveUser(user);
    }

    @Step("Update user role to {role} in project {projectId}")
    public User updateUserRole(User existingUser, Role role, String projectId) {
        assignRole(existingUser, role, projectId);
        return updateUser(existingUser);
    }

    private void assignRole(User user, Role role, String projectId) {
        String roleScope = "g".equals(projectId) ? "p:_Root" : "p:" + projectId;
        user.setRoles(new Roles(List.of(new com.example.teamcity.api.models.Role(role.getRoleName(), roleScope))));
    }

    private User createBaseUser(User templateUser) {
        User user = new User();
        user.setUsername(RandomData.getUniqueName());

        String password = templateUser.getPassword();
        if (password == null || password.isEmpty()) {
            password = RandomData.getString(12);
        }
        user.setPassword(password);

        return user;
    }

    private User saveUser(User user) {
        Response response = checkedRequest.getRequest(ApiEndpoint.USERS).create(user);
        User createdUser = ResponseExtractor.extractModel(response, User.class);
        createdUser.setPassword(user.getPassword());
        return createdUser;
    }

    private User updateUser(User user) {
        String locator = "username:" + user.getUsername();
        CheckedBase<User> userCheckedRequest = checkedRequest.getRequest(ApiEndpoint.USERS, User.class);
        User updatedUser = userCheckedRequest.update(locator, user); // теперь работает с типом User

        updatedUser.setPassword(user.getPassword());
        return updatedUser;
    }

}
