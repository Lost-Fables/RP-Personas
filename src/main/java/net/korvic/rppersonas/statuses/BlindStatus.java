package net.korvic.rppersonas.statuses;

import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.potion.PotionEffectType;

public class BlindStatus extends Status {

	public static final String NAME = "Blindness";
	public static final String DESC = "How are you reading this? You're unable to see anything in front of you.";

	public BlindStatus() {
		super(NAME, '⩹', Material.ENDER_EYE, ChatColor.DARK_PURPLE, DESC, true, DEFAULT_DURATION);
	}

	@Override
	public void applyEffect(Player player, byte severity) {
		refreshEffect(player, severity);
	}

	@Override
	public void clearEffect(Player player) {
		player.removePotionEffect(PotionEffectType.BLINDNESS);
	}

	@Override
	public void refreshEffect(Player player, byte severity) {
		player.addPotionEffect(createInfiniteEffect(PotionEffectType.BLINDNESS));
	}

}
