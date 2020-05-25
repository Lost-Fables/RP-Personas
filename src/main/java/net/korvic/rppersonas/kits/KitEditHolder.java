package net.korvic.rppersonas.kits;

import lombok.Getter;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryHolder;

public class KitEditHolder implements InventoryHolder {

	@Getter private Kit kit;

	public KitEditHolder(Kit kit) {
		this.kit = kit;
	}

	@Override
	public Inventory getInventory() {
		return null;
	}
}
