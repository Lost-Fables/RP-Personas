package net.korvic.rppersonas.commands;

import co.lotc.core.command.annotate.Cmd;
import co.lotc.core.command.annotate.Range;
import net.korvic.rppersonas.RPPersonas;
import net.korvic.rppersonas.time.Season;
import net.korvic.rppersonas.time.TimeManager;
import org.bukkit.World;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

public class TimeCommands extends BaseCommand {

	private RPPersonas plugin;
	public TimeCommands(RPPersonas plugin) {
		this.plugin = plugin;
	}

	@Cmd(value="Get the current date & time", permission=RPPersonas.PERMISSION_START + "time.info")
	public void info(CommandSender sender) {
		sender.sendMessage(RPPersonas.PRIMARY_DARK + "It has been " + RPPersonas.SECONDARY_DARK + TimeManager.getRelativeTimeString(RPPersonas.ANOMA_DATE.getTime()) + RPPersonas.PRIMARY_DARK + " since arriving on Anoma.");

		if (sender instanceof Player) {
			Player p = (Player) sender;
			String message = RPPersonas.PRIMARY_DARK + "The time is: " + RPPersonas.SECONDARY_DARK + p.getWorld().getTime();
			if (TimeManager.getManagerOfWorld(p.getWorld()) != null) {
				message += RPPersonas.PRIMARY_DARK + ", and the season is: " + RPPersonas.SECONDARY_DARK + TimeManager.getManagerOfWorld(p.getWorld()).getSeason();
			}
			sender.sendMessage(message);
		}
	}

	@Cmd(value="Register the current world to use custom time.", permission=RPPersonas.PERMISSION_START + ".time.register")
	public void registerworld(CommandSender sender) {
		if (sender instanceof Player) {
			Player p = (Player) sender;
			TimeManager.registerWorld(p.getWorld(), true);
			msg(RPPersonas.PRIMARY_DARK + "Your current world is now using custom time.");
		} else {
			msg("Stahp it console.");
		}
	}

	@Cmd(value="Unregister the current world from using custom time.", permission=RPPersonas.PERMISSION_START + "time.unregister")
	public void unregisterworld(CommandSender sender, World world) {
		if (sender instanceof Player) {
			Player p = (Player) sender;
			TimeManager.unregisterWorld(p.getWorld(), true);
			msg(RPPersonas.PRIMARY_DARK + "Your current world is no longer using custom time.");
		} else {
			msg("Stahp it console.");
		}
	}

	@Cmd(value="Sync two worlds' times that are using custom time.", permission=RPPersonas.PERMISSION_START + ".time.synctoworld")
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

	@Cmd(value="Update the time for the world you're in and all synced worlds.", permission=RPPersonas.PERMISSION_START + ".time.sethour")
	public void sethour(CommandSender sender, @Range(min=0, max=23) int time) {
		if (sender instanceof Player) {
			Player p = (Player) sender;
			TimeManager manager = TimeManager.getManagerOfWorld(p.getWorld());
			if (manager != null) {
				manager.setTime(time * 1000);
			} else {
				p.getWorld().setTime(time * 1000);
			}
		} else {
			msg("Stahp it console.");
		}
	}

	@Cmd(value="Update the season for the world you're in and all synced worlds.", permission=RPPersonas.PERMISSION_START + ".time.season")
	public void setseason(CommandSender sender, Season season) {
		if (sender instanceof Player) {
			Player p = (Player) sender;
			TimeManager manager = TimeManager.getManagerOfWorld(p.getWorld());
			if (manager != null) {
				manager.setSeason(season.getName(), true);
				msg(RPPersonas.PRIMARY_DARK + "Season updated!");
			} else {
				msg(RPPersonas.PRIMARY_DARK + "You must register this world before you can set the season.");
			}
		} else {
			msg("Stahp it console.");
		}
	}

	@Cmd(value="Update the time scale for the world you're in and all synced worlds.", permission=RPPersonas.PERMISSION_START + ".time.timescale")
	public void settimescale(CommandSender sender, @Range(min=20, max=720) int scale) {
		if (sender instanceof Player) {
			Player p = (Player) sender;
			TimeManager manager = TimeManager.getManagerOfWorld(p.getWorld());
			if (manager != null) {
				manager.setTimeScale(scale, true);
				msg(RPPersonas.PRIMARY_DARK + "Time scale updated!");
			} else {
				msg(RPPersonas.PRIMARY_DARK + "You must register this world before you can set the time scale.");
			}
		} else {
			msg("Stahp it console.");
		}
	}

}
