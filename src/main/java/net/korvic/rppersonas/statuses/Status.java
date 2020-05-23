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
	private static final int INFINITE_POTION_DURATION = 100000;

	@Getter private static List<Status> statuses = new ArrayList<>();

	public static void registerStatus(Status status) {
		if (!statuses.contains(status)) {
			statuses.add(status);
		}
	}

	public static void applyStatus(Status status, Player player, int duration) {
		applyStatus(status, plugin.getPersonaHandler().getLoadedPersona(player), duration);
	}
	public static void applyStatus(Status status, Persona pers, int duration) {
		pers.addStatus(status, duration);
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
	@Getter @Setter	private boolean active = true;

	public Status(String name, char icon, Material material, ChatColor color, String description, boolean toggleable, int defaultDuration) {
		this.name = name;
		this.icon = icon;
		this.material = material;
		this.color = color;
		this.description = description;
		this.toggleable = toggleable;
		this.defaultDuration = defaultDuration;
	}

	public void applyTo(Player player, int duration) {
		Persona pers = plugin.getPersonaHandler().getLoadedPersona(player);
		applyStatus(this, pers, duration);
	}
	public void clearFrom(Player player) {
		clearStatus(name, player);
	}

	// ABSTRACT //
	public abstract void applyEffect(Player player, int severity);
	public abstract void clearEffect(Player player);
	public abstract void refreshEffect(Player player, int severity);
}
