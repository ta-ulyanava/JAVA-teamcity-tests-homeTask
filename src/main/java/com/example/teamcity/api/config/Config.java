package com.example.teamcity.api.config;

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

public class Config {
    // Volatile ensures visibility of changes to variables across threads
    private static volatile Config config;
    private static final String CONFIG_PROPERTIES = "config.properties";
    private Properties properties;

    private Config() {
        loadProperties(CONFIG_PROPERTIES);
    }

    public static Config getConfig() {
        if (config == null) {
            config = new Config();
        }
        return config;
    }

    private void loadProperties(String filename) {
        try (InputStream stream = Config.class.getClassLoader().getResourceAsStream(filename)) {
            if (stream == null) {
                System.err.println("File not found: " + filename);
                return;
            }
            properties.load(stream); // Load properties from file
        } catch (IOException e) {
            System.err.println("Error while reading file " + filename);
            throw new RuntimeException(e);
        }
    }
}
