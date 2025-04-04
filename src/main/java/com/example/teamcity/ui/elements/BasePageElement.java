package com.example.teamcity.ui.elements;

import com.codeborne.selenide.ElementsCollection;
import com.codeborne.selenide.SelenideElement;
import org.openqa.selenium.By;

/**
 * Base class for custom page elements in the UI layer.
 * <p>
 * Provides convenient methods for scoped element search within a parent element.
 */
public abstract class BasePageElement {

    private final SelenideElement element;

    /**
     * Constructs a wrapper for a UI element.
     *
     * @param element root element of the page block
     */
    public BasePageElement(SelenideElement element) {
        this.element = element;
    }

    /**
     * Finds a child element by a Selenium {@link By} selector.
     *
     * @param selector locator strategy
     * @return found Selenide element
     */
    protected SelenideElement find(By selector) {
        return element.$(selector);
    }

    /**
     * Finds a child element by a CSS selector string.
     *
     * @param cssSelector CSS selector
     * @return found Selenide element
     */
    protected SelenideElement find(String cssSelector) {
        return element.$(cssSelector);
    }

    /**
     * Finds all child elements matching the given {@link By} selector.
     *
     * @param selector locator strategy
     * @return collection of matching elements
     */
    protected ElementsCollection findAll(By selector) {
        return element.$$(selector);
    }

    /**
     * Finds all child elements matching the given CSS selector string.
     *
     * @param cssSelector CSS selector
     * @return collection of matching elements
     */
    protected ElementsCollection findAll(String cssSelector) {
        return element.$$(cssSelector);
    }
}
