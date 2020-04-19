package net.korvic.rppersonas.death;

import net.korvic.rppersonas.RPPersonas;
import net.korvic.rppersonas.personas.Persona;
import org.bukkit.Location;
import org.bukkit.entity.Player;

import java.util.HashMap;
import java.util.Map;

public class DeathRequest {

	private Player killer;
	private Persona killerPersona;

	private Player victim;
	private Persona victimPersona;

	private Location location;

	public DeathRequest(Player killer, Player victim) {
		this.killer = killer;
		this.killerPersona = RPPersonas.get().getPersonaHandler().getLoadedPersona(killer);

		this.victim = victim;
		this.victimPersona = RPPersonas.get().getPersonaHandler().getLoadedPersona(victim);

		this.location = victim.getLocation().toBlockLocation();
	}

	public Player getKiller() {
		return killer;
	}
	public Player getVictim() {
		return victim;
	}
	public Location getLocation() {
		return location;
	}

	public void complete(boolean staffInflicted) {
		RPPersonas plugin = RPPersonas.get();

		// TODO kill persona

		Map<Object, Object> data = new HashMap<>();
		data.put("victimpersonaid", this.victimPersona.getPersonaID());
		data.put("victimaccountid", this.victimPersona.getAccountID());
		data.put("victimuuid", this.victim.getUniqueId());

		data.put("killerpersonaid", this.killerPersona.getPersonaID());
		data.put("killeraccountid", this.killerPersona.getAccountID());
		data.put("killeruuid", this.killer.getUniqueId());

		data.put("location", this.location);
		data.put("staff", staffInflicted);

		plugin.getDeathSQL().registerOrUpdate(data);

		// TODO change to ghost
		// TODO drop body
	}

}
