package net.korvic.rppersonas.listeners;

import net.korvic.rppersonas.RPPersonas;
import net.korvic.rppersonas.personas.PersonaDisableListener;
import org.bukkit.entity.Player;
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
		Player p = event.getPlayer();
		if (p.hasPermission("rppersonas.link")) {
			UUID uuid = p.getUniqueId();
			int account = plugin.getUUIDAccountMapSQL().getAccountID(uuid);
			if (account > 0) {
				int persona = plugin.getAccountsSQL().getActivePersonaID(account);
				plugin.getAccountHandler().loadAccount(p, account, persona, false);
				plugin.getPersonaHandler().getLoadedPersona(p).updateModel(p);
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
		plugin.getAccountHandler().unloadAccount(account);
		plugin.getPersonaHandler().unloadPersonas(account, event.getPlayer());
		PersonaDisableListener.enablePlayer(p);
	}

}
