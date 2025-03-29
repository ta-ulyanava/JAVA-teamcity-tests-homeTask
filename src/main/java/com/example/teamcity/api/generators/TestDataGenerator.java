package com.example.teamcity.api.generators;

import com.example.teamcity.api.annotations.Optional;
import com.example.teamcity.api.annotations.Parameterizable;
import com.example.teamcity.api.annotations.Random;
import com.example.teamcity.api.models.BaseModel;
import com.example.teamcity.api.models.TestData;
import io.qameta.allure.Step;
import lombok.Builder;

import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Utility class for generating test data models based on custom annotations.
 * <p>
 * Handles field population for models using reflection and annotations like @Random, @Optional, and @Parameterizable.
 */
public final class TestDataGenerator {

    private TestDataGenerator() {}

    /**
     * Generates a model of the given class and fills its fields using provided parameters or annotations.
     *
     * @param generatedModels list of previously generated models (to maintain references or dependencies)
     * @param generatorClass  model class to instantiate
     * @param parameters      optional parameters for parameterizable fields
     * @param <T>             type extending BaseModel
     * @return generated and populated instance of the given class
     */
    @Step("Generate instance of class {generatorClass}")
    public static <T extends BaseModel> T generate(List<BaseModel> generatedModels, Class<T> generatorClass, Object... parameters) {
        try {
            var instance = generatorClass.getDeclaredConstructor().newInstance();
            var paramIndex = 0;

            for (Field field : generatorClass.getDeclaredFields()) {
                field.setAccessible(true);
                boolean isParamAvailable = parameters.length > paramIndex;
                boolean hasDefault = field.isAnnotationPresent(Builder.Default.class);
                boolean isOptional = field.isAnnotationPresent(Optional.class);

                if (isOptional && !isParamAvailable && !hasDefault) {
                    field.set(instance, null);
                    continue;
                }

                if (field.isAnnotationPresent(Parameterizable.class) && isParamAvailable) {
                    field.set(instance, parameters[paramIndex]);
                    paramIndex++;
                    continue;
                }

                if (field.isAnnotationPresent(Random.class) && field.getType().equals(String.class)) {
                    field.set(instance, RandomData.getString());
                    continue;
                }

                if (field.getType().equals(Boolean.class)) {
                    if (isParamAvailable) {
                        field.set(instance, parameters[paramIndex]);
                        paramIndex++;
                    } else if (!isOptional || hasDefault) {
                        field.set(instance, field.get(instance));
                    } else {
                        field.set(instance, null);
                    }
                    continue;
                }

                if (field.getType().equals(String.class)) {
                    if (isParamAvailable) {
                        field.set(instance, parameters[paramIndex]);
                        paramIndex++;
                    } else if (!isOptional || hasDefault) {
                        field.set(instance, field.get(instance));
                    } else {
                        field.set(instance, null);
                    }
                    continue;
                }

                if (List.class.isAssignableFrom(field.getType())) {
                    var genericType = (Class<?>) ((java.lang.reflect.ParameterizedType) field.getGenericType()).getActualTypeArguments()[0];

                    if (BaseModel.class.isAssignableFrom(genericType)) {
                        List<BaseModel> generatedList = new ArrayList<>();

                        if (isParamAvailable) {
                            field.set(instance, parameters[paramIndex]);
                        } else if (hasDefault) {
                            field.set(instance, field.get(instance));
                        } else if (!isOptional) {
                            generatedList.add(generate(generatedModels, genericType.asSubclass(BaseModel.class)));
                            field.set(instance, generatedList);
                        } else {
                            field.set(instance, null);
                        }

                        if (isParamAvailable) paramIndex++;
                    } else {
                        field.set(instance, isParamAvailable ? parameters[paramIndex] : (hasDefault ? field.get(instance) : (isOptional ? null : List.of())));
                        if (isParamAvailable) paramIndex++;
                    }
                    continue;
                }

                if (BaseModel.class.isAssignableFrom(field.getType())) {
                    if (isParamAvailable) {
                        field.set(instance, parameters[paramIndex]);
                        paramIndex++;
                    } else if (hasDefault) {
                        field.set(instance, field.get(instance));
                    } else if (!isOptional) {
                        var generatedClass = generate(generatedModels, field.getType().asSubclass(BaseModel.class));
                        field.set(instance, generatedClass);
                    } else {
                        field.set(instance, null);
                    }
                }
            }

            return instance;
        } catch (InstantiationException | IllegalAccessException | InvocationTargetException | NoSuchMethodException e) {
            throw new IllegalStateException("Cannot generate test data", e);
        }
    }

    /**
     * Generates a fully populated instance of {@link TestData}, initializing all its BaseModel fields.
     *
     * @return generated TestData instance
     */
    @Step("Generate TestData with all nested BaseModel fields")
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
     * Shortcut for generating models without a predefined list of generated models.
     *
     * @param generatorClass model class to instantiate
     * @param parameters     optional field values
     * @param <T>            type extending BaseModel
     * @return generated model instance
     */
    @Step("Generate model {generatorClass} with parameters")
    public static <T extends BaseModel> T generate(Class<T> generatorClass, Object... parameters) {
        return generate(Collections.emptyList(), generatorClass, parameters);
    }
}
