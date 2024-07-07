package dev.thesheep.simpleresourcepack.legacy;

import dev.thesheep.simpleresourcepack.SimpleResourcepack;
import dev.thesheep.simpleresourcepack.networking.FileHoster;
import org.bukkit.entity.Player;

import java.lang.reflect.Method;

import java.util.HashSet;
import java.util.UUID;
import java.util.logging.Level;

public class ResourcePack {
	private static final boolean USE_LEGACY_METHOD;
	private static Method addResourcePackMethod;
	private static Method removeResourcePacksMethod;

	private static final HashSet<UUID> forcedPlayers = new HashSet<>();

	static {
		boolean useLegacy;
		try {
			Player.class.getMethod("addResourcePack", UUID.class, String.class, byte[].class, String.class, boolean.class);
			useLegacy = false;
		} catch (NoSuchMethodException e) {
			useLegacy = true;
		}

		USE_LEGACY_METHOD = useLegacy;

		try {
			if (USE_LEGACY_METHOD) {
				addResourcePackMethod = Player.class.getMethod("setResourcePack", String.class);
				removeResourcePacksMethod = Player.class.getMethod("setResourcePack", String.class);
			} else {
				addResourcePackMethod = Player.class.getMethod("addResourcePack", UUID.class, String.class, byte[].class, String.class, boolean.class);
				removeResourcePacksMethod = Player.class.getMethod("removeResourcePacks");
			}
		} catch (NoSuchMethodException e) {
			SimpleResourcepack.getInstance().getLogger().severe("Failed to get resource pack methods");
		}
	}

	public static void addResourcePack(Player player, String name, String prompt, boolean forced) {
		try {
			String url = "http://" + FileHoster.getIp() + ":" + FileHoster.getPort() + "/" + System.currentTimeMillis() + "/" + name;

			if (USE_LEGACY_METHOD) {
				addResourcePackMethod.invoke(player, url);
				if (prompt != null && !prompt.isEmpty()) player.sendMessage(prompt);
				if (forced) forcedPlayers.add(player.getUniqueId());
			} else {
				UUID packId = UUID.randomUUID();
				addResourcePackMethod.invoke(player, packId, url, null, prompt, forced);
			}
		} catch (Exception e) {
			SimpleResourcepack.getInstance().getLogger().log(Level.SEVERE, "Failed to add resource pack for " + player.getName(), e);
		}
	}

	public static void removeResourcePacks(Player player) {
		try {
			if (USE_LEGACY_METHOD) {
				removeResourcePacksMethod.invoke(player, "");
			} else {
				removeResourcePacksMethod.invoke(player);
			}
		} catch (Exception e) {
			SimpleResourcepack.getInstance().getLogger().log(Level.SEVERE, "Failed to remove resource packs for " + player.getName(), e);
		}
	}

	public static boolean isUsingLegacyMethod() {
		return USE_LEGACY_METHOD;
	}

	public static boolean isForced(Player player) {
		return forcedPlayers.contains(player.getUniqueId());
	}
}
