package net.korvic.rppersonas.players.personas;

import lombok.Getter;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryHolder;

public class PersonaEnderHolder implements InventoryHolder {

	@Getter
	private int personaID;

	public PersonaEnderHolder(int personaID) {
		this.personaID = personaID;
	}

	@Override
	public Inventory getInventory() {
		return null;
	}

}
