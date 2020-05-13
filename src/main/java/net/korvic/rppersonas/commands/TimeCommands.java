package net.korvic.rppersonas.commands;

import co.lotc.core.command.annotate.Cmd;
import co.lotc.core.command.annotate.Default;
import net.korvic.rppersonas.RPPersonas;
import net.korvic.rppersonas.time.TimeManager;
import org.bukkit.World;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class TimeCommands extends BaseCommand {

	private RPPersonas plugin;

	public TimeCommands(RPPersonas plugin) {
		this.plugin = plugin;
	}

	@Cmd(value="Register the current world to use custom time.")
	public void registerWorld(CommandSender sender) {
		if (sender instanceof Player) {
			Player p = (Player) sender;
			TimeManager.registerWorld(p.getWorld(), true);
			msg(RPPersonas.PRIMARY_DARK + "Your current world is now using custom time.");
		} else {
			msg("Stahp it console.");
		}
	}

	@Cmd(value="Sync two worlds' times that are using custom time.")
	public void syncWorld(CommandSender sender, World world) {
		if (sender instanceof Player) {
			Player p = (Player) sender;
			if (TimeManager.syncWorldTimes(world, p.getWorld())) {
				msg(RPPersonas.PRIMARY_DARK + "Your current world time is now synced to " + RPPersonas.SECONDARY_DARK + world.getName() + RPPersonas.PRIMARY_DARK + ".");
			} else {
				msg(RPPersonas.PRIMARY_DARK + "Either you specified the world you're in, or the specified world does not have a custom time set.");
			}
		} else {
			msg("Stahp it console.");
		}
	}

	@Cmd(value="Get the current time of the world you're in.")
	public void info(CommandSender sender) {
		if (sender instanceof Player) {
			Player p = (Player) sender;
			msg(RPPersonas.PRIMARY_DARK + "The time is: " + RPPersonas.SECONDARY_DARK + p.getWorld().getTime());
		} else {
			msg("Stahp it console.");
		}
	}

}
