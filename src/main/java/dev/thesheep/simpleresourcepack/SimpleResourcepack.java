package dev.thesheep.simpleresourcepack;

import com.google.common.xml.XmlEscapers;
import dev.thesheep.simpleresourcepack.api.ResoucepackCommand;
import dev.thesheep.simpleresourcepack.api.players.ResoucepackEvents;
import dev.thesheep.simpleresourcepack.file.Compressor;
import dev.thesheep.simpleresourcepack.networking.FileHoster;
import org.bstats.charts.SingleLineChart;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.File;
import java.nio.file.Files;
import java.util.UUID;
import java.util.concurrent.Callable;

import org.bstats.bukkit.Metrics;
public final class SimpleResourcepack extends JavaPlugin {

    static SimpleResourcepack instance;
    public static SimpleResourcepack getInstance()
    {
        return instance;
    }

    /**
     * Get the resoucepack folder,
     * The resoucepack folder is where all the resourcepacks are stored and is almost always located at:
     * /plugins/SimpleResoucepack/resoucepacks/
     * @return The file poiting to the folder of the resoucepacks
     */
    public File getResourcepackFolder()
    {
        String folderPath = getDataFolder().getPath();
        return new File(folderPath + "/resourcepacks");
    }

    public File getCacheFolder()
    {
        String folderPath = getDataFolder().getPath();
        return new File(folderPath + "/cache");
    }

    @Override
    public void onEnable() {
        // Plugin startup logic
        instance = this;

        Metrics metrics = new Metrics(this, 21182);
        metrics.addCustomChart(new SingleLineChart("resoucepacks", new Callable<Integer>() {
            @Override
            public Integer call() throws Exception {
                return getResourcepackFolder().listFiles().length;
            }
        }));

        this.getServer().getPluginManager().registerEvents(new ResoucepackEvents(), this);
        this.getCommand("resoucepack").setExecutor(new ResoucepackCommand());

        // Generate basic files for first-time use
        generateFiles();

        // Create file hoster
        String ip = getConfig().getString("ip");
        int port = getConfig().getInt("port");

        new FileHoster(ip, port);

        // Compress all current resoucepacks
        Compressor.compressAll();
    }

    /**
     * Generates files like the resoucepack folder and the config.yml
     * Calling it could repair a broken installation.
     * Files that already exist won't be replaced.
     */
    public void generateFiles()
    {
        try {
            saveDefaultConfig();

            // resource-pack folder
            if (!Files.exists(getResourcepackFolder().toPath()))
                Files.createDirectory(getResourcepackFolder().toPath());

            // Cache folder
            if (!Files.exists(getCacheFolder().toPath()))
                Files.createDirectory(getCacheFolder().toPath());
        } catch (Exception e)
        {
            Bukkit.getLogger().severe("Failed to generate basic files!\n" + e);
            return;
        }
    }

    public void applyResoucepack(Player player, String name)
    {
        if(name.endsWith(".zip"))
        {
            Bukkit.getLogger().severe("When using the applyResoucepack() method, you should add the .zip at the end of the name.");
            return;
        }

        // Should prob improve this
        boolean exists = false;
        for(File file : getCacheFolder().listFiles())
        {
            if(file.getName().contains(name))
                exists = true;
        }

        if(!exists)
        {
            Bukkit.getLogger().severe("Attempted to update the resoucepack of player " + player.getName() + " but the pack " + name + " could not be found.");
            return;
        }

        FileHoster hoster = FileHoster.getInstance();
        String prompt = SimpleResourcepack.getInstance().getConfig().getString("prompt", "No prompt provided");
        boolean forced = SimpleResourcepack.getInstance().getConfig().getBoolean("forced", true);
        player.addResourcePack(UUID.randomUUID(), "http://" + hoster.getIp() + ":" + hoster.getPort() + "/" + name, null, prompt, forced);
    }

    public void sendDefaultPacks(Player player)
    {
        for(String name : SimpleResourcepack.getInstance().getConfig().getStringList("default"))
        {
            applyResoucepack(player, name);
        }
    }


    @Override
    public void onDisable() {
        // Plugin shutdown logic
    }
}
