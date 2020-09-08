package utilities;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Properties;

public class PropertiesLoader {
    private static Logger logger = LoggerFactory.getLogger(PropertiesLoader.class);

    public static Properties loadProperties() {
        Properties properties = new Properties();

        try {
            FileReader fileReader = new FileReader("config.properties");
            properties.load(fileReader);
        } catch (FileNotFoundException e) {
            updateProperties();
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
            if (properties.getProperty("discord-token") == null) {
                properties.setProperty("discord-token", "");
            }
            if (properties.getProperty("youtube-token") == null) {
                properties.setProperty("youtube-token", "");
            }
            if (properties.getProperty("spotify-id") == null) {
                properties.setProperty("spotify-id", "");
            }
            if (properties.getProperty("spotify-secret") == null) {
                properties.setProperty("spotify-secret", "");
            }
            if (properties.getProperty("discord-id") == null) {
                properties.setProperty("discord-id", "");
            }
            try {
                FileWriter fileWriter = new FileWriter("config.properties");
                properties.store(fileWriter, "Config for discord bot.");
            }
            catch (IOException s) {
                s.printStackTrace();
            }
        } catch (FileNotFoundException e) {
            properties.setProperty("discord-token", "");
            properties.setProperty("youtube-token", "");
            properties.setProperty("spotify-id", "");
            properties.setProperty("spotify-secret", "");
            properties.setProperty("discord-id", "");
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

    public static boolean checkProperties() {
        String token = PropertiesLoader.loadProperties().getProperty("discord-token");
        String youTubeKey = PropertiesLoader.loadProperties().getProperty("youtube-token");
        String spotifyId = PropertiesLoader.loadProperties().getProperty("spotify-id");
        String spotifySecret = PropertiesLoader.loadProperties().getProperty("spotify-secret");
        String id = PropertiesLoader.loadProperties().getProperty("discord-id");

        boolean validConfig = true;
        if (token.equals("")) {
            logger.info("Discord Bot Token is missing.");
            validConfig = false;
        }
        if (youTubeKey.equals("")) {
            logger.info("YouTube Token is missing.");
            validConfig = false;
        }
        if (spotifyId.equals("")) {
            logger.info("Spotify Client id is missing.");
            validConfig = false;
        }
        if (spotifySecret.equals("")) {
            logger.info("Spotify Client secret is missing.");
            validConfig = false;
        }
        if (id.equals("")) {
            logger.info("Discord ID is missing.");
        }
        try {
            Long.parseLong(id);
        } catch (NumberFormatException e) {
            logger.info("Discord ID has a invalid format.");
            validConfig = false;
        }
        if (!validConfig) {
            logger.info("Please completely fill in the config.properties");
        }
        return validConfig;
    }
}