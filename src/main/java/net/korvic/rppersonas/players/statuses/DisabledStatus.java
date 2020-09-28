package net.korvic.rppersonas.players.statuses;

import com.destroystokyo.paper.Title;
import net.korvic.rppersonas.players.listeners.StatusEventListener;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.potion.PotionEffectType;

public class DisabledStatus extends Status {

	public static final String NAME = "";
	public static final String DESC = "You are currently disabled from interacting with the world.";

	private final Title title;

	public DisabledStatus(Title title) {
		super(NAME, '✖', Material.BARRIER, ChatColor.DARK_RED, DESC, false, DEFAULT_DURATION);
		this.title = title;
	}

	@Override
	public void applyEffect(Player player, byte severity) {
		refreshEffect(player, severity);
		if (title != null) {
			player.sendTitle(title);
		}
	}

	@Override
	public void clearEffect(Player player) {
		StatusEventListener.allowAll(player);

		player.setInvulnerable(false);

		player.removePotionEffect(PotionEffectType.SLOW);
		player.removePotionEffect(PotionEffectType.BLINDNESS);
		player.removePotionEffect(PotionEffectType.INVISIBILITY);

		player.hideTitle();
	}

	@Override
	public void refreshEffect(Player player, byte severity) {
		StatusEventListener.blockAll(player, plugin.getSpawnLocation());

		player.setInvulnerable(true);

		player.addPotionEffect(createInfiniteEffect(PotionEffectType.SLOW, 255));
		player.addPotionEffect(createInfiniteEffect(PotionEffectType.BLINDNESS));
		player.addPotionEffect(createInfiniteEffect(PotionEffectType.INVISIBILITY));
	}

}