package api.wrapper;

import java.util.List;

public class PlaylistContent {
    private String name;
    private List<String> tracks;

    public PlaylistContent(String name, List<String> tracks) {
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