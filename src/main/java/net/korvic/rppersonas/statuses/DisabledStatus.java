package net.korvic.rppersonas.statuses;

import com.comphenix.packetwrapper.WrapperPlayServerWorldBorder;
import com.comphenix.protocol.wrappers.EnumWrappers;
import com.destroystokyo.paper.Title;
import net.korvic.rppersonas.listeners.StatusEventListener;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.potion.PotionEffectType;

public class DisabledStatus extends Status {

	public static final String NAME = "Disabled";
	public static final String DESC = "You are currently disabled from interacting with the world.";

	private final Title title;

	public DisabledStatus(Title title) {
		super(NAME, '✖', ChatColor.DARK_RED, -1, DESC, false);
		this.title = title;
	}

	@Override
	public void applyEffect(Player player) {
		refreshEffect(player);
		if (title != null) {
			player.sendTitle(title);
		}
	}

	@Override
	public void clearEffect(Player player) {
		StatusEventListener.allowAll(player);

		player.setInvulnerable(false);

		player.removePotionEffect(PotionEffectType.BLINDNESS);
		player.removePotionEffect(PotionEffectType.INVISIBILITY);

		player.hideTitle();
	}

	@Override
	public void refreshEffect(Player player) {
		StatusEventListener.blockAll(player, plugin.getSpawnLocation());

		player.setInvulnerable(true);

		player.addPotionEffect(createInfiniteEffect(PotionEffectType.BLINDNESS));
		player.addPotionEffect(createInfiniteEffect(PotionEffectType.INVISIBILITY));
	}

}