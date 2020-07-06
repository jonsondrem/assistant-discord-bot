package commands;

import com.google.api.client.http.javanet.NetHttpTransport;
import com.google.api.client.json.jackson2.JacksonFactory;
import com.google.api.services.youtube.model.ResourceId;
import com.google.api.services.youtube.model.SearchListResponse;
import com.google.api.services.youtube.model.SearchResult;
import music.PlayerManager;
import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.entities.VoiceChannel;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import net.dv8tion.jda.api.managers.AudioManager;
import com.google.api.services.youtube.YouTube;
import utilities.PropertiesLoader;

import java.io.IOException;
import java.util.List;

public class Play extends Command {

    public Play() {
        super("play");
    }

    @Override
    @SuppressWarnings("ConstantConditions")
    public void run(MessageReceivedEvent event) {
        String[] command = event.getMessage().getContentRaw().split(" ", 2);
        VoiceChannel connectedChannel = event.getMember().getVoiceState().getChannel();
        String actor = event.getMember().getEffectiveName();
        if (connectedChannel == null) {
            event.getChannel().sendMessage("You are not connected to a voice channel. `" + actor + "`").queue();
            return;
        }

        if (command.length < 2) {
            event.getChannel().sendMessage("You need to put an url or search word(s) after the command. `" + actor + "`").queue();
            return;
        }

        AudioManager audioManager = event.getGuild().getAudioManager();
        VoiceChannel botChannel = event.getGuild().getSelfMember().getVoiceState().getChannel();
        if (botChannel != connectedChannel && !event.getMember().getPermissions().contains(Permission.ADMINISTRATOR) &&
                botChannel != null) {
            event.getChannel().sendMessage("I'm in a different voice channel. `" + actor + "`").queue();
            return;
        } else {
            audioManager.openAudioConnection(connectedChannel);
        }

        PlayerManager manager = PlayerManager.getInstance();
        if (command[1].contains("https://")) {
            manager.loadAndPlay(event.getTextChannel(), command[1], event.getMember());
        } else {
            manager.loadAndPlay(event.getTextChannel(), searchYouTube(command[1]), event.getMember());
        }
    }

    private String searchYouTube(String search) {
        String url = "";
        try {
            YouTube youtube = new YouTube.Builder(new NetHttpTransport(), new JacksonFactory(),
                    request -> {
                    }).setApplicationName("youtube-search").build();

            YouTube.Search.List yTSearch = youtube.search().list("id,snippet");

            String apiKey = PropertiesLoader.loadProperties().getProperty("youtube-key");
            yTSearch.setKey(apiKey);
            yTSearch.setQ(search);
            yTSearch.setType("video");
            yTSearch.setFields("items(id)");
            yTSearch.setMaxResults((long) 1);
            yTSearch.setOrder("viewCount");

            SearchListResponse searchResponse = yTSearch.execute();
            List<SearchResult> list = searchResponse.getItems();

            for (SearchResult video : list) {
                ResourceId rId = video.getId();
                url = "https://www.youtube.com/watch?v=" + rId.getVideoId();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return url;
    }
}
