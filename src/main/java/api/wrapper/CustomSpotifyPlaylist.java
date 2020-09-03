package api.wrapper;

import com.google.gson.JsonObject;
import com.wrapper.spotify.model_objects.AbstractModelObject;
import com.wrapper.spotify.model_objects.specification.*;

public class CustomSpotifyPlaylist extends AbstractModelObject {
    private final String name;
    private final Paging<CustomSpotifyPlaylistTrack> tracks;

    private CustomSpotifyPlaylist(final Builder builder) {
        super(builder);

        this.name = builder.name;
        this.tracks = builder.tracks;
    }

    public String getName() {
        return name;
    }

    public Paging<CustomSpotifyPlaylistTrack> getTracks() {
        return tracks;
    }

    @Override
    public Builder builder() {
        return new Builder();
    }

    public static final class Builder extends AbstractModelObject.Builder {
        private String name;
        private Paging<CustomSpotifyPlaylistTrack> tracks;

        public Builder setName(String name) {
            this.name = name;
            return this;
        }

        public Builder setTracks(Paging<CustomSpotifyPlaylistTrack> tracks) {
            this.tracks = tracks;
            return this;
        }

        @Override
        public CustomSpotifyPlaylist build() {
            return new CustomSpotifyPlaylist(this);
        }
    }

    public static final class JsonUtil extends AbstractModelObject.JsonUtil<CustomSpotifyPlaylist> {
        public CustomSpotifyPlaylist createModelObject(JsonObject jsonObject) {
            if (jsonObject == null || jsonObject.isJsonNull()) {
                return null;
            }

            return new CustomSpotifyPlaylist.Builder()
                    .setName(
                            hasAndNotNull(jsonObject, "name")
                                    ? jsonObject.get("name").getAsString()
                                    : null)
                    .setTracks(
                            hasAndNotNull(jsonObject, "tracks")
                                    ? new CustomSpotifyPlaylistTrack.JsonUtil().createModelObjectPaging(
                                    jsonObject.getAsJsonObject("tracks"))
                                    : null)
                    .build();
        }
    }
}
