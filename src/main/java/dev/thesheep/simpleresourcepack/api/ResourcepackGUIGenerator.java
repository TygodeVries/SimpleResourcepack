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
    private final List<Player> activeForPlayers = new ArrayList<>();

    public void openForPlayer(Player player)
    {
        // Make sure the player does not have any other guis open.
        player.closeInventory();

        activeForPlayers.add(player);

        SimpleResourcepack srp = SimpleResourcepack.getInstance();
        List<String> resourcepacks = srp.getResourcepacks();
        int resourcepackCount = resourcepacks.size();

        int rowCount = ((resourcepackCount + 8) / 9) * 2;
        if (rowCount > 6)
        {
            // TODO: Add pagination for support for > 27 resourcepacks.
            throw new RuntimeException("Number of resourcepacks exceeds Paper's max inventory!");
        }

        Inventory inventory = Bukkit.createInventory(player, 9 * rowCount, "Resourcepacks");
        player.openInventory(inventory);

        int index = 0;
        for(String resourcepack : resourcepacks)
        {
            if (index % 9 == 0 && index != 0)
            {
                index += 9;
            }

            File f = new File(srp.getSettingsFolder().toPath() + "/" + resourcepack + ".yml");
            if(!Files.exists(f.toPath()))
            {
                continue;
            }

            YamlConfiguration resourcepackData = YamlConfiguration.loadConfiguration(f);
            String resourcepackPermission = resourcepackData.getString("permission", "none");

            if(resourcepackPermission.equalsIgnoreCase("none") || player.hasPermission(resourcepackPermission))
            {
                Material material = Material.getMaterial(resourcepackData.getString("material", "PAPER"));
                if(material == null)
                {
                    Bukkit.getLogger().severe("Invalid material for pack " + resourcepack + "\"" + resourcepackData.getString("material", "") + "\"");
                    material = Material.PAPER;
                }

                ItemStack itemButton = new ItemStack(material);
                ItemMeta meta = itemButton.getItemMeta();
                meta.setDisplayName(resourcepackData.getString("name", resourcepack));
                meta.setLore(resourcepackData.getStringList("lore"));

                itemButton.setItemMeta(meta);
                inventory.setItem(index, itemButton);

                displayState(player, inventory, resourcepack, index + 9);
            }

            ++index;
        }
    }

    public boolean hasGUIOpen(Player player)
    {
        return activeForPlayers.contains(player);
    }

    public void closeForPlayer(Player player)
    {
        activeForPlayers.remove(player);
        player.closeInventory();
    }

    private void displayState(Player player, Inventory inventory, String resourcepack, int index)
    {
        List<String> defaults = SimpleResourcepack.getInstance().getConfig().getStringList("default");

        ItemStack state;
        if(defaults.contains(resourcepack))
        {
            state = new ItemStack(Material.GRAY_STAINED_GLASS_PANE);
            ItemMeta meta = state.getItemMeta();

            meta.setDisplayName("§7§lDEFAULT");

            List<String> lore = new ArrayList<>(1);
            lore.add("§fThis resourcepack is forced.");
            meta.setLore(lore);

            state.setItemMeta(meta);
        }
        else {
            if (SimpleResourcepack.getInstance().getPlayerPref().getResourcepackPreferences(player).contains(resourcepack)) {
                state = new ItemStack(Material.GREEN_STAINED_GLASS_PANE);
                ItemMeta meta = state.getItemMeta();

                meta.setDisplayName("§a§lENABLED");

                List<String> lore = new ArrayList<>(1);
                lore.add("§fClick to disable.");
                meta.setLore(lore);

                NamespacedKey key = NamespacedKey.fromString("button_disable", SimpleResourcepack.getInstance());
                meta.getPersistentDataContainer().set(key, PersistentDataType.STRING, resourcepack);

                state.setItemMeta(meta);
            }
            else {
                state = new ItemStack(Material.RED_STAINED_GLASS_PANE);
                ItemMeta meta2 = state.getItemMeta();

                List<String> lore2 = new ArrayList<>();
                lore2.add("§fClick to enable.");

                meta2.setDisplayName("§c§lDISABLED");

                NamespacedKey key = NamespacedKey.fromString("button_enable", SimpleResourcepack.getInstance());
                meta2.getPersistentDataContainer().set(key, PersistentDataType.STRING, resourcepack);

                meta2.setLore(lore2);
                state.setItemMeta(meta2);
            }
        }

        inventory.setItem(index, state);
    }
}
