package net.korvic.rppersonas.kits;

import co.lotc.core.bukkit.menu.Menu;
import co.lotc.core.bukkit.menu.MenuAction;
import co.lotc.core.bukkit.menu.MenuAgent;
import co.lotc.core.bukkit.menu.MenuUtil;
import co.lotc.core.bukkit.menu.icon.Button;
import co.lotc.core.bukkit.menu.icon.Icon;
import lombok.Getter;
import net.korvic.rppersonas.RPPersonas;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.entity.Item;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

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
			p.openInventory(inv);
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
