package net.korvic.rppersonas.kits;

import lombok.Getter;
import org.bukkit.Material;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryHolder;

public class KitCreateHolder implements InventoryHolder {

	@Getter private String name;
	@Getter private Material mat;

	public KitCreateHolder(String name, Material mat) {
		this.name = name;
		this.mat = mat;
	}

	@Override
	public Inventory getInventory() {
		return null;
	}

}
