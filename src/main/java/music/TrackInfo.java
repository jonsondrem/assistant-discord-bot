package music;

import com.sedmelluq.discord.lavaplayer.track.AudioTrack;

import java.util.HashMap;
import java.util.Map;

public class TrackInfo {
    private static final Map<AudioTrack, Long> MAP_INSTANCE = new HashMap<>();

    public static synchronized Long getTrackStarter(AudioTrack audioTrack) {
        return MAP_INSTANCE.get(audioTrack);
    }

    public static synchronized void addTrackStarter(AudioTrack audioTrack, Long id) {
        MAP_INSTANCE.put(audioTrack, id);
    }
}