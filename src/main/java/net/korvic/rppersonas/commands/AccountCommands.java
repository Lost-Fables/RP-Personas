package net.korvic.rppersonas.commands;

import co.lotc.core.command.annotate.Arg;
import co.lotc.core.command.annotate.Cmd;
import net.korvic.rppersonas.RPPersonas;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class AccountCommands extends BaseCommand {

	private RPPersonas plugin;
	private static final String NO_CONSOLE_ACCOUNTS = RPPersonas.PREFIX + "The console cannot register an account as it does not have a UUID.";
	private static final String REGISTRATION_SUCCESSFUL = RPPersonas.PREFIX + "Successfully linked your forum account!";

	public AccountCommands(RPPersonas plugin) {
		this.plugin = plugin;
	}

	@Cmd(value="Send a registration message to your forum account.", permission="rppersonas.accepted")
	public void link(CommandSender sender,
						 @Arg(value="Forum ID", description="Your forum account ID.") int id) {
		if (sender instanceof Player) {
			//TODO - Send user a message on the forums for them to confirm their Forum ID that will instead run the line below.
			plugin.getUUIDAccountMapSQL().addMapping(id, (Player) sender);
			sender.sendMessage(REGISTRATION_SUCCESSFUL);
		} else {
			sender.sendMessage(NO_CONSOLE_ACCOUNTS);
		}
	}

	@Cmd(value="Start the register process for someone else.", permission="rppersonas.helper")
	public void other(@Arg(value="The Player", description="The player you're helping register.") Player p,
					  @Arg(value="Forum ID", description="The forum ID of the other player.") int id) {
		link((CommandSender) p, id);
	}
}
