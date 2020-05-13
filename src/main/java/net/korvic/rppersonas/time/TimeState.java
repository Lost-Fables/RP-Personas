package net.korvic.rppersonas.time;

import lombok.Getter;

@Getter
public enum TimeState {

	NOON(       6000,  25, 25, 25, 25), // Noon-Sunset
	ZENITH_FALL(12786, 25, 25, 25, 25), // Sunset-Midnight
	MIDNIGHT(   18000, 25, 25, 25, 25), // Midnight-Sunrise
	ZENITH_RISE(23215, 25, 25, 25, 25); // Sunrise-Noon

	public static final int ONE_DAY_TICKS = 24000;

	@Getter private int minecraftTime;

	@Getter private int summerPercent;
	@Getter private int autumnPercent;
	@Getter private int winterPercent;
	@Getter private int springPercent;

	private TimeState(int minecraftTime, int summerPercent, int autumnPercent, int winterPercent, int springPercent) {
		this.minecraftTime = minecraftTime;
		this.summerPercent = summerPercent;
		this.autumnPercent = autumnPercent;
		this.winterPercent = winterPercent;
		this.springPercent = springPercent;
	}

	public static TimeState getState(int time) {
		TimeState output = ZENITH_RISE;
		for (TimeState value : values()) {
			if (value.getMinecraftTime() < time) {
				output = value;
			} else {
				break;
			}
		}
		return output;
	}

	public static int getTicksToNextState(TimeState state) {
		TimeState nextState = getNext(state);
		int output = nextState.getMinecraftTime() - state.getMinecraftTime();
		if (output < 0) {
			output = (ONE_DAY_TICKS - nextState.getMinecraftTime()) + state.getMinecraftTime();
		}
		return output;
	}

	public static TimeState getNext(TimeState state) {
		for (TimeState value : values()) {
			if (value.getMinecraftTime() > state.getMinecraftTime()) {
				return value;
			}
		}
		return NOON;
	}

}
