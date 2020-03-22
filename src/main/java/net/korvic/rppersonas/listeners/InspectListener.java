package net.korvic.rppersonas.listeners;

import net.korvic.rppersonas.RPPersonas;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerInteractEntityEvent;

public class InspectListener implements Listener {

	private RPPersonas plugin;

	public InspectListener(RPPersonas plugin) {
		this.plugin = plugin;
	}

	@EventHandler
	public void onCrouchRightClick(PlayerInteractEntityEvent e) {
		if (e.getRightClicked() instanceof Player && e.getPlayer().isSneaking()) {
			Player player = (Player) e.getRightClicked();
			e.getPlayer().sendMessage(plugin.getPersonaHandler().getPersonaInfo(player));
		}
	}

}
