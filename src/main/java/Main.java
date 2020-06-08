import net.dv8tion.jda.api.JDABuilder;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;

import javax.security.auth.login.LoginException;
import java.io.File;
import java.io.FileNotFoundException;
import java.util.Scanner;

public class Main extends ListenerAdapter {
    public static void main(String[] args) throws LoginException {
        File file = new File("token.txt");
        String token;
        try {
            Scanner sc = new Scanner(file);
            sc.useDelimiter("\\Z");
            token = sc.next();
            System.out.println(token);
        }
        catch (FileNotFoundException e) {
            System.out.println("Didnt find file");
            return;
        }
        JDABuilder builder = JDABuilder.createDefault(token);
        builder.addEventListeners(new Main());
        builder.build();
    }

    @Override
    public void onMessageReceived(MessageReceivedEvent event) {
        if(event.getMessage().getContentRaw().equals(".test") && !event.getAuthor().isBot()) {
            event.getChannel().sendMessage("I managed to read that command :)").queue();
        }
    }
}
