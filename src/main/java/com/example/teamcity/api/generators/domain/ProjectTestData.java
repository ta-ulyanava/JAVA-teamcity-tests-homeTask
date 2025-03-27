package com.example.teamcity.api.generators.domain;

import com.example.teamcity.api.generators.RandomData;
import com.example.teamcity.api.generators.TestDataGenerator;
import com.example.teamcity.api.models.ParentProject;
import com.example.teamcity.api.models.Project;
import com.example.teamcity.api.validation.EntityValidator;
import org.testng.asserts.SoftAssert;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class ProjectTestData {
    public static String projectId() {
        return "test_" + UUID.randomUUID().toString().substring(0, 8);
    }
    // Метод 1: Создание N проектов с уникальными значениями
    public static List<Project> createProjects(int count) {
        List<Project> projects = new ArrayList<>();
        for (int i = 0; i < count; i++) {
            // Генерация уникальных значений
            Project project = TestDataGenerator.generate(Project.class, RandomData.getUniqueId(), "name", RandomData.getUniqueName());
            projects.add(project);
        }
        return projects;
    }

    // Метод 2: Создание N проектов с общей частью в поле
    public static List<Project> createProjectsWithCommonPart(int count, String field, String commonPart) {
        List<Project> projects = new ArrayList<>();
        for (int i = 0; i < count; i++) {
            // Генерация значения поля с общей частью и уникальной частью
            String fieldValue = commonPart + "_" + RandomData.getUniqueName(); // Общая часть + уникальная часть

            // Генерация проекта, используя TestDataGenerator
            Project project = TestDataGenerator.generate(Project.class, RandomData.getUniqueId(), field, fieldValue);

            // Убедитесь, что parentProject инициализирован как объект, а не строка
            if (project.getParentProject() == null) {
                ParentProject parent = new ParentProject();
                parent.setId("_Root"); // Пример: _Root это id корневого родительского проекта
                project.setParentProject(parent);
            }

            // Добавляем проект в список
            projects.add(project);
        }
        return projects;
    }


    // Метод 3: Создание N проектов с одинаковым значением поля для всех проектов
    public static List<Project> createProjectsWithSameFieldValue(int count, String field, String fieldValue) {
        List<Project> projects = new ArrayList<>();
        for (int i = 0; i < count; i++) {
            // Создаём проекты с одинаковым значением для указанного поля
            Project project = TestDataGenerator.generate(Project.class, RandomData.getUniqueId(), field, fieldValue);
            projects.add(project);
        }
        return projects;
    }

    /**
     * Создает цепочку вложенных проектов, где каждый следующий проект является дочерним для предыдущего.
     *
     * @param count количество проектов для создания
     * @return список созданных проектов в порядке их вложенности
     * @throws IllegalArgumentException если count отрицательный
     */
    public static List<Project> nestedProjects(int count) {
        if (count < 0) {
            throw new IllegalArgumentException("Count cannot be negative");
        }

        List<Project> nestedProjects = new ArrayList<>();
        String currentParentId = "_Root";

        for (int i = 0; i < count; i++) {
            Project project = TestDataGenerator.generate(
                Project.class,
                RandomData.getUniqueName(),
                projectId(),
                new ParentProject(currentParentId, null)
            );
            nestedProjects.add(project);
            currentParentId = project.getId();
        }
        return nestedProjects;
    }

    /**
     * Создает несколько проектов с общим родительским проектом.
     *
     * @param parentId идентификатор родительского проекта
     * @param count количество проектов для создания
     * @return список созданных проектов-сиблингов
     * @throws IllegalArgumentException если count отрицательный
     */
    public static List<Project> siblingProjects(String parentId, int count) {
        if (count < 0) {
            throw new IllegalArgumentException("Count cannot be negative");
        }

        List<Project> siblingProjects = new ArrayList<>();
        for (int i = 0; i < count; i++) {
            Project project = TestDataGenerator.generate(
                    Project.class,
                    RandomData.getUniqueName(),
                    projectId(),
                    new ParentProject(parentId, null)
            );
            siblingProjects.add(project);
        }
        return siblingProjects;
    }

    /**
     * Создает N проектов с точным значением указанного поля.
     * В отличие от createProjectsWithCommonPart, не добавляет уникальную часть к значению поля.
     *
     * @param count количество проектов для создания
     * @param field имя поля для установки
     * @param exactValue точное значение поля, одинаковое для всех проектов
     * @return список созданных проектов
     */
    public static List<Project> createProjectsWithExactFieldValue(int count, String field, String exactValue) {
        List<Project> projects = new ArrayList<>();
        for (int i = 0; i < count; i++) {
            // Создаем проект с уникальным id
            String projectId = RandomData.getUniqueId();
            Project project = new Project();
            project.setId(projectId);
            
            // Устанавливаем точное значение для указанного поля
            try {
                java.lang.reflect.Field classField = Project.class.getDeclaredField(field);
                classField.setAccessible(true);
                classField.set(project, exactValue);
            } catch (NoSuchFieldException | IllegalAccessException e) {
                throw new RuntimeException("Cannot set field " + field, e);
            }
            
            // Создаем и устанавливаем ParentProject
            ParentProject parent = new ParentProject();
            parent.setId("_Root");
            project.setParentProject(parent);
            
            // Добавляем проект в список
            projects.add(project);
        }
        return projects;
    }

    /**
     * Создает проекты с общим префиксом имени и уникальными числовыми суффиксами.
     * Например: prefixName_1, prefixName_2, prefixName_3, ...
     *
     * @param count количество проектов для создания
     * @param prefixName общий префикс имени
     * @return список созданных проектов
     */
    public static List<Project> createProjectsWithPrefixAndNumericSuffix(int count, String prefixName) {
        List<Project> projects = new ArrayList<>();
        for (int i = 0; i < count; i++) {
            // Создаем проект с уникальным id
            String projectId = RandomData.getUniqueId();
            Project project = new Project();
            project.setId(projectId);
            
            // Устанавливаем имя с префиксом и числовым суффиксом
            project.setName(prefixName + "_" + (i + 1));
            
            // Создаем и устанавливаем ParentProject
            ParentProject parent = new ParentProject();
            parent.setId("_Root");
            project.setParentProject(parent);
            
            // Добавляем проект в список
            projects.add(project);
        }
        return projects;
    }

}