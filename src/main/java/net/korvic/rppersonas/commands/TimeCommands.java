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
			TimeManager.registerWorld(p.getWorld());
		}
	}

}
