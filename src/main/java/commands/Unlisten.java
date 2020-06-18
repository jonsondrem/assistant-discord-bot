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

public class Unlisten extends Command {

    public Unlisten() {
        super("unlisten");
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
}
