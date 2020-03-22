package net.korvic.rppersonas.commands;

import co.lotc.core.command.annotate.Arg;
import co.lotc.core.command.annotate.Cmd;
import net.korvic.rppersonas.RPPersonas;
import net.korvic.rppersonas.personas.Persona;
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
		msg(plugin.getPersonaHandler().getPersonaInfo(player));
	}
}
