package commands;

import com.sedmelluq.discord.lavaplayer.track.AudioTrack;
import music.PlayerManager;
import music.TrackScheduler;
import net.dv8tion.jda.api.entities.MessageChannel;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;

public class Queue extends Command {
    public Queue() {
        super("queue");
        this.description = "Shows the current queue.";
    }

    @Override
    public void run(MessageReceivedEvent event) {
        MessageChannel messageChannel = event.getChannel();
        PlayerManager manager = PlayerManager.getInstance();
        TrackScheduler scheduler = manager.getGuildMusicManager(event.getGuild()).scheduler;

        messageChannel.sendMessage("**Current Queue:**").queue();
        int index = 0;
        StringBuilder list = new StringBuilder();
        list.append("```");
        for (AudioTrack track : scheduler.getQueue()) {
            list.append(index+1).append(". ").append(track.getInfo().title).append("\n");
            index++;
            if (index % 5 == 0 ) {
                list.append("```");
                messageChannel.sendMessage(list).queue();
                //Reset list
                list.setLength(0);
                list.append("```");
            }
        }
    }
}
