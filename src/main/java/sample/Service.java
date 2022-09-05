package sample;

import java.io.FileInputStream;
import java.io.IOException;
import java.util.EnumMap;
import java.util.Properties;

public class Service {

    private static final String CONFIG_PROPERTIES = "config.properties";

    private final EnumMap<PropertyName, String> properties = new EnumMap<>(PropertyName.class);

    public Service() {
        readProperties();
    }

    public String getPropertyValue(PropertyName propertyName) {
        return properties.get(propertyName);
    }

    private void readProperties() {
        try (FileInputStream propsInput = new FileInputStream(CONFIG_PROPERTIES)){
            Properties prop = new Properties();
            prop.load(propsInput);
            properties.put(PropertyName.DB_URL, prop.getProperty(PropertyName.DB_URL.name()));
            properties.put(PropertyName.DB_USER, prop.getProperty(PropertyName.DB_USER.name()));
            properties.put(PropertyName.DB_PASSWORD, prop.getProperty(PropertyName.DB_PASSWORD.name()));
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}
