package net.korvic.rppersonas.players;

import co.lotc.core.bukkit.book.BookStream;
import co.lotc.core.bukkit.util.BookUtil;
import co.lotc.core.bukkit.util.InventoryUtil;
import com.destroystokyo.paper.Title;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.Setter;
import net.korvic.rppersonas.BoardManager;
import net.korvic.rppersonas.RPPersonas;
import net.korvic.rppersonas.players.conversation.BaseConvo;
import net.korvic.rppersonas.players.conversation.PersonaCreationConvo;
import net.korvic.rppersonas.players.personas.PersonaEnderHolder;
import net.korvic.rppersonas.players.personas.PersonaInventoryHolder;
import net.korvic.rppersonas.players.personas.PersonaLanguage;
import net.korvic.rppersonas.players.personas.PersonaSkin;
import net.korvic.rppersonas.players.statuses.DisabledStatus;
import net.korvic.rppersonas.players.statuses.StatusEntry;
import net.korvic.rppersonas.sql.PersonaAccountsMapSQL;
import net.korvic.rppersonas.sql.PersonasSQL;
import net.korvic.rppersonas.sql.util.DataMapFilter;
import net.korvic.rppersonas.time.TimeManager;
import net.md_5.bungee.api.ChatColor;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryType;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.PlayerInventory;
import org.bukkit.inventory.meta.BookMeta;
import org.bukkit.scheduler.BukkitRunnable;

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

	private static Map<Integer, Persona> loadedPersonas = new HashMap<>();  // Current PersonaID -> Personas List.
	private static Map<Player, Integer> playerPersonaMap = new HashMap<>(); // Current Player -> PersonaID List.
	private static List<Integer> loadBlocked = new ArrayList<>();           // Current list of PersonaIDs that are blocked from loading.
	private static int highestPersonaID = 1;                                // Current highest known PersonaID.

	/**
	 * @param player The player you're searching for.
	 * @return The persona that player is currently assigned to, if it exists.
	 */
	public static Persona getPersona(Player player) {
		int personaID = playerPersonaMap.get(player);
		if (personaID > 0) {
			return loadedPersonas.get(personaID);
		} else {
			return null;
		}
	}

	/**
	 * @param personaID The Lost Fables persona ID
	 * @return A Persona object which represents the given Lost Fables persona ID.
	 * @throws IllegalArgumentException When the Lost Fables persona ID does not return any data.
	 */
	public static Persona getPersona(int personaID) throws IllegalArgumentException {
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

	public static void startCreationConvo(Player player, int accountID, boolean first) {
		new DisabledStatus(null).applyEffect(player, (byte) 0);

		Persona pers = null;
		String welcomeText = "";
		if (first) {
			welcomeText = RPPersonas.PRIMARY_DARK + "" + ChatColor.BOLD + "Welcome!";
		} else {
			pers = Persona.getPersona(player);
			if (pers != null) {
				pers.save();
				pers.loadLocked = true;
			}
		}
		Title title = new Title(welcomeText,
								RPPersonas.SECONDARY_LIGHT + "Type your Persona's name to continue.",
								20, 60*20, 20);

		Persona finalPers = pers;
		new BukkitRunnable() {
			@Override
			public void run() {
				new DisabledStatus(title).applyEffect(player, (byte) 0);
				if (RPPersonas.get().getSpawnLocation() != null) {
					player.teleportAsync(RPPersonas.get().getSpawnLocation());
				}
				player.getInventory().clear();

				Map<Object, Object> data = new HashMap<>();
				data.put(PersonaAccountsMapSQL.ACCOUNTID, accountID);
				data.put(PersonasSQL.ALIVE, true);
				data.put(PersonasSQL.LIVES, RPPersonas.DEFAULT_LIVES);
				data.put(PersonasSQL.PLAYTIME, 0L);
				data.put(PersonasSQL.FRESH, new Object());
				data.put(PersonasSQL.LOCATION, RPPersonas.get().getSpawnLocation());

				if (finalPers != null) {
					data.put("oldpersona", finalPers);
				}

				if (first) {
					data.put(PersonasSQL.FIRST, new Object());
				}

				new PersonaCreationConvo(RPPersonas.get()).startConvo(player, data, !first);
			}
		}.runTask(RPPersonas.get());
	}

	public static Persona createPersona(DataMapFilter data) {
		int personaID = 0;
		if (data.containsKey(PersonasSQL.PERSONAID)) {
			personaID = (int) data.get(PersonasSQL.PERSONAID);
		}
		if (personaID <= 0) {
			personaID = getHighestPersonaID();
		}

		Persona persona = null;
		try {
			persona = getPersona(personaID);
		} catch (IllegalArgumentException iae) {
			data.put(PersonasSQL.PERSONAID, personaID);
			RPPersonas.get().getPersonasSQL().registerOrUpdate(data);
			RPPersonas.get().getPersonaAccountMapSQL().registerOrUpdate(data);
			persona = getPersona(personaID);
		}

		return persona;
	}

	/**
	 * @return Retrieves and returns the new highest persona ID including this one. Automatically checks
	 * to see if the database has something higher than what's currently on record.
	 */
	public static int getHighestPersonaID() {
		int newHighest = RPPersonas.get().getPersonaAccountMapSQL().getHighestPersonaID();
		if (newHighest > highestPersonaID) {
			highestPersonaID = newHighest;
		}
		return ++highestPersonaID;
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
	public static void unloadPersona(int personaID) {
		loadBlocked.add(personaID);
		Persona persona = loadedPersonas.get(personaID);
		if (persona != null) {
			persona.unload();
			loadedPersonas.remove(personaID);
		}
		loadBlocked.remove(personaID);
	}

	/**
	 * Runs unload for all currently loaded Personas.
	 */
	public static void unloadAllPersonas() {
		for (int personaID : loadedPersonas.keySet()) {
			unloadPersona(personaID);
		}
	}

	/**
	 * Saves all currently loaded personas.
	 */
	public static void saveAllPersonas() {
		for (Persona persona : loadedPersonas.values()) {
			persona.save();
		}
	}

	/**
	 * @param personaID Runs cleanup for the given persona.
	 */
	public static void cleanup(int personaID) {
		Persona persona = loadedPersonas.get(personaID);
		if (persona != null && persona.playerInteraction == null && !persona.loadLocked) {
			persona.unload();
		}
	}

	/**
	 * Runs cleanup and all currently loaded personas.
	 */
	public static void cleanupAll() {
		for (int personaID : loadedPersonas.keySet()) {
			cleanup(personaID);
		}
	}

	//////////////////
	//// INSTANCE ////
	//////////////////

	// Status
	@Getter @Setter private boolean alive;

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
	@Getter private PlayerInteraction playerInteraction;

	// Load Locking to prevent loading into something being unloaded or unloading twice
	@Getter private boolean loadLocked = false;

	// Has had additionalData loaded (assigned/loaded by player), or any details changed.
	private boolean altered = false;

	private Persona(int personaID) {
		DataMapFilter data = RPPersonas.get().getPersonasSQL().getBasicPersonaInfo(personaID);

		if (data.isEmpty()) {
			throw new IllegalArgumentException("Did not get proper info for Persona " + personaID);
		}

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
		}

		if (data.containsKey(PersonasSQL.INVENTORY)) {
			this.savedInventory = (String) data.get(PersonasSQL.INVENTORY);
		}

		if (data.containsKey(PersonasSQL.ENDERCHEST)) {
			this.enderChest = Bukkit.createInventory(new PersonaEnderHolder(this.personaID), InventoryType.ENDER_CHEST, this.nickname + "'s Stash");
			String personaEnderData = (String) data.get(PersonasSQL.ENDERCHEST);
			ItemStack[] items = InventoryUtil.deserializeItemsToArray(personaEnderData);
			this.enderChest.setContents(items);
		}
	}

	// LOAD & SAVE
	private void unload() {
		loadLocked = true;
		save();
		if (this.playerInteraction != null) {
			this.playerInteraction.unload();
			this.playerInteraction = null;
		}
		loadedPersonas.remove(personaID);
	}

	/**
	 * @param player Load the given player into this persona for use.
	 */
	public void loadPlayer(Player player) {
		if (!loadLocked) {
			this.altered = true;
			this.playerInteraction = new PlayerInteraction(player);
			playerPersonaMap.put(player, this.personaID);
		}
	}

	/**
	 * Reloads the player that's currently using this persona.
	 */
	public void reloadPlayer() {
		if (playerInteraction != null && playerInteraction.getRpPlayer() != null) {
			loadPlayer(playerInteraction.getRpPlayer().getPlayer());
		}
	}

	/**
	 * Saves the given persona's data if it has possibly been altered.
	 */
	public void save() {
		if (this.altered) {
			this.loadLocked = true;
			save(null);
		}
	}

	public void save(DataMapFilter data) {
		this.loadLocked = true;
		if (data != null) {
			DataMapFilter newData = getBaseInfo();
			newData.putAllData(data);
			data = newData;
		} else {
			data = getBaseInfo();
		}
		if (playerInteraction != null) {
			playerInteraction.save(data);
		} else {
			RPPersonas.get().getPersonasSQL().registerOrUpdate(data);
		}
		this.loadLocked = false;
	}

	// ACCESSORS
	public String getActiveName() {
		if (this.nickname != null && this.nickname.length() > 0) {
			return this.nickname;
		} else {
			return this.name;
		}
	}
	/**
	 * @return A map of Language Name to Language Level for this Persona.
	 */
	public Map<String, Short> getLanguages() {
		return RPPersonas.get().getLanguageSQL().getLanguages(personaID);
	}

	/**
	 * @return An inventory of the player using, or a new inventory representing
	 *         the contents of the persona's inventory for modification. If this is
	 *         a new inventory, the persona will be load locked.
	 */
	public PlayerInventory getInventory() {
		if (playerInteraction != null) {
			return playerInteraction.getRpPlayer().getPlayer().getInventory();
		} else {
			this.loadLocked = true;
			PlayerInventory inv = (PlayerInventory) Bukkit.createInventory(new PersonaInventoryHolder(this.personaID), InventoryType.PLAYER);
			inv.setContents(InventoryUtil.deserializeItemsToArray(savedInventory));
			return inv;
		}
	}

	/**
	 * @return The current stored description for this persona.
	 */
	// TODO: Replace this with just a description grabber alone?
	public String getDescription() {
		return (String) RPPersonas.get().getPersonasSQL().getBasicPersonaInfo(personaID).get(PersonasSQL.DESCRIPTION);
	}

	/**
	 * @return A string of the persona's information formatted all together.
	 */
	public String getFormattedBasicInfo() {
		DataMapFilter data = RPPersonas.get().getPersonasSQL().getBasicPersonaInfo(personaID);
		Map<String, Short> languages = getLanguages();

		StringBuilder output = new StringBuilder(BaseConvo.DIVIDER +
												 RPPersonas.PRIMARY_DARK + "Persona ID: " + RPPersonas.SECONDARY_LIGHT + String.format("%06d", (int) data.get(PersonasSQL.PERSONAID)) + "\n");
		if (data.containsKey(PersonasSQL.NICKNAME)) {
			output.append(RPPersonas.PRIMARY_DARK).append("Nickname: ").append(RPPersonas.SECONDARY_LIGHT).append(data.get(PersonasSQL.NICKNAME)).append("\n");
		}
		output.append(RPPersonas.PRIMARY_DARK).append("Name: ").append(RPPersonas.SECONDARY_LIGHT).append(data.get(PersonasSQL.NAME)).append("\n")
			  .append(RPPersonas.PRIMARY_DARK).append("Age: ").append(RPPersonas.SECONDARY_LIGHT).append(TimeManager.getRelativeTimeString((long) data.get(PersonasSQL.AGE))).append("\n")
			  .append(RPPersonas.PRIMARY_DARK).append("Race: ").append(RPPersonas.SECONDARY_LIGHT).append(data.get(PersonasSQL.RACE)).append("\n")
			  .append(RPPersonas.PRIMARY_DARK).append("Gender: ").append(RPPersonas.SECONDARY_LIGHT).append(data.get(PersonasSQL.GENDER)).append("\n");

		if (languages != null && languages.size() > 0) {
			StringBuilder languageLine = new StringBuilder(RPPersonas.PRIMARY_DARK + "Languages: ");
			boolean filled = false;
			for (String key : languages.keySet()) {
				if (filled) {
					languageLine.append(RPPersonas.TERTIARY).append(ChatColor.BOLD).append(" | ");
				} else {
					filled = true;
				}

				PersonaLanguage lang = PersonaLanguage.getByName(key);
				if (lang != null) {
					languageLine.append(RPPersonas.SECONDARY_LIGHT);
					if (lang.getTag().length() > 0) {
						languageLine.append(lang.getTag()).append(" ");
					}
					languageLine.append(key.replace("_", " ")).append(" ").append(RPPersonas.SECONDARY_DARK).append(languages.get(key));
				}
			}
			output.append(languageLine).append("\n");
		}

		if (data.containsKey(PersonasSQL.DESCRIPTION)) {
			output.append(RPPersonas.PRIMARY_DARK).append("Description: ").append(RPPersonas.SECONDARY_LIGHT).append(data.get(PersonasSQL.DESCRIPTION)).append("\n");
		}
		output.append(BaseConvo.DIVIDER);

		return output.toString();
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
		if (name != null && name.length() > 0) {
			this.altered = true;
			this.name = name;
		}
	}

	/**
	 * @param name Updates the nickname for the given Persona.
	 */
	public void setNickname(String name) {
		if (name == null || name.length() > 0) {
			this.nickname = name;
		} else {
			this.nickname = null;
		}

		if (playerInteraction != null) {
			playerInteraction.updateNickname();
		}
	}

	/**
	 * @param inventory Update the saved inventory contents to the one provided.
	 */
	public void setSavedInventory(PlayerInventory inventory) {
		this.loadLocked = true;
		this.savedInventory = InventoryUtil.serializeItems(inventory);
		save();
		this.loadLocked = false;
	}

	/**
	 * @param description Update this persona to have the given description.
	 */
	public void setDescription(String description) {
		DataMapFilter data = new DataMapFilter();
		data.put(PersonasSQL.DESCRIPTION, description);
		save(data);
	}


	////////////////////////////
	//// Player Interaction ////
	////////////////////////////

	/**
	 * A sub-class for data that's only loaded when a person is playing as this persona.
	 * NOTE: Player interactions should ONLY go within this class.
	 */
	public class PlayerInteraction {

		// Name
		@Getter private String prefix;
		@Getter @Setter private boolean staffNameEnabled; // Whether to colour the player's RP name.
		@Getter private String[] namePieces = new String[2];

		@Getter private PersonaSkin activeSkin; // Skin
		//private List<StatusEntry> activeStatuses = new ArrayList<>(); // Statuses TODO: Status re-implementation. Ignoring for now.
		@Getter private RPPlayer rpPlayer; // Player Wrapper

		private PlayerInteraction(Player player) {
			// Create additional detail and load a player into the persona.
		}

		// LOAD & SAVE
		/**
		 * Unloads the given additional data such that the Persona is no longer in use.
		 */
		public void unload() {
			playerPersonaMap.remove(rpPlayer.getPlayer());
			// Send player back to menu if they're still online.
		}

		/**
		 * Saves data for this persona with the active RPPlayer.
		 */
		public void save(DataMapFilter base) {
			Player player = rpPlayer.getPlayer();
			DataMapFilter data = new DataMapFilter();
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
			data.putAllData(base);
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
			String currentName = (nickname != null) ? nickname : name;
			if (prefix != null) {
				return "[" + prefix + "] " + currentName;
			} else {
				return currentName;
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

		// MODIFIERS //
		/**
		 * @param prefix Sets the prefix to this. If length 0 then it becomes null.
		 */
		public void setPrefix(String prefix) {
			if (prefix == null || prefix.length() > 0) {
				this.prefix = prefix;
			} else {
				this.prefix = null;
			}
		}

		/**
		 * @param skinID Changes this persona to be using the given skin ID.
		 */
		public void setSkin(int skinID) {
			if (rpPlayer != null) {
				Player player = rpPlayer.getPlayer();
				this.activeSkin = PersonaSkin.getFromID(skinID);
				if (player != null) {
					PersonaSkin.refreshPlayerSync(player);
				}
			}
		}

		// UTIL //
		public void openDescription() {
			if (rpPlayer != null) {
				final Persona persona = getPersona(personaID);
				final Player player = rpPlayer.getPlayer();
				final ItemStack book = new ItemStack(Material.WRITABLE_BOOK);
				{
					BookMeta meta = (BookMeta) book.getItemMeta();
					meta.setPages(BookUtil.getStringAsPages(persona.getDescription()));
					book.setItemMeta(meta);
				}
				new BookStream(player, book, "Open to Edit! Done to complete!") {
					@Override
					public void onBookClose() {
						persona.setDescription(BookUtil.getPagesAsString(this.getMeta()));
					}
				}.open(player);
			}
		}

		/**
		 * Updates the persona's nickname in the event that colour has changed.
		 */
		public void updateNickname() {
			updateNickname(getActiveName());
		}

		private void updateNickname(String name) {
			if (rpPlayer != null) {
				Player player = rpPlayer.getPlayer();
				namePieces = new String[2];
				String prefix = "";
				if (staffNameEnabled) {
					prefix = RPPersonas.getPrefixColor(player);
				}

				String personaName = prefix + nickname;
				int maxMidSize = 16;
				int maxSuffixSize = 64;

				namePieces[0] = personaName.substring(0, Math.min(maxMidSize, personaName.length()));
				if (personaName.length() > maxMidSize) {
					String suffix = prefix + personaName.substring(maxMidSize, personaName.length());
					namePieces[1] = suffix.substring(0, Math.min(maxSuffixSize, suffix.length()));
				}

				BoardManager.addPlayer(player, namePieces);
			}
		}

	}
}
