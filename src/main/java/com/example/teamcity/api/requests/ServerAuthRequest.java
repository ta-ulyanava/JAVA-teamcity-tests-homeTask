package com.example.teamcity.api.requests;

import com.example.teamcity.api.models.ServerAuthSettings;
import io.qameta.allure.Step;
import io.restassured.RestAssured;
import io.restassured.specification.RequestSpecification;
import org.apache.http.HttpStatus;

/**
 * Handles requests to TeamCity server authentication settings endpoint.
 */
public class ServerAuthRequest {

    private static final String SERVER_AUTH_SETTINGS_URL = "/app/rest/server/authSettings";
    private final RequestSpecification spec;

    public ServerAuthRequest(RequestSpecification spec) {
        this.spec = spec;
    }

    /**
     * Reads the current server authentication settings.
     *
     * @return the current {@link ServerAuthSettings}
     */
    @Step("Read server authentication settings")
    public ServerAuthSettings read() {
        return RestAssured.given()
                .spec(spec)
                .get(SERVER_AUTH_SETTINGS_URL)
                .then().assertThat().statusCode(HttpStatus.SC_OK)
                .extract().as(ServerAuthSettings.class);
    }

    /**
     * Updates the server authentication settings.
     *
     * @param authSettings new authentication settings to apply
     * @return updated {@link ServerAuthSettings}
     */
    @Step("Update server authentication settings")
    public ServerAuthSettings update(ServerAuthSettings authSettings) {
        return RestAssured.given()
                .spec(spec)
                .body(authSettings)
                .put(SERVER_AUTH_SETTINGS_URL)
                .then().assertThat().statusCode(HttpStatus.SC_OK)
                .extract().as(ServerAuthSettings.class);
    }
}
