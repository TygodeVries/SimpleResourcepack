package dev.thesheep.simpleresourcepack.api;

import dev.thesheep.simpleresourcepack.SimpleResourcepack;
import dev.thesheep.simpleresourcepack.api.subcommands.*;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.Arrays;
import java.util.Hashtable;

public class ResourcepackCommand implements CommandExecutor {

    Hashtable<String, SubCommand> subCommands;

    private void addSubCommand(String command, SubCommand subCommand) {
        subCommands.put(command, subCommand);
    }

    public ResourcepackCommand()
    {
        subCommands = new Hashtable<>();

        addSubCommand("list", new ListSubCommand());
        addSubCommand("update", new UpdateSubCommand());
        addSubCommand("enable", new EnableSubCommand());
        addSubCommand("disable", new DisableSubCommand());
        addSubCommand("load", new LoadSubCommand());

    }


    public static void ForceUpdate(Player target)
    {
        SimpleResourcepack instance = SimpleResourcepack.getInstance();

        instance.sendDefaultPacks(target);
        instance.sendActivePacks(target);
    }


    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if(args.length == 0)
        {
            ForceUpdate((Player) sender);
            return true;
        }

        String subCommand = args[0];
        if(!subCommands.containsKey(subCommand))
        {
            sender.sendMessage("§cUnknown subcommand");
            return true;
        }

        SubCommand subCommandInstance = subCommands.get(subCommand);

        // Remove first argument
        String[] newArgs = Arrays.copyOfRange(args, 1, args.length);
        subCommandInstance.onSubCommand(sender, newArgs);

        return true;
    }
}
