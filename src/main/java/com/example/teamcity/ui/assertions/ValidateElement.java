package com.example.teamcity.ui.assertions;

import com.codeborne.selenide.Condition;
import com.codeborne.selenide.SelenideElement;
import io.qameta.allure.Step;
import org.testng.asserts.SoftAssert;

/**
 * UI assertion helper for validating Selenide elements using soft assertions.
 */
public class ValidateElement {

    private final SoftAssert softy;

    public ValidateElement(SoftAssert softy) {
        this.softy = softy;
    }

    /**
     * Validates that a given element is visible and has the exact expected text.
     *
     * @param element      Selenide element to validate
     * @param expectedText exact expected text content
     */
    @Step("Validate element text equals '{expectedText}'")
    public void byText(SelenideElement element, String expectedText) {
        element.shouldBe(Condition.visible);
        softy.assertTrue(
                element.has(Condition.exactText(expectedText)),
                String.format("Expected element to have text '%s' but found '%s'", expectedText, element.getText())
        );
    }

    /**
     * Static utility method for validating element text with soft assertion.
     *
     * @param element      Selenide element to validate
     * @param expectedText expected text
     * @param softy        SoftAssert instance
     */
    @Step("Validate element text statically: '{expectedText}'")
    public static void byText(SelenideElement element, String expectedText, SoftAssert softy) {
        new ValidateElement(softy).byText(element, expectedText);
    }
}
