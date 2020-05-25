package net.korvic.rppersonas.kits;

import lombok.Getter;
import net.korvic.rppersonas.RPPersonas;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;

import java.util.ArrayList;
import java.util.List;

public class KitHandler {

	private RPPersonas plugin;
	@Getter private List<Kit> allKits = new ArrayList<>();

	public KitHandler(RPPersonas plugin) {
		this.plugin = plugin;
	}

	public void addKit(Kit kit) {
		if (getKit(kit.getName()) == null) {
			allKits.add(kit);
		}
	}

	public Kit getKit(String name) {
		for (Kit kit : allKits) {
			if (kit.getName().equalsIgnoreCase(name)) {
				return kit;
			}
		}
		return null;
	}

	public void updateKit(Player p, Kit kit) {
		if (kit != null) {
			Inventory inv = Bukkit.createInventory(new KitEditHolder(kit), 2 * 9);
			for (ItemStack item : kit.getItems()) {
				inv.addItem(item);
			}
		}
	}

}
