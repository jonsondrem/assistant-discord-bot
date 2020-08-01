package music;

import com.sedmelluq.discord.lavaplayer.track.AudioTrack;

import java.util.HashMap;
import java.util.Map;

public class TrackInfo {
    private static final Map<TrackInfo, AudioTrack> MAP_INSTANCE = new HashMap<>();
    private static long ID = 0;
    private long trackInitiatorId;
    private long trackInfoId;

    private TrackInfo(long trackInitiatorId) {
        this.trackInitiatorId = trackInitiatorId;
        this.trackInfoId = ID;
        ID++;
    }

    public long getTrackInitiatorId() {
        return this.trackInitiatorId;
    }

    public long getTrackInfoId() {
        return this.trackInfoId;
    }

    public static synchronized TrackInfo getTrackInfo() {
        MAP_INSTANCE.
    }
}
