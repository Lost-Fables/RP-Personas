package net.korvic.rppersonas.commands;

import co.lotc.core.command.annotate.Cmd;
import co.lotc.core.command.annotate.Range;
import net.korvic.rppersonas.RPPersonas;
import net.korvic.rppersonas.time.Season;
import net.korvic.rppersonas.time.TimeManager;
import org.bukkit.World;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class TimeCommands extends BaseCommand {

	private RPPersonas plugin;

	public TimeCommands(RPPersonas plugin) {
		this.plugin = plugin;
	}

	@Cmd(value="Get the current time of the world you're in.")
	public void info(CommandSender sender) {
		if (sender instanceof Player) {
			Player p = (Player) sender;
			String message = RPPersonas.PRIMARY_DARK + "The time is: " + RPPersonas.SECONDARY_DARK + p.getWorld().getTime();
			if (TimeManager.getManagerOfWorld(p.getWorld()) != null) {
				message += RPPersonas.PRIMARY_DARK + ", and the season is: " + RPPersonas.SECONDARY_DARK + TimeManager.getManagerOfWorld(p.getWorld()).getSeason();
			}
			msg(message);
		} else {
			msg("Stahp it console.");
		}
	}

	@Cmd(value="Register the current world to use custom time.")
	public void registerworld(CommandSender sender) {
		if (sender instanceof Player) {
			Player p = (Player) sender;
			TimeManager.registerWorld(p.getWorld(), true);
			msg(RPPersonas.PRIMARY_DARK + "Your current world is now using custom time.");
		} else {
			msg("Stahp it console.");
		}
	}

	@Cmd(value="Unregister the current world from using custom time.")
	public void unregisterworld(CommandSender sender, World world) {
		if (sender instanceof Player) {
			Player p = (Player) sender;
			TimeManager.unregisterWorld(p.getWorld(), true);
			msg(RPPersonas.PRIMARY_DARK + "Your current world is no longer using custom time.");
		} else {
			msg("Stahp it console.");
		}
	}

	@Cmd(value="Sync two worlds' times that are using custom time.")
	public void syncworld(CommandSender sender, World world) {
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

	@Cmd(value="Update the time for the world you're in and all synced worlds.")
	public void sethour(CommandSender sender, @Range(min=0, max=23) int time) {
		if (sender instanceof Player) {
			Player p = (Player) sender;
			TimeManager.getManagerOfWorld(p.getWorld()).setTime(time*1000);
		} else {
			msg("Stahp it console.");
		}
	}

	@Cmd(value="Update the season for the world you're in and all synced worlds.")
	public void setseason(CommandSender sender, Season season) {
		if (sender instanceof Player) {
			Player p = (Player) sender;
			TimeManager.getManagerOfWorld(p.getWorld()).setSeason(season.getName(), true);
		} else {
			msg("Stahp it console.");
		}
	}

}
