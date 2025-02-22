package com.example.teamcity.api.generators;

import com.example.teamcity.api.annotations.Optional;
import com.example.teamcity.api.annotations.Parameterizable;
import com.example.teamcity.api.annotations.Random;
import com.example.teamcity.api.models.BaseModel;
import com.example.teamcity.api.models.ParentProject;
import com.example.teamcity.api.models.TestData;

import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.ParameterizedType;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public final class TestDataGenerator {

    private TestDataGenerator() {
    }

    /**
     * Основной метод генерации тестовых данных.
     *
     * Логика:
     * 1) Если поле `@Optional`, оно пропускается.
     * 2) Если поле `@Parameterizable`:
     *    - Если передан параметр (включая `""`) → используем его.
     *    - Если параметр не передан:
     *        - Строковые поля заполняются случайным значением.
     *        - Объект `ParentProject` получает `locator="_Root"`.
     *        - Остальные `BaseModel`-поля создаются рекурсивно.
     * 3) Если поле `@Random` и строковое, оно заполняется случайными данными.
     * 4) Если поле - объект `BaseModel`, оно создается рекурсивно.
     * 5) Если поле - `List<BaseModel>`, создается список из одной случайной сущности.
     */
    public static <T extends BaseModel> T generate(List<BaseModel> generatedModels, Class<T> generatorClass, Object... parameters) {
        try {
            var instance = generatorClass.getDeclaredConstructor().newInstance();
            var paramIndex = 0;

            for (Field field : generatorClass.getDeclaredFields()) {
                field.setAccessible(true);

                // Если поле @Optional и параметр не передан – НЕ добавляем его в JSON
                if (field.isAnnotationPresent(Optional.class) && parameters.length <= paramIndex) {
                    field.set(instance, null);
                    continue;
                }

                // Если передан параметр - используем его
                if (field.isAnnotationPresent(Parameterizable.class) && parameters.length > paramIndex) {
                    field.set(instance, parameters[paramIndex]);
                    paramIndex++;
                }
                // Если поле @Random, но параметр НЕ передан – генерируем случайное значение
                else if (field.isAnnotationPresent(Random.class)) {
                    if (field.getType().equals(String.class)) {
                        field.set(instance, RandomData.getString());
                    }
                }
                // Если поле @Parameterizable, но параметр НЕ передан – задаем дефолтное значение
                else if (field.isAnnotationPresent(Parameterizable.class)) {
                    if (field.getType().equals(String.class)) {
                        field.set(instance, RandomData.getString());
                    } else if (field.getType().equals(ParentProject.class)) {
                        field.set(instance, ParentProject.defaultRoot());
                    } else if (BaseModel.class.isAssignableFrom(field.getType())) {
                        var generatedClass = generate(generatedModels, field.getType().asSubclass(BaseModel.class));
                        field.set(instance, generatedClass);
                    }
                }
                // Генерация вложенных объектов
                else if (BaseModel.class.isAssignableFrom(field.getType())) {
                    var generatedClass = generatedModels.stream()
                            .filter(m -> m.getClass().equals(field.getType()))
                            .findFirst()
                            .orElseGet(() -> generate(generatedModels, field.getType().asSubclass(BaseModel.class)));

                    field.set(instance, generatedClass);
                }
                // Генерация списков вложенных объектов
                else if (List.class.isAssignableFrom(field.getType())) {
                    if (field.getGenericType() instanceof ParameterizedType pt) {
                        var typeClass = (Class<?>) pt.getActualTypeArguments()[0];
                        if (BaseModel.class.isAssignableFrom(typeClass)) {
                            var generatedClass = generate(generatedModels, typeClass.asSubclass(BaseModel.class));
                            field.set(instance, new ArrayList<>(List.of(generatedClass))); // Изменяемый список
                        }
                    }
                }

                field.setAccessible(false);
            }
            return instance;
        } catch (InstantiationException | IllegalAccessException | InvocationTargetException | NoSuchMethodException e) {
            throw new IllegalStateException("Cannot generate test data", e);
        }
    }

    /**
     * Генерация тестовых данных для `TestData` (получает все сущности в одном месте).
     */
    public static TestData generate() {
        try {
            var instance = TestData.class.getDeclaredConstructor().newInstance();
            var generatedModels = new ArrayList<BaseModel>();
            for (var field : TestData.class.getDeclaredFields()) {
                field.setAccessible(true);
                if (BaseModel.class.isAssignableFrom(field.getType())) {
                    var generatedModel = generate(generatedModels, field.getType().asSubclass(BaseModel.class));
                    field.set(instance, generatedModel);
                    generatedModels.add(generatedModel);
                }
                field.setAccessible(false);
            }
            return instance;
        } catch (InstantiationException | IllegalAccessException | InvocationTargetException | NoSuchMethodException e) {
            throw new IllegalStateException("Cannot generate test data", e);
        }
    }

    /**
     * Метод для генерации одной сущности (без учета уже созданных моделей).
     */
    public static <T extends BaseModel> T generate(Class<T> generatorClass, Object... parameters) {
        return generate(Collections.emptyList(), generatorClass, parameters);
    }
}
