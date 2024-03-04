package dev.thesheep.simpleresourcepack.api;

import dev.thesheep.simpleresourcepack.SimpleResourcepack;
import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

public class ResourcepackCommandSuggestions implements TabCompleter {
    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String label, String[] args) {
        List<String> r = new ArrayList<>();

        if(args.length == 1)
        {
           r.add("help");
           if(sender.hasPermission("simpleresourcepack.enable.self")) r.add("enable");
           if(sender.hasPermission("simpleresourcepack.disable.self")) r.add("disable");
           if(sender.hasPermission("simpleresourcepack.disable.load")) r.add("load");
           r.add("update");
           if(sender.hasPermission("simpleresourcepack.disable.list")) r.add("list");

           return r;
        }

        if(args[0].equalsIgnoreCase("enable"))
        {
            if(args.length == 2) {
                for (File file : SimpleResourcepack.getInstance().getResourcepackFolder().listFiles()) {
                    r.add(file.getName());
                }
            }
            else {
                return getAllPlayers();
            }
        }

        if(args[0].equalsIgnoreCase("disable"))
        {
            if(args.length == 2) {
                for (File file : SimpleResourcepack.getInstance().getResourcepackFolder().listFiles()) {
                    r.add(file.getName());
                }
            }
            else {
                return getAllPlayers();
            }
        }

        return r;
    }

    private List<String> getAllPlayers()
    {
        List<String> r = new ArrayList<>();
        for(Player player : Bukkit.getOnlinePlayers())
        {
            r.add((player.getName()));
        }

        return r;
    }
}
