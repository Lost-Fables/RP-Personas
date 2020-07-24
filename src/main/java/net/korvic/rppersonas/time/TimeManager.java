package net.korvic.rppersonas.time;

import lombok.Getter;
import lombok.Setter;
import net.korvic.rppersonas.RPPersonas;
import org.bukkit.Bukkit;
import org.bukkit.GameRule;
import org.bukkit.World;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.*;

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

	// TIME MANAGER //
	public static TimeManager registerWorld(World world, boolean save) {
		TimeManager output = new TimeManager(world, getRelativeAges(RPPersonas.ANOMA_DATE.getTime()), 240, Season.SUMMER, save);
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

	public static void updateSeasons() {
		int ages = getRelativeAges(RPPersonas.ANOMA_DATE.getTime());
		for (TimeManager mngr : managers.values()) {
			boolean changed = false;
			while (mngr.lastKnownAges < ages) {
				changed = true;
				mngr.setSeason(mngr.getSeason().getNext(), true);
				mngr.setLastKnownAges(mngr.getLastKnownAges() + 1);
			}

			if (changed) {
				String message = mngr.getSeason().getChangeMessage();
				for (Player p : Bukkit.getOnlinePlayers()) {
					if (mngr.getWorlds().contains(p.getWorld())) {
						p.sendMessage(message);
					}
				}
			}

		}
	}

	//////////////
	// INSTANCE //
	//////////////
	@Getter private List<World> worlds = new ArrayList<>();
	@Getter private int lastKnownAges;
	@Getter private int timeScale;
	@Getter private Season season;
	@Setter @Getter private TimeState currentState;

	private BukkitRunnable currentRunnable;

	public TimeManager(World world, int lastKnownAges, int timeScale, Season season, boolean save) {
		if (world != null) {
			this.lastKnownAges = lastKnownAges;
			this.timeScale = timeScale;
			this.season = season;

			addWorld(world);

			if (save) {
				RPPersonas.get().updateConfigForWorld(world.getName(), season, this.timeScale, null);
			}

			this.currentState = TimeState.getState((int) world.getTime());
			startRunnable();
		}
	}

	public boolean hasWorld(World world) {
		for (World synced : worlds) {
			if (world.equals(synced)) {
				return true;
			}
		}
		return false;
	}

	private void addWorld(World world) {
		world.setGameRule(GameRule.DO_DAYLIGHT_CYCLE, false);
		if (!hasWorld(world)) {
			worlds.add(world);
		}
		if (!worlds.get(0).equals(world)) {
			world.setTime(worlds.get(0).getTime());
		}
	}

	public void addSyncedWorld(World world, boolean save) {
		if (world != null) {
			addWorld(world);

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
	}

	public void setLastKnownAges(int ages) {
		this.lastKnownAges = ages;
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

	public void setSeason(String seasonName, boolean save) {
		setSeason(Season.getByName(seasonName), save);
	}
	public void setSeason(Season season, boolean save) {
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
		if (season.equals(Season.SUMMER)) {
			dayPercentage = (float) currentState.getSummerPercent();
		} else if (season.equals(Season.AUTUMN)) {
			dayPercentage = (float) currentState.getAutumnPercent();
		} else if (season.equals(Season.WINTER)) {
			dayPercentage = (float) currentState.getWinterPercent();
		} else if (season.equals(Season.SPRING)) {
			dayPercentage = (float) currentState.getSpringPercent();
		}

		dayPercentage /= 100;

		int delay = Math.round((timeScale * 1200 * dayPercentage) / TimeState.getTicksToNextState(currentState));
		currentRunnable.runTaskLater(RPPersonas.get(), delay);
	}
}
