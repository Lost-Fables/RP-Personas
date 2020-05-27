package net.korvic.rppersonas.commands;

import co.lotc.core.command.annotate.Cmd;
import co.lotc.core.command.annotate.Range;
import net.korvic.rppersonas.RPPersonas;
import net.korvic.rppersonas.personas.Persona;
import net.korvic.rppersonas.personas.PersonaLanguage;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.Map;

public class LanguageCommands extends BaseCommand {

	RPPersonas plugin;

	public LanguageCommands(RPPersonas plugin) {
		this.plugin = plugin;
	}

	@Cmd(value="Adjust the language level of a given player.")
	public void update(CommandSender sender, Player p, PersonaLanguage lang, @Range(min=-255, max=255) int amount) {
		Persona pers = plugin.getPersonaHandler().getLoadedPersona(p);
		if (pers != null) {
			Map<String, Short> languages = plugin.getLanguageSQL().getLanguages(pers.getPersonaID());

		} else {
			msg(RPPersonas.PRIMARY_DARK + "That player doesn't have a registered persona.");
		}
	}

}
