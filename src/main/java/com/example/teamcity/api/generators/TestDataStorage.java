package com.example.teamcity.api.generators;

import com.example.teamcity.api.enums.ApiEndpoint;
import com.example.teamcity.api.models.BaseModel;
import com.example.teamcity.api.requests.unchecked.UncheckedBase;
import com.example.teamcity.api.spec.RequestSpecifications;

import java.util.EnumMap;
import java.util.HashSet;
import java.util.Objects;
import java.util.Set;

/**
 * Хранит созданные сущности для последующего удаления
 */
public class TestDataStorage {
    private static TestDataStorage testDataStorage;
    private final EnumMap<ApiEndpoint, Set<String>> createdEntitiesMap;

    private TestDataStorage() {
        createdEntitiesMap = new EnumMap<>(ApiEndpoint.class);
    }

    public static TestDataStorage getInstance() {
        if (testDataStorage == null) {
            testDataStorage = new TestDataStorage();
        }
        return testDataStorage;
    }

    public void addCreatedEntity(ApiEndpoint apiEndpoint, String id) {
        if (id != null) {
            createdEntitiesMap.computeIfAbsent(apiEndpoint, key -> new HashSet<>()).add(id);
        }
    }

    public void addCreatedEntityByName(ApiEndpoint apiEndpoint, String name) {
        if (name != null) {
            var uncheckedBase = new UncheckedBase(RequestSpecifications.superUserAuthSpec(), apiEndpoint);
            var response = uncheckedBase.read("name:" + name);
            var id = response.jsonPath().getString("buildType[0].id");
            addCreatedEntity(apiEndpoint, id);
        }
    }

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

    public void addCreatedEntity(ApiEndpoint apiEndpoint, BaseModel model) {
        addCreatedEntity(apiEndpoint, getEntityIdOrLocator(model));
    }


    public void deleteCreatedEntities() {
        createdEntitiesMap.forEach(((endpoint, ids) ->
                        ids.forEach(id ->
                                new UncheckedBase(RequestSpecifications.superUserAuthSpec(), endpoint).delete(id)
                        )
                )

        );

        createdEntitiesMap.clear();
    }
}
