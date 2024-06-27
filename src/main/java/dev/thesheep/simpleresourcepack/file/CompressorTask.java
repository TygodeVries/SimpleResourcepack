package dev.thesheep.simpleresourcepack.file;

import dev.thesheep.simpleresourcepack.SimpleResourcepack;
import org.bukkit.scheduler.BukkitRunnable;

import java.io.File;

public class CompressorTask {

    File folder;
    public CompressorTask(File folder)
    {
        this.folder = folder;
    }

    public void start()
    {
        if(hasCompleted)
        {
            return;
        }

        BukkitRunnable runnable = new BukkitRunnable() {
            @Override
            public void run() {
                String name = folder.getName();
                String source = folder.getPath();
                String destination = SimpleResourcepack.getInstance().getCacheFolder().getPath() + "/" + name + ".zip";
                ZipEncoder.createZipFile(source, destination);
            }
        };

        runnable.runTaskAsynchronously(SimpleResourcepack.getInstance());

        hasCompleted = true;
    }

    public boolean hasCompleted = false;
}
