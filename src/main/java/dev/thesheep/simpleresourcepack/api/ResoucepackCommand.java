package dev.thesheep.simpleresourcepack.api;

import dev.thesheep.simpleresourcepack.SimpleResourcepack;
import dev.thesheep.simpleresourcepack.file.Compressor;
import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.Configuration;
import org.bukkit.entity.Player;
import org.checkerframework.checker.nullness.qual.NonNull;

public class ResoucepackCommand implements CommandExecutor {
    @Override
    public boolean onCommand(@NonNull CommandSender sender, @NonNull  Command command, @NonNull  String label, String[] args) {

        Configuration config = SimpleResourcepack.getInstance().getConfig();

        if(args.length > 0 && args[0].equalsIgnoreCase("forceupdate") && (sender.hasPermission("simple_resoucepack.force") || sender.isOp()))
        {
            if(args.length == 1)
            {
                sender.sendMessage("You need to specify a player name");
                return true;
            }

            Player player = Bukkit.getPlayer(args[1]);

            if(player == null)
            {
                sender.sendMessage("Player not found");
                return true;
            }

            sender.sendMessage(config.getString("message_force", "Forcing player to update resoucepack..."));
            SimpleResourcepack.getInstance().sendDefaultPacks(player);
            return true;
        }

        if(args.length > 0 && args[0].equalsIgnoreCase("load") && (sender.hasPermission("simple_resoucepack.") || sender.isOp()))
        {
            sender.sendMessage(config.getString("message_startupdate", "Starting compressing the files..."));
            Compressor.compressAll();
            return true;
        }

        if(sender instanceof  Player) {
            sender.sendMessage(config.getString("message_collecting", "Downloading Resoucepack..."));
            SimpleResourcepack.getInstance().sendDefaultPacks((Player) sender);
            return true;
        }
        else {
            sender.sendMessage("You cant apply a resoucepack since you are not a player.");
        }
        return true;
    }
}
