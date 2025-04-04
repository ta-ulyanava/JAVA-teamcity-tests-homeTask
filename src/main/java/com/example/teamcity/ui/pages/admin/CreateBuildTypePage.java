package com.example.teamcity.ui.pages.admin;

import com.codeborne.selenide.Condition;
import com.codeborne.selenide.Selenide;
import com.codeborne.selenide.SelenideElement;
import com.example.teamcity.api.enums.ApiEndpoint;
import com.example.teamcity.api.generators.TestDataStorage;
import io.qameta.allure.Step;

import static com.codeborne.selenide.Selenide.$;

/**
 * Page object representing the 'Create Build Configuration' UI in TeamCity.
 * <p>
 * Supports form submission, validation, and entity registration for cleanup.
 */
public class CreateBuildTypePage extends CreateBasePage {

    private static final String BUILD_TYPE_SHOW_MODE = "createBuildTypeMenu";

    private final SelenideElement buildTypeNameInput = $("#buildTypeName");
    private final SelenideElement errorEmptyBuildTypeName = $("#error_buildTypeName");

    @Step("Open CreateBuildType page for project '{projectId}'")
    public static CreateBuildTypePage open(String projectId) {
        return Selenide.open(
                "/admin/createObjectMenu.html?projectId=%s&showMode=%s".formatted(projectId, BUILD_TYPE_SHOW_MODE),
                CreateBuildTypePage.class
        );
    }

    @Step("Submit VCS URL in create form: {url}")
    public CreateBuildTypePage createForm(String url) {
        String actualUrl = Selenide.webdriver().driver().url();
        if (!actualUrl.contains("createObjectMenu.html")) {
            throw new IllegalStateException("Create Build Type page not loaded. Current URL: " + actualUrl);
        }
        $("#url").shouldBe(Condition.visible);
        baseCreateForm(url);
        return this;
    }

    /**
     * Fills the build type name and proceeds to creation.
     * Also registers the build type for cleanup if it has a non-empty name.
     *
     * @param buildTypeName name of the new build configuration
     * @return current page object
     */
    @Step("Set build configuration name: {buildTypeName}")
    public CreateBuildTypePage setupBuildType(String buildTypeName) {
        buildTypeNameInput.val(buildTypeName);
        submitButton.click();
        if (!buildTypeName.isEmpty()) {
            TestDataStorage.getInstance().addCreatedEntityByName(ApiEndpoint.BUILD_TYPES, buildTypeName);
        }
        return this;
    }

    /**
     * Waits for the specific validation error to be visible.
     *
     * @return the visible error element
     */
    public SelenideElement waitForErrorVisible() {
        return errorEmptyBuildTypeName.shouldBe(Condition.visible);
    }

    /**
     * Returns the UI element containing the validation error message.
     *
     * @return SelenideElement representing the error
     */
    public SelenideElement getErrorEmptyBuildTypeName() {
        return errorEmptyBuildTypeName;
    }

    /**
     * Asserts that the error message element contains the expected text.
     *
     * @param expectedMessage expected error message
     * @return current page object
     */
    @Step("Assert error message is: {expectedMessage}")
    public CreateBuildTypePage assertErrorMessage(String expectedMessage) {
        errorEmptyBuildTypeName.shouldHave(Condition.text(expectedMessage));
        return this;
    }
}
