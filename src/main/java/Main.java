import net.dv8tion.jda.api.JDABuilder;
import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.MessageChannel;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

import javax.security.auth.login.LoginException;
import java.io.*;
import java.util.Properties;

public class Main extends ListenerAdapter {
    public static void main(String[] args) throws LoginException {
        File file = new File("token.txt");
        Properties config = loadProperties();
        String token = config.getProperty("token");
        if (token.equals("")) {
            System.out.println("Please write in the token in the config.properties file.");
            return;
        }
        JDABuilder builder = JDABuilder.createDefault(token);
        builder.addEventListeners(new Main());
        builder.build();
    }

    @Override
    public void onMessageReceived(MessageReceivedEvent event) {

        Member author = event.getMember();
        assert author != null;
        String msg = event.getMessage().getContentRaw();
        String[] command = msg.split(" ");

        //TODO .listen command
            //TODO add channel to list
        if(command[0].equals(".listen") && author.getPermissions().contains(Permission.ADMINISTRATOR)) {
            addChannel(event.getMessage().getChannel());
        }

        //TODO .unlisten command
            //TODO remove channel from list

        //TODO check if bot listens to channel
            //TODO play music command
    }

    private static Properties loadProperties() {
        Properties properties = new Properties();

        try {
            FileReader fileReader = new FileReader("config.properties");
            properties.load(fileReader);
        } catch (FileNotFoundException e) {
            properties.setProperty("token", "");
            properties.setProperty("channels", "");
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

    private static void addChannel(MessageChannel channel) {
        JSONParser jsonParser = new JSONParser();
        JSONArray channelList;

        JSONObject JSONChannel = new JSONObject();
        JSONChannel.put("channel", channel);

        try (FileReader fileReader = new FileReader("channels.json")) {
            Object obj = jsonParser.parse(fileReader);

            channelList = (JSONArray) obj;

            //TODO put channel into JSON Array
        } catch (FileNotFoundException e) {
            //TODO generate json file
        } catch (IOException | ParseException e) {
            e.printStackTrace();
        }
    }
}
