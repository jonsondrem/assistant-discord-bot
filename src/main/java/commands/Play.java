package commands;

import api.SpotifyModel;
import api.YouTubeScraper;
import api.wrapper.SpotifyPlaylistContent;
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

        if (command[1].contains("spotify:track:") || command[1].contains("https://open.spotify.com/track/")) {
            String songId;
            if (command[1].contains("spotify:track:")) {
                songId = command[1].substring(14);
            } else {
                songId = command[1].substring(31).split("\\?")[0];
            }
            YouTubeScraper.getInstance().searchYouTubeAndPlay(SpotifyModel.getInstance().getSpotifySong(songId),
                    event.getTextChannel(), event.getMember());
        } else if (command[1].contains("spotify:playlist:") || command[1].contains("https://open.spotify.com/playlist/")) {
            String listId;
            if (command[1].contains("spotify:playlist:")) {
                listId = command[1].substring(17);
            } else {
                listId = command[1].substring(34).split("\\?")[0];
            }
            SpotifyPlaylistContent spotifyPlaylistContent = SpotifyModel.getInstance().getSpotifySongs(listId);
            event.getChannel().sendMessage(":white_check_mark: `" + actor + "` **added a Spotify playlist: **" +
                    spotifyPlaylistContent.getName() + " :musical_note:").queue();
            YouTubeScraper.getInstance().searchYouTubeAndPlay(spotifyPlaylistContent.getTracks(), event.getTextChannel(),
                    event.getMember());
        } else if (command[1].contains("https://")) {
            PlayerManager manager = PlayerManager.getInstance();
            manager.loadAndPlay(event.getTextChannel(), command[1], event.getMember());
        } else {
            YouTubeScraper.getInstance().searchYouTubeAndPlay(command[1], event.getTextChannel(), event.getMember());
        }
    }
}
