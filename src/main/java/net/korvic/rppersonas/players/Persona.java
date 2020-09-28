package net.korvic.rppersonas.players;

import lombok.Getter;
import lombok.Setter;
import net.korvic.rppersonas.personas.PersonaSkin;
import net.korvic.rppersonas.statuses.StatusEntry;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;

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

	public static void unloadPersona(int personaID) {
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
	@Getter private String nickname;

	// Inv & Stash
	@Getter private String inventory;
	@Getter private Inventory enderChest;

	// If the persona is loaded we need additional data
	private PersonaData additionalData;

	private Persona(int personaID) {
		// Create a new persona base
	}

	private void unload() {
		// Save and unload persona data
	}


	/**
	 * A sub-class for data that's only loaded when a person is playing as this persona.
	 */
	private static class PersonaData {

		// Name
		private String prefix;
		private String[] namePieces = new String[2];
		private boolean staffNameEnabled = false;

		// Skin
		private PersonaSkin activeSkin = null;

		// Statuses
		private List<StatusEntry> activeStatuses = new ArrayList<>();

		private PersonaData() {
			// Create additional detail and load a player into the persona.
		}

	}
}
