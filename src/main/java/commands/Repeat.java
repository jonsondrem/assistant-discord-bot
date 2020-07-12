package commands;

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

    public Repeat() {
        super("repeat");
        this.voteHolders = new HashMap<>();
    }

    @Override
    @SuppressWarnings("ConstantConditions")
    public void run(MessageReceivedEvent event) {
        PlayerManager manager = PlayerManager.getInstance();
        Long guildId = event.getGuild().getIdLong();
        String actor = event.getMember().getEffectiveName();
        VoiceChannel connectedChannel = event.getMember().getVoiceState().getChannel();
        MessageChannel messageChannel = event.getChannel();
        TrackScheduler scheduler = manager.getGuildMusicManager(event.getGuild()).scheduler;
        double winPercentage = 0.45;

        if (connectedChannel == null) {
            messageChannel.sendMessage("You are not connected to a voice channel. `" + actor + "`").queue();
            return;
        }

        VoiceChannel botChannel = event.getGuild().getSelfMember().getVoiceState().getChannel();
        if (botChannel != connectedChannel && !event.getMember().isOwner()) {
            messageChannel.sendMessage("You have to be in the same voice channel as me to use this command. `"
                    + actor + "`")
                    .queue();
            return;
        }

        if (scheduler.isOnRepeat()) {
            messageChannel.sendMessage("The song is already on repeat. Use skip to go to next.").queue();
            return;
        }

        if (scheduler.getPlayer().getPlayingTrack() == null) {
            messageChannel.sendMessage("I'm currently not playing a song.").queue();
            return;
        }

        if (event.getMember().isOwner()) {
            scheduler.setRepeat(true);
            messageChannel.sendMessage("Owner of the server sat the song on repeat.").queue();
            return;
        }

        VoteHolder voteHolder = this.voteHolders.get(event.getGuild().getIdLong());
        if (voteHolder == null) {
            voteHolder = new VoteHolder();
            this.voteHolders.put(guildId, voteHolder);
        }

        voteHolder.modifyAttributes(connectedChannel.getMembers().size() - 1, winPercentage);

        boolean voted = voteHolder.addVoter(event.getMember());
        int votes = voteHolder.getVotes();
        int voteCap = voteHolder.getVoteCap();
        if (!voted) {
            messageChannel.sendMessage("You have already voted to turn the song on repeat. `" +
                    actor + "`").queue();
        } else {
            messageChannel.sendMessage("`" + actor + "` has voted to set the song to repeat. (" +
                    votes + "/" + this.calculateThreshold(voteCap, winPercentage)+ ")").queue();
        }

        boolean pass = voteHolder.runVoteCheck();
        if (pass) {
            scheduler.setRepeat(true);
            messageChannel.sendMessage("Enough votes. The song is set to repeat.").queue();
        }
    }

    private int calculateThreshold(int voteCap, double winPercentage) {
        double threshold = (double) voteCap * winPercentage;
        return (int) Math.ceil(threshold);
    }
}
