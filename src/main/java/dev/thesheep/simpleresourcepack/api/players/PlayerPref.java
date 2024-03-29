package dev.thesheep.simpleresourcepack.api.players;

import dev.thesheep.simpleresourcepack.SimpleResourcepack;
import org.bukkit.Bukkit;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;

import java.io.File;
import java.util.List;

public class PlayerPref {

    private YamlConfiguration playerPreferences;

    private File file;

    public PlayerPref()
    {
        File dataFolder = SimpleResourcepack.getInstance().getDataFolder();
        file = new File( dataFolder.getPath() + "/playerPreferences.yml");

        if(!file.exists())
        {
            try {
                Bukkit.getLogger().info("Creating playerPreferences...");
                file.createNewFile();
            } catch (Exception e)
            {
                Bukkit.getLogger().severe("Could not create playerPreferences.yml! " + e);
                return;
            }
        }

        try
        {
            playerPreferences = YamlConfiguration.loadConfiguration(file);
            Bukkit.getLogger().info("Loaded player preferences!");
        } catch (Exception e)
        {
            Bukkit.getLogger().severe("Could not load in playerPreferences! " + e);
        }
    }

    public void setResourcepackPreferences(Player player, List<String> packs)
    {
        playerPreferences.set("resourcepacks." + player.getUniqueId(), packs);

        try {
            playerPreferences.save(file);
        } catch (Exception e)
        {
            Bukkit.getLogger().severe("Failed to save preferences: " + e);
        }
    }

    /**
     * Add a resoucepack that the client will download when the player joins
     * @param player The player to edit
     * @param resoucepackName The name of the resoucepack to add
     */
    public void addResourcepackPreference(Player player, String resoucepackName)
    {
        List<String> preferences = getResourcepackPreferences(player);
        preferences.add(resoucepackName);
        setResourcepackPreferences(player, preferences);
    }

    /**
     * Removes a resoucepack that the client will download when the player joins
     * @param player The player to edit
     * @param resoucepackName The name of the resoucepack to remove
     */
    public void removeResourcepackPreference(Player player, String resoucepackName)
    {
        List<String> preferences = getResourcepackPreferences(player);
        preferences.remove(resoucepackName);
        setResourcepackPreferences(player, preferences);
    }

    public List<String> getResourcepackPreferences(Player player)
    {
        List<String> rps = playerPreferences.getStringList("resourcepacks." + player.getUniqueId());
        return rps;
    }
}
