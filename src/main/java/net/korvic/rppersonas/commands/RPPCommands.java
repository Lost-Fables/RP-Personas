package net.korvic.rppersonas.commands;

import co.lotc.core.bukkit.util.PlayerUtil;
import co.lotc.core.command.annotate.Arg;
import co.lotc.core.command.annotate.Cmd;
import net.korvic.rppersonas.BoardManager;
import net.korvic.rppersonas.RPPersonas;
import net.luckperms.api.LuckPerms;
import net.luckperms.api.model.user.User;
import net.luckperms.api.node.Node;
import net.luckperms.api.node.types.InheritanceNode;
import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.plugin.RegisteredServiceProvider;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.function.BiConsumer;

public class RPPCommands extends BaseCommand {

	RPPersonas plugin;

	AltarCommands altarCommands;
	TimeCommands timeCommands;
	KitCommands kitCommands;
	LanguageCommands langaugeCommands;
	KarmaCommands karmaCommands;
	RezCommands rezCommands;

	public RPPCommands(RPPersonas plugin, TimeCommands timeCommands) {
		this.plugin = plugin;

		this.altarCommands = new AltarCommands(plugin);
		this.timeCommands = timeCommands;
		this.kitCommands = new KitCommands(plugin);
		this.langaugeCommands = new LanguageCommands(plugin);
		this.karmaCommands = new KarmaCommands(plugin);
		this.rezCommands = new RezCommands(plugin);
	}

	@Cmd(value="Commands for modifying altars.", permission=RPPersonas.PERMISSION_START + ".altars")
	public BaseCommand altar() {
		return altarCommands;
	}

	@Cmd(value="Commands for adjusting the time.", permission=RPPersonas.PERMISSION_START + ".time")
	public BaseCommand time() {
		return timeCommands;
	}

	@Cmd(value="Commands for modifying kits.", permission=RPPersonas.PERMISSION_START + ".kits")
	public BaseCommand kit() {
		return kitCommands;
	}

	@Cmd(value="Language based commands.", permission=RPPersonas.PERMISSION_START + ".language")
	public BaseCommand language() {
		return langaugeCommands;
	}

	@Cmd(value="Karma based commands.", permission=RPPersonas.PERMISSION_START + ".karma")
	public BaseCommand karma() {
		return karmaCommands;
	}

	@Cmd(value="Resurrection based commands.", permission=RPPersonas.PERMISSION_START + ".rez")
	public BaseCommand rez() {
		return rezCommands;
	}

	@Cmd(value="Set the location to spawn at when registering a persona.", permission=RPPersonas.PERMISSION_START + ".admin")
	public void setspawn(CommandSender sender) {
		if (sender instanceof Player) {
			Player p = (Player) sender;
			plugin.setSpawnLocation(p.getLocation());
			msg(RPPersonas.PRIMARY_DARK + "Spawn updated to your location.");
		} else {
			msg("Console stahp it.");
		}
	}

	@Cmd(value="Set the location to go to when one's persona dies.", permission=RPPersonas.PERMISSION_START + ".admin")
	public void setdeath(CommandSender sender) {
		if (sender instanceof Player) {
			Player p = (Player) sender;
			plugin.setDeathLocation(p.getLocation());
			msg(RPPersonas.PRIMARY_DARK + "Death Zone updated to your location.");
		} else {
			msg("Console stahp it.");
		}
	}

	@Cmd(value="Accept a user. This updates their role and registers them.", permission=RPPersonas.PERMISSION_START + ".accept")
	public void accept(CommandSender sender,
					   @Arg(value="Player Name", description="The username of the player you're accepting.") String player,
					   @Arg(value="Forum ID", description="The forum ID of the player you're accepting.") int forumID) {
		UUID uuid = PlayerUtil.getPlayerUUID(player);
		if (uuid != null) {
			RegisteredServiceProvider<LuckPerms> provider = Bukkit.getServicesManager().getRegistration(LuckPerms.class);
			if (provider != null) {
				LuckPerms api = provider.getProvider();
				CompletableFuture<User> userFuture = api.getUserManager().loadUser(uuid);
				userFuture.whenCompleteAsync(new BiConsumer<User, Throwable>() {
					@Override
					public void accept(User user, Throwable throwable) {
						if (throwable != null) {
							return;
						}
						Node accepted = Node.builder("group.accepted").build();
						user.data().clear();
						user.data().add(accepted);
						api.getUserManager().saveUser(user);
					}
				}, runnable -> Bukkit.getScheduler().runTask(plugin, runnable));
			}

			Player p = Bukkit.getPlayer(uuid);
			if (p != null && p.isOnline()) {
				plugin.getAccountHandler().addLink(p, forumID);
			} else {
				plugin.getAccountHandler().addLink(uuid, forumID);
			}
			msg(RPPersonas.PRIMARY_DARK + "Player successfully accepted.");
		}
	}

	@Cmd(value="Force a full clean of the scoreboard for persona name.", permission=RPPersonas.PERMISSION_START + ".refresh")
	public void refresh() {
		BoardManager.forceFullClean();
	}

}
