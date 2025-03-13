package com.example.teamcity.api.ui.validation;

import com.codeborne.selenide.Condition;
import com.codeborne.selenide.SelenideElement;
import com.example.teamcity.api.ui.errors.UiErrors;
import org.testng.asserts.SoftAssert;

public class ValidateElement {
    private final SoftAssert softy;

    public ValidateElement(SoftAssert softy) {
        this.softy = softy;
    }

    public void byText(SelenideElement element, String expectedText) {
        element.should(Condition.visible);
        softy.assertTrue(element.has(Condition.exactText(expectedText)), 
            String.format("Element should have text '%s'", expectedText));
    }

    public static void byText(SelenideElement element, String expectedText, SoftAssert softy) {
        new ValidateElement(softy).byText(element, expectedText);
    }
} 