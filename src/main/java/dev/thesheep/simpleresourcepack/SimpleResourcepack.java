package dev.thesheep.simpleresourcepack;

import dev.thesheep.simpleresourcepack.api.ResourcepackCommand;
import dev.thesheep.simpleresourcepack.api.ResourcepackCommandSuggestions;
import dev.thesheep.simpleresourcepack.api.players.PlayerPref;
import dev.thesheep.simpleresourcepack.api.players.ResourcepackEvents;
import dev.thesheep.simpleresourcepack.file.Compressor;
import dev.thesheep.simpleresourcepack.networking.FileHoster;
import net.md_5.bungee.api.ChatMessageType;
import net.md_5.bungee.api.chat.TextComponent;
import org.bstats.charts.SingleLineChart;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.File;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.sql.Struct;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.Callable;

import org.bstats.bukkit.Metrics;
public final class SimpleResourcepack extends JavaPlugin {

    static SimpleResourcepack instance;

    /**
     * returns the instance of the plugin.
     */
    public static SimpleResourcepack getInstance()
    {
        return instance;
    }

    /**
     * Get the resourcepack folder,
     * The resoucepack folder is where all the resourcepacks are stored and is almost always located at:
     * /plugins/SimpleResoucepack/resoucepacks/
     * @return The file poiting to the folder of the resoucepacks
     */
    public File getResourcepackFolder()
    {
        String folderPath = getDataFolder().getPath();
        return new File(folderPath + "/resourcepacks");
    }

    /**
     * Returns the folder of caches
     */
    public File getCacheFolder()
    {
        String folderPath = getDataFolder().getPath();
        return new File(folderPath + "/cache");
    }

    private PlayerPref playerPref;

    /**
     * Returns the PlayerPrefs object.
     * @return
     */
    public PlayerPref getPlayerPref()
    {
        return playerPref;
    }

    @Override
    public void onEnable() {
        // Plugin startup logic
        instance = this;
        playerPref = new PlayerPref();

        Metrics metrics = new Metrics(this, 21182);
        metrics.addCustomChart(new SingleLineChart("resoucepacks", new Callable<Integer>() {
            @Override
            public Integer call() throws Exception {
                return getResourcepackFolder().listFiles().length;
            }
        }));

        this.getServer().getPluginManager().registerEvents(new ResourcepackEvents(), this);
        this.getCommand("resourcepack").setExecutor(new ResourcepackCommand());
        this.getCommand("resourcepack").setTabCompleter(new ResourcepackCommandSuggestions());
        // Generate basic files for first-time use
        generateFiles();

        // Create file hoster
        String ip = getConfig().getString("ip");
        int port = getConfig().getInt("port");

        new FileHoster(ip, port);

        // Compress all current resourcepack
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
            if (!Files.exists(getResourcepackFolder().toPath())) {
                Files.createDirectory(getResourcepackFolder().toPath());

                Files.createDirectory(new File(getResourcepackFolder() + "/default").toPath());
                Files.createDirectory(new File(getResourcepackFolder() + "/default/assets").toPath());
                Files.createDirectory(new File(getResourcepackFolder() + "/default/assets/minecraft").toPath());
                Files.createDirectory(new File(getResourcepackFolder() + "/default/assets/minecraft/textures").toPath());
                Files.createDirectory(new File(getResourcepackFolder() + "/default/assets/minecraft/textures/item").toPath());

                String packContent = "{\n" +
                        "    \"pack\": {\n" +
                        "        \"description\": \"Simple Resourcepack, Change this!\",\n" +
                        "        \"pack_format\": 22\n" +
                        "    }\n" +
                        "}";
                Path mcmetaPath = new File(getResourcepackFolder() + "/default/pack.mcmeta").toPath();
                Files.createFile(mcmetaPath);
                Files.write(mcmetaPath, packContent.getBytes(StandardCharsets.UTF_8));
            }

            // Cache folder
            if (!Files.exists(getCacheFolder().toPath()))
                Files.createDirectory(getCacheFolder().toPath());
        } catch (Exception e)
        {
            Bukkit.getLogger().severe("Failed to generate basic files!\n" + e);
            return;
        }
    }

    /**
     * Returns a list of available resourcepacks a player could apply.
     * @return A list of resourcepack names
     */
    public List<String> getResourcepacks()
    {
        List<String> a = new ArrayList<>();
        for(File file : getResourcepackFolder().listFiles())
        {
            a.add(file.getName());
        }

        return a;
    }

    /**
     * Apply a resourcepack to a player
     * @param player the player
     * @param name The name of the resourcepack (without a .zip)
     */
    public void sendResourcepack(Player player, String name)
    {
            if(name.endsWith(".zip"))
            {
                Bukkit.getLogger().severe("Don't include the .zip in the name of your pack. The current name is " + name);
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
                Bukkit.getLogger().severe("Attempted to update the resourcepack of player " + player.getName() + " but the pack " + name + " could not be found.");
                return;
            }

            FileHoster hoster = FileHoster.getInstance();
            String prompt = SimpleResourcepack.getInstance().getConfig().getString("prompt", "No prompt provided");
            boolean forced = SimpleResourcepack.getInstance().getConfig().getBoolean("forced", true);
            player.addResourcePack(UUID.randomUUID(), "http://" + hoster.getIp() + ":" + hoster.getPort() + "/" + System.currentTimeMillis() + "/" + name, null, prompt, forced);
    }

    /**
     * Removes all resourcepacks from a player and sets the player back to default
     * @param player The player
     */
    public void removeResourcepacks(Player player)
    {
        player.removeResourcePacks();
    }



    public void sendActivePacks(Player player)
    {
        List<String> active = getPlayerPref().getResourcepackPreferences(player);
        for(String ac : active)
        {
            sendResourcepack(player, ac);
            String msg = SimpleResourcepack.getInstance().getConfig().getString("message_downloading", "");
            player.sendMessage(msg);
            player.spigot().sendMessage(ChatMessageType.ACTION_BAR, TextComponent.fromLegacyText(msg));
        }
    }


    /**
     * Send all the resourcepacks to a player that a player should have on by default
     * @param player
     */
    public void sendDefaultPacks(Player player)
    {
        removeResourcepacks(player);

        for(String name : SimpleResourcepack.getInstance().getConfig().getStringList("default"))
        {
            sendResourcepack(player, name);
        }
    }


    @Override
    public void onDisable() {
        // Plugin shutdown logic
    }
}
