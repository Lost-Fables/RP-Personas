package net.korvic.rppersonas.commands;

import co.lotc.core.command.annotate.Cmd;
import net.korvic.rppersonas.RPPersonas;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.util.Vector;
import org.dynmap.DynmapCommonAPI;

public class AltarCommands extends BaseCommand {

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
			Location blockLoc = p.getLocation().toBlockLocation().subtract(0, 1, 0);
			blockLoc.setYaw(yaw);

			plugin.getAltarHandler().createAltar(name, blockLoc);
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
