package net.korvic.rppersonas.time;

import lombok.Getter;

@Getter
public enum TimeState {

	NOON(       6000,  25, 25, 25, 25),
	ZENITH_FALL(12786, 25, 25, 25, 25),
	MIDNIGHT(   18000, 25, 25, 25, 25),
	ZENITH_RISE(23215, 25, 25, 25, 25);

	private static long cycleTicks = 288000;
	private static long maxTicks = 24000;

	@Getter private long minecraftTicks;

	@Getter private int summerPrecent;
	@Getter private int autumnPercent;
	@Getter private int winterPercent;
	@Getter private int springPercent;

	private TimeState(long minecraftTicks, int summerPrecent, int autumnPercent, int winterPercent, int springPercent) {
		this.minecraftTicks = minecraftTicks;
		this.summerPrecent = summerPrecent;
		this.autumnPercent = autumnPercent;
		this.winterPercent = winterPercent;
		this.springPercent = springPercent;
	}

	public static long getTicksToNextState(TimeState state) {
		TimeState nextState = getNext(state);
		long output = nextState.getMinecraftTicks() - state.getMinecraftTicks();
		if (output < 0) {
			output = (maxTicks - nextState.getMinecraftTicks()) + state.getMinecraftTicks();
		}
		return output;
	}

	public static TimeState getNext(TimeState state) {
		for (TimeState value : values()) {
			if (value.getMinecraftTicks() > state.getMinecraftTicks()) {
				return value;
			}
		}
		return NOON;
	}

}
