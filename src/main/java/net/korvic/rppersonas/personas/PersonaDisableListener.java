package net.korvic.rppersonas.personas;

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

	// Effects & Public Methods
	private static Map<Player, Location> blindedPlayers = new HashMap<>();

	public static boolean isPlayerDisabled(Player p) {
		return blindedPlayers.containsKey(p);
	}

	public static void disablePlayer(Player p) {
		disablePlayer(p, p.getLocation());
	}

	public static void disablePlayer(Player p, Location loc) {
		blindedPlayers.put(p, loc);
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
		p.setInvulnerable(true);
	}

	private static void clearBlindness(Player p) {
		p.removePotionEffect(PotionEffectType.SLOW);
		p.removePotionEffect(PotionEffectType.BLINDNESS);
		p.removePotionEffect(PotionEffectType.INVISIBILITY);
		p.setInvulnerable(false);
	}

	// Events to listen to.
	@EventHandler
	public void openInventory(InventoryOpenEvent e) {
		if (blindedPlayers.containsKey((Player) e.getPlayer())) {
			e.setCancelled(true);
		}
	}

	@EventHandler
	public void touchAnyInventory(InventoryInteractEvent e) {
		if (blindedPlayers.containsKey((Player) e.getWhoClicked())) {
			e.setCancelled(true);
		}
	}

	@EventHandler
	public void clickAnyInventory(InventoryClickEvent e) {
		if (blindedPlayers.containsKey((Player) e.getWhoClicked())) {
			e.setCancelled(true);
		}
	}

	@EventHandler
	public void dropItem(PlayerDropItemEvent e) {
		if (blindedPlayers.containsKey(e.getPlayer())) {
			e.setCancelled(true);
		}
	}

	@EventHandler
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

	@EventHandler
	public void itemPickUp(EntityPickupItemEvent e) {
		if (e.getEntity() instanceof Player &&
			blindedPlayers.containsKey((Player) e.getEntity())) {
			e.setCancelled(true);
		}
	}

	@EventHandler
	public void breakBlock(BlockBreakEvent e) {
		if (blindedPlayers.containsKey(e.getPlayer())) {
			e.setCancelled(true);
		}
	}

	@EventHandler
	public void placeBlock(BlockPlaceEvent e) {
		if (blindedPlayers.containsKey(e.getPlayer())) {
			e.setCancelled(true);
		}
	}

	@EventHandler
	public void interaction(PlayerInteractEvent e) {
		if (blindedPlayers.containsKey(e.getPlayer())) {
			e.setCancelled(true);
		}
	}

}
