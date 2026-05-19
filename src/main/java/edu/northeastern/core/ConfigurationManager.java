package edu.northeastern.core;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

public class ConfigurationManager {

    private static ConfigurationManager instance;
    private Map<String, Map<String, Object>> configData = new HashMap<>();

    private ConfigurationManager() {}

    public static ConfigurationManager getInstance() {
        if (instance == null) {
            instance = new ConfigurationManager();
        }
        return instance;
    }

    public void loadConfig(File configFile) {
        if (configFile != null && configFile.exists()) {
            try {
                ObjectMapper mapper = new ObjectMapper();
                configData = mapper.readValue(configFile, new TypeReference<Map<String, Map<String, Object>>>() {});
                System.out.println("Successfully loaded configuration from: " + configFile.getAbsolutePath());
            } catch (IOException e) {
                System.err.println("Warning: Failed to load configuration file. Using default thresholds. Error: " + e.getMessage());
            }
        }
    }

    public static int getInt(String smellName, String key, int defaultValue) {
        Number value = getInstance().getTypedValue(smellName, key, defaultValue, Number.class);
        return value != null ? value.intValue() : defaultValue;
    }

    public static double getDouble(String smellName, String key, double defaultValue) {
        Number value = getInstance().getTypedValue(smellName, key, defaultValue, Number.class);
        return value != null ? value.doubleValue() : defaultValue;
    }

    private <T> T getTypedValue(String smellName, String key, T defaultValue, Class<?> type) {
        if (configData.containsKey(smellName)) {
            Map<String, Object> smellConfig = configData.get(smellName);
            if (smellConfig.containsKey(key)) {
                Object value = smellConfig.get(key);
                if (type.isInstance(value)) {
                    return (T) value;
                }
            }
        }
        return defaultValue;
    }
}
