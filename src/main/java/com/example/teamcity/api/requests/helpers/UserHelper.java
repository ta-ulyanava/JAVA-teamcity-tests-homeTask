package com.example.teamcity.api.requests.helpers;

import com.example.teamcity.api.enums.ApiEndpoint;
import com.example.teamcity.api.enums.Role;
import com.example.teamcity.api.generators.RandomData;
import com.example.teamcity.api.models.Roles;
import com.example.teamcity.api.models.User;
import com.example.teamcity.api.requests.CheckedRequest;
import com.example.teamcity.api.responses.ResponseExtractor;
import io.restassured.response.Response;

import java.util.List;

public final class UserHelper {
    private UserHelper() {}

    /**
     * Создает пользователя с указанной ролью в проекте
     */
    public static User createUserWithRole(CheckedRequest checkedRequest, User templateUser, Role role, String projectId) {
        // Создаем базового пользователя
        User user = createBaseUser(templateUser);
        
        // Добавляем роль
        assignRole(user, role, projectId);
        
        // Отправляем запрос и получаем созданного пользователя
        return saveUser(checkedRequest, user);
    }
    
    /**
     * Обновляет существующего пользователя, добавляя ему указанную роль
     * Важно: для изменения ролей пользователя необходимо использовать учетную запись с правами администратора
     * (пользователь не может изменять свои собственные роли в TeamCity)
     */
    public static User updateUserRole(CheckedRequest adminCheckedRequest, User existingUser, Role role, String projectId) {
        // Добавляем роль существующему пользователю
        assignRole(existingUser, role, projectId);
        
        // Обновляем пользователя через API с правами администратора
        return updateUser(adminCheckedRequest, existingUser);
    }

    /**
     * Назначает роль пользователю
     */
    private static void assignRole(User user, Role role, String projectId) {
        String roleScope = "g".equals(projectId) ? "p:_Root" : "p:" + projectId;
        user.setRoles(new Roles(List.of(new com.example.teamcity.api.models.Role(role.getRoleName(), roleScope))));
    }

    /**
     * Создает базового пользователя на основе шаблона
     */
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

    /**
     * Сохраняет пользователя через API
     */
    private static User saveUser(CheckedRequest checkedRequest, User user) {
        Response response = (Response) checkedRequest.getRequest(ApiEndpoint.USERS).create(user);
        User createdUser = ResponseExtractor.extractModel(response, User.class);
        
        // Сохраняем пароль, поскольку API не возвращает его в ответе
        createdUser.setPassword(user.getPassword());
        
        return createdUser;
    }
    
    /**
     * Обновляет существующего пользователя через API
     */
    private static User updateUser(CheckedRequest checkedRequest, User user) {
        // Используем username вместо id, так как id может быть неактуальным
        String locator = "username:" + user.getUsername();
        // Метод update возвращает User напрямую, а не Response
        User updatedUser = (User) checkedRequest.getRequest(ApiEndpoint.USERS).update(locator, user);
        
        // Сохраняем пароль, поскольку API не возвращает его в ответе
        updatedUser.setPassword(user.getPassword());
        
        return updatedUser;
    }

}
