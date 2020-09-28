package net.korvic.rppersonas.players;

import lombok.Getter;
import net.korvic.rppersonas.players.personas.PersonaSkin;
import net.korvic.rppersonas.players.statuses.StatusEntry;
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

	/**
	 * @param personaID The Lost Fables account ID
	 * @return An Account object which represents the given Lost Fables account ID.
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
		private boolean staffNameEnabled;
		private String[] namePieces = new String[2];

		// Skin
		private PersonaSkin activeSkin;

		// Statuses
		private List<StatusEntry> activeStatuses = new ArrayList<>();

		private PersonaData() {
			// Create additional detail and load a player into the persona.
		}

	}
}
