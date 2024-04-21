package dev.thesheep.simpleresourcepack.api;

import dev.thesheep.simpleresourcepack.SimpleResourcepack;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataType;

import java.io.File;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.List;

public class ResourcepackGUIGenerator {
    public void openForPlayer(Player player)
    {
        activeForPlayers.add(player);

        Inventory inventory = Bukkit.createInventory(player, 9 * 2, "Resourcepacks");
        player.openInventory(inventory);

        SimpleResourcepack srp = SimpleResourcepack.getInstance();

        int index = 0;
        for(String foldername : srp.getResourcepacks())
        {
            File f = new File(srp.getSettingsFolder().toPath() + "/" + foldername + ".yml");

            if(Files.exists(f.toPath()))
            {
                YamlConfiguration configuration = YamlConfiguration.loadConfiguration(f);

                String permission = configuration.getString("permission", "none");

                if(permission.equalsIgnoreCase("none") || player.hasPermission(permission)) {

                    Material material = Material.getMaterial(configuration.getString("material", "PAPER"));
                    if(material == null)
                    {
                        Bukkit.getLogger().severe("Invalid material for pack " + foldername + "\"" + configuration.getString("material", "PAPER") + "\"");
                        material = Material.PAPER;
                    }
                    ItemStack button = new ItemStack(material);
                    ItemMeta meta = button.getItemMeta();
                    meta.setDisplayName(configuration.getString("name", foldername));

                    List<String> lore = configuration.getStringList("lore");
                    meta.setLore(lore);

                    button.setItemMeta(meta);
                    inventory.setItem(index, button);

                    List<String> defaults = SimpleResourcepack.getInstance().getConfig().getStringList("default");

                    ItemStack state = new ItemStack(Material.BARRIER);
                    if(defaults.contains(foldername))
                    {
                        state = new ItemStack(Material.GRAY_STAINED_GLASS_PANE);
                        ItemMeta meta2 = state.getItemMeta();
                        List<String> lore2 = new ArrayList<>();
                        lore2.add("§fThis resourcepack is forced.");
                        meta2.setDisplayName("§7§lDEFAULT");
                        meta2.setLore(lore2);
                        state.setItemMeta(meta2);
                    }
                    else {
                        if (SimpleResourcepack.getInstance().getPlayerPref().getResourcepackPreferences(player).contains(foldername)) {
                            state = new ItemStack(Material.GREEN_STAINED_GLASS_PANE);
                            ItemMeta meta2 = state.getItemMeta();
                            List<String> lore2 = new ArrayList<>();
                            meta2.setDisplayName("§a§lENABLED");
                            lore2.add("§fClick to disable.");
                            meta2.setLore(lore2);

                            NamespacedKey key = NamespacedKey.fromString("button_disable", SimpleResourcepack.getInstance());
                            meta2.getPersistentDataContainer().set(key, PersistentDataType.STRING, foldername);

                            state.setItemMeta(meta2);
                        }
                        else {
                            state = new ItemStack(Material.RED_STAINED_GLASS_PANE);
                            ItemMeta meta2 = state.getItemMeta();

                            List<String> lore2 = new ArrayList<>();
                            lore2.add("§fClick to enable.");

                            meta2.setDisplayName("§c§lDISABLED");

                            NamespacedKey key = NamespacedKey.fromString("button_enable", SimpleResourcepack.getInstance());
                            meta2.getPersistentDataContainer().set(key, PersistentDataType.STRING, foldername);

                            meta2.setLore(lore2);
                            state.setItemMeta(meta2);
                        }
                    }

                    inventory.setItem(index + 9, state);
                }
            }

            index++;

        }
    }

    public boolean hasGUIOpen(Player player)
    {
        return activeForPlayers.contains(player);
    }
    private List<Player> activeForPlayers = new ArrayList<>();

    public void closeForPlayer(Player player)
    {
        activeForPlayers.remove(player);
        player.closeInventory();
    }
}
