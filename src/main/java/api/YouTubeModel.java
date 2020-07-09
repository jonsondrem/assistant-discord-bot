package api;

import com.google.api.client.http.javanet.NetHttpTransport;
import com.google.api.client.json.jackson2.JacksonFactory;
import com.google.api.services.youtube.YouTube;
import com.google.api.services.youtube.model.ResourceId;
import com.google.api.services.youtube.model.SearchListResponse;
import com.google.api.services.youtube.model.SearchResult;
import utilities.PropertiesLoader;

import java.io.IOException;
import java.util.List;

public class YouTubeModel {
    private static YouTubeModel INSTANCE;
    private static YouTube.Search.List yTSearch;

    private YouTubeModel() {
        try {
            YouTube youtube = new YouTube.Builder(new NetHttpTransport(), new JacksonFactory(),
                    request -> {
                    }).setApplicationName("youtube-search").build();

            yTSearch = youtube.search().list("id,snippet");

            String apiKey = PropertiesLoader.loadProperties().getProperty("youtube-token");
            yTSearch.setKey(apiKey);
            yTSearch.setType("video");
            yTSearch.setFields("items(id)");
            yTSearch.setMaxResults((long) 1);
            yTSearch.setOrder("relevance");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public String searchYouTube(String search) {
        String url = "";
        yTSearch.setQ(search);
        try {
            SearchListResponse searchResponse = yTSearch.execute();
            List<SearchResult> list = searchResponse.getItems();

            for (SearchResult video : list) {
                ResourceId rId = video.getId();
                url = "https://www.youtube.com/watch?v=" + rId.getVideoId();
            }
        } catch (IOException ignored) {
        }

        return url;
    }

    public static synchronized YouTubeModel getInstance() {
        if (INSTANCE == null) {
            INSTANCE = new YouTubeModel();
        }

        return INSTANCE;
    }
}
