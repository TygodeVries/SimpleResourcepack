package dev.thesheep.simpleresourcepack.api.bots.commands;

import dev.thesheep.simpleresourcepack.SimpleResourcepack;
import dev.thesheep.simpleresourcepack.api.bots.Bot;
import dev.thesheep.simpleresourcepack.api.bots.ScriptCommand;

import java.io.File;
import java.nio.file.Files;

public class CopyCommand extends ScriptCommand {
    public CopyCommand(String name, String[] args) {
        super(name, args);
    }

    @Override
    public void execute(Runnable runnable, Bot bot) {

        try {
            Files.copy(new File(args[0]).toPath(), new File(args[1]).toPath());
        } catch (Exception e)
        {
            SimpleResourcepack.getInstance().getLogger().severe("Could not copy files from " + args[0] + " to " + args[1] + " because " + e);
        }

        runnable.run();
    }
}
