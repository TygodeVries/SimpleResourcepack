package dev.thesheep.simpleresourcepack.api.subcommands;

import dev.thesheep.simpleresourcepack.SimpleResourcepack;
import dev.thesheep.simpleresourcepack.api.ResourcepackCommand;
import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.Configuration;
import org.bukkit.entity.Player;

public class EnableSubCommand extends SubCommand {
    @Override
    public void onSubCommand(CommandSender commandSender, String[] arguments) {

        if(arguments.length == 0)
        {
            commandSender.sendMessage("§cYou need to provide the name of a resourcepack");
        }
        else if(arguments.length == 1)
        {
            enableSelf(commandSender, arguments[0]);
        }
        else if(arguments.length == 2)
        {
            enableOther(commandSender, arguments[1], arguments[0]);
        }
        else {
            commandSender.sendMessage("§cToo many arguments!");
        }
    }

    public void enableSelf(CommandSender commandSender, String packName) {
        if (!commandSender.isOp() && !commandSender.hasPermission("simpleresourcepack.enable.self")) {
            commandSender.sendMessage("§cYou do not have permission todo that.");
            return;
        }

        if (!(commandSender instanceof Player)) {
            commandSender.sendMessage("§cYou need to be a player todo that!");
            return;
        }

        if(!SimpleResourcepack.getInstance().getResourcepacks().contains(packName))
        {
            commandSender.sendMessage("§cResourcepack was not found.");
            return;
        }

        Configuration configuration = SimpleResourcepack.getInstance().getResourcepackConfig(packName);
        if (configuration == null)
        {
            commandSender.sendMessage("§cResourcepack settings where not found.");
            return;
        }

        String permission = configuration.getString("permission", "none");

        if(!commandSender.hasPermission(permission) && !permission.equals("none"))
        {
            commandSender.sendMessage("§cYou are missing the permission: " + permission);
            return;
        }

        addResourcepack((Player) commandSender, packName);
    }

    public void enableOther(CommandSender commandSender, String playerName, String packName)
    {
        if(!commandSender.isOp() && !commandSender.hasPermission("simpleresourcepack.enable.other"))
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

        addResourcepack(player, packName);
        commandSender.sendMessage("§aYou have enabled the resourcepack " + packName + " for " + player.getName());
    }

    private void addResourcepack(Player player, String pack)
    {
        SimpleResourcepack instance = SimpleResourcepack.getInstance();

        instance.getPlayerPref().addResourcepackPreference(player, pack);
        ResourcepackCommand.ForceUpdate(player);
    }
}
