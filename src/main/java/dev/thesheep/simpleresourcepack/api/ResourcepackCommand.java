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

import java.util.List;

public class ResourcepackCommand implements CommandExecutor {

   //#TODO Should prob clean this up at some point

    @Override
    public boolean onCommand(@NonNull CommandSender sender, @NonNull  Command command, @NonNull  String label, String[] args) {

        Configuration config = SimpleResourcepack.getInstance().getConfig();

        if(args.length == 0)
        {
            forceUpdate((Player) sender);
            return true;
        }

        // Update your own pack or someone else's
        if(args[0].equalsIgnoreCase("update"))
        {
            if(args.length == 2)
            {
                Player player = Bukkit.getPlayer(args[1]);

                if(player == null)
                {
                    sendPlayerNotFoundError((Player) sender, args[1]);
                    return true;
                }

                if(!hasPermission(player, "simpleresourcepack.update.other"))
                {
                    return true;
                }

                forceUpdate(player);

                return true;
            }

            forceUpdate((Player) sender);

            return true;
        }

        if(args[0].equalsIgnoreCase("enable"))
        {
            if(!hasPermission((Player) sender, "simpleresourcepack.add.self"))
            {
                return true;
            }

            if(args.length == 1)
            {
                sender.sendMessage(config.getString("error_nopack", "§cYou need to provide a resourcepack name"));
                return true;
            }

            if(args.length == 3)
            {
                Player player = Bukkit.getPlayer(args[2]);

                if(player == null)
                {
                    sendPlayerNotFoundError((Player) sender, args[2]);
                    return true;
                }

                if(!hasPermission(player, "simpleresourcepack.add.other"))
                {
                    return true;
                }

                addResoucepack(player, args[1]);

                return true;
            }

            addResoucepack((Player) sender, args[1]);
            return true;
        }

        if(args[0].equalsIgnoreCase("disable"))
        {
            if(!hasPermission((Player) sender, "simpleresourcepack.remove.self"))
            {
                return true;
            }

            if(args.length == 1)
            {
                sender.sendMessage(config.getString("error_nopack", "§cYou need to provide a resourcepack name"));
                return true;
            }

            if(args.length == 3)
            {
                Player player = Bukkit.getPlayer(args[2]);

                if(player == null)
                {
                    sendPlayerNotFoundError((Player) sender, args[2]);
                    return true;
                }

                if(!hasPermission(player, "simpleresourcepack.remove.other"))
                {
                    return true;
                }

                removeResoucepack(player, args[1]);

                return true;
            }

            removeResoucepack((Player) sender, args[1]);
            return true;
        }

        if(args[0].equalsIgnoreCase("load"))
        {
            if(!hasPermission((Player) sender, "simpleresourcepack.load"))
            {
                return true;
            }

            loadFromFiles();
            return true;
        }

        if(args[0].equalsIgnoreCase("list"))
        {
            sendList((Player) sender);
            return true;
        }

        if(args[0].equalsIgnoreCase("help"))
        {
            sender.sendMessage("https://github.com/TygodeVries/SimpleResoucepack/wiki/Commands");
            return true;
        }

        sender.sendMessage(config.getString("error_invalidcommand", "§cUnknown sub command"));
        return true;
    }

    private void forceUpdate(Player target)
    {
        SimpleResourcepack instance = SimpleResourcepack.getInstance();

        instance.sendDefaultPacks(target);
        instance.sendActivePacks(target);
    }

    private void loadFromFiles()
    {
        SimpleResourcepack instance = SimpleResourcepack.getInstance();
        Compressor.compressAll();
        String msg = instance.getConfig().getString("message_update", "");
        Bukkit.broadcastMessage(msg);
    }

    private void addResoucepack(Player player, String resoucepack)
    {
        SimpleResourcepack instance = SimpleResourcepack.getInstance();

        instance.getPlayerPref().addResourcepackPreference(player, resoucepack);
        forceUpdate(player);
    }

    private void removeResoucepack(Player player, String resoucepack)
    {
        SimpleResourcepack instance = SimpleResourcepack.getInstance();

        instance.getPlayerPref().removeResourcepackPreference(player, resoucepack);
        forceUpdate(player);
    }

    private boolean hasPermission(Player player, String permission)
    {
        if(player.hasPermission(permission))
        {
            return true;
        }

        Configuration config = SimpleResourcepack.getInstance().getConfig();
        String msg = config.getString("error_noperms", "§cYou are missing the permission {permission} todo this.");

        player.sendMessage(msg.replace("{permission}", permission));
        return false;
    }


    private void sendPlayerNotFoundError(Player player, String playername)
    {
        Configuration config = SimpleResourcepack.getInstance().getConfig();
        String defaultMsg = "§cWe can not find the player named {player}";

        player.sendMessage(
                config.getString("error.noplayer", defaultMsg).replace("{player}", playername)
        );
    }

    private void sendList(Player player)
    {
        List<String> active = SimpleResourcepack.getInstance().getPlayerPref().getResourcepackPreferences(player);

        List<String> possible = SimpleResourcepack.getInstance().getResourcepacks();

        List<String> defaults = SimpleResourcepack.getInstance().getConfig().getStringList("default");

        for(String pack : possible)
        {
            if(defaults.contains(pack))
            {
                player.sendMessage("§7[Default] > " + pack);
            }
            else if(active.contains(pack))
            {
                player.sendMessage("§a[Enabled] > " + pack);
            }
            else {
                player.sendMessage("§c[Disabled] > " + pack);
            }
        }
    }

}
