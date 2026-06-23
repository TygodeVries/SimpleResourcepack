package dev.thesheep.simpleresourcepack.api.bots.commands;

import dev.thesheep.simpleresourcepack.SimpleResourcepack;
import dev.thesheep.simpleresourcepack.api.bots.Bot;
import dev.thesheep.simpleresourcepack.api.bots.ScriptCommand;
import org.bukkit.Bukkit;
import org.bukkit.scheduler.BukkitRunnable;

public class RunCommand extends ScriptCommand {
    public RunCommand(String name, String[] args) {
        super(name, args);
    }

    @Override
    public void execute(Runnable runnable, Bot bot) {
        Bukkit.dispatchCommand(Bukkit.getConsoleSender(), args[0]);
        runnable.run();
    }
}
