package commands;

import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.entities.MessageChannel;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;

public class Listen extends Command {

    public Listen() {
        super("listen");
    }

    @Override
    @SuppressWarnings({"unchecked", "ConstantConditions"})
    public void run(MessageReceivedEvent event) {
        if (!event.getMember().getPermissions().contains(Permission.ADMINISTRATOR)) {
            return;
        }

        MessageChannel channel = event.getChannel();
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

        channel.sendMessage("I am now listening to this channel.").queue();
    }
}
