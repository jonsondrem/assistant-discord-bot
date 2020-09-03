package api.wrapper;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.google.gson.JsonObject;
import com.wrapper.spotify.model_objects.AbstractModelObject;
import com.wrapper.spotify.model_objects.specification.Track;

@JsonDeserialize(builder = CustomSpotifyPlaylistTrack.Builder.class)
public class CustomSpotifyPlaylistTrack extends AbstractModelObject {
    private final Track track;

    private CustomSpotifyPlaylistTrack(final Builder builder) {
        super(builder);

        this.track = builder.track;
    }

    public Track getTrack() {
        return track;
    }

    @Override
    public Builder builder() {
        return new Builder();
    }

    public static final class Builder extends AbstractModelObject.Builder {
        private Track track;

        public Builder setTrack(Track track) {
            this.track = track;
            return this;
        }

        @Override
        public CustomSpotifyPlaylistTrack build() {
            return new CustomSpotifyPlaylistTrack(this);
        }
    }

    public static final class JsonUtil extends AbstractModelObject.JsonUtil<CustomSpotifyPlaylistTrack> {
        public CustomSpotifyPlaylistTrack createModelObject(JsonObject jsonObject) {
            if (jsonObject == null || jsonObject.isJsonNull()) {
                return null;
            }

            Track track = null;

            if (hasAndNotNull(jsonObject, "track")) {
                final JsonObject trackObj = jsonObject.getAsJsonObject("track");

                if (hasAndNotNull(trackObj, "type")) {
                    String type = trackObj.get("type").getAsString().toLowerCase();

                    if (type.equals("track")) {
                        track = new Track.JsonUtil().createModelObject(trackObj);
                    }
                }
            }

            return new Builder()
                    .setTrack(track)
                    .build();
        }
    }
}