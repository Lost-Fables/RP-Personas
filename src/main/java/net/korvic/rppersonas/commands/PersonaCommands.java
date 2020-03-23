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

	protected static final String NO_CONSOLE = "The console does not have a persona.";
	private RPPersonas plugin;
	private PersonaSetCommands personaSetCommands;
	private PersonaDescCommands personaDescCommands;

	public PersonaCommands (RPPersonas plugin) {
		this.plugin = plugin;
		this.personaSetCommands = new PersonaSetCommands(plugin);
		this.personaDescCommands = new PersonaDescCommands(plugin);
	}

	@Cmd(value="Get the information on someone else's persona.", permission="rppersonas.accepted")
	public void info(CommandSender sender,
					 @Arg(value="Player", description="The player who's info you wish to see.") Player player) {
		msg(plugin.getPersonaHandler().getPersonaInfo(player));
	}

	@Cmd(value="Set information about your persona.", permission="rppersonas.accepted")
	public BaseCommand set() {
		return personaSetCommands;
	}

	@Cmd(value="Update the description of your persona.", permission="rppersonas.accepted")
	public BaseCommand desc() {
		return personaDescCommands;
	}
}
