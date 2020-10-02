package net.korvic.rppersonas.commands;

import co.lotc.core.command.annotate.Arg;
import co.lotc.core.command.annotate.Cmd;
import co.lotc.core.command.annotate.Default;
import net.korvic.rppersonas.RPPersonas;
import net.korvic.rppersonas.players.Persona;
import net.korvic.rppersonas.players.personas.PersonaLanguage;
import net.korvic.rppersonas.players.personas.PersonaSubRace;
import net.korvic.rppersonas.sql.LanguageSQL;
import net.korvic.rppersonas.sql.PersonasSQL;
import net.korvic.rppersonas.sql.util.DataMapFilter;
import net.korvic.rppersonas.time.TimeManager;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class StaffPersonaCommands extends BaseCommand {

	private RPPersonas plugin;
	public static final String NO_PERSONA = "That player does not have an active persona.";
	public static final String NO_CONSOLE = "Console cannot set data for itself.";

	public StaffPersonaCommands(RPPersonas plugin) {
		this.plugin = plugin;
	}

	@Cmd(value = "Set the race for the given player", permission = RPPersonas.PERMISSION_START + ".managepersonas.rawrace")
	public void setRawRace(CommandSender sender,
						   @Arg(value = "Race", description = "The name of the race with proper capitalization.") String race,
						   @Arg(value = "Player", description = "The player who's race you wish to change.") @Default(value = "@p") Player player) {
		if (!player.equals(sender) && !sender.hasPermission(RPPersonas.PERMISSION_START + ".managepersonas.rawrace.others")) {
			msg(RPPersonas.PRIMARY_DARK + "You do not have permission to edit others' races with raw text.");
			return;
		}
		Persona pers = Persona.getPersona(player);
		if (pers != null) {
			DataMapFilter data = new DataMapFilter().put(PersonasSQL.PERSONAID, pers.getPersonaID())
													.put(PersonasSQL.RAW_RACE, race.replace('_', ' '));
			plugin.getPersonasSQL().registerOrUpdate(data);
			msg(RPPersonas.PRIMARY_DARK + "Set the race of " + player.getName() + "'s persona to " + race.replace('_', ' '));
		} else {
			msg(RPPersonas.PRIMARY_DARK + NO_PERSONA);
		}
	}

	@Cmd(value = "Set the race and default languages for the given player.", permission = RPPersonas.PERMISSION_START + ".managepersonas.race")
	public void setRace(CommandSender sender,
						PersonaSubRace race,
						@Arg(value = "Player", description = "The player who's race you wish to change.") @Default(value = "@p") Player player) {
		if (!player.equals(sender) && !sender.hasPermission(RPPersonas.PERMISSION_START + ".managepersonas.race.others")) {
			msg(RPPersonas.PRIMARY_DARK + "You do not have permission to edit others' races.");
			return;
		}
		Persona pers = Persona.getPersona(player);
		if (pers != null) {
			// Race
			DataMapFilter data = new DataMapFilter().put(PersonasSQL.PERSONAID, pers.getPersonaID())
													.put(PersonasSQL.RACE, race);
			plugin.getPersonasSQL().registerOrUpdate(data);

			// Languages
			plugin.getLanguageSQL().purgeAll(pers.getPersonaID());
			for (PersonaLanguage lang : race.getDefaultLanguages()) {
				DataMapFilter langData = new DataMapFilter().put(LanguageSQL.PERSONAID, pers.getPersonaID())
															.put(LanguageSQL.LANGUAGE, lang.getName())
															.put(LanguageSQL.LEVEL, (short) 190);
				plugin.getLanguageSQL().registerOrUpdate(langData);
			}

			msg(RPPersonas.PRIMARY_DARK + "Set the race and languages of " + player.getName() + "'s persona to " + race.getName() + "'s default values.");
		} else {
			msg(RPPersonas.PRIMARY_DARK + NO_PERSONA);
		}
	}

	@Cmd(value = "Force update a persona's age.", permission = RPPersonas.PERMISSION_START + ".managepersonas.age")
	public void setAge(CommandSender sender,
					   @Arg(value = "Ages", description = "The number of ages since they were born.") int ages,
					   @Arg(value = "Player", description = "The player who's age you wish to change.") @Default(value = "@p") Player player) {
		if (!player.equals(sender) && !sender.hasPermission(RPPersonas.PERMISSION_START + ".managepersonas.age.others")) {
			msg(RPPersonas.PRIMARY_DARK + "You do not have permission to edit others' ages.");
			return;
		}
		Persona pers = Persona.getPersona(player);
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
		if (!player.equals(sender) && !sender.hasPermission(RPPersonas.PERMISSION_START + ".managepersonas.name.others")) {
			msg(RPPersonas.PRIMARY_DARK + "You do not have permission to edit others' names.");
			return;
		}
		Persona pers = Persona.getPersona(player);
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
