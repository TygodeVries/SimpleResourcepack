package dev.thesheep.simpleresourcepack.api.players;

import dev.thesheep.simpleresourcepack.SimpleResourcepack;
import net.md_5.bungee.api.ChatMessageType;
import net.md_5.bungee.api.chat.TextComponent;
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
        SimpleResourcepack.getInstance().sendDefaultPacks((Player) player);
        SimpleResourcepack.getInstance().sendActivePacks((Player) player);
    }

    @EventHandler
    public void on(PlayerResourcePackStatusEvent event)
    {
        if(event.getStatus() == PlayerResourcePackStatusEvent.Status.SUCCESSFULLY_LOADED)
        {
            String msg = SimpleResourcepack.getInstance().getConfig().getString("message_download_complete", "");
            event.getPlayer().spigot().sendMessage(ChatMessageType.ACTION_BAR, TextComponent.fromLegacyText(msg));
        }
        else if(event.getStatus() == PlayerResourcePackStatusEvent.Status.FAILED_DOWNLOAD)
        {
            String msg = SimpleResourcepack.getInstance().getConfig().getString("message_download_failed", "");
            event.getPlayer().spigot().sendMessage(ChatMessageType.ACTION_BAR, TextComponent.fromLegacyText(msg));
        }
    }

}
