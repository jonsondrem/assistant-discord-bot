package api;

import api.wrapper.CustomSpotifyPlaylist;
import api.wrapper.CustomSpotifyPlaylistTrack;
import api.wrapper.SpotifyPlaylistContent;
import com.wrapper.spotify.SpotifyApi;
import com.wrapper.spotify.exceptions.SpotifyWebApiException;
import com.wrapper.spotify.model_objects.credentials.ClientCredentials;
import com.wrapper.spotify.model_objects.specification.*;
import com.wrapper.spotify.requests.authorization.client_credentials.ClientCredentialsRequest;
import com.wrapper.spotify.requests.data.playlists.GetPlaylistRequest;
import org.apache.hc.core5.http.ParseException;
import utilities.PropertiesLoader;

import java.io.IOException;
import java.util.ArrayList;

import static com.wrapper.spotify.SpotifyApi.*;

public class SpotifyModel {
    private static SpotifyModel INSTANCE;
    private static SpotifyApi spotifyApi;
    private static String accessToken;

    private SpotifyModel() {
        spotifyApi = new SpotifyApi.Builder()
                .setClientId(PropertiesLoader.loadProperties().getProperty("spotify-id"))
                .setClientSecret(PropertiesLoader.loadProperties().getProperty("spotify-secret"))
                .build();

        ClientCredentialsRequest clientCredentialsRequest = spotifyApi.clientCredentials().build();

        try {
            ClientCredentials clientCredentials = clientCredentialsRequest.execute();
            spotifyApi.setAccessToken(clientCredentials.getAccessToken());
            accessToken = clientCredentials.getAccessToken();
        } catch (ParseException | SpotifyWebApiException | IOException e) {
            e.printStackTrace();
        }
    }

    public String getSpotifySong(String songId) {
        String songName = "";
        String artist = "";

        try {
            Track track = spotifyApi.getTrack(songId).build().execute();
            songName = track.getName();
            ArtistSimplified[] artistList = track.getArtists();
            artist = artistList[0].getName();
        } catch (SpotifyWebApiException e) {
            try {
                ClientCredentialsRequest clientCredentialsRequest = spotifyApi.clientCredentials().build();
                ClientCredentials clientCredentials = clientCredentialsRequest.execute();
                spotifyApi.setAccessToken(clientCredentials.getAccessToken());
                accessToken = clientCredentials.getAccessToken();
                return getSpotifySong(songId);
            } catch (ParseException | SpotifyWebApiException | IOException err) {
                err.printStackTrace();
            }
        } catch (IOException | ParseException e) {
            e.printStackTrace();
        }

        return songName + " " + artist;
    }

    public SpotifyPlaylistContent getSpotifySongs(String playlistId) {
        String listName = "";
        ArrayList<String> songList = new ArrayList<>();

        try {
            CustomSpotifyPlaylist customSpotifyPlaylist = getPlaylist(playlistId);
            listName = customSpotifyPlaylist.getName();

            for (CustomSpotifyPlaylistTrack cTrack : customSpotifyPlaylist.getTracks().getItems()) {
                ArtistSimplified[] artistList = cTrack.getTrack().getArtists();
                songList.add(cTrack.getTrack().getName() + " " + artistList[0].getName());
            }
        } catch (SpotifyWebApiException e) {
            try {
                ClientCredentialsRequest clientCredentialsRequest = spotifyApi.clientCredentials().build();
                ClientCredentials clientCredentials = clientCredentialsRequest.execute();
                spotifyApi.setAccessToken(clientCredentials.getAccessToken());
                accessToken = clientCredentials.getAccessToken();
                return getSpotifySongs(playlistId);
            } catch (ParseException | SpotifyWebApiException | IOException err) {
                err.printStackTrace();
            }
        } catch (ParseException | IOException e) {
            e.printStackTrace();
        }

        return new SpotifyPlaylistContent(listName, songList);
    }

    private CustomSpotifyPlaylist getPlaylist(String playlist_id) throws IOException, SpotifyWebApiException, ParseException {
        //Get request
        GetPlaylistRequest request = new GetPlaylistRequest.Builder(accessToken)
                .setDefaults(DEFAULT_HTTP_MANAGER, DEFAULT_SCHEME, DEFAULT_HOST, DEFAULT_PORT)
                .playlist_id(playlist_id).build();

        //Return playlist
        return new CustomSpotifyPlaylist.JsonUtil().createModelObject(request.getJson());
    }

    public static synchronized void build() {
        if (INSTANCE == null) {
            INSTANCE = new SpotifyModel();
        }
    }

    public static synchronized SpotifyModel getInstance() {
        if (INSTANCE == null) {
            INSTANCE = new SpotifyModel();
        }

        return INSTANCE;
    }
}
