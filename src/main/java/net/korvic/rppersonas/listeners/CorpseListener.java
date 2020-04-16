package net.korvic.rppersonas.listeners;

import co.lotc.core.bukkit.util.ItemUtil;
import net.korvic.rppersonas.RPPersonas;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEvent;

public class CorpseListener implements Listener {

	private static final String CORPSE_KEY = "rppersonas_corpse";
	private RPPersonas plugin;

	public CorpseListener(RPPersonas plugin) {
		this.plugin = plugin;
	}

	@EventHandler
	public void onAltarInteract(PlayerInteractEvent e) {
		if (ItemUtil.hasCustomTag(e.getItem(), CORPSE_KEY)) {
			// Corpse interaction.
		} else {
			e.setCancelled(true);
			if (RPPersonas.DEBUGGING && e.getAction().equals(Action.RIGHT_CLICK_BLOCK)) {
				if (plugin.getAltarHandler().getAltarOfBlock(e.getClickedBlock()) != null) {
					e.getPlayer().sendMessage(RPPersonas.PRIMARY_DARK + "Yes, this is an altar.");
				} else {
					e.getPlayer().sendMessage(RPPersonas.PRIMARY_DARK + "Negative, Ghostrider.");
				}
			}
		}
	}
}
