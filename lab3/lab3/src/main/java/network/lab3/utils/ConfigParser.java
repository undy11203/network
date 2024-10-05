package network.lab3.utils;

import java.io.InputStream;
import java.util.Properties;

public class ConfigParser {
    private static Properties properties = new Properties();

    static {
        try (InputStream input = ConfigParser.class.getClassLoader().getResourceAsStream("keys.properties")) {
            if (input != null) {
                properties.load(input);
            } else {
                throw new RuntimeException("Файл keys.properties не найден!");
            }
        } catch (Exception e) {
            throw new RuntimeException("Ошибка при загрузке файла keys.properties", e);
        }
    }

    public static String getValue(String key) {
        return properties.getProperty(key);
    }
}
