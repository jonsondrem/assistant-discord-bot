import api.SpotifyModel;
import api.YouTubeScraper;
import commands.*;
import music.PlayerManager;
import net.dv8tion.jda.api.JDABuilder;
import net.dv8tion.jda.api.entities.MessageChannel;
import net.dv8tion.jda.api.entities.VoiceChannel;
import net.dv8tion.jda.api.events.GenericEvent;
import net.dv8tion.jda.api.events.guild.voice.GuildVoiceLeaveEvent;
import net.dv8tion.jda.api.events.guild.voice.GuildVoiceMoveEvent;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import net.dv8tion.jda.api.hooks.EventListener;
import net.dv8tion.jda.api.managers.AudioManager;
import org.jetbrains.annotations.NotNull;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import utilities.PropertiesLoader;

import javax.security.auth.login.LoginException;
import java.io.*;
import java.util.ArrayList;
import java.util.List;

public class Assistant implements EventListener {

    private List<Command> commandList;
    private Command listenCommand;
    private static Logger logger = LoggerFactory.getLogger(Assistant.class);

    public Assistant() {
        this.commandList = insertCommands();
        this.listenCommand = new Listen();
    }

    public static void main(String[] args) throws LoginException {
        PropertiesLoader.updateProperties();
        boolean validConfig = PropertiesLoader.checkProperties();
        if (!validConfig) { return; }
        YouTubeScraper.build();
        SpotifyModel.build();
        JDABuilder builder = JDABuilder.createDefault(PropertiesLoader.loadProperties().getProperty("discord-token"));
        builder.addEventListeners(new Assistant());
        builder.build();
    }

    private List<Command> insertCommands() {
        List<Command> list = new ArrayList<>();
        list.add(new Play());
        list.add(new Skip());
        list.add(new Stop());
        list.add(new Unlisten());
        list.add(new Repeat());
        list.add(new Shutdown());
        list.add(new GoTo());
        list.add(new Shuffle());
        list.add(new Queue());

        list.add(new Help(list));
        return list;
    }

    @Override
    @SuppressWarnings("ConstantConditions")
    public void onEvent(@NotNull GenericEvent event) {
        if (event instanceof MessageReceivedEvent) {
            this.onMessage((MessageReceivedEvent) event);
        }

        else if (event instanceof GuildVoiceLeaveEvent) {
            VoiceChannel channel = ((GuildVoiceLeaveEvent) event).getGuild().getSelfMember().getVoiceState()
                    .getChannel();
            this.disconnectBotIfEmpty(channel);
        }

        else if (event instanceof GuildVoiceMoveEvent) {
            VoiceChannel channel = ((GuildVoiceMoveEvent) event).getGuild().getSelfMember().getVoiceState()
                    .getChannel();
            this.disconnectBotIfEmpty(channel);
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

    @SuppressWarnings("unchecked")
    private String getGuildSyntax(MessageReceivedEvent event) {
        String id = event.getGuild().getId();
        JSONParser jsonParser = new JSONParser();
        JSONObject jsonChannel = new JSONObject();
        jsonChannel.put("guildId", id);

        try (FileReader fileReader = new FileReader("guildsyntax.json")) {
            JSONArray channelList = (JSONArray) jsonParser.parse(fileReader);
            for (Object o : channelList) {
                JSONObject jsonObj = (JSONObject) o;
                if (jsonObj.get("guildId").equals(jsonChannel.get("guildId"))) {
                    return jsonObj.get("syntax").toString();
                }
            }

            FileWriter fileWriter = new FileWriter("guildsyntax.json");
            jsonChannel.put("syntax", ".");
            channelList.add(jsonChannel);
            fileWriter.write(channelList.toJSONString());
            fileWriter.flush();
            fileWriter.close();

            return jsonChannel.get("syntax").toString();
        } catch (FileNotFoundException f) {
            try {
                FileWriter fileWriter = new FileWriter("guildsyntax.json");
                JSONArray channelList = new JSONArray();
                jsonChannel.put("syntax", ".");
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

        return ".";
    }

    private void onMessage(MessageReceivedEvent event) {
        String syntax = this.getGuildSyntax(event);
        String msg = event.getMessage().getContentRaw();
        String[] command = msg.split(" ", 2);
        command[0] = command[0].toLowerCase();

        if ((syntax + this.listenCommand.getCommand()).equals(command[0])) {
            this.listenCommand.run(event);
            return;
        }

        boolean isListening = isChannelListening(event.getMessage().getChannel());
        if (isListening) {
            for (Command com : this.commandList) {
                if ((syntax + com.getCommand()).equals(command[0])) {
                    com.run(event);
                    return;
                }
            }
        }
    }

    private void disconnectBotIfEmpty(VoiceChannel channel) {
        if (channel != null && channel.getMembers().size() == 1) {
            PlayerManager manager = PlayerManager.getInstance();
            manager.getGuildMusicManager(channel.getGuild()).player.startTrack(null, false);

            AudioManager audioManager = channel.getGuild().getAudioManager();
            audioManager.closeAudioConnection();
        }
    }
}
