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

}
