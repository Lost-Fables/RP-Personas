package net.korvic.rppersonas.death;

import com.comphenix.packetwrapper.WrapperPlayServerWorldBorder;
import com.comphenix.protocol.wrappers.EnumWrappers;
import org.bukkit.entity.Player;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

import java.util.ArrayList;
import java.util.List;

public class GhostHandler {

	private static List<Player> ghostList = new ArrayList<>();

	public static void addGhost(Player p) {
		addEffects(p);
		ghostList.add(p);
	}

	public static void removeGhost(Player p) {
		removeEffects(p);
		ghostList.remove(p);
	}

	public static boolean isGhost(Player p) {
		return ghostList.contains(p);
	}

	public static void addEffects(Player p) {
		WrapperPlayServerWorldBorder packet = new WrapperPlayServerWorldBorder();
		packet.setAction(EnumWrappers.WorldBorderAction.SET_WARNING_BLOCKS);
		packet.setWarningDistance(Integer.MAX_VALUE);
		packet.sendPacket(p);

		p.setInvulnerable(true);
		p.setWalkSpeed(0.17f);

		p.addPotionEffect(new PotionEffect(PotionEffectType.INVISIBILITY, 100000, 255, false, false));
	}

	public static void removeEffects(Player p) {
		WrapperPlayServerWorldBorder packet = new WrapperPlayServerWorldBorder();
		packet.setAction(EnumWrappers.WorldBorderAction.SET_WARNING_BLOCKS);
		packet.setWarningDistance(0);
		packet.sendPacket(p);

		p.setInvulnerable(false);
		p.setWalkSpeed(0.2f);

		p.removePotionEffect(PotionEffectType.INVISIBILITY);
	}

}
