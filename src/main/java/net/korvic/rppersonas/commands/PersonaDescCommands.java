package net.korvic.rppersonas.commands;

import co.lotc.core.command.annotate.Cmd;
import net.korvic.rppersonas.RPPersonas;
import net.korvic.rppersonas.personas.Persona;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class PersonaDescCommands extends BaseCommand {

	private RPPersonas plugin;

	public PersonaDescCommands(RPPersonas plugin) {
		this.plugin = plugin;
	}

	@Cmd(value = "Add text to the end of your persona's description.")
	public void add(CommandSender sender, String[] text) {
		if (sender instanceof Player) {
			Player p = (Player) sender;
			Persona pers = plugin.getPersonaHandler().getLoadedPersona(p);
			if (pers != null) {
				String newDesc = pers.addToDescription(p, text);
				msg(RPPersonas.PRIMARY_DARK + "Your persona's description is now:\n " + RPPersonas.SECONDARY_LIGHT + newDesc);
			} else {
				msg(RPPersonas.PRIMARY_DARK + "You need to register a persona first! Be sure to link your account to get started.");
			}
		} else {
			msg(RPPersonas.PRIMARY_DARK + PersonaCommands.NO_CONSOLE);
		}
	}

	@Cmd(value = "Reset the description of your persona to nothing.")
	public void clear(CommandSender sender) {
		if (sender instanceof Player) {
			Player p = (Player) sender;
			Persona pers = plugin.getPersonaHandler().getLoadedPersona(p);
			if (pers != null) {
				pers.clearDescription(p);
				msg(RPPersonas.PRIMARY_DARK + "Your persona's description has been cleared.");
			} else {
				msg(RPPersonas.PRIMARY_DARK + "You need to register a persona first! Be sure to link your account to get started.");
			}
		} else {
			msg(RPPersonas.PRIMARY_DARK + PersonaCommands.NO_CONSOLE);
		}
	}

}
