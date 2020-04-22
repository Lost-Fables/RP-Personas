package net.korvic.rppersonas.statuses;

import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.potion.PotionEffectType;

public class BlindStatus extends Status {

	public BlindStatus(long duration) {
		super("Blindness", '⩹', ChatColor.DARK_PURPLE, duration, "How are you reading this? You're unable to see anything in front of you.", true);
	}

	@Override
	public void applyEffect(Player player) {
		refreshEffect(player);
	}

	@Override
	public void clearEffect(Player player) {
		player.removePotionEffect(PotionEffectType.BLINDNESS);
	}

	@Override
	public void refreshEffect(Player player) {
		player.addPotionEffect(createInfiniteEffect(PotionEffectType.BLINDNESS));
	}

}
