package com.example.teamcity.ui.pages.admin;

import com.codeborne.selenide.Condition;
import com.codeborne.selenide.Selenide;
import com.codeborne.selenide.SelenideElement;
import com.example.teamcity.api.enums.ApiEndpoint;
import com.example.teamcity.api.enums.WebRoute;
import com.example.teamcity.api.generators.TestDataStorage;
import io.qameta.allure.Step;

import static com.codeborne.selenide.Selenide.$;
import static com.codeborne.selenide.Selenide.page;

public class CreateBuildTypePage extends CreateBasePage {
    private static final String BUILD_TYPE_SHOW_MODE = "createBuildTypeMenu";
    private final SelenideElement buildTypeNameInput = $("#buildTypeName");
    private final SelenideElement errorMessage = $(".error");

    @Step("Open CreateBuildType page")
    public static CreateBuildTypePage open(String projectId) {
        return Selenide.open(WebRoute.CREATE_BUILD_TYPE_PAGE.getUrl().formatted(projectId), CreateBuildTypePage.class);
    }

    public CreateBuildTypePage createForm(String url) {
        baseCreateForm(url);
        return this;
    }

    public CreateBuildTypePage setupBuildType(String buildTypeName) {
        buildTypeNameInput.val(buildTypeName);
        submitButton.click();
        if (!buildTypeName.isEmpty()) {
            TestDataStorage.getInstance().addCreatedEntityByName(ApiEndpoint.BUILD_TYPES, buildTypeName);
        }
        return this;
    }

    public SelenideElement getErrorMessage() {
        return errorMessage;
    }

    public CreateBuildTypePage assertErrorMessage(String expectedMessage) {
        errorMessage.shouldHave(Condition.text(expectedMessage));
        return this;
    }
} 