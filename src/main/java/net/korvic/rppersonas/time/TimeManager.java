package net.korvic.rppersonas.time;

import lombok.Getter;
import lombok.Setter;
import net.korvic.rppersonas.RPPersonas;
import org.bukkit.GameRule;
import org.bukkit.World;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class TimeManager {
	public static final long WEEK_IN_MILLIS = RPPersonas.DAY_IN_MILLIS * 7;
	public static final long MONTH_IN_MILLIS = RPPersonas.DAY_IN_MILLIS * 30;
	public static final long YEAR_IN_MILLIS = RPPersonas.DAY_IN_MILLIS * 365;

	private static Map<World, TimeManager> managers = new HashMap<>();

	// STATIC CONVERTERS //
	public static long getCurrentMillisTime() {
		return (RPPersonas.BASE_LONG_VALUE + System.currentTimeMillis());
	}

	public static long getMillisFromAge(int ages) {
		return (getCurrentMillisTime() - (ages * 2 * WEEK_IN_MILLIS));
	}

	public static long getMillisFromEra(int eras) {
		return (getCurrentMillisTime() - (eras * 8 * WEEK_IN_MILLIS));
	}

	public static int getRelativeAges(long millis) {
		return (int) (((getCurrentMillisTime() - millis) / WEEK_IN_MILLIS) / 2);
	}

	public static int getRelativeEras(long millis) {
		return (int) (((getCurrentMillisTime() - millis) / WEEK_IN_MILLIS) / 8);
	}

	public static String getRelativeTimeString(long millis) {
		return (getRelativeAges(millis) + " Ages; (" + getRelativeEras(millis) + " Eras)");
	}

	public static TimeManager registerWorld(World world, boolean save) {
		TimeManager output = new TimeManager(world, "Summer", 240, save);
		managers.put(world, output);
		return output;
	}

	public static TimeManager getManagerOfWorld(World world) {
		return managers.get(world);
	}

	public static boolean syncWorldTimes(World toSyncTo, World toBeSynced) {
		if (toSyncTo.equals(toBeSynced)) {
			return false;
		}
		TimeManager manager = getManagerOfWorld(toSyncTo);
		if (manager == null) {
			return false;
		}
		manager.addSyncedWorld(toBeSynced, true);
		managers.put(toBeSynced, manager);
		return true;
	}

	public static void unregisterWorld(World world, boolean save) {
		TimeManager manager = getManagerOfWorld(world);
		if (manager != null) {
			manager.unregister(world, save);
		}
	}

	//////////////
	// INSTANCE //
	//////////////
	@Getter private List<World> worlds = new ArrayList<>();
	@Getter private int timeScale;
	@Getter private String season;
	@Setter @Getter private TimeState currentState;

	private BukkitRunnable currentRunnable;

	public TimeManager(World world, String season, int timeScale, boolean save) {
		world.setGameRule(GameRule.DO_DAYLIGHT_CYCLE, false);
		this.timeScale = timeScale;
		this.season = season;

		if (save) {
			RPPersonas.get().updateConfigForWorld(world.getName(), season, this.timeScale, null);
		}

		worlds.add(world);
		this.currentState = TimeState.getState((int) world.getTime());
		startRunnable();
	}

	public void addSyncedWorld(World world, boolean save) {
		worlds.add(world);

		if (save) {
			List<String> syncedWorldNames = new ArrayList<>();
			for (World syncedWorld : worlds) {
				if (!syncedWorld.equals(worlds.get(0))) {
					syncedWorldNames.add(syncedWorld.getName());
				}
			}
			RPPersonas.get().updateConfigForWorld(worlds.get(0).getName(), null, 0, syncedWorldNames);
		}
	}

	public void setTime(int time) {
		for (World world : worlds) {
			world.setTime(time);
		}
		currentState = TimeState.getState(time);
	}

	public void setTimeScale(int timeScale, boolean save) {
		this.timeScale = timeScale;
		if (save) {
			RPPersonas.get().updateConfigForWorld(worlds.get(0).getName(), null, this.timeScale, null);
		}
	}

	public void setSeason(String season, boolean save) {
		this.season = season;
		if (save) {
			RPPersonas.get().updateConfigForWorld(worlds.get(0).getName(), season, 0, null);
		}
	}

	protected void unregister(World world, boolean save) {
		worlds.remove(world);
		if (worlds.size() <= 0) {
			stopRunnable();
		}

		world.setGameRule(GameRule.DO_DAYLIGHT_CYCLE, true);
		managers.remove(world);

		if (save) {
			RPPersonas.get().deleteConfigForWorld(world.getName());
		}
	}

	protected void stopRunnable() {
		if (!currentRunnable.isCancelled()) {
			currentRunnable.cancel();
		}
		new BukkitRunnable() {
			@Override
			public void run() {
				if (!currentRunnable.isCancelled()) {
					currentRunnable.cancel();
				}
			}
		}.runTaskLaterAsynchronously(RPPersonas.get(), 20);
	}

	protected void startRunnable() {
		TimeManager manager = this;
		this.currentRunnable = new BukkitRunnable() {
			private TimeManager timeManager = manager;

			@Override
			public void run() {
				for (World world : timeManager.getWorlds()) {
					int newTime = (world.getTime() + 1 >= TimeState.ONE_DAY_TICKS) ? 0 : (int) (world.getTime() + 1);
					world.setTime(newTime);

					TimeState nextState = TimeState.getNext(timeManager.getCurrentState());
					if (world.getTime() >= nextState.getMinecraftTime() || world.getTime() == 0) {
						timeManager.setCurrentState(nextState);
					}
				}

				timeManager.startRunnable();
			}
		};

		float dayPercentage = 25;
		if (season.equalsIgnoreCase("Summer")) {
			dayPercentage = (float) currentState.getSummerPercent();
		} else if (season.equalsIgnoreCase("Autumn")) {
			dayPercentage = (float) currentState.getAutumnPercent();
		} else if (season.equalsIgnoreCase("Winter")) {
			dayPercentage = (float) currentState.getWinterPercent();
		} else if (season.equalsIgnoreCase("Spring")) {
			dayPercentage = (float) currentState.getSpringPercent();
		}

		dayPercentage /= 100;

		int delay = Math.round((timeScale * 1200 * dayPercentage) / TimeState.getTicksToNextState(currentState));
		currentRunnable.runTaskLater(RPPersonas.get(), delay);
	}
}
