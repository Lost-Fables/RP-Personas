package net.korvic.rppersonas.players;

import co.lotc.core.bukkit.util.InventoryUtil;
import lombok.Getter;
import net.korvic.rppersonas.RPPersonas;
import net.korvic.rppersonas.players.personas.PersonaEnderHolder;
import net.korvic.rppersonas.players.personas.PersonaSkin;
import net.korvic.rppersonas.players.statuses.StatusEntry;
import net.korvic.rppersonas.sql.PersonasSQL;
import net.korvic.rppersonas.sql.util.DataMapFilter;
import org.bukkit.Bukkit;
import org.bukkit.event.inventory.InventoryType;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.PlayerInventory;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * The base persona class to load and read data from.
 */
public class Persona {

	////////////////
	//// STATIC ////
	////////////////

	private static Map<Integer, Persona> loadedPersonas = new HashMap<>();
	private static List<Integer> loadBlocked = new ArrayList<>();

	/**
	 * @param personaID The Lost Fables persona ID
	 * @return A Persona object which represents the given Lost Fables persona ID.
	 */
	public static Persona getPersona(int personaID) {
		Persona p = null;
		if (personaID > 0 && !loadBlocked.contains(personaID)) {
			p = loadedPersonas.get(personaID);
			if (p == null) {
				p = new Persona(personaID);
				loadedPersonas.put(personaID, p);
			}
		}
		return p;
	}

	/**
	 * @param personaID Forcefully unload the given persona ID. This may kick players back to the main menu
	 *                  and/or to the lobby itself.
	 */
	protected static void unloadPersona(int personaID) {
		loadBlocked.add(personaID);
		Persona p = loadedPersonas.get(personaID);
		if (p != null) {
			p.unload();
			loadedPersonas.remove(personaID);
		}
		loadBlocked.remove(personaID);
	}


	//////////////////
	//// INSTANCE ////
	//////////////////

	// Status
	@Getter private boolean alive;

	// ID data
	@Getter private int accountID;
	@Getter	private int personaID;

	// Quick reference name
	@Getter private String name;
	@Getter private String nickname;

	// Inv & Stash
	@Getter private String savedInventory;
	@Getter private Inventory enderChest;

	// If the persona is loaded we need additional data
	private PersonaData additionalData;

	private Persona(int personaID) {
		DataMapFilter data = RPPersonas.get().getPersonasSQL().getBasicPersonaInfo(personaID);

		if (data.containsKey(PersonasSQL.ALIVE)) {
			this.alive = (boolean) data.get(PersonasSQL.ALIVE);
		} else {
			this.alive = true;
		}

		this.accountID = RPPersonas.get().getPersonaAccountMapSQL().getAccountOf(personaID);
		this.personaID = personaID;

		if (data.containsKey(PersonasSQL.NAME)) {
			if (data.containsKey(PersonasSQL.NICKNAME)) {
				this.nickname = (String) data.get(PersonasSQL.NICKNAME);
			} else {
				this.nickname = (String) data.get(PersonasSQL.NAME);
			}
			this.name = (String) data.get(PersonasSQL.NAME);
		} else {
			this.name = "Unknown Persona";
			this.nickname = "Unknown Persona";
		}

		if (data.containsKey(PersonasSQL.INVENTORY)) {
			this.savedInventory = (String) data.get(PersonasSQL.INVENTORY);
		}

		if (data.containsKey(PersonasSQL.ENDERCHEST)) {
			this.enderChest = Bukkit.createInventory(new PersonaEnderHolder(), InventoryType.ENDER_CHEST, this.nickname + "'s Stash");
			String personaEnderData = (String) data.get(PersonasSQL.ENDERCHEST);
			ItemStack[] items = InventoryUtil.deserializeItemsToArray(personaEnderData);
			this.enderChest.setContents(items);
		}
	}

	private void unload() {
		this.additionalData.unload();
		this.additionalData = null;
		// Save and unload persona data
	}


	/**
	 * A sub-class for data that's only loaded when a person is playing as this persona.
	 */
	private static class PersonaData {

		// Name
		@Getter private String prefix;
		@Getter private boolean staffNameEnabled;
		@Getter private String[] namePieces = new String[2];

		// Skin
		private PersonaSkin activeSkin;

		// Statuses
		private List<StatusEntry> activeStatuses = new ArrayList<>();

		// Linked Player
		@Getter private RPPlayer rpPlayer;

		private PersonaData() {
			// Create additional detail and load a player into the persona.
		}

		/**
		 * Unloads the given additional data such that the Persona is no longer in use.
		 */
		public void unload() {

		}

	}
}
