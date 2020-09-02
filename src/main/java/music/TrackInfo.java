package music;

import com.sedmelluq.discord.lavaplayer.track.AudioTrack;
import net.dv8tion.jda.api.entities.TextChannel;

import java.util.HashMap;
import java.util.Map;

public class TrackInfo {
    private static final Map<AudioTrack, TrackInfo> MAP_INSTANCE = new HashMap<>();
    private long initiatorId;
    private TextChannel textChannel;

    private TrackInfo(long initiatorId, TextChannel textChannel) {
        this.initiatorId = initiatorId;
        this.textChannel = textChannel;
    }

    public long getInitiatorId() {
        return this.initiatorId;
    }

    public TextChannel getTextChannel() {
        return this.textChannel;
    }

    public static synchronized TrackInfo getTrackInfo(AudioTrack audioTrack) {
        return MAP_INSTANCE.get(audioTrack);
    }

    public static synchronized void addTrackInfo(AudioTrack audioTrack, Long id, TextChannel channel) {
        TrackInfo trackInfo = new TrackInfo(id, channel);
        MAP_INSTANCE.put(audioTrack, trackInfo);
    }
}