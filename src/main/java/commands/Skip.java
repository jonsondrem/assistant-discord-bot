package commands;

import com.sedmelluq.discord.lavaplayer.track.AudioTrack;
import music.PlayerManager;
import music.TrackInfo;
import music.TrackScheduler;
import net.dv8tion.jda.api.entities.MessageChannel;
import net.dv8tion.jda.api.entities.VoiceChannel;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import utilities.VoteHolder;

import java.util.HashMap;
import java.util.Map;

public class Skip extends Command {
    private Map<Long, VoteHolder> voteHolders;
    private AudioTrack currentTrack;

    public Skip() {
        super("skip");
        this.voteHolders = new HashMap<>();
    }

    @Override
    @SuppressWarnings("ConstantConditions")
    public void run(MessageReceivedEvent event) {
        VoiceChannel connectedChannel = event.getMember().getVoiceState().getChannel();
        String actor = event.getMember().getEffectiveName();
        MessageChannel messageChannel = event.getChannel();
        if (connectedChannel == null) {
            messageChannel.sendMessage("You are not connected to a voice channel. `" + actor + "`").queue();
            return;
        }

        VoiceChannel botChannel = event.getGuild().getSelfMember().getVoiceState().getChannel();
        if (botChannel != connectedChannel && !event.getMember().isOwner()) {
            messageChannel.sendMessage("You have to be in the same voice channel as me to the skip song. `"
                    + actor + "`")
                    .queue();
            return;
        }

        PlayerManager manager = PlayerManager.getInstance();
        TrackScheduler scheduler = manager.getGuildMusicManager(event.getGuild()).scheduler;

        if (scheduler.getPlayer().getPlayingTrack() == null) {
            messageChannel.sendMessage("I'm currently not playing a song.").queue();
            return;
        }

        /*if (event.getMember().isOwner()) {
            scheduler.nextTrack(true);
            event.getChannel().sendMessage("Owner of the server skipped the song.").queue();
            return;
        }*/

        long id = ((TrackInfo) scheduler.getPlayer().getPlayingTrack()).getTrackInitiatorId();
        if (event.getMember().getIdLong() == id) {
            event.getChannel().sendMessage("`" + actor + "` - " + "Initiator of the song `" +
                    scheduler.getPlayer().getPlayingTrack().getInfo().title +
                    "`, skipped this song.").queue();
            scheduler.nextTrack(true);
            return;
        }

        long guildId = event.getGuild().getIdLong();
        double winPercentage = 0.45;
        VoteHolder voteHolder = this.voteHolders.get(event.getGuild().getIdLong());

        if (voteHolder == null) {
            voteHolder = new VoteHolder();
            this.voteHolders.put(guildId, voteHolder);
        }

        voteHolder.modifyAttributes(connectedChannel.getMembers().size() - 1, winPercentage);

        if (scheduler.getPlayer().getPlayingTrack() != this.currentTrack) {
            voteHolder.resetCounter();
            this.currentTrack = scheduler.getPlayer().getPlayingTrack();
        }

        boolean voted = voteHolder.addVoter(event.getMember());
        int votes = voteHolder.getVotes();
        int voteCap = voteHolder.getVoteCap();
        if (!voted) {
            messageChannel.sendMessage("You have already voted to skip the song. `" +
                    actor + "`").queue();
        } else {
            messageChannel.sendMessage("`" + actor + "` has voted to skip. (" +
                    votes + "/" + this.calculateThreshold(voteCap, winPercentage)+ ")").queue();
        }

        boolean pass = voteHolder.runVoteCheck();
        if (pass) {
            scheduler.nextTrack(true);
            messageChannel.sendMessage("Enough votes. I am skipping the song.").queue();
        }
    }

    private int calculateThreshold(int voteCap, double winPercentage) {
        double threshold = (double) voteCap * winPercentage;
        return (int) Math.ceil(threshold);
    }
}
