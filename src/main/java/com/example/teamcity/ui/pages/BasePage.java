package com.example.teamcity.ui.pages;

import com.codeborne.selenide.ElementsCollection;
import com.codeborne.selenide.SelenideElement;
import com.example.teamcity.ui.elements.BasePageElement;

import java.time.Duration;
import java.util.List;
import java.util.function.Function;

/**
 * Base class for all UI pages.
 * <p>
 * Provides shared waiting time constants and utilities for mapping elements to page objects.
 */
public abstract class BasePage {

    protected static final Duration BASE_WAITING = Duration.ofSeconds(30);
    protected static final Duration LONG_WAITING = Duration.ofMinutes(3);

    /**
     * Maps a collection of Selenide elements to a list of page elements using the provided creator function.
     *
     * @param collection ElementsCollection from the page
     * @param creator    mapping function that wraps each SelenideElement into a page element
     * @param <T>        type extending BasePageElement
     * @return list of mapped page elements
     */
    protected <T extends BasePageElement> List<T> generatePageElements(ElementsCollection collection, Function<SelenideElement, T> creator) {
        return collection.stream().map(creator).toList();
    }
}
