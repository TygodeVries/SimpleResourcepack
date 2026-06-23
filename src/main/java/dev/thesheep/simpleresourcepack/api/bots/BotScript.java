package dev.thesheep.simpleresourcepack.api.bots;

import dev.thesheep.simpleresourcepack.SimpleResourcepack;
import org.bukkit.Bukkit;

import java.io.File;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.List;

public class BotScript {

    private List<String> lines;

    public BotScript(File file) {
        if(!file.exists())
        {
            this.lines = new ArrayList<>();
            SimpleResourcepack.getInstance().getLogger().severe("Failed to load bot script at: " + file.getName() + " because the file does not exist!");
            return;
        }

        try {
            this.lines = Files.readAllLines(file.toPath());
        } catch (IOException e) {
            SimpleResourcepack.getInstance().getLogger().severe("Failed to load bot script at: " + file.getName() + " because " + e);
        }

        if(lines == null)
        {
            SimpleResourcepack.getInstance().getLogger().severe("Failed to load bot script at: " + file.getName() + " no commands where loaded.");
            lines = new ArrayList<>();
        }
    }

    public List<ScriptCommand> getCommands() {
        List<ScriptCommand> commands = new ArrayList<>();

        for (String line : lines) {
            line = line.trim();

            if (line.isEmpty() || line.startsWith("#")) {
                continue;
            }
            commands.add(ScriptCommand.parseCommand(line));
        }

        return commands;
    }

    public static BotScript getBotScriptFromFile(File file) {
        return new BotScript(file);
    }
}