package commands;

import com.sedmelluq.discord.lavaplayer.track.AudioTrack;
import music.PlayerManager;
import music.TrackScheduler;
import net.dv8tion.jda.api.entities.MessageChannel;
import net.dv8tion.jda.api.entities.VoiceChannel;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import utilities.VoteHolder;

import java.util.HashMap;
import java.util.Map;

public class Repeat extends Command {
    private Map<Long, VoteHolder> voteHolders;
    private AudioTrack currentTrack;

    public Repeat() {
        super("repeat");
        this.voteHolders = new HashMap<>();
    }

    @Override
    @SuppressWarnings("ConstantConditions")
    public void run(MessageReceivedEvent event) {
        String actor = event.getMember().getEffectiveName();
        VoiceChannel connectedChannel = event.getMember().getVoiceState().getChannel();
        MessageChannel messageChannel = event.getChannel();

        if (connectedChannel == null) {
            messageChannel.sendMessage(":x: **You are not connected to a voice channel!** `" + actor + "`")
                    .queue();
            return;
        }

        VoiceChannel botChannel = event.getGuild().getSelfMember().getVoiceState().getChannel();
        if (botChannel != connectedChannel && !event.getMember().isOwner()) {
            messageChannel.sendMessage(":exclamation: **You have to be in the same voice channel as me to use " +
                    "this command!** `" + actor + "`").queue();
            return;
        }

        PlayerManager manager = PlayerManager.getInstance();
        TrackScheduler scheduler = manager.getGuildMusicManager(event.getGuild()).scheduler;
        if (scheduler.isOnRepeat()) {
            messageChannel.sendMessage(":x: The song is already on repeat. Use '.skip' to go to the " +
                    "next song. `" + actor + "`").queue();
            return;
        }

        if (scheduler.getPlayer().getPlayingTrack() == null) {
            messageChannel.sendMessage(":x: **I'm not currently playing a song!** `" + actor + "`").queue();
            return;
        }

        Long guildId = event.getGuild().getIdLong();
        VoteHolder voteHolder = this.voteHolders.get(event.getGuild().getIdLong());
        if (voteHolder == null) {
            voteHolder = new VoteHolder();
            this.voteHolders.put(guildId, voteHolder);
        }

        double winPercentage = 0.45;
        voteHolder.modifyAttributes(connectedChannel.getMembers().size() - 1, winPercentage);

        if (scheduler.getPlayer().getPlayingTrack() != this.currentTrack) {
            voteHolder.resetCounter();
            this.currentTrack = scheduler.getPlayer().getPlayingTrack();
        }

        if (event.getMember().isOwner()) {
            scheduler.setRepeat(true);
            messageChannel.sendMessage(":white_check_mark: **Owner of the server sat the current song " +
                    "on repeat.** :repeat:").queue();
            return;
        }

        boolean voted = voteHolder.addVoter(event.getMember());
        int votes = voteHolder.getVotes();
        int voteCap = voteHolder.getVoteCap();
        if (!voted) {
            messageChannel.sendMessage(":exclamation: **You have already voted to turn the song on repeat!** `" +
                    actor + "`").queue();
        } else {
            messageChannel.sendMessage(":ballot_box_with_check: `" + actor + "` **has voted to set the current " +
                    "song on repeat:** (" + votes + "/" + this.calculateThreshold(voteCap, winPercentage)+ ")").queue();
        }

        boolean pass = voteHolder.runVoteCheck();
        if (pass) {
            scheduler.setRepeat(true);
            messageChannel.sendMessage(":white_check_mark: " +
                    "**Enough votes. Current song is set on repeat.** :repeat:")
                    .queue();
        }
    }

    private int calculateThreshold(int voteCap, double winPercentage) {
        double threshold = (double) voteCap * winPercentage;
        return (int) Math.ceil(threshold);
    }
}
