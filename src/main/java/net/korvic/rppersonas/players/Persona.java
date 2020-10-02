package net.korvic.rppersonas.players;

import co.lotc.core.bukkit.util.InventoryUtil;
import lombok.Getter;
import net.korvic.rppersonas.RPPersonas;
import net.korvic.rppersonas.players.personas.PersonaEnderHolder;
import net.korvic.rppersonas.players.personas.PersonaSkin;
import net.korvic.rppersonas.players.statuses.StatusEntry;
import net.korvic.rppersonas.sql.PersonaAccountsMapSQL;
import net.korvic.rppersonas.sql.PersonasSQL;
import net.korvic.rppersonas.sql.util.DataMapFilter;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
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
		Persona persona = null;
		if (personaID > 0) {
			if (!loadBlocked.contains(personaID)) {
				persona = loadedPersonas.get(personaID);
			}
			if (persona == null) {
				persona = new Persona(personaID);
				loadedPersonas.put(personaID, persona);
			}
		}
		return persona;
	}

	/**
	 * @param personaID The Lost Fables persona ID
	 * @return A Persona object if, and only if, this persona is loaded and not actively load blocked.
	 */
	public static Persona getLoadedPersona(int personaID) {
		Persona output = null;
		if (!loadBlocked.contains(personaID)) {
			output = loadedPersonas.get(personaID);
		}
		return output;
	}

	/**
	 * @param personaID Forcefully unload the given persona ID. This may kick players back to the main menu
	 *                  and/or to the lobby itself.
	 */
	protected static void unloadPersona(int personaID) {
		loadBlocked.add(personaID);
		Persona persona = loadedPersonas.get(personaID);
		if (persona != null) {
			persona.unload();
			loadedPersonas.remove(personaID);
		}
		loadBlocked.remove(personaID);
	}

	protected static void cleanup(int personaID) {
		Persona persona = loadedPersonas.get(personaID);
		if (persona != null && persona.playerInteraction == null && !persona.loadLocked) {
			persona.unload();
		}
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
	private PlayerInteraction playerInteraction;

	// Load Locking to prevent loading into something being unloaded or unloading twice
	private boolean loadLocked = false;

	// Has had additionalData loaded (assigned/loaded by player), or any details changed.
	private boolean altered = false;

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

	// LOAD & SAVE
	private void unload() {
		loadLocked = true;
		this.playerInteraction.unload();
		this.playerInteraction = null;
		save();
		loadedPersonas.remove(personaID);
	}

	/**
	 * @param player Load the given player into this persona for use.
	 */
	public void loadPlayer(Player player) {
		if (!loadLocked) {
			this.altered = true;
			this.playerInteraction = new PlayerInteraction(player);
		}
	}

	/**
	 * Saves the given persona's data if it has possibly been altered.
	 */
	public void save() {
		this.loadLocked = true;
		if (this.altered) {
			if (playerInteraction != null) {
				playerInteraction.save();
			} else {
				RPPersonas.get().getPersonasSQL().registerOrUpdate(getBaseInfo());
			}
		}
		this.loadLocked = false;
	}

	// ACCESSORS

	/**
	 * @return A map of Language Name to Language Level for this Persona.
	 */
	public Map<String, Short> getLanguages() {
		return RPPersonas.get().getLanguageSQL().getLanguages(personaID);
	}

	private DataMapFilter getBaseInfo() {
		if (playerInteraction != null) {
			playerInteraction.updateSavedInventory();
		}

		DataMapFilter data = new DataMapFilter();
		data.put(PersonaAccountsMapSQL.ACCOUNTID, accountID)
			.put(PersonasSQL.PERSONAID, personaID)
			.put(PersonasSQL.ALIVE, alive)
			.put(PersonasSQL.INVENTORY, savedInventory)
			.put(PersonasSQL.NAME, name);
		if (nickname.equals(name)) {
			data.put(PersonasSQL.NICKNAME, null);
		} else {
			data.put(PersonasSQL.NICKNAME, nickname);
		}
		if (enderChest != null) {
			data.put(PersonasSQL.ENDERCHEST, InventoryUtil.serializeItems(enderChest));
		}
		return data;
	}

	// MODIFIERS

	/**
	 * @param name Sets the name to the given String.
	 */
	public void setName(String name) {
		this.altered = true;
		this.name = name;
	}

	/**
	 * @param inventory Update the saved inventory contents to the one provided.
	 */
	public void setSavedInventory(PlayerInventory inventory) {
		this.savedInventory = InventoryUtil.serializeItems(inventory);
	}


	/**
	 * A sub-class for data that's only loaded when a person is playing as this persona.
	 * NOTE: Player interactions should ONLY go within this class.
	 */
	private class PlayerInteraction {

		// Name
		@Getter private String prefix;
		@Getter private boolean staffNameEnabled;
		@Getter private String[] namePieces = new String[2];

		// Skin
		@Getter private PersonaSkin activeSkin;

		// Statuses
		private List<StatusEntry> activeStatuses = new ArrayList<>();

		// Linked Player
		@Getter private RPPlayer rpPlayer;

		private PlayerInteraction(Player player) {
			// Create additional detail and load a player into the persona.
		}

		// LOAD & SAVE
		/**
		 * Unloads the given additional data such that the Persona is no longer in use.
		 */
		public void unload() {
			// Send player back to menu if they're still online.
		}

		/**
		 * Saves data for this persona with the active RPPlayer.
		 */
		public void save() {
			Player player = rpPlayer.getPlayer();
			DataMapFilter data = getBaseInfo();
			data.put(PersonasSQL.PREFIX, prefix);
			if (player != null) {
				data.put(PersonasSQL.LOCATION, player.getLocation())
					.put(PersonasSQL.HEALTH, player.getHealth())
					.put(PersonasSQL.HUNGER, player.getFoodLevel());
			}
			if (activeSkin != null) {
				data.put(PersonasSQL.SKINID, activeSkin.getSkinID());
			} else {
				data.put(PersonasSQL.SKINID, 0);
			}
			RPPersonas.get().getPersonasSQL().registerOrUpdate(data);
			// Save data with the given RPPlayer
		}

		private void updateSavedInventory() {
			setSavedInventory(rpPlayer.getPlayer().getInventory());
		}

		// ACCESSORS //
		/**
		 * @return A formatted name without colours for the given character as [Prefix] Name
		 */
		public String getChatName() {
			if (prefix != null) {
				return "[" + prefix + "] " + nickname;
			} else {
				return nickname;
			}
		}

		/**
		 * @return The active skin ID.
		 */
		public int getActiveSkinID() {
			if (activeSkin != null) {
				return activeSkin.getSkinID();
			} else {
				return 0;
			}
		}

	}
}
