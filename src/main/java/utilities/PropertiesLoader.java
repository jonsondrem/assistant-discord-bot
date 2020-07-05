package utilities;

import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Properties;

public class PropertiesLoader {
    public static Properties loadProperties() {
        Properties properties = new Properties();

        try {
            FileReader fileReader = new FileReader("config.properties");
            properties.load(fileReader);
        } catch (FileNotFoundException e) {
            properties.setProperty("token", "");
            properties.setProperty("youtube-key", "");
            try {
                FileWriter fileWriter = new FileWriter("config.properties");
                properties.store(fileWriter, "Config for discord bot.");
            }
            catch (IOException s) {
                s.printStackTrace();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }

        return properties;
    }

    public static void updateProperties() {
        Properties properties = new Properties();

        try {
            FileReader fileReader = new FileReader("config.properties");
            properties.load(fileReader);
            if (properties.getProperty("token") == null) {
                properties.setProperty("token", "");
            }
            if (properties.getProperty("youtube-key") == null) {
                properties.setProperty("youtube-key", "");
            }
            try {
                FileWriter fileWriter = new FileWriter("config.properties");
                properties.store(fileWriter, "Config for discord bot.");
            }
            catch (IOException s) {
                s.printStackTrace();
            }
        } catch (FileNotFoundException e) {
            properties.setProperty("token", "");
            properties.setProperty("youtube-key", "");
            try {
                FileWriter fileWriter = new FileWriter("config.properties");
                properties.store(fileWriter, "Config for discord bot.");
            }
            catch (IOException s) {
                s.printStackTrace();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
