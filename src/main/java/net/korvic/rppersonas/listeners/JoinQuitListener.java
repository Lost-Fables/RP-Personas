package net.korvic.rppersonas.listeners;

import net.korvic.rppersonas.RPPersonas;
import net.korvic.rppersonas.personas.Persona;
import net.korvic.rppersonas.personas.PersonaHandler;
import net.korvic.rppersonas.sql.AccountsSQL;
import net.korvic.rppersonas.sql.extras.DataMapFilter;
import net.korvic.rppersonas.statuses.DisabledStatus;
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
		p.setCollidable(false);
	}

	@EventHandler
	public void onQuit(PlayerQuitEvent event) {
		Player p = event.getPlayer();
		UUID uuid = event.getPlayer().getUniqueId();
		int account = plugin.getUuidAccountMapSQL().getAccountID(uuid);
		refreshAccountPlaytime(p);
		plugin.getAccountHandler().unloadAccount(account);
		playerLoginTime.remove(p);

		Persona pers = plugin.getPersonaHandler().getLoadedPersona(p);
		if (pers != null) {
			pers.queueSave(p);
			pers.clearAllStatuses();
			pers.unloadPersona(true);
		}
		PersonaHandler.stopSkipping(p);
	}


	// STATIC //
	public static void loadIntoPersona(Player p) {
		UUID uuid = p.getUniqueId();
		int accountID = plugin.getUuidAccountMapSQL().getAccountID(uuid);

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

		DataMapFilter data = new DataMapFilter();
		data.put(AccountsSQL.ACCOUNTID, plugin.getUuidAccountMapSQL().getAccountID(p.getUniqueId()))
			.put(AccountsSQL.PLAYTIME, playtime);

		plugin.getAccountsSQL().registerOrUpdate(data);
	}

}
