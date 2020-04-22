package net.korvic.rppersonas.statuses;

import com.comphenix.packetwrapper.WrapperPlayServerWorldBorder;
import com.comphenix.protocol.wrappers.EnumWrappers;
import net.korvic.rppersonas.listeners.StatusEventListener;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.potion.PotionEffectType;

public class EtherealStatus extends Status {

	public static final String NAME = "Ethereal";
	public static final String DESC = "A ghost, a specter, a spookmeister themselves. This person is an ethereal being!";

	public EtherealStatus(long duration) {
		super(NAME, '☠', ChatColor.WHITE, duration, DESC, false);
	}

	@Override
	public void applyEffect(Player player) {
		refreshEffect(player);
	}

	@Override
	public void clearEffect(Player player) {
		StatusEventListener.allowItems(player);
		StatusEventListener.allowInteractions(player);
		getRedBorderPacket(false).sendPacket(player);

		player.setInvulnerable(false);
		player.setWalkSpeed(0.2f);

		player.removePotionEffect(PotionEffectType.INVISIBILITY);
	}

	@Override
	public void refreshEffect(Player player) {
		StatusEventListener.blockItems(player);
		StatusEventListener.blockInteractions(player);
		getRedBorderPacket(true).sendPacket(player);

		player.setInvulnerable(true);
		player.setWalkSpeed(0.17f);

		player.addPotionEffect(createInfiniteEffect(PotionEffectType.INVISIBILITY));
	}

	private WrapperPlayServerWorldBorder getRedBorderPacket(boolean active) {
		WrapperPlayServerWorldBorder packet = new WrapperPlayServerWorldBorder();
		packet.setAction(EnumWrappers.WorldBorderAction.SET_WARNING_BLOCKS);

		if (active) {
			packet.setWarningDistance(Integer.MAX_VALUE);
		} else {
			packet.setWarningDistance(0);
		}

		return packet;
	}

}