import commands.Command;
import net.dv8tion.jda.api.JDABuilder;
import net.dv8tion.jda.api.entities.MessageChannel;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import org.jetbrains.annotations.NotNull;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

import javax.security.auth.login.LoginException;
import java.io.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Properties;

public class Assistant extends ListenerAdapter {
    List<Command> commandList;

    public Assistant() {
        this.commandList = getCommandsInPackage();
    }

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

    public List<Command> getCommandsInPackage() {
        String packageName = "commands";
        String path = packageName.replaceAll("\\.", File.separator);
        List<Command> classes = new ArrayList<>();
        String[] classPathEntries = System.getProperty("java.class.path").split(
                System.getProperty("path.separator")
        );

        String name;
        for (String classpathEntry : classPathEntries) {
            try {
                File base = new File(classpathEntry + File.separatorChar + path);
                for (File file : Objects.requireNonNull(base.listFiles())) {
                    name = file.getName();
                    if (name.endsWith(".class") && !name.equals("Command.class")) {
                        name = name.substring(0, name.length() - 6);
                        Class<?> cl = Class.forName(packageName + "." + name);
                        Command com = (Command) cl.newInstance();
                        classes.add(com);
                    }
                }
            } catch (Exception ignored) {
            }
        }

        return classes;
    }

    @Override
    public void onMessageReceived(@NotNull MessageReceivedEvent event) {

        String syntax = this.getGuildSyntax(event);
        String msg = event.getMessage().getContentRaw();
        String[] command = msg.split(" ", 2);

        boolean isListening = isChannelListening(event.getMessage().getChannel());
        if (isListening) {
            for (Command com : commandList) {
                if ((syntax + com.getCommand()).equals(command[0])) {
                    com.run(event);
                    return;
                }
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
