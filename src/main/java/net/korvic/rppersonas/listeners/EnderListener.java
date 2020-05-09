package net.korvic.rppersonas.listeners;

import net.korvic.rppersonas.RPPersonas;
import net.korvic.rppersonas.personas.Persona;
import net.korvic.rppersonas.personas.PersonaEnderHolder;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.Inventory;

public class EnderListener implements Listener {

	private RPPersonas plugin;

	public EnderListener(RPPersonas plugin) {
		this.plugin = plugin;
	}

	@EventHandler
	public void openEnderEvent(PlayerInteractEvent e) {
		Block block = e.getClickedBlock();
		if (e.getAction().equals(Action.RIGHT_CLICK_BLOCK) &&
			block != null && block.getType().equals(Material.ENDER_CHEST)) {

			Player p = e.getPlayer();
			Persona pers = plugin.getPersonaHandler().getLoadedPersona(p);
			if (pers != null) {
				Inventory inv = pers.getEnderchest();
				if (inv != null) {
					e.setCancelled(true);
					p.openInventory(inv);
					p.getLocation().getWorld().playSound(p.getLocation(), Sound.BLOCK_ENDER_CHEST_OPEN, 0.4f, 1.0f);
				}
			}
		}
	}

	@EventHandler
	public void closeEnderEvent(InventoryCloseEvent e) {
		if (e.getInventory().getHolder() instanceof PersonaEnderHolder) {
			Player p = (Player) e.getPlayer();
			p.getLocation().getWorld().playSound(p.getLocation(), Sound.BLOCK_ENDER_CHEST_CLOSE, 0.4f, 1.0f);
		}
	}

}
