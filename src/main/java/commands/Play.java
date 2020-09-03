package commands;

import api.SpotifyModel;
import api.wrapper.SpotifyPlaylistContent;
import api.YouTubeModel;
import music.PlayerManager;
import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.entities.VoiceChannel;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import net.dv8tion.jda.api.managers.AudioManager;

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
            event.getChannel().sendMessage(":x: **You are not connected to a voice channel!** `" + actor + "`")
                    .queue();
            return;
        }

        if (command.length < 2) {
            event.getChannel().sendMessage(":x: **You need to put an url or search word(s) " +
                    "after the command!** `" + actor + "`").queue();
            return;
        }

        AudioManager audioManager = event.getGuild().getAudioManager();
        VoiceChannel botChannel = event.getGuild().getSelfMember().getVoiceState().getChannel();
        if (botChannel != connectedChannel && !event.getMember().getPermissions().contains(Permission.ADMINISTRATOR) &&
                botChannel != null) {
            event.getChannel().sendMessage(":exclamation: **Sorry, but I'm in a different voice channel.** `" +
                    actor + "`").queue();
            return;
        } else {
            audioManager.openAudioConnection(connectedChannel);
        }

        PlayerManager manager = PlayerManager.getInstance();
        if (command[1].contains("spotify:track:")) {
            String songId = command[1].substring(14);
            String trackUrl = YouTubeModel.getInstance().searchYouTube(SpotifyModel.getInstance().getSpotifySong(songId));
            manager.loadAndPlay(event.getTextChannel(), trackUrl,
                    event.getMember());
        } else if (command[1].contains("https://open.spotify.com/track/")) {
            String songId = command[1].substring(31).split("\\?")[0];
            String trackUrl = YouTubeModel.getInstance().searchYouTube(SpotifyModel.getInstance().getSpotifySong(songId));
            manager.loadAndPlay(event.getTextChannel(), trackUrl,
                    event.getMember());
        } else if (command[1].contains("spotify:playlist:")) {
            String listId = command[1].substring(17);
            SpotifyPlaylistContent spotifyPlaylistContent = SpotifyModel.getInstance().getSpotifySongs(listId);
            event.getChannel().sendMessage(":white_check_mark: `" + actor + "` **added a Spotify playlist: **" +
                    spotifyPlaylistContent.getName() + " :musical_note:").queue();
            for (String songName : spotifyPlaylistContent.getTracks()) {
                String trackUrl = YouTubeModel.getInstance().searchYouTube(songName);
                manager.loadAndPlayNoLoadInfo(event.getTextChannel(), trackUrl, event.getMember());
            }
        } else if (command[1].contains("https://open.spotify.com/playlist/")) {
            String listId = command[1].substring(34).split("\\?")[0];
            SpotifyPlaylistContent spotifyPlaylistContent = SpotifyModel.getInstance().getSpotifySongs(listId);
            event.getChannel().sendMessage(":white_check_mark: `" + actor + "` **added a Spotify playlist: **" +
                    spotifyPlaylistContent.getName() + " :musical_note:").queue();
            for (String songName : spotifyPlaylistContent.getTracks()) {
                String trackUrl = YouTubeModel.getInstance().searchYouTube(songName);
                manager.loadAndPlayNoLoadInfo(event.getTextChannel(), trackUrl, event.getMember());
            }
        } else if (command[1].contains("https://")) {
            manager.loadAndPlay(event.getTextChannel(), command[1], event.getMember());
        } else {
            String trackUrl = YouTubeModel.getInstance().searchYouTube(command[1]);
            manager.loadAndPlay(event.getTextChannel(), trackUrl, event.getMember());
        }
    }
}
