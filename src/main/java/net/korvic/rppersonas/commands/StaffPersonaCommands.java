package net.korvic.rppersonas.commands;

import co.lotc.core.command.annotate.Arg;
import co.lotc.core.command.annotate.Cmd;
import co.lotc.core.command.annotate.Default;
import net.korvic.rppersonas.RPPersonas;
import net.korvic.rppersonas.personas.Persona;
import net.korvic.rppersonas.personas.PersonaSubRace;
import net.korvic.rppersonas.sql.PersonasSQL;
import net.korvic.rppersonas.sql.util.DataMapFilter;
import net.korvic.rppersonas.time.TimeManager;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class StaffPersonaCommands extends BaseCommand {

	private RPPersonas plugin;
	public static final String NO_PERSONA = "That player does not have an active persona.";

	public StaffPersonaCommands(RPPersonas plugin) {
		this.plugin = plugin;
	}

	@Cmd(value = "Set the race for the given player", permission = RPPersonas.PERMISSION_START + ".managepersonas.rawrace")
	public void setRawRace(CommandSender sender,
						   @Arg(value = "Race", description = "The name of the race with proper capitalization.") String race,
						   @Arg(value = "Player", description = "The player who's race you wish to change.") @Default(value = "@p") Player player) {
		Persona pers = plugin.getPersonaHandler().getLoadedPersona(player);
		if (pers != null) {
			DataMapFilter data = new DataMapFilter().put(PersonasSQL.PERSONAID, pers.getPersonaID())
													.put(PersonasSQL.RAW_RACE, race.replace('_', ' '));
			plugin.getPersonasSQL().registerOrUpdate(data);
			msg(RPPersonas.PRIMARY_DARK + "Set the race of " + player.getName() + "'s persona to " + race.replace('_', ' '));
		} else {
			msg(RPPersonas.PRIMARY_DARK + NO_PERSONA);
		}
	}

	@Cmd(value = "Set the race for the given player", permission = RPPersonas.PERMISSION_START + ".managepersonas.race")
	public void setRace(CommandSender sender,
						PersonaSubRace race,
						@Arg(value = "Player", description = "The player who's race you wish to change.") @Default(value = "@p") Player player) {
		Persona pers = plugin.getPersonaHandler().getLoadedPersona(player);
		if (pers != null) {
			DataMapFilter data = new DataMapFilter().put(PersonasSQL.PERSONAID, pers.getPersonaID())
													.put(PersonasSQL.RACE, race);
			plugin.getPersonasSQL().registerOrUpdate(data);
			msg(RPPersonas.PRIMARY_DARK + "Set the race of " + player.getName() + "'s persona to " + race.getName());
		} else {
			msg(RPPersonas.PRIMARY_DARK + NO_PERSONA);
		}
	}

	@Cmd(value = "Force update a persona's age.", permission = RPPersonas.PERMISSION_START + ".managepersonas.age")
	public void setAge(CommandSender sender,
					   @Arg(value = "Ages", description = "The number of ages since they were born.") int ages,
					   @Arg(value = "Player", description = "The player who's age you wish to change.") @Default(value = "@p") Player player) {
		Persona pers = plugin.getPersonaHandler().getLoadedPersona(player);
		if (pers != null) {
			long age = TimeManager.getMillisFromAge(ages);
			DataMapFilter data = new DataMapFilter().put(PersonasSQL.PERSONAID, pers.getPersonaID())
													.put(PersonasSQL.AGE, age);
			plugin.getPersonasSQL().registerOrUpdate(data);
			msg(RPPersonas.PRIMARY_DARK + "Set the age of " + player.getName() + "'s persona to " + ages + " Ages.");
		} else {
			msg(RPPersonas.PRIMARY_DARK + NO_PERSONA);
		}
	}

	@Cmd(value = "Set the birth name of a given player.", permission = RPPersonas.PERMISSION_START + ".managepersonas.name")
	public void setName(CommandSender sender,
						@Arg(value = "Name", description = "The birth name to set for a given player.") String name,
						@Arg(value = "Player", description = "The player who's age you wish to change.") @Default(value = "@p") Player player) {
		Persona pers = plugin.getPersonaHandler().getLoadedPersona(player);
		if (pers != null) {
			DataMapFilter data = new DataMapFilter().put(PersonasSQL.PERSONAID, pers.getPersonaID())
													.put(PersonasSQL.NAME, name.replace('_', ' '));
			plugin.getPersonasSQL().registerOrUpdate(data);
			msg(RPPersonas.PRIMARY_DARK + "Set the name of " + player.getName() + "'s persona to " + name.replace('_', ' '));
		} else {
			msg(RPPersonas.PRIMARY_DARK + NO_PERSONA);
		}
	}

}
