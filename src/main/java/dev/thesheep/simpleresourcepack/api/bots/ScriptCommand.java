package dev.thesheep.simpleresourcepack.api.bots;

import dev.thesheep.simpleresourcepack.api.bots.commands.*;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public abstract class ScriptCommand {

    protected String name;
    public String[] args;

    public ScriptCommand(String name, String[] args)
    {
        this.name = name;
        this.args = args;
    }

    public abstract void execute(Runnable runnable, Bot bot);

    public static ScriptCommand parseCommand(String line) {
        Pattern pattern = Pattern.compile("\"([^\"]*)\"|(\\S+)");
        Matcher matcher = pattern.matcher(line);

        List<String> tokens = new ArrayList<>();

        while (matcher.find()) {
            if (matcher.group(1) != null) {
                tokens.add(matcher.group(1)); // quoted text without quotes
            } else {
                tokens.add(matcher.group(2));
            }
        }

        if (tokens.isEmpty()) {
            throw new IllegalArgumentException("Empty command");
        }

        String name = tokens.remove(0).toLowerCase();

        if(name.equalsIgnoreCase("copy"))
            return new CopyCommand(name, tokens.toArray(new String[0]));

        if(name.equalsIgnoreCase("wait"))
            return new WaitCommand(name, tokens.toArray(new String[0]));

        if(name.equalsIgnoreCase("run"))
            return new RunCommand(name, tokens.toArray(new String[0]));

        if(name.equalsIgnoreCase("message"))
            return new MessageCommand(name, tokens.toArray(new String[0]));

        return new NullCommand(name,  tokens.toArray(new String[0]));
    }
}
