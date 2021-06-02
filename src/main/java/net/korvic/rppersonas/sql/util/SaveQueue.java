package net.korvic.rppersonas.sql.util;

import net.korvic.rppersonas.RPPersonas;
import net.korvic.rppersonas.listeners.JoinQuitListener;
import org.bukkit.scheduler.BukkitRunnable;

import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class SaveQueue {

	private static RPPersonas plugin;

	public SaveQueue(RPPersonas plugin, int ticks, int amount, int percent) {
		SaveQueue.plugin = plugin;
	}

	public void executeWithNotification(PreparedStatement statement) {
		long startMillis = System.currentTimeMillis();
		try {
			statement.executeUpdate();
			plugin.getLogger().info(RPPersonas.PRIMARY_DARK + "Saved " + RPPersonas.SECONDARY_LIGHT + " row(s) " + RPPersonas.PRIMARY_DARK + "in " + RPPersonas.SECONDARY_LIGHT + (System.currentTimeMillis() - startMillis) + "ms");
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
}
