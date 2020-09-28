package net.korvic.rppersonas.players.statuses;

import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.potion.PotionEffectType;

public class SlowStatus extends Status {

	public static final String NAME = "Slowness";
	public static final String DESC = "Palms are sweaty, knees weak, arms are heavy. All movements are more sluggish than normal.";

	public SlowStatus() {
		super(NAME, '❄', Material.ICE, ChatColor.AQUA, DESC, true, DEFAULT_DURATION);
	}

	@Override
	public void applyEffect(Player player, byte severity) {
		refreshEffect(player, severity);
	}

	@Override
	public void clearEffect(Player player) {
		player.removePotionEffect(PotionEffectType.SLOW);
		player.removePotionEffect(PotionEffectType.SLOW_DIGGING);
	}

	@Override
	public void refreshEffect(Player player, byte severity) {
		player.addPotionEffect(createInfiniteEffect(PotionEffectType.SLOW, severity));
		player.addPotionEffect(createInfiniteEffect(PotionEffectType.SLOW_DIGGING, severity));
	}

}
