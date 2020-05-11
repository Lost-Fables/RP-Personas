package net.korvic.rppersonas.listeners;

import co.lotc.core.bukkit.menu.Menu;
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

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class StatusEventListener implements Listener {

	private RPPersonas plugin;
	public StatusEventListener(RPPersonas plugin) {
		this.plugin = plugin;
	}

	private static List<Player> itemBlocked = new ArrayList<>();
	private static List<Player> interactBlocked = new ArrayList<>();
	private static Map<Player, Location> movementBlocked = new HashMap<>();

	// ADD //
	public static void blockAll(Player player, Location loc) {
		blockItems(player);
		blockInteractions(player);
		blockMovement(player, loc);
	}
	public static void blockItems(Player player) {
		if (!itemBlocked.contains(player)) {
			itemBlocked.add(player);
		}
	}
	public static void blockInteractions(Player player) {
		if (!interactBlocked.contains(player)) {
			interactBlocked.add(player);
		}
	}
	public static void blockMovement(Player player, Location loc) {
		if (!movementBlocked.containsKey(player)) {
			movementBlocked.put(player, loc);
		}
	}

	// REMOVE //
	public static void allowAll(Player player) {
		allowItems(player);
		allowInteractions(player);
		allowMovement(player);
	}
	public static void allowItems(Player player) {
		itemBlocked.remove(player);
	}
	public static void allowInteractions(Player player) {
		interactBlocked.remove(player);
	}
	public static void allowMovement(Player player) {
		movementBlocked.remove(player);
	}

	// QUERY //
	public static boolean isAllowedAny(Player player) {
		return isAllowedItems(player) || isAllowedInteract(player) || isAllowedMovement(player);
	}
	public static boolean isAllowedItems(Player player) {
		return !itemBlocked.contains(player);
	}
	public static boolean isAllowedInteract(Player player) {
		return !interactBlocked.contains(player);
	}
	public static boolean isAllowedMovement(Player player) {
		return !movementBlocked.containsKey(player);
	}

	// ITEMS //
	@EventHandler(ignoreCancelled = true)
	public void openInventory(InventoryOpenEvent e) {
		if (!(e.getInventory().getHolder() instanceof Menu) && itemBlocked.contains((Player) e.getPlayer())) {
			e.setCancelled(true);
		}
	}

	@EventHandler(ignoreCancelled = true)
	public void touchAnyInventory(InventoryInteractEvent e) {
		if (!(e.getInventory().getHolder() instanceof Menu) && itemBlocked.contains((Player) e.getWhoClicked())) {
			e.setCancelled(true);
		}
	}

	@EventHandler(ignoreCancelled = true)
	public void clickAnyInventory(InventoryClickEvent e) {
		if (!(e.getInventory().getHolder() instanceof Menu) && itemBlocked.contains((Player) e.getWhoClicked())) {
			e.setCancelled(true);
		}
	}

	@EventHandler(ignoreCancelled = true)
	public void dropItem(PlayerDropItemEvent e) {
		if (itemBlocked.contains(e.getPlayer())) {
			e.setCancelled(true);
		}
	}

	@EventHandler(ignoreCancelled = true)
	public void swapItem(PlayerSwapHandItemsEvent e) {
		if (itemBlocked.contains(e.getPlayer())) {
			e.setCancelled(true);
		}
	}

	@EventHandler(ignoreCancelled = true)
	public void itemPickUp(EntityPickupItemEvent e) {
		if (e.getEntity() instanceof Player &&
			itemBlocked.contains((Player) e.getEntity())) {
			e.setCancelled(true);
		}
	}

	// INTERACT //
	@EventHandler(ignoreCancelled = true)
	public void breakBlock(BlockBreakEvent e) {
		if (interactBlocked.contains(e.getPlayer())) {
			e.setCancelled(true);
		}
	}

	@EventHandler(ignoreCancelled = true)
	public void placeBlock(BlockPlaceEvent e) {
		if (interactBlocked.contains(e.getPlayer())) {
			e.setCancelled(true);
		}
	}

	@EventHandler(ignoreCancelled = true)
	public void interaction(PlayerInteractEvent e) {
		if (interactBlocked.contains(e.getPlayer())) {
			e.setCancelled(true);
		}
	}

	// MOVEMENT //
	@EventHandler
	public void jump(PlayerJumpEvent e) {
		if (movementBlocked.containsKey(e.getPlayer())) {
			e.setCancelled(true);
			new BukkitRunnable() {
				@Override
				public void run() {
					e.getPlayer().teleportAsync(movementBlocked.get(e.getPlayer()));
				}
			}.runTaskLater(plugin, 10);
		}
	}

}
