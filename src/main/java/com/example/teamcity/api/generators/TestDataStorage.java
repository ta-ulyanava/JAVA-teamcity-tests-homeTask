package com.example.teamcity.api.generators;

import com.example.teamcity.api.enums.ApiEndpoint;
import com.example.teamcity.api.models.BaseModel;
import com.example.teamcity.api.requests.unchecked.UncheckedBase;
import com.example.teamcity.api.spec.request.RequestSpecs;
import io.qameta.allure.Step;

import java.util.EnumMap;
import java.util.HashSet;
import java.util.Objects;
import java.util.Set;

/**
 * Singleton class for storing created test data entities during test execution.
 * <p>
 * Entities are tracked by their API endpoint and ID or locator, so they can be deleted afterward.
 */
public class TestDataStorage {

    private static TestDataStorage testDataStorage;
    private final EnumMap<ApiEndpoint, Set<String>> createdEntitiesMap;

    private TestDataStorage() {
        createdEntitiesMap = new EnumMap<>(ApiEndpoint.class);
    }

    /**
     * Retrieves the singleton instance of the TestDataStorage.
     *
     * @return instance of TestDataStorage
     */
    public static TestDataStorage getInstance() {
        if (testDataStorage == null) {
            testDataStorage = new TestDataStorage();
        }
        return testDataStorage;
    }

    /**
     * Adds a created entity ID to the internal storage for later deletion.
     *
     * @param apiEndpoint API endpoint associated with the entity
     * @param id          unique identifier of the entity
     */
    @Step("Add created entity with ID '{id}' to storage under {apiEndpoint}")
    public void addCreatedEntity(ApiEndpoint apiEndpoint, String id) {
        if (id != null) {
            createdEntitiesMap.computeIfAbsent(apiEndpoint, key -> new HashSet<>()).add(id);
        }
    }

    /**
     * Adds a created entity to storage by locating it by name via API and extracting its ID.
     *
     * @param apiEndpoint API endpoint associated with the entity
     * @param name        name used to find the entity
     */
    @Step("Add created entity by name '{name}' under {apiEndpoint}")
    public void addCreatedEntityByName(ApiEndpoint apiEndpoint, String name) {
        if (name != null) {
            var uncheckedBase = new UncheckedBase(RequestSpecs.superUserAuthSpec(), apiEndpoint);
            var response = uncheckedBase.read("name:" + name);
            var id = response.jsonPath().getString("buildType[0].id");
            addCreatedEntity(apiEndpoint, id);
        }
    }

    /**
     * Retrieves the identifier or locator for a given model instance via reflection.
     *
     * @param model the model to extract ID or locator from
     * @return ID or locator value
     */
    private String getEntityIdOrLocator(BaseModel model) {
        try {
            var idField = model.getClass().getDeclaredField("id");
            idField.setAccessible(true);
            var idFieldValue = Objects.toString(idField.get(model), null);
            idField.setAccessible(false);
            return idFieldValue;
        } catch (NoSuchFieldException | IllegalAccessException e) {
            try {
                var locatorField = model.getClass().getDeclaredField("locator");
                locatorField.setAccessible(true);
                var locatorFieldValue = Objects.toString(locatorField.get(model), null);
                locatorField.setAccessible(false);
                return locatorFieldValue;
            } catch (NoSuchFieldException | IllegalAccessException ex) {
                throw new IllegalStateException("Cannot get id or locator of entity", e);
            }
        }
    }

    /**
     * Adds a created entity to storage by extracting its ID or locator.
     *
     * @param apiEndpoint API endpoint associated with the entity
     * @param model       entity instance
     */
    @Step("Add created entity by model under {apiEndpoint}")
    public void addCreatedEntity(ApiEndpoint apiEndpoint, BaseModel model) {
        addCreatedEntity(apiEndpoint, getEntityIdOrLocator(model));
    }

    /**
     * Deletes all entities tracked in the storage.
     */
    @Step("Delete all tracked created entities")
    public void deleteCreatedEntities() {
        createdEntitiesMap.forEach((endpoint, ids) ->
                ids.forEach(id ->
                        new UncheckedBase(RequestSpecs.superUserAuthSpec(), endpoint).delete(id)
                )
        );
        createdEntitiesMap.clear();
    }
}
