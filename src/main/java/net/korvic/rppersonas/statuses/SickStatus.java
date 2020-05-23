package net.korvic.rppersonas.statuses;

import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.potion.PotionEffectType;

public class SickStatus extends Status {

	public static final String NAME = "Sickness";
	public static final String DESC = "Vomit on your sweater already. The world spins around you, and you feel sick to your stomach.";

	public SickStatus() {
		super(NAME, '⚕', Material.SLIME_BALL, ChatColor.DARK_GREEN, DESC, true, DEFAULT_DURATION);
	}

	@Override
	public void applyEffect(Player player, byte severity) {
		refreshEffect(player, severity);
	}

	@Override
	public void clearEffect(Player player) {
		player.removePotionEffect(PotionEffectType.CONFUSION);
	}

	@Override
	public void refreshEffect(Player player, byte severity) {
		player.addPotionEffect(createInfiniteEffect(PotionEffectType.CONFUSION));
	}

}
