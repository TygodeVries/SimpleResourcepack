package dev.thesheep.simpleresourcepack.api.subcommands;

import dev.thesheep.simpleresourcepack.SimpleResourcepack;
import dev.thesheep.simpleresourcepack.file.Compressor;
import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class LoadSubCommand extends SubCommand{
    @Override
    public void onSubCommand(CommandSender commandSender, String[] arguments) {
        if (!commandSender.isOp() && !commandSender.hasPermission("simpleresourcepack.load")) {
            commandSender.sendMessage("§cYou do not have permission todo that.");
            return;
        }


        if(commandSender instanceof Player) {
            SimpleResourcepack.getInstance().reloadResourcepackFilesWithDebug((Player) commandSender);
        }
        else {
            SimpleResourcepack.getInstance().reloadResourcepackFiles();
        }
    }
}
