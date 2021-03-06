package net.korvic.rppersonas.death;

import co.lotc.core.util.DataMapFilter;
import net.korvic.rppersonas.RPPersonas;
import net.korvic.rppersonas.personas.Persona;
import net.korvic.rppersonas.sql.KarmaSQL;
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
		DeathRequest req = new DeathRequest(killer, victim);
		requestMap.put(victim, req);
		req.ping();
	}

	public boolean acceptExecute(Player killer, Player victim) {
		Persona killerPersona = plugin.getPersonaHandler().getLoadedPersona(killer);
		Persona victimPersona = plugin.getPersonaHandler().getLoadedPersona(victim);

		boolean output = false;
		if (requestMap.containsKey(victim)) {
			DeathRequest req = requestMap.get(victim);
			if (req.getKiller().equals(killer)) {
				req.complete(false);
				requestMap.remove(victim);
				output = true;
			}
		}

		DataMapFilter data = new DataMapFilter().put(KarmaSQL.PERSONAID, killerPersona.getPersonaID())
												.put(KarmaSQL.ACTION, "EXECUTE")
												.put(KarmaSQL.MODIFIER, plugin.getKarmaSQL().calculateExecuteModifier(killerPersona.getPersonaID(), victimPersona.getPersonaID()));
		plugin.getKarmaSQL().registerOrUpdate(data);

		return output;
	}

	public void forceExecute(Player killer, Player victim) {
		new DeathRequest(killer, victim).complete(true);
	}

	public boolean hasRequest(Player victim) {
		return requestMap.containsKey(victim);
	}

	public void pingRequest(Player victim) {
		if (requestMap.containsKey(victim)) {
			requestMap.get(victim).ping();
		}
	}

	public void deleteRequest(Player victim) {
		requestMap.remove(victim);
	}

}
