package net.korvic.rppersonas.personas;

import co.lotc.core.bukkit.util.InventoryUtil;
import org.bukkit.inventory.ItemStack;

import java.util.ArrayList;
import java.util.List;

public class PersonaInventory {

	private List<ItemStack> inventory = new ArrayList<>();

	public PersonaInventory(String inventoryData) {
		updateInventoryContents(inventoryData);
	}

	public List<ItemStack> getInventoryContents() {
		return inventory;
	}

	public void updateInventoryContents(String inventoryData) {
		if (inventoryData != null) {
			updateInventoryContents(InventoryUtil.deserializeItems(inventoryData));
		}
	}

	public void updateInventoryContents(List<ItemStack> inventory) {
		this.inventory = inventory;
	}

}
