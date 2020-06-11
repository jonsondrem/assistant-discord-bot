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
            if (command[0].equals(".play")) {
                this.playSong(event, command);
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

    private void playSong(MessageReceivedEvent event, String[] command) {
        VoiceChannel connectedChannel = event.getMember().getVoiceState().getChannel();
        if (connectedChannel == null) {
            event.getChannel().sendMessage("You are not connected to a voice channel.").queue();
            return;
        }

        if (command.length < 2) {
            event.getChannel().sendMessage("You need to put an url after the command.").queue();
            return;
        }

        AudioManager audioManager = event.getGuild().getAudioManager();
        VoiceChannel botChannel = event.getGuild().getSelfMember().getVoiceState().getChannel();
        if (botChannel != connectedChannel && !event.getMember().getPermissions().contains(Permission.ADMINISTRATOR)) {
            event.getChannel().sendMessage("I'm in a different voice channel.").queue();
            return;
        } else if (botChannel == null) {
            audioManager.openAudioConnection(connectedChannel);
        }

        PlayerManager manager = PlayerManager.getInstance();
        manager.loadAndPlay(event.getTextChannel(), command[1]);

        manager.getGuildMusicManager(event.getGuild()).player.setVolume(100);
    }
}
