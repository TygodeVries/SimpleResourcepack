package dev.thesheep.simpleresourcepack.api.subcommands;

import dev.thesheep.simpleresourcepack.SimpleResourcepack;
import dev.thesheep.simpleresourcepack.api.ResourcepackGUIGenerator;
import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class ListSubCommand extends SubCommand{
    @Override
    public void onSubCommand(CommandSender commandSender, String[] arguments) {

        if(arguments.length == 0)
        {
            openForSelf(commandSender);
        }
        else if(arguments.length == 1)
        {
            String playerName = arguments[0];
            openForOther(commandSender, playerName);
        }
        else {
            commandSender.sendMessage("§cToo many arguments!");
        }
    }

    private void openForSelf(CommandSender commandSender)
    {
        if(!commandSender.isOp() && !commandSender.hasPermission("simpleresourcepack.list.self"))
        {
            commandSender.sendMessage("§cYou do not have permission todo that.");
            return;
        }

        if(!(commandSender instanceof Player))
        {
            commandSender.sendMessage("§cYou must be a player todo this!");
            return;
        }

        Player player = (Player) commandSender;

        ResourcepackGUIGenerator guiGenerator = SimpleResourcepack.getInstance().getGuiGenerator();
        guiGenerator.openForPlayer(player);
    }

    private void openForOther(CommandSender commandSender, String playerName)
    {
        if(!commandSender.isOp() && !commandSender.hasPermission("simpleresourcepack.list.other"))
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

        ResourcepackGUIGenerator guiGenerator = SimpleResourcepack.getInstance().getGuiGenerator();
        guiGenerator.openForPlayer(player);
        commandSender.sendMessage("§aYou have opened the resourcepack selection screen for " + player.getName());
    }
}
