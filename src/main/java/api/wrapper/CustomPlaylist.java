package api.wrapper;

import com.google.gson.JsonObject;
import com.wrapper.spotify.model_objects.AbstractModelObject;
import com.wrapper.spotify.model_objects.specification.*;

public class CustomPlaylist extends AbstractModelObject {
    private final String name;
    private final Paging<CustomPlaylistTrack> tracks;

    private CustomPlaylist(final Builder builder) {
        super(builder);

        this.name = builder.name;
        this.tracks = builder.tracks;
    }

    public String getName() {
        return name;
    }

    public Paging<CustomPlaylistTrack> getTracks() {
        return tracks;
    }

    @Override
    public Builder builder() {
        return new Builder();
    }

    public static final class Builder extends AbstractModelObject.Builder {
        private String name;
        private Paging<CustomPlaylistTrack> tracks;

        public Builder setName(String name) {
            this.name = name;
            return this;
        }

        public Builder setTracks(Paging<CustomPlaylistTrack> tracks) {
            this.tracks = tracks;
            return this;
        }

        @Override
        public CustomPlaylist build() {
            return new CustomPlaylist(this);
        }
    }

    public static final class JsonUtil extends AbstractModelObject.JsonUtil<CustomPlaylist> {
        public CustomPlaylist createModelObject(JsonObject jsonObject) {
            if (jsonObject == null || jsonObject.isJsonNull()) {
                return null;
            }

            return new CustomPlaylist.Builder()
                    .setName(
                            hasAndNotNull(jsonObject, "name")
                                    ? jsonObject.get("name").getAsString()
                                    : null)
                    .setTracks(
                            hasAndNotNull(jsonObject, "tracks")
                                    ? new CustomPlaylistTrack.JsonUtil().createModelObjectPaging(
                                    jsonObject.getAsJsonObject("tracks"))
                                    : null)
                    .build();
        }
    }
}
