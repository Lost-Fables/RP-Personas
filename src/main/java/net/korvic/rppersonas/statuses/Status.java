package net.korvic.rppersonas.statuses;

import lombok.Getter;
import lombok.Setter;
import net.korvic.rppersonas.RPPersonas;
import net.korvic.rppersonas.personas.Persona;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

import java.util.ArrayList;
import java.util.List;

public abstract class Status {

	// STATIC //
	protected static final RPPersonas plugin = RPPersonas.get();
	protected static final int DEFAULT_DURATION = 1000*15;

	private static final int INFINITE_POTION_DURATION = 100000;

	@Getter private static List<Status> statuses = new ArrayList<>();

	public static void applyStatus(Status status, Player player, byte severity, long duration) {
		applyStatus(status, plugin.getPersonaHandler().getLoadedPersona(player), severity, duration);
	}
	public static void applyStatus(Status status, Persona pers, byte severity, long duration) {
		pers.addStatus(status, severity, duration);
	}

	public static void clearStatus(String name, Player player) {
		clearStatus(name, plugin.getPersonaHandler().getLoadedPersona(player));
	}
	public static void clearStatus(String name, Persona pers) {
		pers.clearStatus(name);
	}

	// UTIL //
	public static PotionEffect createInfiniteEffect(PotionEffectType type) {
		return createInfiniteEffect(type, 1);
	}
	public static PotionEffect createInfiniteEffect(PotionEffectType type, int amplifier) {
		return new PotionEffect(type, INFINITE_POTION_DURATION, amplifier, false , false, false);
	}

	// INSTANCE //
	@Getter private final String name;
	@Getter private final char icon;
	@Getter private final ChatColor color;
	@Getter private final String description;
	@Getter private final Material material;
	@Getter private final int defaultDuration;

	@Getter private final boolean toggleable;

	public Status(String name, char icon, Material material, ChatColor color, String description, boolean toggleable, int defaultDuration) {
		this.name = name;
		this.icon = icon;
		this.material = material;
		this.color = color;
		this.description = description;
		this.toggleable = toggleable;
		this.defaultDuration = defaultDuration;
	}

	public void registerStatus() {
		if (!statuses.contains(this)) {
			statuses.add(this);
		}
	}

	public void applyTo(Player player, byte severity, long duration) {
		Persona pers = plugin.getPersonaHandler().getLoadedPersona(player);
		applyStatus(this, pers, severity, duration);
	}
	public void clearFrom(Player player) {
		clearStatus(name, player);
	}

	// ABSTRACT //
	public abstract void applyEffect(Player player, byte severity);
	public abstract void clearEffect(Player player);
	public abstract void refreshEffect(Player player, byte severity);
}
