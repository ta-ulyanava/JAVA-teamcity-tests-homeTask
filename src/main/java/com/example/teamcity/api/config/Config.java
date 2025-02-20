package com.example.teamcity.api.config;

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;
//Создание конфига для распарсинга данных из congif.properties
public class Config {
    // Что храним?
    // 1. сам конфиг для реализации Синглтона
    // 2. прочитанные проперти (читаем 1 раз при создании конфига) ака настройку конфигурации
    // 3. имя файла пропертей
    private static Config config;
    private Properties properties;
    //constant properties filename static +  final  + capital letters
    private static final String CONFIG_PROPERTIES = "config.properties";


    private Config() {
        // перед тем как читать проперти мы их создаем
        properties = new Properties();
        //когда читаем проперти? один раз, когда создаем конфиг, поэтому кладем их в конструктор
        loadProperties(CONFIG_PROPERTIES);
    }

    public static Config getConfig() {
        if (config == null) {
            config = new Config();
        }
        return config;
    }

    //Метод, который читаем саму пропертю
    public static String getProperty(String key){
        return getConfig().properties.getProperty(key); // ✅ Теперь возвращает значение из конфига
    }


// Метод, который читает заданный файл и вычленяет из него проперти
    private void loadProperties(String filename) {
        //создаем инпут стрим на основании нашего файла (т.е. ресурса, который мы хотим открыть)
        // try с ресурсами безопасно открывает файл
        // getClassLoader ищет конкретный путь к ресурсу
        try (InputStream stream = Config.class.getClassLoader().getResourceAsStream(filename)) {
            if (stream == null) {
                System.err.println("File not found: " + filename);
                return;
            }
            //считываем со стрима пропертиз
            properties.load(stream); // Load properties from file
        } catch (IOException e) {
            System.err.println("Error while reading file " + filename + e);
            throw new RuntimeException(e);
        }
    }
}
