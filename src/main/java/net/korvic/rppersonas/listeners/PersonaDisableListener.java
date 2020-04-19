package net.korvic.rppersonas.listeners;

import com.destroystokyo.paper.Title;
import com.destroystokyo.paper.event.player.PlayerJumpEvent;
import net.korvic.rppersonas.RPPersonas;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.entity.EntityPickupItemEvent;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryInteractEvent;
import org.bukkit.event.inventory.InventoryOpenEvent;
import org.bukkit.event.player.*;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.HashMap;
import java.util.Map;

public class PersonaDisableListener implements Listener {

	private RPPersonas plugin;
	public PersonaDisableListener(RPPersonas plugin) {
		this.plugin = plugin;
	}

	// STATIC //
	private static Map<Player, Location> blindedPlayers = new HashMap<>();

	public static boolean isPlayerEnabled(Player p) {
		return !blindedPlayers.containsKey(p);
	}

	public static void disablePlayer(Player p) {
		disablePlayer(p, p.getLocation());
	}

	public static void disablePlayer(Player p, Location loc) {
		disablePlayer(p, loc, null);
	}

	public static void disablePlayer(Player p, Title title) {
		disablePlayer(p, p.getLocation(), title);
	}

	public static void disablePlayer(Player p, Location loc, Title title) {
		blindedPlayers.put(p, loc);
		blindPlayer(p);
		if (title != null) {
			p.sendTitle(title);
		}
	}

	public static void enableAll() {
		for (Player p : blindedPlayers.keySet()) {
			enablePlayer(p);
		}
	}

	public static void enablePlayer(Player p) {
		p.hideTitle();
		clearBlindness(p);
		blindedPlayers.remove(p);
	}

	// EFFECTS //
	private static void blindPlayer(Player p) {
		p.addPotionEffect(new PotionEffect(PotionEffectType.SLOW, 100000, 255, false, false, false));
		p.addPotionEffect(new PotionEffect(PotionEffectType.BLINDNESS, 100000, 255, false, false, false));
		p.addPotionEffect(new PotionEffect(PotionEffectType.INVISIBILITY, 100000, 255, false, false, false));
		p.setInvulnerable(true);
	}

	private static void clearBlindness(Player p) {
		p.removePotionEffect(PotionEffectType.SLOW);
		p.removePotionEffect(PotionEffectType.BLINDNESS);
		p.removePotionEffect(PotionEffectType.INVISIBILITY);
		p.setInvulnerable(false);
	}

	// Events to listen to.
	@EventHandler(ignoreCancelled = true)
	public void openInventory(InventoryOpenEvent e) {
		if (blindedPlayers.containsKey((Player) e.getPlayer())) {
			e.setCancelled(true);
		}
	}

	@EventHandler(ignoreCancelled = true)
	public void touchAnyInventory(InventoryInteractEvent e) {
		if (blindedPlayers.containsKey((Player) e.getWhoClicked())) {
			e.setCancelled(true);
		}
	}

	@EventHandler(ignoreCancelled = true)
	public void clickAnyInventory(InventoryClickEvent e) {
		if (blindedPlayers.containsKey((Player) e.getWhoClicked())) {
			e.setCancelled(true);
		}
	}

	@EventHandler(ignoreCancelled = true)
	public void dropItem(PlayerDropItemEvent e) {
		if (blindedPlayers.containsKey(e.getPlayer())) {
			e.setCancelled(true);
		}
	}

	@EventHandler(ignoreCancelled = true)
	public void swapItem(PlayerSwapHandItemsEvent e) {
		if (blindedPlayers.containsKey(e.getPlayer())) {
			e.setCancelled(true);
		}
	}

	@EventHandler
	public void jump(PlayerJumpEvent e) {
		if (blindedPlayers.containsKey(e.getPlayer())) {
			e.setCancelled(true);
			new BukkitRunnable() {
				@Override
				public void run() {
					e.getPlayer().teleportAsync(blindedPlayers.get(e.getPlayer()));
				}
			}.runTaskLater(plugin, 10);
		}
	}

	@EventHandler(ignoreCancelled = true)
	public void itemPickUp(EntityPickupItemEvent e) {
		if (e.getEntity() instanceof Player &&
			blindedPlayers.containsKey((Player) e.getEntity())) {
			e.setCancelled(true);
		}
	}

	@EventHandler(ignoreCancelled = true)
	public void breakBlock(BlockBreakEvent e) {
		if (blindedPlayers.containsKey(e.getPlayer())) {
			e.setCancelled(true);
		}
	}

	@EventHandler(ignoreCancelled = true)
	public void placeBlock(BlockPlaceEvent e) {
		if (blindedPlayers.containsKey(e.getPlayer())) {
			e.setCancelled(true);
		}
	}

	@EventHandler(ignoreCancelled = true)
	public void interaction(PlayerInteractEvent e) {
		if (blindedPlayers.containsKey(e.getPlayer())) {
			e.setCancelled(true);
		}
	}

}
