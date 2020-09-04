package commands;

import net.dv8tion.jda.api.events.message.MessageReceivedEvent;

import java.util.List;

public class Help extends Command {
    private List<Command> commandList;

    public Help(List<Command> commandList) {
        super("help");
        this.commandList = commandList;
        this.commandList.add(new Listen());
        this.description = "Shows the list of available commands.";
    }

    @Override
    public void run(MessageReceivedEvent event) {
        event.getChannel().sendMessage(generateListString()).queue();
    }

    private String generateListString() {
        StringBuilder list = new StringBuilder("**Command List:**\n```\n");
        int index = 1;
        for (Command command : commandList) {
            list.append(index).append(". ").append(command.getCommand()).append(": ").append(command.getDescription())
                    .append("\n");
            index++;
        }
        list.append("```");
        return list.toString();
    }
}
