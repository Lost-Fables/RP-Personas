package net.korvic.rppersonas.commands;

import co.lotc.core.bukkit.util.PlayerUtil;
import co.lotc.core.command.annotate.Arg;
import co.lotc.core.command.annotate.Cmd;
import net.korvic.rppersonas.RPPersonas;
import net.luckperms.api.LuckPerms;
import net.luckperms.api.model.user.User;
import net.luckperms.api.node.Node;
import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.plugin.RegisteredServiceProvider;

import java.util.UUID;
import java.util.concurrent.CompletableFuture;

public class RPPCommands extends BaseCommand {

	RPPersonas plugin;

	AltarCommands altarCommands;
	TimeCommands timeCommands;
	KitCommands kitCommands;
	LanguageCommands langaugeCommands;
	KarmaCommands karmaCommands;

	public RPPCommands(RPPersonas plugin, TimeCommands timeCommands) {
		this.plugin = plugin;

		this.altarCommands = new AltarCommands(plugin);
		this.timeCommands = timeCommands;
		this.kitCommands = new KitCommands(plugin);
		this.langaugeCommands = new LanguageCommands(plugin);
		this.karmaCommands = new KarmaCommands(plugin);
	}

	@Cmd(value="Commands for modifying altars.")
	public BaseCommand altar() {
		return altarCommands;
	}

	@Cmd(value="Commands for adjusting the time.")
	public BaseCommand time() {
		return timeCommands;
	}

	@Cmd(value="Commands for modifying kits.")
	public BaseCommand kit() {
		return kitCommands;
	}

	@Cmd(value="Language based commands.")
	public BaseCommand language() {
		return langaugeCommands;
	}

	@Cmd(value="Karma based commands.")
	public BaseCommand karma() {
		return karmaCommands;
	}

	@Cmd(value="Set the location to spawn at when registering a persona.")
	public void setspawn(CommandSender sender) {
		if (sender instanceof Player) {
			Player p = (Player) sender;
			plugin.setSpawnLocation(p.getLocation());
			msg(RPPersonas.PRIMARY_DARK + "Spawn updated to your location.");
		} else {
			msg("Console stahp it.");
		}
	}

	@Cmd(value="Set the location to go to when one's persona dies.")
	public void setdeath(CommandSender sender) {
		if (sender instanceof Player) {
			Player p = (Player) sender;
			plugin.setDeathLocation(p.getLocation());
			msg(RPPersonas.PRIMARY_DARK + "Death Zone updated to your location.");
		} else {
			msg("Console stahp it.");
		}
	}

	@Cmd(value="Accept a user. This updates their role and registers them.")
	public void accept(CommandSender sender,
					   @Arg(value="Player Name", description="The username of the player you're accepting.") String player,
					   @Arg(value="Forum ID", description="The forum ID of the player you're accepting.") int forumID) {
		UUID uuid = PlayerUtil.getPlayerUUID(player);
		if (uuid != null) {
			RegisteredServiceProvider<LuckPerms> provider = Bukkit.getServicesManager().getRegistration(LuckPerms.class);
			if (provider != null) {
				LuckPerms api = provider.getProvider();
				CompletableFuture<User> userFuture = api.getUserManager().loadUser(uuid);
				userFuture.thenAcceptAsync(user -> {
					for (Node node : user.getDistinctNodes()) {
						if (node.getKey().equalsIgnoreCase("rppersonas.accepted")) {
							msg(RPPersonas.PRIMARY_DARK + "That player is already accepted.");
							return;
						}
					}
					user.setPrimaryGroup("accepted");
				});
			}

			Player p = Bukkit.getPlayer(uuid);
			if (p != null && p.isOnline()) {
				plugin.getAccountHandler().addLink(p, forumID);
				p.kickPlayer("You've been accepted! Please relog.");
			} else {
				plugin.getAccountHandler().addLink(uuid, forumID);
			}
			msg(RPPersonas.PRIMARY_DARK + "Player successfully accepted.");
		}
	}

}
