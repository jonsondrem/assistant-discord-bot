package api;

import music.PlayerManager;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.TextChannel;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeOptions;

import java.nio.file.Paths;
import java.util.List;

public class YouTubeScraper {
    public static void searchYouTubeAndPlay(String query, TextChannel channel, Member member) {
        System.setProperty("webdriver.chrome.driver", Paths.get("src/main/java/api/chromedriver.exe").toString());
        ChromeOptions options = new ChromeOptions();
        options.addArguments("--headless", "--disable-gpu");
        WebDriver webDriver = new ChromeDriver(options);

        query = query.replace("+", "%2");
        query = query.replace(" ", "+");

        try {
            webDriver.get("https://www.youtube.com/results?search_query=" + query);
            List<WebElement> list = webDriver.findElements(By.id("video-title"));
            String url = list.get(0).getAttribute("href");
            if (url == null) {
                url = list.get(1).getAttribute("href");
            }
            PlayerManager manager = PlayerManager.getInstance();
            manager.loadAndPlay(channel, url, member);
        } finally {
            webDriver.quit();
        }
    }

    public static void searchYouTubeAndPlay(List<String> query, TextChannel channel, Member member) {
        System.setProperty("webdriver.chrome.driver", Paths.get("src/main/java/api/chromedriver.exe").toString());
        ChromeOptions options = new ChromeOptions();
        options.addArguments("--headless", "--disable-gpu");
        WebDriver webDriver = new ChromeDriver(options);

        PlayerManager manager = PlayerManager.getInstance();

        try {
            for (String s : query) {
                s = s.replace("+", "%2");
                s = s.replace(" ", "+");
                webDriver.get("https://www.youtube.com/results?search_query=" + s);
                List<WebElement> list = webDriver.findElements(By.id("video-title"));
                String url = list.get(0).getAttribute("href");
                if (url == null) {
                    url = list.get(1).getAttribute("href");
                }
                manager.loadAndPlayNoLoadInfo(channel, url, member);
            }
        } finally {
            webDriver.close();
            webDriver.quit();
        }
    }
}