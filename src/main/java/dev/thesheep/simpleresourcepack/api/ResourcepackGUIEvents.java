package dev.thesheep.simpleresourcepack.api;

import dev.thesheep.simpleresourcepack.SimpleResourcepack;
import org.bukkit.Bukkit;
import org.bukkit.NamespacedKey;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.event.player.PlayerDropItemEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataType;

public class ResourcepackGUIEvents implements Listener {
    @EventHandler
    public void on(InventoryCloseEvent event)
    {
        ResourcepackGUIGenerator guiGenerator = SimpleResourcepack.getInstance().getGuiGenerator();

        Player player = (Player) event.getPlayer();

        if(guiGenerator.hasGUIOpen(player))
        {
            guiGenerator.closeForPlayer(player);
        }
    }

    @EventHandler
    public void on(InventoryClickEvent event)
    {
        ResourcepackGUIGenerator guiGenerator = SimpleResourcepack.getInstance().getGuiGenerator();

        Player player = (Player) event.getWhoClicked();
        if(guiGenerator.hasGUIOpen(player))
        {
            if(event.getClickedInventory() == null)
            {
                return;
            }

            // Do stuff
            event.setCancelled(true);

            int slot = event.getSlot();
            ItemStack itemStack = event.getClickedInventory().getItem(slot);
            if(itemStack == null)
            {
                return;
            }

            ItemMeta meta = itemStack.getItemMeta();
            assert meta != null;

            NamespacedKey disableKey = NamespacedKey.fromString("button_disable", SimpleResourcepack.getInstance());
            String disableName = meta.getPersistentDataContainer().get(disableKey, PersistentDataType.STRING);

            NamespacedKey enableKey = NamespacedKey.fromString("button_enable", SimpleResourcepack.getInstance());
            String enableName = meta.getPersistentDataContainer().get(enableKey, PersistentDataType.STRING);

            if(disableName != null)
            {
                SimpleResourcepack.getInstance().getPlayerPref().removeResourcepackPreference(player, disableName);
                player.closeInventory();
                Bukkit.getScheduler().runTaskLater(SimpleResourcepack.getInstance(), new Runnable() {
                    @Override
                    public void run() {
                        SimpleResourcepack.getInstance().getGuiGenerator().openForPlayer(player);
                        player.sendMessage(SimpleResourcepack.getInstance().getConfig().getString("message_listupdated", "§aYou have updated your resourcepack preferences. Be sure to run §l/rp update§a to active them."));
                    }
                }, 1);
            }

            if(enableName != null)
            {
                SimpleResourcepack.getInstance().getPlayerPref().addResourcepackPreference(player, enableName);
                player.closeInventory();
                Bukkit.getScheduler().runTaskLater(SimpleResourcepack.getInstance(), new Runnable() {
                    @Override
                    public void run() {
                        SimpleResourcepack.getInstance().getGuiGenerator().openForPlayer(player);
                        player.sendMessage(SimpleResourcepack.getInstance().getConfig().getString("message_listupdated", "§aYou have updated your resourcepack preferences. Be sure to run §l/rp update§a to active them."));
                    }
                }, 1);
            }
        }
    }

    @EventHandler
    public void on(PlayerDropItemEvent event)
    {
        ResourcepackGUIGenerator guiGenerator = SimpleResourcepack.getInstance().getGuiGenerator();

        Player player = (Player) event.getPlayer();
        if(guiGenerator.hasGUIOpen(player))
        {
            event.setCancelled(true);
        }
    }
}
