package dev.thesheep.simpleresourcepack.file;

import dev.thesheep.simpleresourcepack.SimpleResourcepack;
import org.bukkit.Bukkit;
import org.bukkit.scheduler.BukkitRunnable;

import java.io.File;
import java.util.Objects;

public class Compressor {
    /**
     * Compresses a folder into a zip
     * @param folder the folder to compress
     * @return A compressortask to keep track of the progress
     */
    public static CompressorTask createCompressionTask(File folder, boolean silent)
    {
        if(!folder.exists())
        {
            Bukkit.getLogger().severe("Could not find the folder specified.");
            return null;
        }

        CompressorTask compressorTask = new CompressorTask(folder);
        compressorTask.start();

        new BukkitRunnable() {
            @Override
            public void run() {
                if(compressorTask.hasCompleted)
                {
                    String updateMessage = SimpleResourcepack.getInstance().getConfig().getString("message_update", "");
                    if(!updateMessage.equalsIgnoreCase("") && !silent)
                        Bukkit.broadcastMessage(updateMessage);

                    this.cancel();
                }
            }
        }.runTaskTimer(SimpleResourcepack.getInstance(), 3, 5);

        return compressorTask;
    }

    /**
     * Compresses all resourepacks in the default packs folder.
     */
    public static void compressAll()
    {
        File[] resourcepackFolders = SimpleResourcepack.getInstance().getResourcepackFolder().listFiles();
        assert resourcepackFolders != null;
        for(File file : resourcepackFolders)
        {
            createCompressionTask(file, true);
        }

        String updateMessage = SimpleResourcepack.getInstance().getConfig().getString("message_update", "");
        if(updateMessage != "")
            Bukkit.broadcastMessage(updateMessage);
    }
}
