package net.korvic.rppersonas.players.listeners;

import net.korvic.rppersonas.RPPersonas;
import net.korvic.rppersonas.players.Persona;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerInteractEntityEvent;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class InspectListener implements Listener {

	private static final int COOLDOWN = 20; // Time in ticks between inspections.
	private RPPersonas plugin;
	private Map<Player, List<Player>> recentInteractions = new HashMap<>();

	public InspectListener(RPPersonas plugin) {
		this.plugin = plugin;
	}

	@EventHandler
	public void onCrouchRightClick(PlayerInteractEntityEvent e) {
		Player inspector = e.getPlayer();
		if (e.getRightClicked() instanceof Player && inspector.isSneaking()) {
			Player target = (Player) e.getRightClicked();
			if (!(recentInteractions.containsKey(inspector) && recentInteractions.get(inspector).contains(target))) {
				Persona persona = Persona.getPersona(target);
				if (persona != null) {
					inspector.sendMessage(RPPersonas.SECONDARY_DARK + target.getName() + "'s active persona.\n" + persona.getFormattedBasicInfo());
				} else {
					inspector.sendMessage(RPPersonas.PRIMARY_DARK + "Unable to find loaded persona for the given player.");
				}

				if (recentInteractions.containsKey(inspector)) {
					recentInteractions.get(inspector).add(target);
				} else {
					List<Player> newList = new ArrayList<>();
					newList.add(target);
					recentInteractions.put(inspector, newList);
				}

				removeFromRecent(inspector, target);
			}
		}
	}

	private void removeFromRecent(Player inspector, Player target) {
		new BukkitRunnable() {
			@Override
			public void run() {
				if (recentInteractions.containsKey(inspector)) {
					recentInteractions.get(inspector).remove(target);
				}
			}
		}.runTaskLater(plugin, COOLDOWN);
	}

}
