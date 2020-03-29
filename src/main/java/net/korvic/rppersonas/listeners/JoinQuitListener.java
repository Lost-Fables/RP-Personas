package net.korvic.rppersonas.listeners;

import net.korvic.rppersonas.RPPersonas;
import net.korvic.rppersonas.personas.PersonaDisableListener;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class JoinQuitListener implements Listener {

	private static RPPersonas plugin;
	private static Map<Player, Long> playerLoginTime = new HashMap<>();

	public JoinQuitListener(RPPersonas plugin) {
		JoinQuitListener.plugin = plugin;
	}

	@EventHandler
	public void onJoin(PlayerJoinEvent event) {
		Player p = event.getPlayer();
		if (p.hasPermission("rppersonas.link")) {
			UUID uuid = p.getUniqueId();
			int account = plugin.getUUIDAccountMapSQL().getAccountID(uuid);

			if (account > 0) {
				refreshAccountPlaytime(event.getPlayer());
				int persona = plugin.getAccountsSQL().getActivePersonaID(account);
				plugin.getAccountHandler().loadAccount(p, account, persona, false);
			} else {
				plugin.getUnregisteredHandler().add(p);
			}
		}
	}

	@EventHandler
	public void onQuit(PlayerQuitEvent event) {
		Player p = event.getPlayer();
		UUID uuid = event.getPlayer().getUniqueId();
		int account = plugin.getUUIDAccountMapSQL().getAccountID(uuid);
		refreshAccountPlaytime(p);
		plugin.getAccountHandler().unloadAccount(account);
		playerLoginTime.remove(p);
		plugin.getPersonaHandler().unloadPersonas(account, event.getPlayer());
		PersonaDisableListener.enablePlayer(p);
	}

	public static void refreshAllAccountPlaytime() {
		for (Player p : Bukkit.getOnlinePlayers()) {
			refreshAccountPlaytime(p);
		}
	}

	public static void refreshAccountPlaytime(Player p) {
		long playtime = 0;
		if (playerLoginTime.containsKey(p)) {
			playtime = System.currentTimeMillis() - playerLoginTime.get(p);
		}
		playerLoginTime.put(p, System.currentTimeMillis());

		Map<Object, Object> data = new HashMap<>();
		data.put("playtime", playtime);

		try {
			plugin.getSaveQueue().addToQueue(plugin.getAccountsSQL().getSaveStatement(data));
		} catch (Exception e) {
			if (RPPersonas.DEBUGGING) {
				e.printStackTrace();
			}
		}
	}

}
