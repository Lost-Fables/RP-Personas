package net.korvic.rppersonas.listeners;

import net.korvic.rppersonas.RPPersonas;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;

import java.util.UUID;

public class JoinQuitListener implements Listener {

	RPPersonas plugin;

	public JoinQuitListener(RPPersonas plugin) {
		this.plugin = plugin;
	}

	@EventHandler
	public void onJoin(PlayerJoinEvent event) {
		UUID uuid = event.getPlayer().getUniqueId();
		int account = plugin.getUUIDAccountMapSQL().getAccountID(uuid);
		if (account > 0) {
			int persona = plugin.getAccountsSQL().getActivePersonaID(account);
			plugin.getAccountHandler().loadAccount(account, persona);
		} else {
			plugin.getUnregisteredHandler().add(event.getPlayer());
		}
	}

	@EventHandler
	public void onQuit(PlayerQuitEvent event) {
		UUID uuid = event.getPlayer().getUniqueId();
		int account = plugin.getUUIDAccountMapSQL().getAccountID(uuid);
		plugin.getAccountHandler().unloadAccount(account);
		plugin.getPersonaHandler().unloadPersonas(account);
	}

}
