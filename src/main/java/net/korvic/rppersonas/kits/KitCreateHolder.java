package net.korvic.rppersonas.kits;

import lombok.Getter;
import org.bukkit.Material;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryHolder;

public class KitCreateHolder implements InventoryHolder {

	@Getter private String name;

	public KitCreateHolder(String name) {
		this.name = name;
	}

	@Override
	public Inventory getInventory() {
		return null;
	}

}
