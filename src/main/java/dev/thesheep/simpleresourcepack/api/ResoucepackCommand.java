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

import javax.script.ScriptEngine;
import java.io.File;
import java.util.ArrayList;
import java.util.List;

public class ResoucepackCommand implements CommandExecutor {

   //#TODO Should prob clean this up at some point

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

        if(args.length > 0 && args[0].equalsIgnoreCase("add") && (sender.hasPermission("simple_resoucepack.addself") || sender.isOp()))
        {
            if(args.length == 1)
            {
                sender.sendMessage(SimpleResourcepack.getInstance().getConfig().getString("message_needname", "You need to provide a name of the resoucepack you want to load"));
                return true;
            }
            String packname = args[1];

            boolean exists = false;
            for (File file : SimpleResourcepack.getInstance().getResourcepackFolder().listFiles())
            {
                if(file.getName().equals(packname))
                {
                    exists = true;
                }
            }

            if(!exists)
            {
                sender.sendMessage("We cant find a resoucepack named " + packname);
                return true;
            }

            Player player = (Player) sender;

            List<String> active = SimpleResourcepack.getInstance().getPlayerPref().getActiveResoucepacks(player);
            active.add(packname);
            SimpleResourcepack.getInstance().getPlayerPref().setPlayerPreferences(player, active);

            SimpleResourcepack.getInstance().sendResoucepack(player, packname);
            return true;
        }

        if(args.length > 0 && args[0].equalsIgnoreCase("forceadd") && (sender.hasPermission("simple_resoucepack.addother") || sender.isOp())) {
            if (args.length == 1) {
                sender.sendMessage(SimpleResourcepack.getInstance().getConfig().getString("message_needname", "You need to provide a name of the resoucepack you want to load"));
                return true;
            }
            String packname = args[1];

            boolean exists = false;
            for (File file : SimpleResourcepack.getInstance().getResourcepackFolder().listFiles())
            {
                if(file.getName().equals(packname))
                {
                    exists = true;
                }
            }

            if(!exists)
            {
                sender.sendMessage("We cant find a resoucepack named " + packname);
                return true;
            }

            if(args.length == 2)
            {
                sender.sendMessage(SimpleResourcepack.getInstance().getConfig().getString("message_needname", "You need to provide a name of the resoucepack you want to load"));
                return true;
            }

            Player player = Bukkit.getPlayer(args[2]);
            if(player == null)
            {
                sender.sendMessage("You need to provide a valid name!");
                return true;
            }

            List<String> active = SimpleResourcepack.getInstance().getPlayerPref().getActiveResoucepacks(player);
            active.add(packname);
            SimpleResourcepack.getInstance().getPlayerPref().setPlayerPreferences(player, active);

            SimpleResourcepack.getInstance().sendResoucepack(player, packname);
            return true;
        }

        if(args.length > 0 && args[0].equalsIgnoreCase("removeall") && (sender.hasPermission("simple_resoucepack.removeall") || sender.isOp()))
        {
            Player player = (Player) sender;

            if(args.length == 1)
            {
                sender.sendMessage(config.getString("message_removeall", "Forcing player to remove resoucepack..."));
                SimpleResourcepack.getInstance().getPlayerPref().setPlayerPreferences(player, new ArrayList<>());
                SimpleResourcepack.getInstance().removeResoucepacks(player);
                SimpleResourcepack.getInstance().sendDefaultPacks(player);
                return true;
            }

            player = Bukkit.getPlayer(args[1]);

            if(player == null)
            {
                sender.sendMessage("Player not found");
                return true;
            }

            sender.sendMessage(config.getString("message_removeall", "Forcing player to remove resoucepack..."));
            SimpleResourcepack.getInstance().getPlayerPref().setPlayerPreferences(player, new ArrayList<>());
            SimpleResourcepack.getInstance().removeResoucepacks(player);
            SimpleResourcepack.getInstance().sendDefaultPacks(player);
            return true;
        }

        if(args.length > 0 && args[0].equalsIgnoreCase("load") && (sender.hasPermission("simple_resoucepack.load") || sender.isOp()))
        {
            sender.sendMessage(config.getString("message_startupdate", "Starting compressing the files..."));
            Compressor.compressAll();
            return true;
        }

        if(args.length > 0 && args[0].equalsIgnoreCase("list") && (sender.hasPermission("simple_resoucepack.list") || sender.isOp()))
        {


            File[] available = SimpleResourcepack.getInstance().getResourcepackFolder().listFiles();

            List<String> active = new ArrayList<>();

            if(sender instanceof Player) {
                Player player = (Player) sender;
                active = SimpleResourcepack.getInstance().getPlayerPref().getActiveResoucepacks(player);
            }


            sender.sendMessage("§7- Here is a list of resoucepacks that are avalible -");
            for(File file : available)
            {
                if(active.contains(file.getName()))
                {
                    sender.sendMessage("§a[Active] " + file.getName());
                }
                else if(SimpleResourcepack.getInstance().getConfig().getStringList("default").contains(file.getName()))
                {
                    sender.sendMessage("§a[Active] " + file.getName() + "§7 (forced)");
                }
                else {
                    sender.sendMessage("§c[Available] " + file.getName());
                }
            }

            return true;
        }

        if(sender instanceof  Player) {
            sender.sendMessage(config.getString("message_collecting", "Downloading Resoucepack..."));
            SimpleResourcepack.getInstance().sendDefaultPacks((Player) sender);
            SimpleResourcepack.getInstance().sendActivePacks((Player) sender);
            return true;
        }
        else {
            sender.sendMessage("You cant apply a resoucepack since you are not a player.");
        }
        return true;
    }
}
