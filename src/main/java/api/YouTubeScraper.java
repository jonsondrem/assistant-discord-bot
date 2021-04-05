package api;

import music.PlayerManager;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.TextChannel;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeOptions;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;

import java.nio.file.Paths;
import java.util.List;

public class YouTubeScraper {
    private static YouTubeScraper INSTANCE;
    private final WebDriver webDriver;

    private YouTubeScraper() {
        System.setProperty("webdriver.chrome.driver", Paths.get("src/main/java/api/chromedriver.exe").toString());
        ChromeOptions options = new ChromeOptions();
        options.addArguments("--headless", "--disable-gpu");
        this.webDriver = new ChromeDriver(options);

        webDriver.get("https://www.youtube.com");
        WebDriverWait wait = new WebDriverWait(webDriver, 20);
        wait.until(ExpectedConditions.elementToBeClickable(By.tagName("button"))).click();
    }

    public void searchYouTubeAndPlay(String query, TextChannel channel, Member member) {
        PlayerManager manager = PlayerManager.getInstance();
        String search = query;
        query = query.replace("+", "%2");
        query = query.replace(" ", "+");

        webDriver.get("https://www.youtube.com/results?search_query=" + query);
        List<WebElement> list = webDriver.findElements(By.id("video-title"));

        boolean found = false;
        for (int i = 0; i < 10; i++) {
            String url = list.get(i).getAttribute("href");
            if (url != null) {
                manager.loadAndPlay(channel, url, member);
                found = true;
                i = 10;
            }
        }

        if (!found) {
            channel.sendMessage(":x: **Could not find song: **" + search).queue();
        }
    }

    public void searchYouTubeAndPlay(List<String> query, TextChannel channel, Member member, String trackListName) {
        PlayerManager manager = PlayerManager.getInstance();

        for (String s : query) {
            String search = s;
            s = s.replace("+", "%2");
            s = s.replace(" ", "+");
            webDriver.get("https://www.youtube.com/results?search_query=" + s);
            List<WebElement> list = webDriver.findElements(By.id("video-title"));

            boolean found = false;
            for (int i = 0; i < 10; i++) {
                String url = list.get(i).getAttribute("href");
                if (url != null) {
                    manager.loadAndPlayNoLoadInfo(channel, url, member);
                    found = true;
                    i = 10;
                }
            }

            if (!found) {
                channel.sendMessage(":x: **Could not find song: **" + search).queue();
            }
        }

        channel.sendMessage(":white_check_mark: **Done loading Spotify Playlist:** "
                + trackListName + " :musical_note:").queue();
    }

    public void turnOffWebDriver() {
        webDriver.quit();
    }

    public static synchronized void build() {
        if (INSTANCE == null) {
            INSTANCE = new YouTubeScraper();
        }
    }

    public static synchronized YouTubeScraper getInstance() {
        if (INSTANCE == null) {
            INSTANCE = new YouTubeScraper();
        }

        return INSTANCE;
    }
}