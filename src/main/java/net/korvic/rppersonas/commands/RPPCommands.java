package net.korvic.rppersonas.commands;

import co.lotc.core.bukkit.util.InventoryUtil;
import co.lotc.core.command.annotate.Cmd;
import net.korvic.rppersonas.RPPersonas;
import net.korvic.rppersonas.death.Altar;
import net.korvic.rppersonas.personas.Persona;
import net.korvic.rppersonas.statuses.EtherealStatus;
import org.bukkit.Location;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.dynmap.DynmapCommonAPI;

public class RPPCommands extends BaseCommand {

	RPPersonas plugin;

	AltarCommands altarCommands;
	TimeCommands timeCommands;
	KitCommands kitCommands;

	public RPPCommands(RPPersonas plugin, TimeCommands timeCommands) {
		this.plugin = plugin;

		altarCommands = new AltarCommands(plugin);
		this.timeCommands = timeCommands;
		kitCommands = new KitCommands(plugin);
	}

	@Cmd(value="Commands for modifying altars.")
	public BaseCommand altar() {
		return altarCommands;
	}

	@Cmd(value="Commands for adjusting the time.")
	public BaseCommand time() {
		return timeCommands;
	}

	@Cmd(value="Commands for modifying kits.")
	public BaseCommand kit() {
		return kitCommands;
	}

}
