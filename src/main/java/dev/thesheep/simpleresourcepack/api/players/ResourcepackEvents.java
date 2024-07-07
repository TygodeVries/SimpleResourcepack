package dev.thesheep.simpleresourcepack.api.players;

import dev.thesheep.simpleresourcepack.SimpleResourcepack;
import dev.thesheep.simpleresourcepack.legacy.ActionBar;
import dev.thesheep.simpleresourcepack.legacy.ResourcePack;
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
        else if (ResourcePack.isUsingLegacyMethod()
                && event.getStatus() == PlayerResourcePackStatusEvent.Status.DECLINED
                && ResourcePack.isForced(event.getPlayer()))
        {
            // TODO: Consider making this message configurable in config.yml (See if it's also possible to do the same for 1.20+ for consistency)
            event.getPlayer().kickPlayer("You must accept the resource pack to play on this server.\nEnsure you set Edit Server > Server Resource Packs to Enabled");
        }
        else if(event.getStatus() == PlayerResourcePackStatusEvent.Status.FAILED_DOWNLOAD)
        {
            String msg = SimpleResourcepack.getInstance().getConfig().getString("message_download_failed", "");
            ActionBar.sendActionBar(event.getPlayer(), msg);
        }
    }

}
