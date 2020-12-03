package net.korvic.rppersonas.statuses;

import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.potion.PotionEffectType;

public class SpeedStatus extends Status {

	public static final String NAME = "Speed";
	public static final String DESC = "You eat lightning and crap thunder. Run like the wind!";

	public SpeedStatus() {
		super(NAME, '➟', Material.SUGAR, ChatColor.GREEN, DESC, true, DEFAULT_DURATION);
	}

	@Override
	public void applyEffect(Player player, byte severity) {
		refreshEffect(player, severity);
	}

	@Override
	public void clearEffect(Player player) {
		player.removePotionEffect(PotionEffectType.SPEED);
		player.removePotionEffect(PotionEffectType.FAST_DIGGING);
	}

	@Override
	public void refreshEffect(Player player, byte severity) {
		player.addPotionEffect(createInfiniteEffect(PotionEffectType.SPEED, severity));
		player.addPotionEffect(createInfiniteEffect(PotionEffectType.FAST_DIGGING));
	}

}
