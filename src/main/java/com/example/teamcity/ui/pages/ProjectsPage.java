package com.example.teamcity.ui.pages;

import com.codeborne.selenide.*;
import com.example.teamcity.ui.elements.ProjectElement;
import io.qameta.allure.Step;

import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import static com.codeborne.selenide.Selenide.$;
import static com.codeborne.selenide.Selenide.$$;

/**
 * Page object for the "Favorite Projects" page in TeamCity UI.
 * <p>
 * Provides access to project tiles and utility actions.
 */
public class ProjectsPage extends BasePage {

    private static final String PROJECTS_URL = "/favorite/projects";

    private final ElementsCollection projectsElements = $$("span[class*='MiddleEllipsis']");
    private final SelenideElement spanFavoriteProjects = $("span[class='ProjectPageHeader__title--ih']");
    private final SelenideElement header = $(".MainPanel__router--gF > div");

    /**
     * Opens the TeamCity favorite projects page.
     *
     * @return initialized ProjectsPage
     */
    @Step("Open projects page")
    public static ProjectsPage open() {
        return Selenide.open(PROJECTS_URL, ProjectsPage.class);
    }

    /**
     * Constructor that waits for the header to appear before proceeding.
     */
    public ProjectsPage() {
        header.shouldBe(Condition.visible, BASE_WAITING);
    }

    /**
     * Returns the list of project elements displayed on the page.
     *
     * @return list of {@link ProjectElement}
     */
    public List<ProjectElement> getProjects() {
        return generatePageElements(projectsElements, ProjectElement::new);
    }

    /**
     * Logs names of all visible projects to the console (useful for debugging).
     */
    @Step("Log all visible project blocks")
    public void logVisibleProjects() {
        getProjects().forEach(project -> {
            String name = project.getName().text();
            System.out.println("> " + name);
        });
    }

    /**
     * Waits for a project with the given name to appear on the page.
     *
     * @param name expected project name
     */
    @Step("Wait for project to appear: {name}")
    public void waitForProjectToAppear(String name) {
        projectsElements.findBy(Condition.text(name))
                .shouldBe(Condition.visible, Duration.ofSeconds(5));
    }
    /**
     * Returns a list of all visible project names on the Projects page.
     *
     * @return list of visible project names
     */
    @Step("Get visible project names from Projects page")
    public List<String> getVisibleProjectNames() {
        projectsElements.shouldBe(CollectionCondition.sizeGreaterThan(0), Duration.ofSeconds(5));
        return projectsElements.stream()
                .map(element -> element.shouldBe(Condition.visible).getText())
                .collect(Collectors.toList());
    }



}
