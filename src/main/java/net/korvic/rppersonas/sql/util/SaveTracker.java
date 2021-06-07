package net.korvic.rppersonas.sql.util;

import net.korvic.rppersonas.RPPersonas;
import org.bukkit.scheduler.BukkitRunnable;

import java.sql.PreparedStatement;

public class SaveTracker {

	private static RPPersonas plugin;
	private static int rows = 0;
	private static long millis = 0;

	public static void setPlugin(RPPersonas plugin) {
		SaveTracker.plugin = plugin;

		BukkitRunnable runnable = new BukkitRunnable() {
			@Override
			public void run() {
				SaveTracker.printSavedRows();
			}
		};
		runnable.runTaskTimerAsynchronously(plugin, 0, (5 * 60 * 20));
	}

	public static void printSavedRows() {
		int rowsCopy = rows;
		rows = 0;
		long millisCopy = millis;
		millis = 0;

		plugin.getLogger().info(RPPersonas.PRIMARY_DARK + "Saved " + RPPersonas.SECONDARY_LIGHT + rowsCopy + " row(s) " + RPPersonas.PRIMARY_DARK + "in " + RPPersonas.SECONDARY_LIGHT + millisCopy + "ms");
	}

	public static void executeWithTracker(PreparedStatement statement) {
		long startMillis = System.currentTimeMillis();
		try {
			statement.executeUpdate();
			millis += (System.currentTimeMillis() - startMillis);
			rows++;
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
}
