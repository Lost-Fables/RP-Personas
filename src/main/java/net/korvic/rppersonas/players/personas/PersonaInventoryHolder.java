package net.korvic.rppersonas.players.personas;

import lombok.Getter;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryHolder;

public class PersonaInventoryHolder implements InventoryHolder {

	@Getter private int personaID;

	public PersonaInventoryHolder(int personaID) {
		this.personaID = personaID;
	}

	@Override
	public Inventory getInventory() {
		return null;
	}

}

