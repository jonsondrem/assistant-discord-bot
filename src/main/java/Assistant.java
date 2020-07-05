import commands.*;
import net.dv8tion.jda.api.JDABuilder;
import net.dv8tion.jda.api.entities.MessageChannel;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
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

public class Assistant extends ListenerAdapter {

    private List<Command> commandList;
    private static Logger logger = LoggerFactory.getLogger(Assistant.class);

    public Assistant() {
        this.commandList = insertCommands();
    }

    public static void main(String[] args) throws LoginException {
        PropertiesLoader.updateProperties();
        String token = PropertiesLoader.loadProperties().getProperty("token");
        String youTubeKey = PropertiesLoader.loadProperties().getProperty("youtube-key");

        if (token.equals("") || youTubeKey.equals("")) {
            System.out.println("Discord Bot Token and/or YouTube key are missing. Fill them in the config.properties " +
                    "file.");
            return;
        }
        JDABuilder builder = JDABuilder.createDefault(token);
        builder.addEventListeners(new Assistant());
        builder.build();
    }

    private List<Command> insertCommands() {
        List<Command> list = new ArrayList<>();
        list.add(new Listen());
        list.add(new Play());
        list.add(new Skip());
        list.add(new Stop());
        list.add(new Unlisten());

        return list;
    }

    @Override
    public void onMessageReceived(@NotNull MessageReceivedEvent event) {

        String syntax = this.getGuildSyntax(event);
        String msg = event.getMessage().getContentRaw();
        String[] command = msg.split(" ", 2);

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
}
