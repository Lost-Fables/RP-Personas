package net.korvic.rppersonas.players.statuses;

import lombok.Getter;
import lombok.Setter;

public class StatusEntry {

	@Getter @Setter private Status status;
	@Getter @Setter private byte severity;
	@Getter @Setter private long expiration;
	@Getter @Setter private boolean enabled;

	public StatusEntry(Status status, byte severity, long expiration, boolean enabled) {
		this.status = status;
		this.severity = severity;
		this.expiration = expiration;
		this.enabled = enabled;
	}

}
