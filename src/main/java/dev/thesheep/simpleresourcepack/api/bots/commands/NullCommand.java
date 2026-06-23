package dev.thesheep.simpleresourcepack.api.bots.commands;

import dev.thesheep.simpleresourcepack.SimpleResourcepack;
import dev.thesheep.simpleresourcepack.api.bots.Bot;
import dev.thesheep.simpleresourcepack.api.bots.ScriptCommand;
import org.bukkit.Bukkit;

public class NullCommand extends ScriptCommand {
    public NullCommand(String name, String[] args) {
        super(name, args);
    }

    @Override
    public void execute(Runnable runnable, Bot bot) {
        SimpleResourcepack.getInstance().getLogger().severe("Command " + name + " could not be found.");
        runnable.run();
    }
}
