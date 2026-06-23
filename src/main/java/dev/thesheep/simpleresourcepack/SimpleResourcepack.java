package dev.thesheep.simpleresourcepack;

import dev.thesheep.simpleresourcepack.api.ResourcepackCommand;
import dev.thesheep.simpleresourcepack.api.ResourcepackCommandSuggestions;
import dev.thesheep.simpleresourcepack.api.ResourcepackGUIEvents;
import dev.thesheep.simpleresourcepack.api.ResourcepackGUIGenerator;
import dev.thesheep.simpleresourcepack.api.bots.Bot;
import dev.thesheep.simpleresourcepack.api.bots.BotScript;
import dev.thesheep.simpleresourcepack.api.players.PlayerPref;
import dev.thesheep.simpleresourcepack.api.players.ResourcepackEvents;
import dev.thesheep.simpleresourcepack.file.Compressor;
import dev.thesheep.simpleresourcepack.versioning.ActionBarCompatibilityManager;
import dev.thesheep.simpleresourcepack.versioning.ResourcePackCompatibilityManager;
import dev.thesheep.simpleresourcepack.networking.FileHoster;
import org.bstats.bukkit.Metrics;
import org.bstats.charts.SingleLineChart;
import org.bukkit.Bukkit;
import org.bukkit.configuration.Configuration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitRunnable;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.JarURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.List;
import java.util.Objects;
import java.util.function.Consumer;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;

public final class SimpleResourcepack extends JavaPlugin {
    private static String PROMPT_MSG;
    private static boolean IS_FORCED;

    private static SimpleResourcepack instance;

    /**
     * returns the instance of the plugin.
     */
    public static SimpleResourcepack getInstance()
    {
        return instance;
    }

    /**
     * Get the resourcepack folder,
     * The resourcepack folder is where all the resourcepacks are stored and is almost always located at:
     * /plugins/SimpleResourcepack/resourcepacks/
     * @return The file poiting to the folder of the resourcepacks
     */
    public File getResourcepackFolder()
    {
        String folderPath = getDataFolder().getPath();
        return new File(folderPath + "/resourcepacks");
    }

    public void reloadResourcepackFiles()
    {
        reloadResourcepackFilesWithDebug(null);
    }

    public void reloadResourcepackFilesWithDebug(Player player)
    {
        SimpleResourcepack.getInstance().getLogger().info("§7Loading config...");
        if(player != null)
        {
            player.sendMessage("§7Loading config...");
        }

        SimpleResourcepack.getInstance().reloadConfig();


        if(getConfig().contains("execute_before_load")) {
            for (String scriptName : getConfig().getStringList("execute_before_load")) {

                BotScript script = BotScript.getBotScriptFromFile(new File(getBotsFolder(), scriptName));

                Bot bot = new Bot(script.getCommands(), new Consumer<String>() {
                    @Override
                    public void accept(String s) {
                        player.sendMessage(s);
                        SimpleResourcepack.getInstance().getLogger().info(s);
                    }
                }, new BukkitRunnable() {
                    @Override
                    public void run() {
                        redoPacks();
                    }
                });

                bot.execute();
            }
        }
        else {
            getLogger().warning("No bot commands have been set in execute_before_load. If that is intentional, you can ignore this message.");
            redoPacks();
        }

    }

    private void redoPacks() {
        Compressor.compressAll();


        String msg = getConfig().getString("message_update", "");
        Bukkit.broadcastMessage(msg);
    }

    public File getSettingsFolder()
    {
        String folderPath = getDataFolder().getPath();
        return new File(folderPath + "/settings");
    }

    /**
     * Returns the folder of caches
     */
    public File getCacheFolder()
    {
        String folderPath = getDataFolder().getPath();
        return new File(folderPath + "/cache");
    }

    /**
     * Returns the folder of bots
     */
    public File getBotsFolder()
    {
        String folderPath = getDataFolder().getPath();
        return new File(folderPath + "/bots");
    }

    private PlayerPref playerPref;

    /**
     * Returns the PlayerPrefs object.
     * @return The PlayerPrefs object
     */
    public PlayerPref getPlayerPref()
    {
        return playerPref;
    }

    private ResourcepackGUIGenerator guiGenerator;
    public ResourcepackGUIGenerator getGuiGenerator()
    {
        return guiGenerator;
    }

    @Override
    public void onEnable() {
        // Plugin startup logic
        instance = this;

        if(isDebugMode())
        {
            getLogger().info("Debug mode is enabled!");
        }

        PROMPT_MSG = getInstance().getConfig().getString("prompt", "No prompt provided");
        IS_FORCED = getInstance().getConfig().getBoolean("forced", true);

        if(!getDataFolder().exists())
        {
            boolean created = getDataFolder().mkdirs();
            if (!created) getLogger().warning("Failed to create plugin data folder!");
        }

        playerPref = new PlayerPref();
        guiGenerator = new ResourcepackGUIGenerator();

        Metrics metrics = new Metrics(this, 21182);
        metrics.addCustomChart(new SingleLineChart("resourcepacks", () -> Objects.requireNonNull(getResourcepackFolder().listFiles()).length));


        this.getServer().getPluginManager().registerEvents(new ResourcepackGUIEvents(), this);

        this.getServer().getPluginManager().registerEvents(new ResourcepackEvents(), this);
        Objects.requireNonNull(this.getCommand("resourcepack")).setExecutor(new ResourcepackCommand());
        Objects.requireNonNull(this.getCommand("resourcepack")).setTabCompleter(new ResourcepackCommandSuggestions());

        // Generate basic files for first-time use
        generateFiles();

        // Create file hoster
        String ip = getConfig().getString("ip");
        int port = getConfig().getInt("port");

        if(isDebugMode())
        {
            getLogger().info("Setup done, starting hosting.");
        }

        FileHoster.initialize(ip, port);

        // Compress all current resourcepack
        Compressor.compressAll();
    }

    public static void debugLog(String msg) {
        if(getInstance().isDebugMode())
        {
            getInstance().getLogger().info(msg);
        }
    }

    /**
     * Generates files like the resourcepack folder and the config.yml
     * Calling it could repair a broken installation.
     * Files that already exist won't be replaced.
     */
    public void generateFiles()
    {
        try {
            saveDefaultConfig();

            // Default setting
            if(!Files.exists(getSettingsFolder().toPath()))
            {
                Files.createDirectory(getSettingsFolder().toPath());
                Files.createFile(new File(getSettingsFolder().toPath() + "/default.yml").toPath());

                YamlConfiguration configuration = new YamlConfiguration();
                configuration.set("name", "Default");
                configuration.set("material", "SPONGE");
                List<String> lore = new ArrayList<>();
                lore.add("§fA default pack.");
                lore.add("§6Edit this in settings/default.yml");
                configuration.set("lore", lore);
                configuration.set("permission", "none");
                configuration.save(new File(getSettingsFolder().toPath() + "/default.yml"));
            }

            // resource-pack folder
            if (!Files.exists(getResourcepackFolder().toPath())) {
                Files.createDirectory(getResourcepackFolder().toPath());
                setupResourcepackFolder();
            }

            // Cache folder
            if (!Files.exists(getCacheFolder().toPath()))
                Files.createDirectory(getCacheFolder().toPath());

            if (!Files.exists(getBotsFolder().toPath()))
                Files.createDirectory(getBotsFolder().toPath());
        } catch (Exception e)
        {
            Bukkit.getLogger().severe("Failed to generate basic files!\n" + e);
        }
    }

    public boolean isDebugMode() {
        return getConfig().getBoolean("debug", false);
    }

    /**
     * Returns a list of available resourcepacks a player could apply.
     * @return A list of resourcepack names
     */
    public List<String> getResourcepacks()
    {
        List<String> a = new ArrayList<>();
        for(File file : Objects.requireNonNull(getResourcepackFolder().listFiles()))
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
        // TODO: Should prob improve this
        boolean exists = false;
        for(File file : Objects.requireNonNull(getCacheFolder().listFiles()))
        {
	        if(file.getName().contains(name))
            {
		        exists = true;
		        break;
	        }
        }

        if(!exists)
        {
            Bukkit.getLogger().severe("Attempted to update the resourcepack of player " + player.getName() + " but the pack " + name + " could not be found.");
            return;
        }

        ResourcePackCompatibilityManager.addResourcePack(player, name, PROMPT_MSG, IS_FORCED);
    }

    /**
     * Removes all resourcepacks from a player and sets the player back to default
     * @param player The player
     */
    public void removeResourcepacks(Player player)
    {
        ResourcePackCompatibilityManager.removeResourcePacks(player);
    }

    public void sendActivePacks(Player player)
    {
        List<String> active = getPlayerPref().getResourcepackPreferences(player);
        for(String ac : active)
        {
            sendResourcepack(player, ac);
            String msg = SimpleResourcepack.getInstance().getConfig().getString("message_downloading", "");
            player.sendMessage(msg);
            ActionBarCompatibilityManager.sendActionBar(player, msg);
        }
    }

    /**
     * Send all the resourcepacks to a player that a player should have on by default
     * @param player The player to send the default packs to
     */
    public void sendDefaultPacks(Player player)
    {
        removeResourcepacks(player);

        for(String name : SimpleResourcepack.getInstance().getConfig().getStringList("resourcepacks"))
        {
            sendResourcepack(player, name);
        }
    }

    @Override
    public void onDisable() {
        // Plugin shutdown logic
        // Attempt to shut down file host

        Bukkit.getScheduler().cancelTasks(this);

        if (!FileHoster.isDisabled()) {
            FileHoster.shutdown();
        }
    }

    public Configuration getResourcepackConfig(String packName) {
        File f = new File(
                SimpleResourcepack.getInstance().getSettingsFolder(),
                packName + ".yml"
        );

        YamlConfiguration config = YamlConfiguration.loadConfiguration(f);

        List<String> defaultLore = new ArrayList<>();
        defaultLore.add("§cDefault Settings, Please change in settings/" + packName + ".yml");

        config.addDefault("name", packName);
        config.addDefault("material", "JUKEBOX");
        config.addDefault("lore", defaultLore);
        config.addDefault("permission", "none");

        config.options().copyDefaults(true);

        try {
            config.save(f);
        } catch (Exception e)
        {
           Bukkit.getLogger().severe("Failed to save a default configuration for a resourcepack because: " + e);
        }
        return config;
    }
    public void setupResourcepackFolder() {
        File folder = getResourcepackFolder(); // Assumed method returning your target directory
        Path folderPath = folder.toPath();

        if (!Files.exists(folderPath)) {
            try {
                Files.createDirectories(folderPath);
                // Copy the contents of the "resourcepacks" folder inside your JAR to the plugin data folder
                copyResourceDir("resourcepacks", folder);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    private void copyResourceDir(String sourceDir, File targetDir) throws IOException {
        URL dirURL = getClass().getClassLoader().getResource(sourceDir);

        if (dirURL == null) {
            throw new IllegalArgumentException("Resource folder '" + sourceDir + "' not found inside JAR.");
        }

        // Handle standard JAR execution environment
        if (dirURL.getProtocol().equals("jar")) {
            JarURLConnection jarConn = (JarURLConnection) dirURL.openConnection();
            try (JarFile jar = jarConn.getJarFile()) {
                Enumeration<JarEntry> entries = jar.entries();

                while (entries.hasMoreElements()) {
                    JarEntry entry = entries.nextElement();
                    String name = entry.getName();

                    // Check if the entry is inside our source directory
                    if (name.startsWith(sourceDir + "/")) {
                        // Determine the relative path to paste into the plugin folder
                        String relativePath = name.substring(sourceDir.length());
                        File destinationFile = new File(targetDir, relativePath);

                        if (entry.isDirectory()) {
                            destinationFile.mkdirs();
                        } else {
                            // Create parent directories if they don't exist yet
                            destinationFile.getParentFile().mkdirs();
                            // Standard Spigot method to save individual files safely
                            try (InputStream in = getClass().getClassLoader().getResourceAsStream(name)) {
                                if (in != null) {
                                    Files.copy(in, destinationFile.toPath(), java.nio.file.StandardCopyOption.REPLACE_EXISTING);
                                }
                            }
                        }
                    }
                }
            }
        } else {
            // Fallback for IDE/development environments running unzipped classes
            File file = new File(dirURL.getFile());
            if (file.exists()) {
                copyLocalDirectory(file, targetDir);
            }
        }
    }

    private void copyLocalDirectory(File source, File destination) throws IOException {
        if (source.isDirectory()) {
            if (!destination.exists()) destination.mkdirs();
            String[] files = source.list();
            if (files != null) {
                for (String file : files) {
                    copyLocalDirectory(new File(source, file), new File(destination, file));
                }
            }
        } else {
            Files.copy(source.toPath(), destination.toPath(), java.nio.file.StandardCopyOption.REPLACE_EXISTING);
        }
    }

}
