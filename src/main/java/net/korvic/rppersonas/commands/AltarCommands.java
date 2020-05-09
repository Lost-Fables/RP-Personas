package net.korvic.rppersonas.commands;

import co.lotc.core.bukkit.util.InventoryUtil;
import co.lotc.core.command.annotate.Cmd;
import net.korvic.rppersonas.RPPersonas;
import net.korvic.rppersonas.death.Altar;
import net.korvic.rppersonas.personas.Persona;
import net.korvic.rppersonas.statuses.EtherealStatus;
import org.bukkit.Location;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.dynmap.DynmapCommonAPI;

public class AltarCommands extends BaseCommand {

	private static final String CREATION_SUCCESS = "A new altar has been created where you stand.";
	private static final String CREATION_FAILURE = "Failed to create altar. Check the name isn't already used and try again.";
	private static final String CONSOLE = "Only players may run this command.";

	private RPPersonas plugin;
	private DynmapCommonAPI dynmapAPI;

	public AltarCommands(RPPersonas plugin) {
		this.plugin = plugin;
		this.dynmapAPI = (DynmapCommonAPI) plugin.getServer().getPluginManager().getPlugin("Dynmap");
	}

	@Cmd(value = "Create a new resurrection altar at your location.")
	public void create(CommandSender sender, String name) {
		if (sender instanceof Player) {
			Player p = (Player) sender;
			float yaw = roundYaw(p.getLocation().getYaw());
			Location blockLoc = p.getLocation().toBlockLocation();
			blockLoc.setYaw(yaw);
			blockLoc.setPitch(0);

			if (plugin.getAltarHandler().createAltar(name, blockLoc) != null) {
				msg(RPPersonas.PRIMARY_DARK + CREATION_SUCCESS);
			} else {
				msg(RPPersonas.PRIMARY_DARK + CREATION_FAILURE);
			}
		} else {
			msg(RPPersonas.PRIMARY_DARK + CONSOLE);
		}
	}

	@Cmd(value = "Teleport to a given altar.")
	public void tp(CommandSender sender, Altar altar) {
		if (sender instanceof Player) {
			((Player) sender).teleportAsync(altar.getTPLocation());
			msg(RPPersonas.PRIMARY_DARK + "You've been teleported to " + altar.getLabel() + ".");
		} else {
			msg(RPPersonas.PRIMARY_DARK + CONSOLE);
		}
	}

	@Cmd(value = "Debug command.")
	public void getCorpse(CommandSender sender, Player player) {
		if (sender instanceof Player) {
			Player p = (Player) sender;
			InventoryUtil.addOrDropItem(p, plugin.getCorpseHandler().createCorpse(player).getItem());
		} else {
			msg(RPPersonas.PRIMARY_DARK + CONSOLE);
		}
	}

	@Cmd(value = "Debug command.")
	public void toggleGhost(Player sender) {
		Persona pers = plugin.getPersonaHandler().getLoadedPersona(sender);
		if (pers != null) {
			if (pers.hasStatus(EtherealStatus.NAME)) {
				pers.clearStatus(EtherealStatus.NAME);
			} else {
				pers.addStatus(new EtherealStatus(-1));
			}
		}
	}

	private float roundYaw(float yaw) {
		int halfwayGap = 45;
		if (yaw >= 180-halfwayGap) {
			yaw = 180;
		} else if (yaw >= 90-halfwayGap) {
			yaw = 90;
		} else if (yaw >= -halfwayGap) {
			yaw = 0;
		} else if (yaw >= -90-halfwayGap) {
			yaw = -90;
		} else {
			yaw = -180;
		}

		return yaw;
	}

}
