package commands;

import api.YouTubeScraper;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import utilities.PropertiesLoader;

public class Shutdown extends Command{

    public Shutdown() {
        super("shutdown");
        this.description = "Shutdowns the bot. Only for the bot owner.";
    }

    @Override
    @SuppressWarnings("ConstantConditions")
    public void run(MessageReceivedEvent event) {
        if (event.getMember().getIdLong() != Long.parseLong(PropertiesLoader.loadProperties()
                .getProperty("discord-id"))) {
            event.getChannel().sendMessage(":exclamation: **Only the bot owner can use this command.** "
                    + event.getMember().getEffectiveName()).queue();
            return;
        }

        event.getChannel().sendMessage(":white_check_mark: **Shutting down.**").queue();
        YouTubeScraper.getInstance().turnOffWebDriver();
        event.getJDA().shutdownNow();
        System.exit(0);
    }
}
