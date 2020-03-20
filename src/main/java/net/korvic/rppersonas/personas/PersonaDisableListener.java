package net.korvic.rppersonas.personas;

import net.korvic.rppersonas.RPPersonas;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryInteractEvent;
import org.bukkit.event.inventory.InventoryOpenEvent;
import org.bukkit.event.player.PlayerDropItemEvent;
import org.bukkit.event.player.PlayerSwapHandItemsEvent;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

import java.util.ArrayList;
import java.util.List;

public class PersonaDisableListener implements Listener {

	private RPPersonas plugin;

	public PersonaDisableListener(RPPersonas plugin) {
		this.plugin = plugin;
	}

	// Effects & Public Methods
	private static List<Player> blindedPlayers = new ArrayList<>();

	public static void disablePlayer(Player p) {
		blindedPlayers.add(p);
		blindPlayer(p);
	}

	public static void enablePlayer(Player p) {
		clearBlindness(p);
		blindedPlayers.remove(p);
	}

	// EFFECTS //
	private static void blindPlayer(Player p) {
		p.addPotionEffect(new PotionEffect(PotionEffectType.SLOW, 100000, 255, false, false, false));
		p.addPotionEffect(new PotionEffect(PotionEffectType.BLINDNESS, 100000, 255, false, false, false));
		p.addPotionEffect(new PotionEffect(PotionEffectType.INVISIBILITY, 100000, 255, false, false, false));
	}

	private static void clearBlindness(Player p) {
		p.removePotionEffect(PotionEffectType.SLOW);
		p.removePotionEffect(PotionEffectType.BLINDNESS);
		p.removePotionEffect(PotionEffectType.INVISIBILITY);
	}

	// Events to listen to.
	@EventHandler
	public void openInventory(InventoryOpenEvent e) {
		if (blindedPlayers.contains((Player) e.getPlayer())) {
			e.setCancelled(true);
		}
	}

	@EventHandler
	public void touchAnyInventory(InventoryInteractEvent e) {
		if (blindedPlayers.contains((Player) e.getWhoClicked())) {
			e.setCancelled(true);
		}
	}

	@EventHandler
	public void dropItem(PlayerDropItemEvent e) {
		if (blindedPlayers.contains(e.getPlayer())) {
			e.setCancelled(false);
		}
	}

	@EventHandler
	public void swapItem(PlayerSwapHandItemsEvent e) {
		if (blindedPlayers.contains(e.getPlayer())) {
			e.setCancelled(true);
		}
	}
}
