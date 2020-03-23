package net.korvic.rppersonas.commands;

import co.lotc.core.command.annotate.Arg;
import co.lotc.core.command.annotate.Cmd;
import net.korvic.rppersonas.RPPersonas;
import net.korvic.rppersonas.personas.Persona;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class PersonaSetCommands extends BaseCommand {

	private RPPersonas plugin;

	public PersonaSetCommands(RPPersonas plugin) {
		this.plugin = plugin;
	}

	@Cmd(value = "Set the display name of your current persona.")
	public void name(CommandSender sender,
					 @Arg(value = "Name", description = "The new name of your persona.") String name) {
		if (sender instanceof Player) {
			Player p = (Player) sender;
			Persona pers = plugin.getPersonaHandler().getLoadedPersona(p);
			pers.setNickName(name);
			msg(RPPersonas.PRIMARY_COLOR + "Display Name updated to " + RPPersonas.SECONDARY_COLOR + name + RPPersonas.PRIMARY_COLOR + ".");
		} else {
			msg(RPPersonas.PRIMARY_COLOR + PersonaCommands.NO_CONSOLE);
		}
	}

	@Cmd(value = "Set the prefix for your current persona.")
	public void prefix(CommandSender sender,
					   @Arg(value = "Prefix", description = "The prefix to use (no brackets needed).") String prefix) {
		if (sender instanceof Player) {
			Player p = (Player) sender;
			Persona pers = plugin.getPersonaHandler().getLoadedPersona(p);
			pers.setPrefix(prefix);
			msg(RPPersonas.PRIMARY_COLOR + "Prefix updated to " + RPPersonas.SECONDARY_COLOR + "[" + prefix + "]" + RPPersonas.PRIMARY_COLOR + ".");
		} else {
			msg(RPPersonas.PRIMARY_COLOR + PersonaCommands.NO_CONSOLE);
		}
	}

}
