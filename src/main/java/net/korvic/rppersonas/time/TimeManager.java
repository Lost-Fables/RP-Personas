package net.korvic.rppersonas.time;

import net.korvic.rppersonas.RPPersonas;
import org.bukkit.GameRule;
import org.bukkit.World;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.ArrayList;
import java.util.List;

public class TimeManager {
	public static final long WEEK_IN_MILLIS = RPPersonas.DAY_IN_MILLIS * 7;
	public static final long MONTH_IN_MILLIS = RPPersonas.DAY_IN_MILLIS * 30;
	public static final long YEAR_IN_MILLIS = RPPersonas.DAY_IN_MILLIS * 365;

	private static List<TimeManager> managers = new ArrayList<>();

	// STATIC CONVERTERS //
	public static long getCurrentTime() {
		return (RPPersonas.BASE_LONG_VALUE + System.currentTimeMillis());
	}

	public static long getMillisFromAge(int ages) {
		return (getCurrentTime() - (ages * 2 * WEEK_IN_MILLIS));
	}

	public static long getMillisFromEra(int eras) {
		return (getCurrentTime() - (eras * 8 * WEEK_IN_MILLIS));
	}

	public static int getRelativeAges(long millis) {
		return (int) (((getCurrentTime() - millis) / WEEK_IN_MILLIS) / 2);
	}

	public static int getRelativeEras(long millis) {
		return (int) (((getCurrentTime() - millis) / WEEK_IN_MILLIS) / 8);
	}

	public static String getRelativeTimeString(long millis) {
		return (getRelativeAges(millis) + " Ages; (" + getRelativeEras(millis) + " Eras)");
	}

	public static TimeManager registerWorld(World world) {
		TimeManager output = new TimeManager(world);
		managers.add(output);
		return output;
	}

	// INSTANCE //
	private World world;
	private long currentTime;
	private BukkitRunnable currentRunnable;

	public TimeManager(World world) {
		world.setGameRule(GameRule.DO_DAYLIGHT_CYCLE, false);

		// TODO - CONFIG SAVING

		this.world = world;
		this.currentTime = world.getTime();
	}
}
