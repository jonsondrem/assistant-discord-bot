package api.wrapper;

import java.util.List;

public class SpotifyPlaylistContent {
    private String name;
    private List<String> tracks;

    public SpotifyPlaylistContent(String name, List<String> tracks) {
        this.name = name;
        this.tracks = tracks;
    }

    public String getName() {
        return name;
    }

    public List<String> getTracks() {
        return tracks;
    }
}