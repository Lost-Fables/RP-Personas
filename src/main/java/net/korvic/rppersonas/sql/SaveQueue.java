package net.korvic.rppersonas.sql;

import net.korvic.rppersonas.RPPersonas;
import org.bukkit.scheduler.BukkitRunnable;

import java.sql.PreparedStatement;
import java.util.ArrayList;
import java.util.List;

public class SaveQueue {

	private static RPPersonas plugin;
	private static List<PreparedStatement> queue = new ArrayList<>();

	private final int TICKS;
	private final int AMOUNT;
	private final int PERCENT;

	private BukkitRunnable runnable;

	public SaveQueue(RPPersonas plugin, int ticks, int amount, int percent) {
		SaveQueue.plugin = plugin;
		TICKS = ticks;
		AMOUNT = amount;
		PERCENT = percent;

		runnable = new BukkitRunnable() {
			@Override
			public void run() {
				runQueue();
			}
		};

		runnable.runTaskTimerAsynchronously(plugin, 0, TICKS);
	}

	public void addToQueue(PreparedStatement statement) {
		queue.add(statement);
	}

	public void stopSaving() {
		if (!runnable.isCancelled()) {
			runnable.cancel();
		}
	}

	public void startSaving() {
		if (runnable.isCancelled()) {
			runnable.runTaskTimerAsynchronously(plugin, 0, TICKS);
		}
	}

	private void runQueue() {
		if (queue.size() > 0) {
			long startMillis = System.currentTimeMillis();
			int amountThisRun = AMOUNT;

			if (queue.size() < amountThisRun) {
				amountThisRun = queue.size();
			} else if (PERCENT > 0 && queue.size() * ( ((float) PERCENT)/100F ) > amountThisRun) {
				amountThisRun = (int) (queue.size() * ( ((float) PERCENT)/100F ));
			}

			for (int i = 0; i < amountThisRun; i++) {
				if (queue.size() > 0) {
					PreparedStatement ps = queue.get(0);
					try {
						queue.remove(ps);
						if (ps != null) {
							ps.executeUpdate();
						}
					} catch (Exception e) {
						if (RPPersonas.DEBUGGING) {
							e.printStackTrace();
						}
					} finally {
						try {
							if (ps != null) {
								ps.close();
							}
						} catch (Exception e) {
							if (RPPersonas.DEBUGGING) {
								e.printStackTrace();
							}
						}
					}
				}
			}

			plugin.getLogger().info(RPPersonas.PRIMARY_COLOR + "Saved " + RPPersonas.SECONDARY_COLOR + amountThisRun + " row(s) " + RPPersonas.PRIMARY_COLOR + "in " + RPPersonas.SECONDARY_COLOR + (System.currentTimeMillis() - startMillis) + "ms");
		}
	}

	public void completeAllSaves() {
		runnable.cancel();
		long startMillis = System.currentTimeMillis();
		for (PreparedStatement ps : queue) {
			try {
				if (ps != null) {
					ps.executeUpdate();
				}
			} catch (Exception e) {
				if (RPPersonas.DEBUGGING) {
					e.printStackTrace();
				}
			} finally {
				try {
					if (ps != null) {
						ps.close();
					}
				} catch (Exception e) {
					if (RPPersonas.DEBUGGING) {
						e.printStackTrace();
					}
				}
			}
		}

		plugin.getLogger().info(RPPersonas.PRIMARY_COLOR + "Saved " + RPPersonas.SECONDARY_COLOR + queue.size() + " row(s) " + RPPersonas.PRIMARY_COLOR + "in " + RPPersonas.SECONDARY_COLOR + (System.currentTimeMillis() - startMillis) + "ms");
	}
}
