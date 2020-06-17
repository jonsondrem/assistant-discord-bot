package commands;

import music.PlayerManager;
import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.entities.VoiceChannel;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import net.dv8tion.jda.api.managers.AudioManager;

public class Play extends Command {

    public Play() {
        super("play");
    }

    @Override
    @SuppressWarnings("ConstantConditions")
    public void run(MessageReceivedEvent event) {
        String[] command = event.getMessage().getContentRaw().split(" ", 2);
        VoiceChannel connectedChannel = event.getMember().getVoiceState().getChannel();
        String actor = event.getMember().getEffectiveName();
        if (connectedChannel == null) {
            event.getChannel().sendMessage("You are not connected to a voice channel. `" + actor + "`").queue();
            return;
        }

        if (command.length < 2) {
            event.getChannel().sendMessage("You need to put an url or search word(s) after the command. `" + actor + "`").queue();
            return;
        }

        AudioManager audioManager = event.getGuild().getAudioManager();
        VoiceChannel botChannel = event.getGuild().getSelfMember().getVoiceState().getChannel();
        if (botChannel != connectedChannel && !event.getMember().getPermissions().contains(Permission.ADMINISTRATOR) &&
                botChannel != null) {
            event.getChannel().sendMessage("I'm in a different voice channel. `" + actor + "`").queue();
            return;
        } else {
            audioManager.openAudioConnection(connectedChannel);
        }

        PlayerManager manager = PlayerManager.getInstance();
        if (command[1].contains("https://")) {
            manager.loadAndPlay(event.getTextChannel(), command[1], event.getMember());
        } else {
            manager.loadAndPlay(event.getTextChannel(), "ytsearch: " + command[1], event.getMember());
        }
    }
}
