package net.korvic.rppersonas.commands;

import co.lotc.core.command.annotate.Arg;
import co.lotc.core.command.annotate.Cmd;
import net.korvic.rppersonas.RPPersonas;
import net.korvic.rppersonas.kits.Kit;
import net.korvic.rppersonas.kits.KitCreateHolder;
import net.korvic.rppersonas.kits.KitHandler;
import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;

public class KitCommands extends BaseCommand {

	RPPersonas plugin;

	public KitCommands(RPPersonas plugin) {
		this.plugin = plugin;
	}

	@Cmd(value="Create a new kit and add it to the list.")
	public void create(CommandSender sender,
					   @Arg(value="Name", description="The name for the kit you're creating.") String name) {
		if (sender instanceof Player) {
			Inventory inv = Bukkit.createInventory(new KitCreateHolder(name), KitHandler.KIT_SIZE);
			((Player) sender).openInventory(inv);
		}
	}

	@Cmd(value="Edit a kit that already exists.")
	public void edit(CommandSender sender, Kit kit) {
		if (sender instanceof Player) {
			plugin.getKitHandler().editKit((Player) sender, kit);
		}
	}

	@Cmd(value="Delete a kit from existence.")
	public void delete(CommandSender sender, Kit kit) {
		plugin.getKitHandler().deleteKit(kit);
	}

}
