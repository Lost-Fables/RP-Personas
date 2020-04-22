package net.korvic.rppersonas.statuses;

import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.potion.PotionEffectType;

public class SpeedStatus extends Status {

	private int severity;

	public SpeedStatus(long duration, int severity) {
		super("Speed", '➟', ChatColor.GREEN, duration, "You eat lightning and crap thunder. Run like the wind!", true);
		this.severity = severity;
	}

	@Override
	public void applyEffect(Player player) {
		refreshEffect(player);
	}

	@Override
	public void clearEffect(Player player) {
		player.removePotionEffect(PotionEffectType.SPEED);
		player.removePotionEffect(PotionEffectType.FAST_DIGGING);
	}

	@Override
	public void refreshEffect(Player player) {
		player.addPotionEffect(createInfiniteEffect(PotionEffectType.SPEED, severity));
		player.addPotionEffect(createInfiniteEffect(PotionEffectType.FAST_DIGGING));
	}

}
