package commands;

import music.PlayerManager;
import music.TrackInfo;
import music.TrackScheduler;
import net.dv8tion.jda.api.entities.MessageChannel;
import net.dv8tion.jda.api.entities.VoiceChannel;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;

public class GoTo extends Command {
    public GoTo() {
        super("goto");
        this.description = "Go to a specific point in the current playing song.";
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
            messageChannel.sendMessage(":x: I'm currently not playing a song! `" + actor + "`").queue();
            return;
        }

        if (event.getMember().getIdLong() != TrackInfo.getTrackInfo(scheduler.getPlayer().getPlayingTrack())
                .getInitiatorId()) {
            event.getChannel().sendMessage(":exclamation: **Only the song initiator may use this command on " +
                    "their song.** `" + actor + "`").queue();
            return;
        }

        String[] command = event.getMessage().getContentRaw().split(" ", 2);
        long positionLong;
        //0: Invalid format, 1: Minute Format, 2: Hour Format
        int type = checkFormat(command[1]);
        try {
            if (type == 1) {
                positionLong = convertMinuteToMs(command[1]);
            }
            else if (type == 2) {
                positionLong = convertHourToMs(command[1]);
            }
            else {
                event.getChannel().sendMessage(":x: **Invalid format. Valid formats: 00:00 OR 00x:00:00** `"
                        + actor + "`").queue();
                return;
            }
        } catch (NumberFormatException | IndexOutOfBoundsException e) {
            event.getChannel().sendMessage(":x: **Logical error occured, Jon implemented this poorly :frown:** `"
                    + actor + "`").queue();
            return;
        }

        if (positionLong > scheduler.getPlayer().getPlayingTrack().getDuration()) {
            event.getChannel().sendMessage(":x: **That's not possible with this song.** `"
                    + actor + "`").queue();
            return;
        }

        scheduler.getPlayer().getPlayingTrack().setPosition(positionLong);
        event.getChannel().sendMessage(":white_check_mark: `" + actor + "`" +
                " **set song position to " + command[1] + ".**").queue();
    }

    private int checkFormat(String position) {
        long colonCount = position.chars().filter(ch -> ch == ':').count();
        String[] positionSplit = position.split(":");

        //Check format
        if (colonCount == 1 || colonCount == 2) {
            int addIndex = 0;
            if (colonCount == 2) { addIndex = 1; }
            if (positionSplit[addIndex].length() == 2 && positionSplit[1+addIndex].length() == 2) {
                try {
                    //Return 0 if there are more than 60 minutes
                    int minutes = Integer.parseInt(positionSplit[addIndex]);
                    if (minutes > 59 || minutes < 0) {
                        return 0;
                    }
                    //Return 0 if there are more than 60 seconds
                    int seconds = Integer.parseInt(positionSplit[1+addIndex]);
                    if (seconds > 59 || seconds < 0) {
                        return 0;
                    }
                    //Try to parse hours if it exists
                    if (colonCount == 2 && Integer.parseInt(positionSplit[0]) < 0) {
                        return 0;
                    }
                }
                //Return 0 if there are letters in the string
                catch (NumberFormatException ignored) {
                    return 0;
                }
            }
            //Return 0 if 'minutes' or 'seconds' is bigger than 2 letters/numbers
            else {
                return 0;
            }

            //If everything has passed correctly return 0 or 1 for type of format
            if (colonCount == 1) { return 1; }
            return 2;
        }

        //If there are too few or too many colons return 0 for invalid format
        return 0;
    }

    private long convertHourToMs(String position) throws NumberFormatException, IndexOutOfBoundsException {
        String[] positionSplit = position.split(":");
        int hours = Integer.parseInt(positionSplit[0]);
        int minutes = Integer.parseInt(positionSplit[1]);
        int seconds = Integer.parseInt(positionSplit[2]);

        return hours*60*60*1000 + minutes*60*1000 + seconds*1000;
    }

    private long convertMinuteToMs(String position) throws NumberFormatException, IndexOutOfBoundsException {
        String[] positionSplit = position.split(":");
        int minutes = Integer.parseInt(positionSplit[0]);
        int seconds = Integer.parseInt(positionSplit[1]);

        return minutes*60*1000 + seconds*1000;
    }
}
