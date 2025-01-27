package me.oldboy.config.util;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

/**
 * Класс предназначенный для чтения "application.properties" файла.
 *
 * Singleton utility class for reading properties from the "application.properties" file.
 */
public class PropertiesUtil {
    private static final Properties PROPERTIES = new Properties();

    static {
        loadProperties();
    }

    private PropertiesUtil() {
    }

    public static String get(String key) {
        return PROPERTIES.getProperty(key);
    }

    private static void loadProperties() {
        try (InputStream inputStream =
                     PropertiesUtil.class.getClassLoader().getResourceAsStream("application.properties"))
        {
            PROPERTIES.load(inputStream);
        } catch (FileNotFoundException e) {
            throw new RuntimeException("Property file not found!");
        } catch (IOException e) {
            throw new RuntimeException("Error reading configuration file: " + e.getMessage());
        }
    }
}
