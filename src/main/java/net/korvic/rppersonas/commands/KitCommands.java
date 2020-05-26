package net.korvic.rppersonas.commands;

import co.lotc.core.bukkit.util.InventoryUtil;
import co.lotc.core.command.annotate.Arg;
import co.lotc.core.command.annotate.Cmd;
import net.korvic.rppersonas.RPPersonas;
import net.korvic.rppersonas.kits.Kit;
import net.korvic.rppersonas.kits.KitCreateHolder;
import net.korvic.rppersonas.kits.KitHandler;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;

import java.util.List;

public class KitCommands extends BaseCommand {

	RPPersonas plugin;

	public KitCommands(RPPersonas plugin) {
		this.plugin = plugin;
	}

	@Cmd(value="Create a new kit and add it to the list.")
	public void create(CommandSender sender,
					   @Arg(value="Name", description="The name for the kit you're creating.") String name,
					   @Arg(value="Icon", description="The material to represent this kit.") Material mat) {
		if (sender instanceof Player) {
			Inventory inv = Bukkit.createInventory(new KitCreateHolder(name, mat), KitHandler.KIT_SIZE);
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
		msg(RPPersonas.SECONDARY_DARK + kit.getName() + RPPersonas.PRIMARY_DARK + " has been successfully deleted.");
	}

	@Cmd(value="Get a set of the items from a given kit.")
	public void get(CommandSender sender, Kit kit) {
		if (sender instanceof Player) {
			Player p = (Player) sender;
			for (ItemStack item : kit.getItems()) {
				if (item != null) {
					InventoryUtil.addOrDropItem(p, item);
				}
			}
			msg(RPPersonas.PRIMARY_DARK + "Items from the " + RPPersonas.SECONDARY_DARK + kit.getName() + RPPersonas.PRIMARY_DARK + " kit spawned.");
		}
	}

}
