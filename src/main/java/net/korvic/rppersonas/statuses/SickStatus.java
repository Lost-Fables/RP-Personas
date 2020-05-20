package net.korvic.rppersonas.statuses;

import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.potion.PotionEffectType;

public class SickStatus extends Status {

	public SickStatus() {
		super("Sickness", '☣', ChatColor.DARK_GREEN, "The world spins around you, and you feel sick to your stomach.", true);
	}

	@Override
	public void applyEffect(Player player) {
		refreshEffect(player);
	}

	@Override
	public void clearEffect(Player player) {
		player.removePotionEffect(PotionEffectType.CONFUSION);
	}

	@Override
	public void refreshEffect(Player player) {
		player.addPotionEffect(createInfiniteEffect(PotionEffectType.CONFUSION));
	}

}
