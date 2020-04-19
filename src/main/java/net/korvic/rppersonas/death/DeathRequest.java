package net.korvic.rppersonas.death;

import org.bukkit.Location;
import org.bukkit.entity.Player;

public class DeathRequest {

	private Player killer;
	private Player victim;
	private Location location;

	public DeathRequest(Player killer, Player victim) {
		this.killer = killer;
		this.victim = victim;
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
		// TODO kill persona, add entry to deathSQL, change to ghost, drop body.
	}

}
