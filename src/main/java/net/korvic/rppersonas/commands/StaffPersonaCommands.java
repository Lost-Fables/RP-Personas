package net.korvic.rppersonas.commands;

import co.lotc.core.command.annotate.Arg;
import co.lotc.core.command.annotate.Cmd;
import co.lotc.core.command.annotate.Default;
import net.korvic.rppersonas.RPPersonas;
import net.korvic.rppersonas.personas.Persona;
import net.korvic.rppersonas.sql.PersonasSQL;
import net.korvic.rppersonas.sql.util.DataMapFilter;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class StaffPersonaCommands extends BaseCommand {

	private RPPersonas plugin;

	public StaffPersonaCommands(RPPersonas plugin) {
		this.plugin = plugin;
	}

	@Cmd(value = "Set the race for the given player", permission = RPPersonas.PERMISSION_START + ".managepersonas.race")
	public void setRace(CommandSender sender,
						@Arg(value = "Player", description = "The player who's race you wish to change.") @Default(value = "@p") Player player,
						@Arg(value = "Race", description = "The name of the race with proper capitalization.") String race) {
		Persona pers = plugin.getPersonaHandler().getLoadedPersona(player);
		if (pers != null) {
			DataMapFilter data = new DataMapFilter().put(PersonasSQL.PERSONAID, pers.getPersonaID())
													.put(PersonasSQL.RAW_RACE, race);
			plugin.getPersonasSQL().registerOrUpdate(data);
		} else {
			msg(RPPersonas.PRIMARY_DARK + "That player does not have an active persona.");
		}
	}

}
