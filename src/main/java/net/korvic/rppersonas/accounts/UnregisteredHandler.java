package net.korvic.rppersonas.accounts;

import net.korvic.rppersonas.RPPersonas;
import net.korvic.rppersonas.statuses.DisabledStatus;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.ArrayList;
import java.util.List;

public class UnregisteredHandler {

	private static final String REGISTER_REMINDER = RPPersonas.PRIMARY_DARK + "Your account has not been accepted yet. If you haven't put through a whitelist application or use /account altlink, be sure to do so on the forums!";
	private RPPersonas plugin;
	private BukkitRunnable runnable;

	List<Player> players = new ArrayList<>();

	public UnregisteredHandler(RPPersonas plugin) {
		this.plugin = plugin;
		this.runnable = new BukkitRunnable() {
			@Override
			public void run() {
				pingUnregistered();
			}
		};
		runnable.runTaskTimerAsynchronously(plugin, 0, 300 * 20);
	}

	public void add(Player p) {
		players.add(p);
		new DisabledStatus(null).applyEffect(p, (byte) 1);
		ping(p);
	}

	public void remove(Player p) {
		players.remove(p);
		new DisabledStatus(null).clearEffect(p);
	}

	public void pingUnregistered() {
		List<Player> toRemove = new ArrayList<>();
		for (Player p : players) {
			if (p.isOnline()) {
				ping(p);
			} else {
				toRemove.add(p);
			}
		}

		for (Player p : toRemove) {
			players.remove(p);
		}
	}

	private void ping(Player p) {
		p.sendMessage(REGISTER_REMINDER);
	}

}
