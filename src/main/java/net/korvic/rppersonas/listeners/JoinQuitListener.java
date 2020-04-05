package net.korvic.rppersonas.listeners;

import net.korvic.rppersonas.RPPersonas;
import net.korvic.rppersonas.personas.modification.PersonaDisableListener;
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

	// EVENT //
	@EventHandler
	public void onJoin(PlayerJoinEvent event) {
		Player p = event.getPlayer();
		if (p.hasPermission("rppersonas.link")) {
			loadIntoPersona(p);
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
		plugin.getPersonaHandler().unloadPersona(p);
		PersonaDisableListener.enablePlayer(p);
	}


	// STATIC //
	public static void loadIntoPersona(Player p) {
		UUID uuid = p.getUniqueId();
		int accountID = plugin.getUUIDAccountMapSQL().getAccountID(uuid);

		if (accountID > 0) {
			refreshAccountPlaytime(p);
			int personaID = plugin.getPersonaAccountMapSQL().getCurrentPersonaID(uuid);
			plugin.getAccountHandler().loadAccount(p, accountID, personaID, false);
		} else {
			plugin.getUnregisteredHandler().add(p);
		}
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
		data.put("accountid", plugin.getUUIDAccountMapSQL().getAccountID(p.getUniqueId()));
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
