package net.korvic.rppersonas.commands;

import co.lotc.core.command.annotate.Arg;
import co.lotc.core.command.annotate.Cmd;
import co.lotc.core.command.annotate.Default;
import net.korvic.rppersonas.RPPersonas;
import net.korvic.rppersonas.personas.Persona;
import org.apache.commons.lang.WordUtils;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class PersonaSetCommands extends BaseCommand {

	private RPPersonas plugin;

	public PersonaSetCommands(RPPersonas plugin) {
		this.plugin = plugin;
	}

	@Cmd(value = "Set the display name of your current persona.")
	public void name(CommandSender sender,
					 @Arg(value = "Name", description = "The new display name of your persona.") String[] name) {
		if (sender instanceof Player) {
			Player p = (Player) sender;
			Persona pers = plugin.getPersonaHandler().getLoadedPersona(p);

			StringBuilder builder = new StringBuilder();
			for (String s : name) {
				if (builder.length() > 0) {
					builder.append(" ");
				}
				builder.append(s);
			}

			if ((p.hasPermission(RPPersonas.PERMISSION_START + ".longname") && builder.length() <= 48) || builder.length() <= 32) {
				final String regex = ".*[^A-Za-zÀ-ÿ \\-'\"].*?";
				final Pattern pattern = Pattern.compile(regex, Pattern.MULTILINE);
				final Matcher matcher = pattern.matcher(builder.toString());
				if (!matcher.find()) {
					String finalName = WordUtils.capitalizeFully(builder.toString());
					pers.setNickName(p, finalName);
					msg(RPPersonas.PRIMARY_DARK + "Display Name updated to " + RPPersonas.SECONDARY_LIGHT + builder.toString() + RPPersonas.PRIMARY_DARK + ".");
				} else {
					msg(RPPersonas.PRIMARY_DARK + "That name contained illegal lettering.");
				}
			} else {
				msg(RPPersonas.PRIMARY_DARK + "That name is too long! Please enter something shorter.");
			}
		} else {
			msg(RPPersonas.PRIMARY_DARK + PersonaCommands.NO_CONSOLE);
		}
	}

	@Cmd(value = "Set the prefix for your current persona.")
	public void prefix(CommandSender sender,
					   @Arg(value = "Prefix", description = "The prefix to use (no brackets needed). Leave empty to clear.") @Default(value = "") String prefix) {
		if (sender instanceof Player) {
			Player p = (Player) sender;
			Persona pers = plugin.getPersonaHandler().getLoadedPersona(p);
			pers.setPrefix(p, prefix);
			msg(RPPersonas.PRIMARY_DARK + "Prefix updated to " + RPPersonas.SECONDARY_LIGHT + "[" + prefix + "]" + RPPersonas.PRIMARY_DARK + ".");
		} else {
			msg(RPPersonas.PRIMARY_DARK + PersonaCommands.NO_CONSOLE);
		}
	}

}
