package dev.thesheep.simpleresourcepack.api.players;

import dev.thesheep.simpleresourcepack.SimpleResourcepack;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;

public class ResoucepackEvents implements Listener {
    @EventHandler
    public void on(PlayerJoinEvent event)
    {
        Player player = event.getPlayer();
        SimpleResourcepack.getInstance().sendDefaultPacks(player);
    }
}
