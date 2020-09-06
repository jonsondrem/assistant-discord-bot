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
    private Map<Long, AudioTrack> currentTrack;

    public Skip() {
        super("skip");
        this.voteHolders = new HashMap<>();
        this.currentTrack = new HashMap<>();
        this.description = "Skips the current playing song.";
    }

    @Override
    @SuppressWarnings("ConstantConditions")
    public void run(MessageReceivedEvent event) {
        VoiceChannel connectedChannel = event.getMember().getVoiceState().getChannel();
        String actor = event.getMember().getEffectiveName();
        MessageChannel messageChannel = event.getChannel();
        if (connectedChannel == null) {
            messageChannel.sendMessage(":x: **You are not connected to a voice channel!** `" + actor + "`")
                    .queue();
            return;
        }

        VoiceChannel botChannel = event.getGuild().getSelfMember().getVoiceState().getChannel();
        if (botChannel != connectedChannel && !event.getMember().isOwner()) {
            messageChannel.sendMessage(":exclamation: **You have to be in the same voice channel as me to the" +
                    " skip song!** `" + actor + "`").queue();
            return;
        }

        PlayerManager manager = PlayerManager.getInstance();
        TrackScheduler scheduler = manager.getGuildMusicManager(event.getGuild()).scheduler;

        if (scheduler.getPlayer().getPlayingTrack() == null) {
            messageChannel.sendMessage(":x: I'm currently not playing a song! `" + actor + "`").queue();
            return;
        }

        if (event.getMember().isOwner()) {
            event.getChannel().sendMessage(":white_check_mark: **Owner of the server skipped the song.**" +
                    " :fast_forward:").queue();
            scheduler.nextTrack(true);
            return;
        }

        if (event.getMember().getIdLong() == TrackInfo.getTrackInfo(scheduler.getPlayer().getPlayingTrack())
                .getInitiatorId()) {
            event.getChannel().sendMessage(":white_check_mark: `" + actor + "`" +
                    " **skipped their song: **" + scheduler.getPlayer().getPlayingTrack().getInfo().title +
                    " :fast_forward:").queue();
            scheduler.nextTrack(true);
            return;
        }

        long guildId = event.getGuild().getIdLong();
        double winPercentage = 0.45;
        VoteHolder voteHolder = this.voteHolders.get(guildId);

        if (voteHolder == null) {
            voteHolder = new VoteHolder();
            this.voteHolders.put(guildId, voteHolder);
        }

        voteHolder.modifyAttributes(connectedChannel.getMembers().size() - 1, winPercentage);

        if (scheduler.getPlayer().getPlayingTrack() != this.currentTrack.get(guildId)) {
            voteHolder.resetCounter();
            System.out.println("Test");
            this.currentTrack.put(guildId, scheduler.getPlayer().getPlayingTrack());
        }

        boolean voted = voteHolder.addVoter(event.getMember());
        int votes = voteHolder.getVotes();
        int voteCap = voteHolder.getVoteCap();
        if (!voted) {
            messageChannel.sendMessage(":exclamation: **You have already voted to skip the song!** `" +
                    actor + "`").queue();
        } else {
            messageChannel.sendMessage(":ballot_box_with_check: `" + actor + "` **has voted to skip the current" +
                    " song.** (" + votes + "/" + this.calculateThreshold(voteCap, winPercentage)+ ")").queue();
        }

        boolean pass = voteHolder.runVoteCheck();
        if (pass) {
            scheduler.nextTrack(true);
            messageChannel.sendMessage(":white_check_mark::fast_forward: " +
                    "**Enough votes. Skipping current song.**").queue();
        }
    }

    private int calculateThreshold(int voteCap, double winPercentage) {
        double threshold = (double) voteCap * winPercentage;
        return (int) Math.ceil(threshold);
    }
}
