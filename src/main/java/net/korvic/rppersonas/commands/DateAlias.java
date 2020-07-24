package net.korvic.rppersonas.commands;

import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;

public class DateAlias implements CommandExecutor {

	private TimeCommands timeCommands;

	public DateAlias(TimeCommands timeCommands) {
		this.timeCommands = timeCommands;
	}

	@Override
	public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
		timeCommands.info(sender);
		return true;
	}

}
