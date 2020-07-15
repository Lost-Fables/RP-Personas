package net.korvic.rppersonas.commands;

import co.lotc.core.bukkit.util.PlayerUtil;
import co.lotc.core.command.annotate.Arg;
import co.lotc.core.command.annotate.Cmd;
import co.lotc.core.util.MojangCommunicator;
import net.korvic.rppersonas.BoardManager;
import net.korvic.rppersonas.RPPersonas;
import net.korvic.rppersonas.personas.Persona;
import net.luckperms.api.LuckPerms;
import net.luckperms.api.model.user.User;
import net.luckperms.api.node.Node;
import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.plugin.RegisteredServiceProvider;

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
	StaffPersonaCommands staffPersonaCommands;

	public RPPCommands(RPPersonas plugin, TimeCommands timeCommands) {
		this.plugin = plugin;

		this.altarCommands = new AltarCommands(plugin);
		this.timeCommands = timeCommands;
		this.kitCommands = new KitCommands(plugin);
		this.langaugeCommands = new LanguageCommands(plugin);
		this.karmaCommands = new KarmaCommands(plugin);
		this.rezCommands = new RezCommands(plugin);
		this.staffPersonaCommands = new StaffPersonaCommands(plugin);
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

	@Cmd(value="Staff persona handling commands.", permission=RPPersonas.PERMISSION_START + ".managepersonas")
	public BaseCommand personas() {
		return staffPersonaCommands;
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
				api.getUserManager().savePlayerData(uuid, PlayerUtil.getPlayerName(uuid));
				CompletableFuture<User> userFuture = api.getUserManager().loadUser(uuid);
				userFuture.whenCompleteAsync(new BiConsumer<User, Throwable>() {
					@Override
					public void accept(User user, Throwable throwable) {
						if (throwable != null) {
							return;
						}
						Node accepted = Node.builder("group.accepted").build();
						user.data().add(accepted);
						api.getUserManager().saveUser(user);
						api.getUserManager().cleanupUser(user);
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

	@Cmd(value = "Toggle the colouring on one's nameplate.", permission = RPPersonas.PERMISSION_START + ".staff")
	public void toggleTag(CommandSender sender) {
		if (sender instanceof Player) {
			Player p = (Player) sender;
			Persona pers = plugin.getPersonaHandler().getLoadedPersona(p);
			pers.setStaffNameEnabled(!pers.isStaffNameEnabled());
			pers.setNickName(pers.getNickName());
			if (pers.isStaffNameEnabled()) {
				msg(RPPersonas.PRIMARY_DARK + "You are now displaying your staff colour.");
			} else {
				msg(RPPersonas.PRIMARY_DARK + "You are no longer displaying your staff colour.");
			}
		} else {
			msg(RPPersonas.PRIMARY_DARK + "Stahp it, console.");
		}
	}

	@Cmd(value="Move all data from one account ID to a new account ID", permission=RPPersonas.PERMISSION_START + ".datashuffle")
	public void shuffle(CommandSender sender,
						@Arg(value="Account From") int from,
						@Arg(value="Account To") int to) {
		plugin.getUuidAccountMapSQL().moveAllAccounts(from, to);
		plugin.getSkinsSQL().moveAllAccounts(from, to);
		plugin.getPersonaAccountMapSQL().moveAllAccounts(from, to);
		plugin.getDeathSQL().moveAllAccounts(from, to);
		plugin.getAccountsSQL().moveAllAccounts(from, to);
		msg(RPPersonas.PRIMARY_DARK + "Successfully remapped all account IDs. Please give this a moment to reflect in the database.");
	}

}
