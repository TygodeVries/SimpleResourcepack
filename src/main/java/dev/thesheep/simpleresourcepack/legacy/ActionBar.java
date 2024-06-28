package dev.thesheep.simpleresourcepack.legacy;

import dev.thesheep.simpleresourcepack.SimpleResourcepack;
import net.md_5.bungee.api.ChatMessageType;
import net.md_5.bungee.api.chat.BaseComponent;
import net.md_5.bungee.api.chat.TextComponent;
import org.bukkit.entity.Player;

import java.lang.reflect.Method;
import java.util.logging.Level;

public class ActionBar {
	private static final boolean USE_LEGACY_METHOD;
	private static Method fromLegacyMethod;

	static {
		boolean useLegacy;
		try {
			Class.forName("net.md_5.bungee.api.chat.TextComponent").getMethod("fromLegacy", String.class);
			useLegacy = false;
		} catch (NoSuchMethodException | ClassNotFoundException e) {
			useLegacy = true;
		}

		USE_LEGACY_METHOD = useLegacy;

		try {
			fromLegacyMethod = TextComponent.class.getMethod(USE_LEGACY_METHOD ? "fromLegacyText" : "fromLegacy", String.class);
		} catch (NoSuchMethodException e) {
			SimpleResourcepack.getInstance().getLogger().severe("Failed to get method fromLegacyText");
		}
	}

	public static void sendActionBar(Player player, String message) {
		try {
			Object component = fromLegacyMethod.invoke(null, message);

			if (USE_LEGACY_METHOD) {
				player.spigot().sendMessage(ChatMessageType.ACTION_BAR, (BaseComponent[]) component);
			} else {
				player.spigot().sendMessage(ChatMessageType.ACTION_BAR, (BaseComponent) component);
			}
		} catch (Exception e) {
			SimpleResourcepack.getInstance().getLogger().log(Level.WARNING, "Failed to send action bar message to " + player.getName(), e);
		}
	}
}
