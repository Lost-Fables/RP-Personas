package net.korvic.rppersonas.listeners;

import net.korvic.rppersonas.RPPersonas;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
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

			e.setCancelled(true);
			Player p = e.getPlayer();

			Inventory inv = plugin.getPersonaHandler().getLoadedPersona(p).getEnderchest();
			if (inv != null) {
				p.openInventory(inv);
				p.getLocation().getWorld().playSound(p.getLocation(), Sound.BLOCK_ENDER_CHEST_OPEN, 0.5f, 1.0f);
			}
		}
	}

}
