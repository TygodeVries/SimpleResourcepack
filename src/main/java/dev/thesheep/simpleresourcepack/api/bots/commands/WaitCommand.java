package dev.thesheep.simpleresourcepack.api.bots.commands;

import dev.thesheep.simpleresourcepack.SimpleResourcepack;
import dev.thesheep.simpleresourcepack.api.bots.Bot;
import dev.thesheep.simpleresourcepack.api.bots.ScriptCommand;
import org.bukkit.scheduler.BukkitRunnable;

public class WaitCommand extends ScriptCommand {
    public WaitCommand(String name, String[] args) {
        super(name, args);
    }

    @Override
    public void execute(Runnable runnable, Bot bot) {
        new BukkitRunnable() {
            @Override
            public void run() {
                runnable.run();
            }
        }.runTaskLater(SimpleResourcepack.getInstance(), Integer.parseInt(args[0]));
    }
}
