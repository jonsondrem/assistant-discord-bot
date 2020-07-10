package api;

import com.wrapper.spotify.SpotifyApi;
import com.wrapper.spotify.exceptions.SpotifyWebApiException;
import com.wrapper.spotify.model_objects.credentials.ClientCredentials;
import com.wrapper.spotify.model_objects.specification.ArtistSimplified;
import com.wrapper.spotify.model_objects.specification.Track;
import com.wrapper.spotify.requests.authorization.client_credentials.ClientCredentialsRequest;
import org.apache.hc.core5.http.ParseException;
import utilities.PropertiesLoader;

import java.io.IOException;
import java.util.concurrent.CancellationException;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionException;

public class SpotifyModel {
    private static SpotifyModel INSTANCE;
    private static SpotifyApi spotifyApi;

    private SpotifyModel() {
        spotifyApi = new SpotifyApi.Builder()
                .setClientId(PropertiesLoader.loadProperties().getProperty("spotify-id"))
                .setClientSecret(PropertiesLoader.loadProperties().getProperty("spotify-secret"))
                .build();

        ClientCredentialsRequest clientCredentialsRequest = spotifyApi.clientCredentials().build();

        try {
            ClientCredentials clientCredentials = clientCredentialsRequest.execute();
            spotifyApi.setAccessToken(clientCredentials.getAccessToken());
        } catch (ParseException | SpotifyWebApiException | IOException e) {
            e.printStackTrace();
        }
    }

    public String getSpotifySong(String songId) {
        String songName = "";
        String artist = "";

        try {
            CompletableFuture<Track> trackFuture = spotifyApi.getTrack(songId).build().executeAsync();
            Track track = trackFuture.join();
            songName = track.getName();
            ArtistSimplified[] artistList = track.getArtists();
            artist = artistList[0].getName();
        } catch (CompletionException e) {
            try {
                ClientCredentialsRequest clientCredentialsRequest = spotifyApi.clientCredentials().build();
                ClientCredentials clientCredentials = clientCredentialsRequest.execute();
                spotifyApi.setAccessToken(clientCredentials.getAccessToken());
                return getSpotifySong(songId);
            } catch (ParseException | SpotifyWebApiException | IOException err) {
                e.printStackTrace();
            }
        } catch (CancellationException e) {
            e.printStackTrace();
        }

        return songName + " " + artist;
    }

    public static synchronized SpotifyModel getInstance() {
        if (INSTANCE == null) {
            INSTANCE = new SpotifyModel();
        }

        return INSTANCE;
    }
}
