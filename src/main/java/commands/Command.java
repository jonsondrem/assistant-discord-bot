package commands;

import net.dv8tion.jda.api.events.message.MessageReceivedEvent;

public abstract class Command {
    private String command;
    protected String description;

    public Command(String command) {
        this.command = command;
    }

    public String getCommand() {
        return this.command;
    }

    public String getDescription() {
        if (this.description == null) {
            return "";
        }
        return this.description;
    }

    public abstract void run(MessageReceivedEvent event);
}
