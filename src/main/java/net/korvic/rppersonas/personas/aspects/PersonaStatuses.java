package net.korvic.rppersonas.personas.aspects;

import net.md_5.bungee.api.ChatColor;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

public enum PersonaStatuses {
	BLINDNESS(0, "Blindness", '⩹', ChatColor.DARK_PURPLE, new PotionEffect[] { createInfiniteEffect(PotionEffectType.BLINDNESS) } ),
	SLOWNESS(1, "Slowness", '❄', ChatColor.AQUA, new PotionEffect[] { createInfiniteEffect(PotionEffectType.SLOW), createInfiniteEffect(PotionEffectType.SLOW_DIGGING) } ),
	HYPER(2, "Hyper", '➟', ChatColor.GREEN, new PotionEffect[] { createInfiniteEffect(PotionEffectType.SPEED, 2), createInfiniteEffect(PotionEffectType.FAST_DIGGING) } ),
	ILL(3, "Ill", '☠', ChatColor.DARK_GRAY, new PotionEffect[] { createInfiniteEffect(PotionEffectType.CONFUSION) } );

	private static final int DURATION = 100000;
	private final int id;
	private final String name;
	private final char icon;
	private final ChatColor iconColor;
	private final PotionEffect[] effects;

	PersonaStatuses(int id, String name, char icon, ChatColor iconColor, PotionEffect[] effects) {
		this.id = id;
		this.name = name;
		this.icon = icon;
		this.iconColor = iconColor;
		this.effects = effects;
	}


	// PUBLIC //
	public int getID() {
		return id;
	}

	public String getName() {
		return name;
	}

	public char getIcon() {
		return icon;
	}

	public ChatColor getIconColor() {
		return iconColor;
	}

	public PotionEffect[] getEffects() {
		return effects;
	}

	public static PersonaStatuses getByName(String name) {
		for (PersonaStatuses status : values()) {
			if (status.getName().equalsIgnoreCase(name)) {
				return status;
			}
		}
		return null;
	}

	// PRIVATE //
	private static PotionEffect createInfiniteEffect(PotionEffectType type) {
		return createInfiniteEffect(type, 1);
	}
	private static PotionEffect createInfiniteEffect(PotionEffectType type, int amplifier) {
		return new PotionEffect(type, DURATION, amplifier, false ,false, false);
	}
}
