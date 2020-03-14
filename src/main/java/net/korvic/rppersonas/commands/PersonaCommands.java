package net.korvic.rppersonas.commands;

import co.lotc.core.command.annotate.Arg;
import co.lotc.core.command.annotate.Cmd;
import net.korvic.rppersonas.RPPersonas;
import net.korvic.rppersonas.personas.PersonaCreationDialog;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.Map;

public class PersonaCommands extends BaseCommand {

	private static final String NO_CONSOLE = "The console does not have a persona.";
	private RPPersonas plugin;

	public PersonaCommands (RPPersonas plugin) {
		this.plugin = plugin;
	}

	@Cmd(value="Get the information on someone else's persona.", permission="rppersonas.accepted")
	public void info(CommandSender sender,
					 @Arg(value="Player", description="The player who's info you wish to see.") Player player) {
		player.sendMessage(formatPersonaBasicInfo(player));
	}

	private String formatPersonaBasicInfo(Player p) {
		Map<String, Object> data = plugin.getPersonaHandler().getPersona(p).getBasicInfo();

		String output = PersonaCreationDialog.DIVIDER +
						RPPersonas.PREFIX + "Persona ID: " + RPPersonas.ALT_COLOR + data.get("personaid") + "\n";
		if (data.containsKey("nickname")) {
			output += RPPersonas.PREFIX + "Nickname: " + RPPersonas.ALT_COLOR + data.get("nickname") + "\n";
		}
		output += RPPersonas.PREFIX + "Name: " + RPPersonas.ALT_COLOR + data.get("name") + "\n" +
				  RPPersonas.PREFIX + "Age: " + RPPersonas.ALT_COLOR + data.get("age") + "\n" +
				  RPPersonas.PREFIX + "Race: " + RPPersonas.ALT_COLOR + data.get("race") + "\n" +
				  RPPersonas.PREFIX + "Gender: " + RPPersonas.ALT_COLOR + data.get("gender") + "\n";
		if (data.containsKey("description")) {
			output += RPPersonas.PREFIX + "Description: " + RPPersonas.ALT_COLOR + data.get("description") + "\n";
		}
		output += PersonaCreationDialog.DIVIDER;

		return output;
	}
}
