package net.korvic.rppersonas.kits;

import co.lotc.core.bukkit.util.InventoryUtil;
import lombok.Getter;
import net.korvic.rppersonas.RPPersonas;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;

import java.util.List;

public class Kit {

	@Getter private String name;
	@Getter private List<ItemStack> items;

	public Kit(String name, String items) {
		this.name = name;
		setItems(InventoryUtil.deserializeItems(items));
	}

	public Kit(String name, List<ItemStack> items) {
		this.name = name;
		setItems(items);
	}

	public String getPermission() {
		return RPPersonas.PERMISSION_START + ".kit." + name.toLowerCase();
	}

	public void setItems(List<ItemStack> items) {
		this.items = items;
		// TODO - Save to config.
	}

}
