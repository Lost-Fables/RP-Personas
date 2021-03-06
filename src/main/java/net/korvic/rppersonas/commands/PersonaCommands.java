package net.korvic.rppersonas.commands;

import co.lotc.core.bukkit.util.InventoryUtil;
import co.lotc.core.bukkit.util.ItemUtil;
import co.lotc.core.bukkit.util.LocationUtil;
import co.lotc.core.bukkit.util.PlayerUtil;
import co.lotc.core.command.annotate.Arg;
import co.lotc.core.command.annotate.Cmd;
import co.lotc.core.command.annotate.Default;
import co.lotc.core.util.DataMapFilter;
import net.korvic.rppersonas.RPPersonas;
import net.korvic.rppersonas.death.Corpse;
import net.korvic.rppersonas.death.CorpseHandler;
import net.korvic.rppersonas.listeners.CorpseListener;
import net.korvic.rppersonas.listeners.SkinDisplayListener;
import net.korvic.rppersonas.personas.Persona;
import net.korvic.rppersonas.personas.PersonaSkin;
import net.korvic.rppersonas.sql.KarmaSQL;
import net.korvic.rppersonas.statuses.DisabledStatus;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.ArrayList;
import java.util.List;

public class PersonaCommands extends BaseCommand {

	protected static final String NO_CONSOLE = "The console does not have a persona.";

	private static final double EXECUTE_DISTANCE = 20;

	private RPPersonas plugin;
	private PersonaSetCommands personaSetCommands;
	private PersonaDescCommands personaDescCommands;
	private StatusCommands statusCommands;

	public PersonaCommands (RPPersonas plugin) {
		this.plugin = plugin;
		this.personaSetCommands = new PersonaSetCommands(plugin);
		this.personaDescCommands = new PersonaDescCommands(plugin);
		this.statusCommands = new StatusCommands(plugin);
	}

	@Cmd(value = "Get the information on someone else's persona.", permission = RPPersonas.PERMISSION_START + ".accepted")
	public void info(CommandSender sender,
					 @Arg(value = "Player", description = "The player who's info you wish to see.") @Default(value = "@p") Player player) {
		msg(plugin.getPersonaHandler().getPersonaInfo(player));
	}

	@Cmd(value = "Execute the given player's current persona by your current persona.", permission = RPPersonas.PERMISSION_START + ".execute")
	public void execute(CommandSender sender,
						@Arg(value = "Player", description = "The player which you're executing.") Player victim) {
		if (sender instanceof Player) {
			Persona victimPersona = plugin.getPersonaHandler().getLoadedPersona(victim);
			if (victimPersona != null && victimPersona.isAlive()) {
				Player killer = (Player) sender;
				if (LocationUtil.isClose(killer, victim, EXECUTE_DISTANCE)) {
					if (!plugin.getDeathHandler().hasRequest(victim)) {
						Persona killerPersona = plugin.getPersonaHandler().getLoadedPersona(killer);
						if (killerPersona != null && victimPersona.getAccountID() != killerPersona.getAccountID()) {
							plugin.getDeathHandler().requestExecute(killer, victim);
							msg(RPPersonas.PRIMARY_DARK + "Execution request sent!");
						} else {
							msg(RPPersonas.PRIMARY_DARK + "You cannot execute your own persona!");
						}
					} else {
						msg(RPPersonas.PRIMARY_DARK + "That player already has an execution request pending!");
					}
				} else {
					msg(RPPersonas.PRIMARY_DARK + "You must be within " + EXECUTE_DISTANCE + " blocks to execute someone.");
				}
			} else {
				msg(RPPersonas.PRIMARY_DARK + "That user is not on a live persona!");
			}
		} else {
			msg(NO_CONSOLE);
		}
	}

	@Cmd(value = "Accept being executed by a given player.", permission = RPPersonas.PERMISSION_START + ".execute")
	public void executeaccept(CommandSender sender,
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

	@Cmd(value = "Ruin a corpse so it can no longer be resurrected.", permission = RPPersonas.PERMISSION_START + ".ruincorpse")
	public void ruinCorpse(CommandSender sender) {
		if (sender instanceof Player) {
			Player p = (Player) sender;
			DisabledStatus status = new DisabledStatus(null);
			status.applyEffect(p, (byte) 1);
			ItemStack corpse = takeCorpseFromHand(p);
			status.clearEffect(p);

			if (corpse != null) {
				ItemMeta meta = corpse.getItemMeta();
				String[] name = meta.getDisplayName().split(" ");
				if (name.length > 0) {
					meta.setDisplayName(RPPersonas.PRIMARY_DARK + "" + ChatColor.BOLD + name[0] + " Ruined Corpse");
				}

				List<String> lore = new ArrayList<>();
				lore.add(RPPersonas.SECONDARY_DARK + "This corpse has been ruined!");
				lore.add(RPPersonas.SECONDARY_DARK + "It may no longer be resurrected, however");
				lore.add(RPPersonas.SECONDARY_DARK + "it may be placed on the ground now.");
				meta.setLore(lore);
				corpse.setItemMeta(meta);

				Persona pers = plugin.getPersonaHandler().getLoadedPersona(p);
				DataMapFilter data = new DataMapFilter().put(KarmaSQL.PERSONAID, pers.getPersonaID())
														.put(KarmaSQL.ACTION, "RUIN_CORPSE")
														.put(KarmaSQL.MODIFIER, plugin.getKarmaSQL().calculateRuinModifier(pers.getPersonaID()));
				plugin.getKarmaSQL().registerOrUpdate(data);

				ItemUtil.removeCustomTag(corpse, CorpseHandler.CORPSE_KEY);
				InventoryUtil.addOrDropItem(p, corpse);
			} else {
				msg(RPPersonas.PRIMARY_DARK + "You must be holding a corpse in your hand in order to ruin it!");
			}
		} else {
			msg(NO_CONSOLE);
		}
	}

	@Cmd(value = "Show the Minecraft Usernames of a player instead of their RP name.", permission = RPPersonas.PERMISSION_START + ".shownames")
	public void showMCnames(CommandSender sender) {
		if (sender instanceof Player) {
			Player p = (Player) sender;
			if (SkinDisplayListener.showingMCNames.contains(p)) {
				SkinDisplayListener.showingMCNames.remove(p);
			} else {
				SkinDisplayListener.showingMCNames.add(p);
			}
			PersonaSkin.refreshOthers(p);

			if (SkinDisplayListener.showingMCNames.contains(p)) {
				msg(RPPersonas.PRIMARY_DARK + "Now showing MC names.");
			} else {
				msg(RPPersonas.PRIMARY_DARK + "Now showing RP names.");
			}
		} else {
			msg(NO_CONSOLE);
		}
	}

	private ItemStack takeCorpseFromHand(Player p) {
		ItemStack corpseItem = p.getInventory().getItemInMainHand();
		boolean isCorpse = false;
		if (ItemUtil.hasCustomTag(corpseItem, CorpseHandler.CORPSE_KEY)) {
			p.getInventory().setItem(CorpseListener.getIndexFromInventory(p.getInventory(), corpseItem), null);
			isCorpse = true;
		} else {
			corpseItem = p.getInventory().getItemInOffHand();
			if (ItemUtil.hasCustomTag(corpseItem, CorpseHandler.CORPSE_KEY)) {
				p.getInventory().setItem(CorpseListener.getIndexFromInventory(p.getInventory(), corpseItem), null);
				isCorpse = true;
			}
		}
		if (isCorpse) {
			return corpseItem;
		} else {
			return null;
		}
	}

	@Cmd(value = "Commands to modify the statuses on your persona.", permission = RPPersonas.PERMISSION_START + ".status")
	public BaseCommand status() {
		return statusCommands;
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