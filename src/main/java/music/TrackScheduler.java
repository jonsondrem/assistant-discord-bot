package music;

import com.sedmelluq.discord.lavaplayer.player.AudioPlayer;
import com.sedmelluq.discord.lavaplayer.player.event.AudioEventAdapter;
import com.sedmelluq.discord.lavaplayer.track.AudioTrack;
import com.sedmelluq.discord.lavaplayer.track.AudioTrackEndReason;
import net.dv8tion.jda.api.entities.Activity;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.TextChannel;
import net.dv8tion.jda.api.managers.AudioManager;

import java.util.concurrent.*;

/**
 * This class schedules tracks for the audio player. It contains the queue of tracks.
 */
public class TrackScheduler extends AudioEventAdapter {
    private final AudioPlayer player;
    private final BlockingQueue<AudioTrack> queue;
    private boolean onRepeat;
    private AudioTrack currentTrack;

    /**
     * @param player The audio player this scheduler uses
     */
    public TrackScheduler(AudioPlayer player) {
        this.player = player;
        this.queue = new LinkedBlockingQueue<>();
        this.onRepeat = false;
    }

    /**
     * Add the next track to queue or play right away if nothing is in the queue.
     *
     * @param track The track to play or add to queue.
     */
    public void queue(AudioTrack track) {
        // Calling startTrack with the noInterrupt set to true will start the track only if nothing is currently playing. If
        // something is playing, it returns false and does nothing. In that case the player was already playing so this
        // track goes to the queue instead.
        if (!player.startTrack(track, true)) {
            queue.offer(track);
        }
    }

    public void nextTrack() {
        nextTrack(false);
    }

    /**
     * Start the next track, stopping the current one if it is playing.
     */
    public void nextTrack(boolean skip) {
        // Start the next track, regardless of if something is already playing or not. In case queue was empty, we are
        // giving null to startTrack, which is a valid argument and will simply stop the player.
        if (this.onRepeat && !skip) {
            player.startTrack(this.currentTrack.makeClone(), false);
            return;
        }

        if (skip) {
            this.onRepeat = false;
        }

        player.startTrack(queue.poll(), false);
    }

    @Override
    public void onTrackStart(AudioPlayer player, AudioTrack track) {
        this.currentTrack = track;
        TextChannel channel = TrackInfo.getTrackInfo(track).getTextChannel();
        channel.sendMessage(":arrow_forward: **Now Playing: **" + track.getInfo().title + " :notes:").queue();
        Activity activity = Activity.playing(track.getInfo().title);
        channel.getJDA().getPresence().setActivity(activity);
    }

    @Override
    public void onTrackEnd(AudioPlayer player, AudioTrack track, AudioTrackEndReason endReason) {
        TextChannel channel = TrackInfo.getTrackInfo(track).getTextChannel();
        channel.getJDA().getPresence().setActivity(null);

        //When nothing is playing, schedule a disconnect from voice-channel
        if (queue.isEmpty()) {
            Executors.newSingleThreadScheduledExecutor().schedule(() -> {
                //Only disconnect if nothing has been played during the time.
                if (player.getPlayingTrack() == null && track == currentTrack) {
                    PlayerManager manager = PlayerManager.getInstance();
                    Guild guild = channel.getGuild();
                    manager.getGuildMusicManager(guild).player.startTrack(null, false);

                    AudioManager audioManager = guild.getAudioManager();
                    audioManager.closeAudioConnection();
                }
            }, 5, TimeUnit.MINUTES);
        }

        // Only start the next track if the end reason is suitable for it (FINISHED or LOAD_FAILED)
        if (endReason.mayStartNext) {
            nextTrack();
        }
    }

    public void setRepeat(boolean onRepeat) {
        this.onRepeat = onRepeat;
    }

    public boolean isOnRepeat() {
        return this.onRepeat;
    }

    public AudioPlayer getPlayer() {
        return this.player;
    }

    public void clearQueue() {
        queue.clear();
    }
}