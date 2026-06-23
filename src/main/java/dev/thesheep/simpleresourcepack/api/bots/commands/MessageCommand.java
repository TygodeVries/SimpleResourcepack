package dev.thesheep.simpleresourcepack.api.bots.commands;

import dev.thesheep.simpleresourcepack.SimpleResourcepack;
import dev.thesheep.simpleresourcepack.api.bots.Bot;
import dev.thesheep.simpleresourcepack.api.bots.ScriptCommand;
import org.bukkit.scheduler.BukkitRunnable;

public class MessageCommand extends ScriptCommand {
    public MessageCommand(String name, String[] args) {
        super(name, args);
    }

    @Override
    public void execute(Runnable runnable, Bot bot) {
        bot.messageCallback.accept(args[0]);
        runnable.run();
    }
}
