package net.korvic.rppersonas.kits;

import co.lotc.core.bukkit.menu.Menu;
import co.lotc.core.bukkit.menu.MenuAction;
import co.lotc.core.bukkit.menu.MenuAgent;
import co.lotc.core.bukkit.menu.icon.Button;
import co.lotc.core.bukkit.menu.icon.Icon;
import co.lotc.core.bukkit.util.InventoryUtil;
import lombok.Getter;
import net.korvic.rppersonas.RPPersonas;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;

import java.util.ArrayList;
import java.util.List;

public class Kit {

	@Getter private String name;
	@Getter private List<ItemStack> items;

	public Kit(String name, List<ItemStack> items) {
		this.name = name;
		setItems(items);
	}

	public String getPermission() {
		return RPPersonas.PERMISSION_START + ".kit." + name.toLowerCase();
	}

	public void setItems(List<ItemStack> items) {
		while(items.contains(null)) {
			items.remove(null);
		}
		this.items = items;
		RPPersonas.get().updateConfigForKit(this);
	}

	public Menu getPreview() {
		List<Icon> icons = new ArrayList<>();

		for (ItemStack item : items) {
			icons.add(new Button() {
				@Override
				public ItemStack getItemStack(MenuAgent menuAgent) {
					return item;
				}

				@Override
				public void click(MenuAction menuAction) {}
			});
		}

		return Menu.fromIcons(ChatColor.BOLD + name, icons);
	}

}
