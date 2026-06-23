package dev.thesheep.simpleresourcepack.api.bots;

import dev.thesheep.simpleresourcepack.SimpleResourcepack;
import org.bukkit.Bukkit;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.scheduler.BukkitTask;

import java.io.File;
import java.util.List;
import java.util.function.Consumer;

public class Bot {


    private boolean isRunning = false;
    private int index = 0;
    private List<ScriptCommand> commands;
    public Consumer<String> messageCallback;
    public BukkitRunnable onFinish;
    public Bot(List<ScriptCommand> commands, Consumer<String> messageCallback, BukkitRunnable onFinish)
    {
        this.messageCallback = messageCallback;
        this.commands = commands;
        this.onFinish = onFinish;
    }

    public void execute() {

        if(isRunning)
            return;

        isRunning = true;

        index = 0;
        executeNext();
    }

    private void executeNext() {

        if(index == commands.size())
        {
            onFinish.runTask(SimpleResourcepack.getInstance());
            return;
        }
        ScriptCommand command = commands.get(index);
        Bot bot = this;
        command.execute(new Runnable() {
            @Override
            public void run() {
                executeNext();
            }
        }, bot);
    }
}
