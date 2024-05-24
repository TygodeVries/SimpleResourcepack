package dev.thesheep.simpleresourcepack.api.subcommands;

import org.bukkit.command.CommandSender;
public abstract class SubCommand {
    public abstract void onSubCommand(CommandSender sender, String[] arguments);
}
