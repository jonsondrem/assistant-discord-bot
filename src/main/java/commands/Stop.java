package commands;

import music.PlayerManager;
import net.dv8tion.jda.api.entities.VoiceChannel;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;

public class Stop extends Command {

    public Stop() {
        super("stop");
        this.description = "Stops the current song and clears the queue.";
    }

    @Override
    @SuppressWarnings("ConstantConditions")
    public void run(MessageReceivedEvent event) {
        if (!event.getMember().isOwner()) {
            event.getChannel().sendMessage(":exclamation: **Only the owner of this server can use this command.**")
                    .queue();
            return;
        }
        VoiceChannel connectedChannel = event.getMember().getVoiceState().getChannel();
        String actor = event.getMember().getEffectiveName();
        /*if (connectedChannel == null) {
            event.getChannel().sendMessage(":x: **You are not connected to a voice channel!** `" + actor + "`")
                    .queue();
            return;
        }

        VoiceChannel botChannel = event.getGuild().getSelfMember().getVoiceState().getChannel();
        if (botChannel != connectedChannel && !event.getMember().isOwner()) {
            event.getChannel().sendMessage(":x: **You have to be in the same voice channel as me!** `" + actor +
                    "`").queue();
            return;
        }*/

        PlayerManager manager = PlayerManager.getInstance();
        manager.getGuildMusicManager(event.getGuild()).player.startTrack(null, false);
        manager.getGuildMusicManager(event.getGuild()).scheduler.clearQueue();
        event.getChannel().sendMessage(":white_check_mark: **Removed the queue and stopped playing songs.** " +
                ":stop_button:").queue();
    }
}
