package com.apu.EmailLoaderSimple.storage;

import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Properties;

public class SettingsStorage {

    private static final String PROPERTIES_FILE_PATH = "email.properties";
    
    public static Properties loadPropertiesFromFile() throws IOException {
        Properties properties = new Properties();
        FileInputStream in;
        in = new FileInputStream(PROPERTIES_FILE_PATH);
        properties.load(in);
        in.close();    
        return properties;
    }
    
    public static void savePropertiesToFile(Properties properties) throws IOException {
        FileOutputStream propFile = new FileOutputStream(PROPERTIES_FILE_PATH);
        properties.store(propFile, null);
        propFile.close();
    }
    
}
