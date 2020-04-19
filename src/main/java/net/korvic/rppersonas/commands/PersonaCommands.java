package net.korvic.rppersonas.commands;

import co.lotc.core.command.annotate.Arg;
import co.lotc.core.command.annotate.Cmd;
import net.korvic.rppersonas.RPPersonas;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

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

	@Cmd(value = "Get the information on someone else's persona.", permission = RPPersonas.PERMISSION_START + ".accepted")
	public void info(CommandSender sender,
					 @Arg(value = "Player", description = "The player who's info you wish to see.") Player player) {
		msg(plugin.getPersonaHandler().getPersonaInfo(player));
	}

	@Cmd(value = "Execute the given player's current persona by your current persona.", permission = RPPersonas.PERMISSION_START + ".accepted")
	public void Execute(CommandSender sender,
						@Arg(value = "Player", description = "The player which you're executing.") Player player) {
		if (sender instanceof Player &&
			!plugin.getDeathHandler().requestExecute((Player) sender, player)) {
			msg(RPPersonas.PRIMARY_DARK + "That player already has an execution request pending!");
		} else {
			msg(NO_CONSOLE);
		}
	}

	@Cmd(value = "Accept being executed by a given player.", permission = RPPersonas.PERMISSION_START + ".accepted")
	public void ExecuteAccept(CommandSender sender,
							  @Arg(value = "Player", description = "The player executing you.") Player killer) {
		if (sender instanceof Player) {
			Player victim = (Player) sender;
			if (plugin.getDeathHandler().hasRequest(victim)) {
				if (!plugin.getDeathHandler().acceptExecute(killer, victim)) {
					plugin.getDeathHandler().pingRequest(victim);
				}
			} else {
				msg(RPPersonas.PRIMARY_DARK + "You have no pending execution requests.");
			}
		} else {
			msg(NO_CONSOLE);
		}
	}

	@Cmd(value = "Set information about your persona.", permission = RPPersonas.PERMISSION_START + ".accepted")
	public BaseCommand set() {
		return personaSetCommands;
	}

	@Cmd(value = "Update the description of your persona.", permission = RPPersonas.PERMISSION_START + ".accepted")
	public BaseCommand desc() {
		return personaDescCommands;
	}
}
