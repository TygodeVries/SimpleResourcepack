package dev.thesheep.simpleresourcepack.api.players;

import dev.thesheep.simpleresourcepack.SimpleResourcepack;
import dev.thesheep.simpleresourcepack.versioning.ActionBarCompatibilityManager;
import dev.thesheep.simpleresourcepack.versioning.ResourcePackCompatibilityManager;
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
            ActionBarCompatibilityManager.sendActionBar(event.getPlayer(), msg);
        }
        else if (ResourcePackCompatibilityManager.isUsingLegacyMethod()
                && event.getStatus() == PlayerResourcePackStatusEvent.Status.DECLINED
                && ResourcePackCompatibilityManager.isForced(event.getPlayer()))
        {
            // TODO: Consider making this message configurable in config.yml (See if it's also possible to do the same for 1.20+ for consistency)
            event.getPlayer().kickPlayer("You must accept the resource pack to play on this server.\nEnsure you set Edit Server > Server Resource Packs to Enabled");
        }
        else if(event.getStatus() == PlayerResourcePackStatusEvent.Status.FAILED_DOWNLOAD)
        {
            String msg = SimpleResourcepack.getInstance().getConfig().getString("message_download_failed", "");
            ActionBarCompatibilityManager.sendActionBar(event.getPlayer(), msg);
        }
    }

}
