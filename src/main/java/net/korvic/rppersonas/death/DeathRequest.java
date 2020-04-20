package net.korvic.rppersonas.death;

import net.korvic.rppersonas.RPPersonas;
import net.korvic.rppersonas.personas.Persona;
import net.korvic.rppersonas.sql.DeathSQL;
import net.korvic.rppersonas.sql.extras.DataMapFilter;
import org.bukkit.Location;
import org.bukkit.entity.Player;

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

		DataMapFilter data = new DataMapFilter();
		data.put(DeathSQL.VICTIM_PERSONAID, this.victimPersona.getPersonaID())
			.put(DeathSQL.VICTIM_ACCOUNTID, this.victimPersona.getAccountID())
			.put(DeathSQL.VICTIM_UUID, this.victim.getUniqueId())

			.put(DeathSQL.KILLER_PERSONAID, this.killerPersona.getPersonaID())
			.put(DeathSQL.KILLER_ACCOUNTID, this.killerPersona.getAccountID())
			.put(DeathSQL.KILLER_UUID, this.killer.getUniqueId())

			.put(DeathSQL.LOCATION, this.location)
			.put(DeathSQL.STAFF, staffInflicted);

		plugin.getDeathSQL().registerOrUpdate(data);

		// TODO change to ghost
		// TODO drop body
	}

}
