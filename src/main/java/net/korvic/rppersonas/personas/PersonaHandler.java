package net.korvic.rppersonas.personas;

import co.lotc.core.bukkit.util.InventoryUtil;
import co.lotc.core.bukkit.util.LocationUtil;
import com.destroystokyo.paper.Title;
import net.korvic.rppersonas.RPPersonas;
import net.korvic.rppersonas.conversation.PersonaCreationAbandonedListener;
import net.korvic.rppersonas.conversation.PersonaCreationConvo;
import net.korvic.rppersonas.listeners.PersonaDisableListener;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.conversations.ConversationFactory;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

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
		PersonaDisableListener.disablePlayer(p);

		String welcomeText = "";
		if (first) {
			welcomeText = RPPersonas.PRIMARY_DARK + "" + ChatColor.BOLD + "Welcome!";
		} else if (plugin.getPersonaHandler().getLoadedPersona(p) != null) {
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
			factory.withFirstPrompt(new PersonaCreationConvo.StartingPrompt());
		} else {
			factory.withFirstPrompt(new PersonaCreationConvo.PersonaNamePrompt(false, false))
				   .addConversationAbandonedListener(new PersonaCreationAbandonedListener());
			addAbandoners(factory);
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

		String personaEnderData = null;
		if (data.containsKey("enderchest")) {
			personaEnderData = (String) data.get("enderchest");
		}

		double health = 20.0;
		if (data.containsKey("health")) {
			health = (double) data.get("health");
		}
		p.setHealth(health);

		int hunger = 20;
		if (data.containsKey("hunger")) {
			hunger = (int) data.get("hunger");
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
			plugin.getPersonaHandler().swapToPersona(p, accountID, personaID, saveCurrentPersona);
		}

		if (data.containsKey("location")) {
			Location loc = (Location) data.get("location");
			if (!LocationUtil.isClose(p, loc, 1.0D)) {
				p.teleport(loc);
			}
		}

		Persona persona = new Persona(plugin, p, personaID, accountID, prefix, nickName, personaInvData, personaEnderData, isAlive , activeSkinID);
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

	// SWAPPING //
	public void swapToPersonaIfOwned(Player p, int accountID, int personaID, boolean alive, boolean saveCurrentPersona) {
		Map<Integer, UUID> personas = plugin.getPersonaAccountMapSQL().getPersonasOf(accountID, alive);
		if (personas.containsKey(personaID) && personas.get(personaID) == null) {
			swapToPersona(p, accountID, personaID, saveCurrentPersona);
		}
	}

	public void swapToPersona(Player p, int accountID, int personaID, boolean saveCurrentPersona) {
		Persona originalPersona = plugin.getPersonaHandler().getLoadedPersona(p);
		if (originalPersona != null) {
			if (saveCurrentPersona) {
				originalPersona.queueSave(p);
				plugin.getSaveQueue().addToQueue(plugin.getPersonaAccountMapSQL().getSaveStatement(originalPersona.getPersonaID(), accountID, originalPersona.isAlive(), null));
			}
			unloadPersona(personaID, false);
		}

		Map<Object, Object> data = new HashMap<>();
		data.put("accountid", accountID);
		plugin.getAccountsSQL().registerOrUpdate(data);
		plugin.getSaveQueue().addToQueue(plugin.getPersonaAccountMapSQL().getSaveStatement(personaID, accountID, true, p.getUniqueId()));

		Persona newPersona = plugin.getPersonaHandler().loadPersona(p, accountID, personaID, saveCurrentPersona);
		ItemStack[] items = newPersona.getInventory();
		if (items != null) {
			p.getInventory().setContents(items);
		} else {
			p.getInventory().clear();
		}
		PersonaSkin.refreshPlayer(p);
		p.teleportAsync(plugin.getPersonasSQL().getLocation(personaID));
	}

	// UNLOADING //
	public void unloadPersona(Player p, boolean keepLinked) {
		unloadPersona(playerObjectToID.get(p), keepLinked);
	}

	public void unloadPersona(int personaID, boolean keepLinked) {
		unloadPersona(loadedPersonas.get(personaID), keepLinked);
	}

	public void unloadPersona(Persona pers, boolean keepLinked) {
		if (pers != null) {
			UUID uuid = null;
			if (keepLinked) {
				uuid = pers.getUsingPlayer().getUniqueId();
			}
			plugin.getPersonaAccountMapSQL().addOrUpdateMapping(pers.getPersonaID(), pers.getAccountID(), pers.isAlive(), uuid);
			removeFromMemory(pers.getPersonaID(), pers.getUsingPlayer(), keepLinked);
		}
	}

	private void removeFromMemory(int personaID, Player p, boolean keepLinked) {
		loadedPersonas.remove(personaID);
		playerObjectToID.remove(p);
	}

	// FACTORY //
	private static ConversationFactory getFreshFactory() {
		return new ConversationFactory(plugin)
				.thatExcludesNonPlayersWithMessage("Console does not participate in dialogues.")
				.withModality(true);
	}

	private static void addAbandoners(ConversationFactory factory) {
		factory.withEscapeSequence("quit")
			   .withEscapeSequence("exit")
			   .withEscapeSequence("cancel")
			   .withEscapeSequence("stop")
			   .withEscapeSequence("help");
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

	public void saveAllPersonas() {
		for (Player p : playerObjectToID.keySet()) {
			if (PersonaDisableListener.isPlayerEnabled(p)) {
				loadedPersonas.get(playerObjectToID.get(p)).queueSave(p);
			}
		}
	}

	// DELETE //
	public void deletePersona(int personaID) {
		unloadPersona(personaID, false);
		try {
			plugin.getSaveQueue().addToQueue(plugin.getPersonasSQL().getDeleteStatement(personaID));
			plugin.getSaveQueue().addToQueue(plugin.getPersonaAccountMapSQL().getDeleteStatement(personaID));
		} catch (Exception e) {
			if (RPPersonas.DEBUGGING) {
				e.printStackTrace();
			}
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
