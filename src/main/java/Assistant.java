import music.PlayerManager;
import net.dv8tion.jda.api.JDABuilder;
import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.MessageChannel;
import net.dv8tion.jda.api.entities.VoiceChannel;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import net.dv8tion.jda.api.managers.AudioManager;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

import javax.security.auth.login.LoginException;
import java.io.*;
import java.util.Properties;

public class Assistant extends ListenerAdapter {
    public static void main(String[] args) throws LoginException {
        Properties config = loadProperties();
        String token = config.getProperty("token");
        if (token.equals("")) {
            System.out.println("Please write in the token in the config.properties file.");
            return;
        }
        JDABuilder builder = JDABuilder.createDefault(token);
        builder.addEventListeners(new Assistant());
        builder.build();
    }

    @Override
    public void onMessageReceived(MessageReceivedEvent event) {

        Member author = event.getMember();
        assert author != null;
        String msg = event.getMessage().getContentRaw();
        String[] command = msg.split(" ", 2);

        if (command[0].equals(".listen") && author.getPermissions().contains(Permission.ADMINISTRATOR)) {
            this.addChannel(event.getMessage().getChannel());
            return;
        }

        if (command[0].equals(".unlisten") && author.getPermissions().contains(Permission.ADMINISTRATOR)) {
            this.removeChannel(event.getMessage().getChannel());
            return;
        }

        boolean isListening = isChannelListening(event.getMessage().getChannel());
        if (isListening) {
            switch (command[0]) {
                case ".play":
                    this.queueSong(event, command);
                    break;
                case ".skip":
                    this.skipSong(event);
                    break;
                case ".stop":
                    this.stopAudioPlayer(event);
                    break;
            }
        }
    }

    private static Properties loadProperties() {
        Properties properties = new Properties();

        try {
            FileReader fileReader = new FileReader("config.properties");
            properties.load(fileReader);
        } catch (FileNotFoundException e) {
            properties.setProperty("token", "");
            try {
                FileWriter fileWriter = new FileWriter("config.properties");
                properties.store(fileWriter, "Config for discord bot.");
            }
            catch (IOException s) {
                s.printStackTrace();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }

        return properties;
    }

    @SuppressWarnings("unchecked")
    private void addChannel(MessageChannel channel) {
        JSONParser jsonParser = new JSONParser();
        JSONObject jsonChannel = new JSONObject();
        jsonChannel.put("channel", channel.toString());

        try (FileReader fileReader = new FileReader("channels.json")) {
            JSONArray channelList = (JSONArray) jsonParser.parse(fileReader);
            for (Object o : channelList) {
                JSONObject jsonObj = (JSONObject) o;
                if (jsonObj.get("channel").equals(jsonChannel.get("channel"))) {
                    channel.sendMessage("I am already listening to this channel.").queue();
                    return;
                }
            }
            channelList.add(jsonChannel);

            FileWriter fileWriter = new FileWriter("channels.json");
            fileWriter.write(channelList.toJSONString());
            fileWriter.flush();
            fileWriter.close();
        } catch (FileNotFoundException f) {
            try {
                FileWriter fileWriter = new FileWriter("channels.json");
                JSONArray channelList = new JSONArray();
                channelList.add(jsonChannel);
                fileWriter.write(channelList.toJSONString());
                fileWriter.flush();
                fileWriter.close();
            } catch (IOException s) {
                s.printStackTrace();
            }
        } catch (IOException | ParseException e) {
            e.printStackTrace();
        }

        channel.sendMessage("I have now added this channel to the listening JSON file.").queue();
    }

    @SuppressWarnings("unchecked")
    private void removeChannel(MessageChannel channel){
        JSONParser jsonParser = new JSONParser();
        JSONObject jsonChannel = new JSONObject();
        jsonChannel.put("channel", channel.toString());
        boolean found = false;

        try (FileReader fileReader = new FileReader("channels.json")) {
            JSONArray channelList = (JSONArray) jsonParser.parse(fileReader);
            JSONArray toRemove = new JSONArray();
            for (Object o : channelList) {
                JSONObject jsonObj = (JSONObject) o;
                if (jsonObj.get("channel").equals(jsonChannel.get("channel"))) {
                    toRemove.add(o);
                    found = true;
                }
            }

            channelList.removeAll(toRemove);
            FileWriter fileWriter = new FileWriter("channels.json");
            fileWriter.write(channelList.toJSONString());
            fileWriter.flush();
            fileWriter.close();
        } catch (FileNotFoundException ignored) {
        } catch (IOException | ParseException e) {
            e.printStackTrace();
        }

        if (found) {
            channel.sendMessage("I am now ignoring this channel.").queue();
        }
    }

    @SuppressWarnings("unchecked")
    private boolean isChannelListening(MessageChannel channel) {
        boolean isChannelListening = false;
        try (FileReader fileReader = new FileReader("channels.json")) {
            JSONParser jsonParser = new JSONParser();
            JSONObject jsonChannel = new JSONObject();
            jsonChannel.put("channel", channel.toString());
            JSONArray channelList = (JSONArray) jsonParser.parse(fileReader);
            for (Object o : channelList) {
                JSONObject jsonObj = (JSONObject) o;
                if (jsonObj.get("channel").equals(jsonChannel.get("channel"))) {
                    isChannelListening = true;
                }
            }

        } catch (FileNotFoundException ignored) {
        } catch (IOException | ParseException e) {
            e.printStackTrace();
        }

        return isChannelListening;
    }

    @SuppressWarnings("ConstantConditions")
    private void queueSong(MessageReceivedEvent event, String[] command) {
        VoiceChannel connectedChannel = event.getMember().getVoiceState().getChannel();
        String actor = event.getMember().getEffectiveName();
        if (connectedChannel == null) {
            event.getChannel().sendMessage("You are not connected to a voice channel. `" + actor + "`").queue();
            return;
        }

        if (command.length < 2) {
            event.getChannel().sendMessage("You need to put an url after the command. `" + actor + "`").queue();
            return;
        }

        AudioManager audioManager = event.getGuild().getAudioManager();
        VoiceChannel botChannel = event.getGuild().getSelfMember().getVoiceState().getChannel();
        if (botChannel != connectedChannel && !event.getMember().getPermissions().contains(Permission.ADMINISTRATOR)) {
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

    @SuppressWarnings("ConstantConditions")
    private void skipSong(MessageReceivedEvent event) {
        VoiceChannel connectedChannel = event.getMember().getVoiceState().getChannel();
        String actor = event.getMember().getEffectiveName();
        if (connectedChannel == null) {
            event.getChannel().sendMessage("You are not connected to a voice channel. `" + actor + "`").queue();
            return;
        }

        VoiceChannel botChannel = event.getGuild().getSelfMember().getVoiceState().getChannel();
        if (botChannel != connectedChannel && !event.getMember().isOwner()) {
            event.getChannel().sendMessage("You have to be in the same voice channel as me to the skip song. `"
                    + actor + "`")
                    .queue();
            return;
        }

        PlayerManager manager = PlayerManager.getInstance();
        manager.getGuildMusicManager(event.getGuild()).scheduler.voteHolder
                .modifyAttributes(connectedChannel.getMembers().size() - 1, 0.45);
        if (event.getMember().isOwner()) {
            manager.getGuildMusicManager(event.getGuild()).scheduler.nextTrack();
            event.getChannel().sendMessage("Owner of the server skipped the song.").queue();
            return;
        }
        boolean voted = manager.getGuildMusicManager(event.getGuild()).scheduler.voteHolder.addVoter(event.getMember());
        if (!voted) {
            event.getChannel().sendMessage("You have already voted to skip the song. `" +
                    actor + "`").queue();
        } else {
            event.getChannel().sendMessage("`" + actor + "` has voted to skip. (" +
                    manager.getGuildMusicManager(event.getGuild()).scheduler.voteHolder.getVotes() + "/" +
                    manager.getGuildMusicManager(event.getGuild()).scheduler.voteHolder.getVoteCap() + ")").queue();
        }
        boolean pass = manager.getGuildMusicManager(event.getGuild()).scheduler.voteHolder.runVoteCheck();
        if (pass) {
            manager.getGuildMusicManager(event.getGuild()).scheduler.nextTrack();
            event.getChannel().sendMessage("Enough votes. I am skipping the song.").queue();
        }
    }

    @SuppressWarnings("ConstantConditions")
    private void stopAudioPlayer(MessageReceivedEvent event) {
        VoiceChannel connectedChannel = event.getMember().getVoiceState().getChannel();
        String actor = event.getMember().getEffectiveName();
        if (connectedChannel == null) {
            event.getChannel().sendMessage("You are not connected to a voice channel. `" + actor + "`").queue();
            return;
        }

        VoiceChannel botChannel = event.getGuild().getSelfMember().getVoiceState().getChannel();
        if (botChannel != connectedChannel && !event.getMember().isOwner()) {
            event.getChannel().sendMessage("You have to be in the same voice channel as me. `" + actor + "`")
                    .queue();
            return;
        }

        PlayerManager manager = PlayerManager.getInstance();
        manager.getGuildMusicManager(event.getGuild()).player.startTrack(null, false);
        event.getChannel().sendMessage("I removed the queue and stopped playing.").queue();
    }
}
