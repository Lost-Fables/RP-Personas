package net.korvic.rppersonas;

import org.bukkit.entity.Player;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.scheduler.BukkitScheduler;

import java.util.ArrayList;
import java.util.List;

public class UnregisteredHandler {

	private static final String REGISTER_REMINDER = RPPersonas.PREFIX + "Please register your forum account with " + RPPersonas.ALT_COLOR + "/account link Forum_ID" + RPPersonas.PREFIX + "!\n" +
													RPPersonas.PREFIX + "If you need help, please use " + RPPersonas.ALT_COLOR + "/request new" + RPPersonas.PREFIX + " and someone will swing by to give you a hand.";
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
		p.addPotionEffect(new PotionEffect(PotionEffectType.SLOW, 100000, 10));
		p.addPotionEffect(new PotionEffect(PotionEffectType.WEAKNESS, 100000, 10));
		ping(p);
	}

	public void remove(Player p) {
		players.remove(p);
		for (PotionEffect pe : p.getActivePotionEffects()) {
			p.removePotionEffect(pe.getType());
		}
	}

	public void pingUnregistered() {
		for (Player p : players) {
			if (p.isOnline()) {
				ping(p);
			} else {
				players.remove(p);
			}
		}
	}

	private void ping(Player p) {
		p.sendMessage(REGISTER_REMINDER);
	}

}
