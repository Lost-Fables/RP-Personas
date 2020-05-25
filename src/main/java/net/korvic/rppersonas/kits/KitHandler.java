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
	public static final int KIT_SIZE = 2*9;

	public KitHandler(RPPersonas plugin) {
		this.plugin = plugin;
	}

	public void addKit(Kit kit) {
		if (getKit(kit.getName()) == null) {
			allKits.add(kit);
		}
	}

	public void editKit(Player p, Kit kit) {
		if (kit != null) {
			Inventory inv = Bukkit.createInventory(new KitEditHolder(kit), KIT_SIZE);
			for (ItemStack item : kit.getItems()) {
				if (item != null) {
					inv.addItem(item);
				}
			}
		}
	}

	public void deleteKit(Kit kit) {
		allKits.remove(kit);
	}

	public Kit getKit(String name) {
		for (Kit kit : allKits) {
			if (kit.getName().equalsIgnoreCase(name)) {
				return kit;
			}
		}
		return null;
	}

	public List<String> getKitNameList() {
		List<String> names = new ArrayList<>();
		for (Kit kit : allKits) {
			names.add(kit.getName());
		}
		return names;
	}

}
