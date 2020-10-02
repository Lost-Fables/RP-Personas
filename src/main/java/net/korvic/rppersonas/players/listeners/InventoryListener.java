package net.korvic.rppersonas.players.listeners;

import net.korvic.rppersonas.RPPersonas;
import net.korvic.rppersonas.players.Persona;
import net.korvic.rppersonas.players.personas.OldPersona;
import net.korvic.rppersonas.players.personas.PersonaEnderHolder;
import net.korvic.rppersonas.players.personas.PersonaInventoryHolder;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.event.inventory.InventoryOpenEvent;
import org.bukkit.event.inventory.InventoryType;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.PlayerInventory;

public class InventoryListener implements Listener {

	private RPPersonas plugin;

	public InventoryListener(RPPersonas plugin) {
		this.plugin = plugin;
	}

	@EventHandler
	public void openEnderEvent(InventoryOpenEvent e) {
		if (e.getInventory().getType().equals(InventoryType.ENDER_CHEST) && e.getInventory().getHolder() == null) {
			Player player = (Player) e.getPlayer();
			Persona persona = Persona.getPersona(player);
			if (persona != null) {
				Inventory inv = persona.getEnderChest();
				if (inv != null) {
					e.setCancelled(true);
					player.openInventory(inv);
					player.getLocation().getWorld().playSound(player.getLocation(), Sound.BLOCK_ENDER_CHEST_OPEN, 0.4f, 1.0f);
				}
			}
		}
	}

	@EventHandler
	public void closeInventoryEvent(InventoryCloseEvent e) {
		if (e.getInventory().getHolder() instanceof PersonaEnderHolder) {
			Player player = (Player) e.getPlayer();
			player.getLocation().getWorld().playSound(player.getLocation(), Sound.BLOCK_ENDER_CHEST_CLOSE, 0.4f, 1.0f);
		} else if (e.getInventory() instanceof PlayerInventory && e.getInventory().getHolder() instanceof PersonaInventoryHolder) {
			Persona persona = Persona.getPersona(((PersonaInventoryHolder) e.getInventory().getHolder()).getPersonaID());
			persona.setSavedInventory((PlayerInventory) e.getInventory());
		}
	}

}
