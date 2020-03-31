package net.korvic.rppersonas.personas;

import co.lotc.core.bukkit.util.InventoryUtil;
import co.lotc.core.bukkit.util.LocationUtil;
import com.destroystokyo.paper.Title;
import net.korvic.rppersonas.RPPersonas;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.conversations.ConversationFactory;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class PersonaHandler {

	private static RPPersonas plugin;
	private Map<Integer, Persona> loadedPersonas = new HashMap<>(); // personaID, persona
	private Map<Player, Integer> playerObjectToID = new HashMap<>(); // player, personaID
	private static int highestPersonaID = 1;

	public PersonaHandler(RPPersonas plugin) {
		PersonaHandler.plugin = plugin;
	}

	public static void updateHighestPersonaID(int personaID) {
		if (personaID >= highestPersonaID) {
			highestPersonaID = personaID + 1;
		}
	}

	// CREATION //
	public static void createPersona(Player p, int accountID, boolean first) {
		String welcomeText = "";
		if (first || plugin.getPersonaHandler().getLoadedPersona(p) == null) {
			welcomeText = RPPersonas.PRIMARY_DARK + "" + ChatColor.BOLD + "Welcome!";
		} else {
			plugin.getPersonaHandler().getLoadedPersona(p).queueSave(p);
		}
		Title title = new Title(welcomeText,
								RPPersonas.SECONDARY_LIGHT + "Type your Persona's name to continue.",
								20, 60*20, 20);

		PersonaDisableListener.disablePlayer(p, plugin.getSpawnLocation(), title);
		p.teleportAsync(plugin.getSpawnLocation());
		p.getInventory().clear();

		Map<Object, Object> data = new HashMap<>();
		data.put("accountid", accountID);
		data.put("alive", true);
		data.put("lives", 3);
		data.put("playtime", 0L);
		data.put("fresh", new Object());
		data.put("location", plugin.getSpawnLocation());

		ConversationFactory factory = getFreshFactory();
		factory.withInitialSessionData(data);
		if (first) {
			factory.withFirstPrompt(new PersonaCreationDialog.StartingPrompt());
			//factory.addConversationAbandonedListener(new FirstPersonaAbandonListener());
		} else {
			factory.withFirstPrompt(new PersonaCreationDialog.PersonaNamePrompt(false));
		}
		factory.buildConversation(p).begin();
	}

	public Persona loadPersona(Player p, int accountID, int personaID, boolean saveCurrentPersona) {
		Map<Object, Object> personaData = new HashMap<>();
		personaData.put("personaid", personaID);
		personaData.put("accountid", accountID);
		personaData.putAll(plugin.getPersonasSQL().getLoadingInfo(personaID));

		return registerPersona(personaData, p, saveCurrentPersona);
	}

	public static Persona registerPersona(Map<Object, Object> data, Player p, boolean saveCurrentPersona) {
		int personaID = highestPersonaID;
		if (data.containsKey("personaid")) {
			personaID = (int) data.get("personaid");
		}
		updateHighestPersonaID(personaID);

		int accountID = (int) data.get("accountid");

		String prefix = null;
		if (data.containsKey("prefix")) {
			prefix = (String) data.get("prefix");
		}

		String nickName = null;
		if (data.containsKey("nickname")) {
			nickName = (String) data.get("nickname");
		} else if (data.containsKey("name")) {
			nickName = (String) data.get("name");
		}

		String personaInvData = null;
		if (data.containsKey("inventory")) {
			personaInvData = (String) data.get("inventory");

			if (personaInvData != null) {
				List<ItemStack> items = InventoryUtil.deserializeItems(personaInvData);
				ItemStack[] arrayItems = new ItemStack[items.size()];
				for (int i = 0; i < arrayItems.length; i++) {
					arrayItems[i] = items.get(i);
				}
				p.getInventory().setContents(arrayItems);
			} else {
				p.getInventory().clear();
			}
		}

		double health = 20.0;
		if (data.containsKey("health")) {
			health = (double) data.get("health");
		}
		p.setHealth(health);

		int hunger = 20;
		if (data.containsKey("hunger")) {
			health = (int) data.get("hunger");
		}
		p.setFoodLevel(hunger);

		int activeSkinID = 0;
		if (data.containsKey("skinid")) {
			activeSkinID = (int) data.get("skinid");
		}

		boolean isAlive = false;
		if (data.containsKey("alive")) {
			isAlive = true;
		}

		if (data.containsKey("fresh")) {
			p.setSaturation(20); // Give the player 20 saturation if they're a new persona so they can run around a bit more.
			data.put("personaid", personaID);
			plugin.getPersonasSQL().registerOrUpdate(data);

			plugin.getPersonaAccountMapSQL().addOrUpdateMapping(personaID, accountID, isAlive, p.getUniqueId());
			plugin.getAccountHandler().getLoadedAccount(accountID).swapToPersona(p, personaID, saveCurrentPersona);
		}

		if (data.containsKey("location")) {
			Location loc = (Location) data.get("location");
			if (!LocationUtil.isClose(p, loc, 1.0D)) {
				p.teleport(loc);
			}
		}

		Persona persona = new Persona(plugin, p, personaID, accountID, prefix, nickName, personaInvData, isAlive , activeSkinID);
		PersonaHandler handler = plugin.getPersonaHandler();
		handler.playerObjectToID.put(p, personaID);
		handler.loadedPersonas.put(personaID, persona);

		return persona;
	}

	// GET //
	public Persona getLoadedPersona(Player p) {
		if (playerObjectToID.containsKey(p)) {
			return getLoadedPersona(playerObjectToID.get(p));
		} else {
			return null;
		}
	}

	public Persona getLoadedPersona(int personaID) {
		return loadedPersonas.getOrDefault(personaID, null);
	}

	public String getPersonaInfo(Player player) {
		Persona pers = getLoadedPersona(player);
		if (pers != null) {
			return RPPersonas.SECONDARY_DARK + player.getName() + "'s active persona.\n" + pers.getFormattedBasicInfo();
		} else {
			return RPPersonas.PRIMARY_DARK + "Unable to find loaded persona for the given player.";
		}
	}

	// UNLOADING //
	public void unloadPersona(Persona pers) {
		plugin.getPersonaAccountMapSQL().addOrUpdateMapping(pers.getPersonaID(), pers.getAccountID(), pers.isAlive(), null);
		unloadPersona(pers.getPersonaID(), pers.getUsingPlayer());
	}

	public void unloadPersona(int personaID, Player p) {
		loadedPersonas.remove(personaID);
		playerObjectToID.remove(p);
	}

	// FACTORY //
	private static ConversationFactory getFreshFactory() {
		return new ConversationFactory(plugin)
				.thatExcludesNonPlayersWithMessage("Console does not participate in dialogues.")
				.withModality(true);
	}

	// UPDATE //
	public void updateActiveSkin(int personaID, int skinID, Player p) {
		if (loadedPersonas.containsKey(personaID)) {
			p.closeInventory();
			loadedPersonas.get(personaID).setSkin(skinID);
		}

		Map<Object, Object> map = new HashMap<>();
		map.put("personaid", personaID);
		map.put("skinid", skinID);
		plugin.getPersonasSQL().registerOrUpdate(map);
	}

	public void queueSavingAll() {
		for (Player p : playerObjectToID.keySet()) {
			if (PersonaDisableListener.isPlayerEnabled(p)) {
				loadedPersonas.get(playerObjectToID.get(p)).queueSave(p);
			}
		}
	}

	// DELETE //
	public void deletePersona(int personaID) {
		try {
			plugin.getSaveQueue().addToQueue(plugin.getPersonasSQL().getDeleteStatement(personaID));
			plugin.getSaveQueue().addToQueue(plugin.getPersonaAccountMapSQL().getDeleteStatement(personaID));
		} catch (Exception e) {
			if (RPPersonas.DEBUGGING) {
				e.printStackTrace();
			}
		}
		Persona pers = getLoadedPersona(personaID);
		if (pers != null) {
			pers.unloadPersona();
		}
	}

	public void deleteSkin(int skinID) {
		try {
			plugin.getSaveQueue().addToQueue(plugin.getSkinsSQL().getDeleteStatement(skinID));
		} catch (Exception e) {
			if (RPPersonas.DEBUGGING) {
				e.printStackTrace();
			}
		}
	}

}
