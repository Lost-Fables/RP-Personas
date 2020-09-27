package net.korvic.rppersonas.events;

import org.bukkit.event.Cancellable;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;
import org.jetbrains.annotations.NotNull;

public class PlayerCreatePersonaEvent extends Event implements Cancellable {

	private static final HandlerList HANDLER_LIST = new HandlerList();
	private boolean cancelled;

	public PlayerCreatePersonaEvent() {
		this.cancelled = false;
	}

	@Override
	public boolean isCancelled() {
		return cancelled;
	}

	@Override
	public void setCancelled(boolean cancel) {
		this.cancelled = cancel;
	}

	@Override
	public @NotNull HandlerList getHandlers() {
		return HANDLER_LIST;
	}
}
