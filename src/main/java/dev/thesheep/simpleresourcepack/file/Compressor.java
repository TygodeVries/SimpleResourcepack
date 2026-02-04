package dev.thesheep.simpleresourcepack.file;

import dev.thesheep.simpleresourcepack.SimpleResourcepack;
import org.bukkit.Bukkit;
import org.bukkit.scheduler.BukkitRunnable;

import java.io.File;
import java.nio.file.CopyOption;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;

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

        if(folder.isFile())
        {
            if(folder.getPath().endsWith(".zip"))
            {
                Bukkit.getLogger().info("Detected precompressed resourcepack at " + folder.getPath());

                String dest = SimpleResourcepack.getInstance().getCacheFolder().getPath() + "/" + folder.getName();

                try {
                    Files.copy(folder.toPath(), new File(dest).toPath(), StandardCopyOption.REPLACE_EXISTING);
                } catch (Exception e)
                {
                    Bukkit.getLogger().severe("Could not copy precompressed resource pack because " + e);
                }
                return null;
            }

            Bukkit.getLogger().info("Could not compress " + folder.getPath() + " not a folder, and an unknown format.");
            return null;
        }

        Bukkit.getLogger().info("Compressing resourcepack at: " + folder.getPath().toString());

        CompressorTask compressorTask = new CompressorTask(folder);
        compressorTask.start();

        new BukkitRunnable() {
            @Override
            public void run() {
                if(compressorTask.hasCompleted)
                {
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
    }
}
