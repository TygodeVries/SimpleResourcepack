package dev.thesheep.simpleresourcepack.api.subcommands;

import dev.thesheep.simpleresourcepack.api.ResourcepackCommand;
import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class UpdateSubCommand extends SubCommand {
    @Override
    public void onSubCommand(CommandSender commandSender, String[] arguments) {
        if(arguments.length == 0)
        {
            updateSelf(commandSender);
        }
        else if(arguments.length == 1) {
            if (arguments[0].equalsIgnoreCase("all")) {
                updateAll(commandSender);
            }
            else
            {
                updateOther(commandSender, arguments[0]);
            }
        }
        else {
            commandSender.sendMessage("§cToo many arguments!");
        }
    }

    private void updateOther(CommandSender commandSender, String playerName)
    {
        if(!commandSender.isOp() && !commandSender.hasPermission("simpleresourcepack.update.other"))
        {
            commandSender.sendMessage("§cYou do not have permission todo that.");
            return;
        }

        Player player = Bukkit.getPlayer(playerName);
        if(player == null)
        {
            commandSender.sendMessage("§c" + playerName + " is not an online player.");
            return;
        }

        ResourcepackCommand.ForceUpdate(player);
        commandSender.sendMessage("§aYou have forced updated the resourcepack for " + player.getName());
    }
    private void updateSelf(CommandSender commandSender)
    {
        if(!(commandSender instanceof Player))
        {
            commandSender.sendMessage("§cYou must be a player todo this!");
            return;
        }

        Player player = (Player) commandSender;
        ResourcepackCommand.ForceUpdate(player);
    }

    private void updateAll(CommandSender commandSender)
    {
        if(!commandSender.isOp() && !commandSender.hasPermission("simpleresourcepack.update.all"))
        {
            commandSender.sendMessage("§cYou do not have permission todo that.");
            return;
        }

        for(Player player : Bukkit.getOnlinePlayers())
        {
            ResourcepackCommand.ForceUpdate(player);
        }
    }
}
