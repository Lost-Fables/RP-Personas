package net.korvic.rppersonas.statuses;

import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.potion.PotionEffectType;

public class SlowStatus extends Status {

	private int severity;

	public SlowStatus(int severity) {
		super("Slowness", '❄', ChatColor.AQUA, "Your feet are heavy and your arms slow, all movements are more sluggish than normal.", true);
		this.severity = severity;
	}

	@Override
	public void applyEffect(Player player) {
		refreshEffect(player);
	}

	@Override
	public void clearEffect(Player player) {
		player.removePotionEffect(PotionEffectType.SLOW);
		player.removePotionEffect(PotionEffectType.SLOW_DIGGING);
	}

	@Override
	public void refreshEffect(Player player) {
		player.addPotionEffect(createInfiniteEffect(PotionEffectType.SLOW, severity));
		player.addPotionEffect(createInfiniteEffect(PotionEffectType.SLOW_DIGGING, severity));
	}

}
