package net.korvic.rppersonas.commands;

import co.lotc.core.command.annotate.Cmd;
import net.korvic.rppersonas.RPPersonas;
import net.korvic.rppersonas.players.Persona;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class KarmaCommands extends BaseCommand {

	private RPPersonas plugin;

	public KarmaCommands(RPPersonas plugin) {
		this.plugin = plugin;
	}

	@Cmd(value="Inspect the current Karma of a given player")
	public void get(CommandSender sender, Player player) {
		Persona pers = Persona.getPersona(player);
		if (pers != null) {
			int karma = plugin.getKarmaSQL().calculateKarma(pers.getPersonaID());
			msg(RPPersonas.SECONDARY_DARK + pers.getNickname() + RPPersonas.PRIMARY_DARK + " currently has " + RPPersonas.SECONDARY_DARK + karma + RPPersonas.PRIMARY_DARK + " Karma.");
		} else {
			msg(RPPersonas.PRIMARY_DARK + "That player does not have a persona yet.");
		}
	}

}
