package commands;

import com.sedmelluq.discord.lavaplayer.track.AudioTrack;
import music.PlayerManager;
import music.TrackInfo;
import music.TrackScheduler;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.MessageChannel;
import net.dv8tion.jda.api.entities.TextChannel;
import net.dv8tion.jda.api.entities.VoiceChannel;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import utilities.VoteHolder;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;

public class Shuffle extends Command {
    private Map<Long, VoteHolder> voteHolders;

    public Shuffle() {
        super("shuffle");
        this.voteHolders = new HashMap<>();
        this.description = "Shuffles the queue randomly.";
    }

    @Override
    @SuppressWarnings("ConstantConditions")
    public void run(MessageReceivedEvent event) {
        VoiceChannel connectedChannel = event.getMember().getVoiceState().getChannel();
        String actor = event.getMember().getEffectiveName();
        MessageChannel messageChannel = event.getChannel();
        if (connectedChannel == null) {
            event.getChannel().sendMessage(":x: **You are not connected to a voice channel!** `" + actor + "`")
                    .queue();
            return;
        }

        VoiceChannel botChannel = event.getGuild().getSelfMember().getVoiceState().getChannel();
        if (botChannel != connectedChannel && !event.getMember().isOwner()) {
            messageChannel.sendMessage(":exclamation: **You have to be in the same voice channel as me to use" +
                    " this command!** `" + actor + "`").queue();
            return;
        }

        PlayerManager manager = PlayerManager.getInstance();
        TrackScheduler scheduler = manager.getGuildMusicManager(event.getGuild()).scheduler;
        if (scheduler.getPlayer().getPlayingTrack() == null) {
            messageChannel.sendMessage(":x: **I'm currently not playing a song!** `" + actor + "`").queue();
            return;
        }

        if (scheduler.getQueue().isEmpty()) {
            messageChannel.sendMessage(":x: **There are no queue for me to shuffle!** `" + actor + "`").queue();
            return;
        }

        long guildId = event.getGuild().getIdLong();
        double winPercentage = 0.45;
        VoteHolder voteHolder = this.voteHolders.get(guildId);

        if (voteHolder == null) {
            voteHolder = new VoteHolder();
            this.voteHolders.put(guildId, voteHolder);
        }

        if (event.getMember().isOwner()) {
            voteHolder.resetCounter();
            event.getChannel().sendMessage(":white_check_mark: **Owner of the server shuffled the queue.**" +
                    " :twisted_rightwards_arrows:").queue();
            shuffleList(scheduler, event.getMember().getIdLong(), event.getTextChannel());
            return;
        }

        voteHolder.modifyAttributes(connectedChannel.getMembers().size() - 1, winPercentage);

        boolean voted = voteHolder.addVoter(event.getMember());
        int votes = voteHolder.getVotes();
        int voteCap = voteHolder.getVoteCap();
        if (!voted) {
            messageChannel.sendMessage(":exclamation: **You have already voted to shuffle the queue!** `" +
                    actor + "`").queue();
        } else {
            messageChannel.sendMessage(":ballot_box_with_check: `" + actor + "` **has voted to shuffle the current" +
                    " queue.** (" + votes + "/" + this.calculateThreshold(voteCap, winPercentage)+ ")").queue();
        }

        boolean pass = voteHolder.runVoteCheck();
        if (pass) {
            voteHolder.resetCounter();
            messageChannel.sendMessage(":white_check_mark: " +
                    "**Enough votes. Shuffling Queue.** :twisted_rightwards_arrows:").queue();
            shuffleList(scheduler, event.getMember().getIdLong(), event.getTextChannel());
        }
    }

    private int calculateThreshold(int voteCap, double winPercentage) {
        double threshold = (double) voteCap * winPercentage;
        return (int) Math.ceil(threshold);
    }

    private void shuffleList(TrackScheduler scheduler, long memberId, TextChannel channel) {
        ArrayList<AudioTrack> queueList = new ArrayList<>(scheduler.getQueue());
        AudioTrack trackClone = scheduler.getPlayer().getPlayingTrack().makeClone();
        queueList.add(trackClone);
        TrackInfo.addTrackInfo(trackClone, memberId, channel);
        scheduler.getPlayer().startTrack(null, false);
        Collections.shuffle(queueList);
        BlockingQueue<AudioTrack> newQueue = new LinkedBlockingQueue<>(queueList);
        scheduler.setQueue(newQueue);
        scheduler.nextTrack();
    }
}
