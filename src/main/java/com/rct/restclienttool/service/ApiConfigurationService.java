package com.rct.restclienttool.service;

import com.rct.restclienttool.model.ApiConfiguration;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
* Service class for managing API configuration persistence.
* Handles saving, loading, and deleting API configurations to/from the file system.
*
 * @author REST Client Tool
* @version 1.0
* @since 1.0
*/
public class ApiConfigurationService {
    private static final Logger LOGGER = Logger.getLogger(ApiConfigurationService.class.getName());
    private static final String CONFIG_DIR = System.getProperty("user.home") + File.separator + ".restclienttool";
    private static final String CONFIG_FILE = CONFIG_DIR + File.separator + "api-configurations.dat";

    /**
     * Constructor that ensures the configuration directory exists.
     * Creates the directory if it doesn't exist.
     */
    public ApiConfigurationService() {
        // Create config directory if it doesn't exist
        try {
            Path configDir = Paths.get(CONFIG_DIR);
            if (!Files.exists(configDir)) {
                Files.createDirectories(configDir);
            }
        } catch (IOException e) {
            LOGGER.log(Level.SEVERE, "Failed to create configuration directory", e);
        }
    }

    /**
     * Saves an API configuration to persistent storage.
     * If a configuration with the same name exists, it will be updated.
     *
     * @param config the API configuration to save
     */
    public void saveConfiguration(ApiConfiguration config) {
        List<ApiConfiguration> configs = loadAllConfigurations();
       
        // Check if configuration with same name exists
        boolean exists = false;
        for (int i = 0; i < configs.size(); i++) {
            if (configs.get(i).getName().equals(config.getName())) {
                configs.set(i, config);
                exists = true;
                break;
            }
        }
       
        if (!exists) {
            configs.add(config);
        }
       
        saveAllConfigurations(configs);
    }

    /**
     * Loads all saved API configurations from persistent storage.
     *
     * @return list of all saved API configurations
     */
    public List<ApiConfiguration> loadAllConfigurations() {
        List<ApiConfiguration> configs = new ArrayList<>();
       
        File file = new File(CONFIG_FILE);
        if (!file.exists()) {
            return configs;
        }
       
        try (ObjectInputStream ois = new ObjectInputStream(new FileInputStream(file))) {
            Object obj = ois.readObject();
            if (obj instanceof List<?>) {
                for (Object item : (List<?>) obj) {
                    if (item instanceof ApiConfiguration) {
                        configs.add((ApiConfiguration) item);
                    }
                }
            }
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Failed to load configurations", e);
        }
       
        return configs;
    }

    /**
     * Deletes an API configuration by name.
     *
     * @param name the name of the configuration to delete
     */
    public void deleteConfiguration(String name) {
        List<ApiConfiguration> configs = loadAllConfigurations();
        configs.removeIf(config -> config.getName().equals(name));
        saveAllConfigurations(configs);
    }

    /**
     * Saves all configurations to the file system.
     *
     * @param configs the list of configurations to save
     */
    private void saveAllConfigurations(List<ApiConfiguration> configs) {
        try (ObjectOutputStream oos = new ObjectOutputStream(new FileOutputStream(CONFIG_FILE))) {
            oos.writeObject(configs);
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Failed to save configurations", e);
        }
    }
}