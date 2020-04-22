package net.korvic.rppersonas.statuses;

import net.korvic.rppersonas.RPPersonas;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

public abstract class Status {

	// STATIC //
	protected static final RPPersonas plugin = RPPersonas.get();
	private static final int INFINITE_POTION_DURATION = 100000;

	public static void applyStatus(Status status, Player player) {
		plugin.getPersonaHandler().getLoadedPersona(player).addStatus(status);
	}
	public static void clearStatus(Status status, Player player) {
		plugin.getPersonaHandler().getLoadedPersona(player).clearStatus(status);
	}

	// INSTANCE //
	private final String name;
	private final char icon;
	private final ChatColor color;
	private final long expiry;
	private final String description;

	private final boolean toggleable;

	public Status(String name, char icon, ChatColor color, long duration, String description, boolean toggleable) {
		this.name = name;
		this.icon = icon;
		this.color = color;
		if (duration > 0) {
			this.expiry = System.currentTimeMillis() + duration;
		} else {
			this.expiry = -1;
		}
		this.description = description;

		this.toggleable = toggleable;
	}

	public String getName() {
		return name;
	}
	public char getIcon() {
		return icon;
	}
	public ChatColor getColor() {
		return color;
	}
	public long getExpiry() {
		return expiry;
	}
	public String getDescription() {
		return description;
	}

	public boolean isToggleable() {
		return toggleable;
	}

	public void applyTo(Player player) {
		applyStatus(this, player);
	}
	public void clearFrom(Player player) {
		clearStatus(this, player);
	}

	// UTIL //
	public static PotionEffect createInfiniteEffect(PotionEffectType type) {
		return createInfiniteEffect(type, 1);
	}
	public static PotionEffect createInfiniteEffect(PotionEffectType type, int amplifier) {
		return new PotionEffect(type, INFINITE_POTION_DURATION, amplifier, false , false, false);
	}

	// ABSTRACT //
	public abstract void applyEffect(Player player);
	public abstract void clearEffect(Player player);
	public abstract void refreshEffect(Player player);
}
