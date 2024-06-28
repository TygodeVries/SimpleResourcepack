package dev.thesheep.simpleresourcepack.api.players;

import dev.thesheep.simpleresourcepack.SimpleResourcepack;
import dev.thesheep.simpleresourcepack.legacy.ActionBar;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerResourcePackStatusEvent;

public class ResourcepackEvents implements Listener {
    @EventHandler
    public void on(PlayerJoinEvent event)
    {
        Player player = event.getPlayer();
        SimpleResourcepack.getInstance().sendDefaultPacks(player);
        SimpleResourcepack.getInstance().sendActivePacks(player);
    }

    @EventHandler
    public void on(PlayerResourcePackStatusEvent event)
    {
        if(event.getStatus() == PlayerResourcePackStatusEvent.Status.SUCCESSFULLY_LOADED)
        {
            String msg = SimpleResourcepack.getInstance().getConfig().getString("message_download_complete", "");
            ActionBar.sendActionBar(event.getPlayer(), msg);
        }
        else if(event.getStatus() == PlayerResourcePackStatusEvent.Status.FAILED_DOWNLOAD)
        {
            String msg = SimpleResourcepack.getInstance().getConfig().getString("message_download_failed", "");
            ActionBar.sendActionBar(event.getPlayer(), msg);
        }
    }

}
