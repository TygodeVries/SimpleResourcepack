package dev.thesheep.simpleresourcepack.api.subcommands;

import dev.thesheep.simpleresourcepack.SimpleResourcepack;
import dev.thesheep.simpleresourcepack.file.Compressor;
import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;

public class LoadSubCommand extends SubCommand{
    @Override
    public void onSubCommand(CommandSender commandSender, String[] arguments) {
        if (!commandSender.isOp() && !commandSender.hasPermission("simpleresourcepack.load")) {
            commandSender.sendMessage("§cYou do not have permission todo that.");
            return;
        }

        commandSender.sendMessage("§7Loading config...");
        SimpleResourcepack.getInstance().reloadConfig();

        commandSender.sendMessage("§7Loading packs...");
        loadFromFiles();
    }
    private void loadFromFiles()
    {
        SimpleResourcepack instance = SimpleResourcepack.getInstance();
        Compressor.compressAll();
        String msg = instance.getConfig().getString("message_update", "");
        Bukkit.broadcastMessage(msg);
    }
}
