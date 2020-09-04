package music;

import com.sedmelluq.discord.lavaplayer.player.AudioLoadResultHandler;
import com.sedmelluq.discord.lavaplayer.player.AudioPlayerManager;
import com.sedmelluq.discord.lavaplayer.player.DefaultAudioPlayerManager;
import com.sedmelluq.discord.lavaplayer.source.AudioSourceManagers;
import com.sedmelluq.discord.lavaplayer.tools.FriendlyException;
import com.sedmelluq.discord.lavaplayer.track.AudioPlaylist;
import com.sedmelluq.discord.lavaplayer.track.AudioTrack;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.TextChannel;

import java.util.HashMap;
import java.util.Map;

public class PlayerManager {
    private static PlayerManager INSTANCE;
    private final AudioPlayerManager playerManager;
    private final Map<Long, GuildMusicManager> musicManagers;

    private PlayerManager() {
        this.musicManagers = new HashMap<>();
        this.playerManager = new DefaultAudioPlayerManager();
        AudioSourceManagers.registerRemoteSources(playerManager);
        AudioSourceManagers.registerLocalSource(playerManager);
    }

    public synchronized GuildMusicManager getGuildMusicManager(Guild guild) {
        long guildId = guild.getIdLong();
        GuildMusicManager musicManager = musicManagers.get(guildId);

        if (musicManager == null) {
            musicManager = new GuildMusicManager(playerManager);
            musicManagers.put(guildId, musicManager);
        }

        guild.getAudioManager().setSendingHandler(musicManager.getSendHandler());

        return musicManager;
    }

    public void loadAndPlay(TextChannel channel, String trackUrl, Member actor) {
        GuildMusicManager musicManager = getGuildMusicManager(channel.getGuild());
        long actorId = actor.getIdLong();
        String actorName = actor.getEffectiveName();

        playerManager.loadItemOrdered(musicManager, trackUrl, new AudioLoadResultHandler() {
            @Override
            public void trackLoaded(AudioTrack track) {
                channel.sendMessage(":white_check_mark: `" + actorName + "` **added to queue: **" +
                        track.getInfo().title + " :musical_note:").queue();

                TrackInfo.addTrackInfo(track, actorId, channel);

                play(musicManager, track);
            }

            @Override
            public void playlistLoaded(AudioPlaylist playlist) {
                channel.sendMessage(":white_check_mark: `" + actorName + "` **added a playlist: **" +
                        playlist.getName() + " :musical_note:").queue();

                for (AudioTrack track : playlist.getTracks()) {
                    TrackInfo.addTrackInfo(track, actorId, channel);
                    play(musicManager, track);
                }
            }

            @Override
            public void noMatches() {
                channel.sendMessage(":x: **Nothing found with: **" + trackUrl)
                        .queue();
            }

            @Override
            public void loadFailed(FriendlyException exception) {
                channel.sendMessage(":x: **Could not play song** " + trackUrl + "**: **" + exception.getMessage())
                        .queue();
            }
        });
    }

    public void loadAndPlayNoLoadInfo(TextChannel channel, String trackUrl, Member actor) {
        GuildMusicManager musicManager = getGuildMusicManager(channel.getGuild());
        long actorId = actor.getIdLong();

        playerManager.loadItemOrdered(musicManager, trackUrl, new AudioLoadResultHandler() {
            @Override
            public void trackLoaded(AudioTrack track) {
                TrackInfo.addTrackInfo(track, actorId, channel);

                play(musicManager, track);
            }

            @Override
            public void playlistLoaded(AudioPlaylist playlist) {}

            @Override
            public void noMatches() {
                channel.sendMessage(":x: **Nothing found with: **" + trackUrl)
                        .queue();
            }

            @Override
            public void loadFailed(FriendlyException exception) {
                channel.sendMessage(":x: **Could not play song** " + trackUrl + "**: **" + exception.getMessage())
                        .queue();
            }
        });
    }

    private void play(GuildMusicManager musicManager, AudioTrack track) {
        musicManager.scheduler.queue(track);
    }

    public static synchronized PlayerManager getInstance() {
        if (INSTANCE == null) {
            INSTANCE = new PlayerManager();
        }

        return INSTANCE;
    }
}
