package net.korvic.rppersonas.death;

import net.korvic.rppersonas.RPPersonas;
import org.bukkit.entity.Player;

import java.util.HashMap;
import java.util.Map;

public class DeathHandler {

	private Map<Player, DeathRequest> requestMap = new HashMap<>(); // Victim, Request

	private final RPPersonas plugin;

	public DeathHandler(RPPersonas plugin) {
		this.plugin = plugin;
	}

	public void requestExecute(Player killer, Player victim) {
		if (!requestMap.containsKey(victim)) {
			requestMap.put(victim, new DeathRequest(killer, victim));
		} else {
			killer.sendMessage(RPPersonas.PRIMARY_DARK + "That player already has an execution request pending!");
		}
		pingRequest(victim);
	}

	public void forceExecute(Player killer, Player victim, boolean event) {

	}

	private void pingRequest(Player victim) {

	}

}
