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
		if (first) {
			welcomeText = RPPersonas.PRIMARY_COLOR + ChatColor.BOLD + "Welcome!";
		} else {
			plugin.getPersonaHandler().getLoadedPersona(p).queueSave(p);
		}
		Title title = new Title(welcomeText,
								RPPersonas.SECONDARY_COLOR + "Type your Persona's name to continue.",
								20, 60*20, 20);

		PersonaDisableListener.disablePlayer(p, plugin.getSpawnLocation(), title);
		p.teleportAsync(plugin.getSpawnLocation());
		p.getInventory().clear();

		Map<Object, Object> data = new HashMap<>();
		data.put("accountid", accountID);
		data.put("alive", new Object());
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

			List<ItemStack> items = InventoryUtil.deserializeItems(personaInvData);
			ItemStack[] arrayItems = new ItemStack[items.size()];
			for (int i = 0; i < arrayItems.length; i++) {
				arrayItems[i] = items.get(i);
			}
			p.getInventory().setContents(arrayItems);
		}

		int activeSkinID = 0;
		if (data.containsKey("skinid")) {
			activeSkinID = (int) data.get("skinid");
		}

		boolean isAlive = false;
		if (data.containsKey("alive")) {
			isAlive = true;
		}

		if (data.containsKey("fresh")) {
			data.put("personaid", personaID);
			plugin.getPersonasSQL().registerOrUpdate(data);

			plugin.getPersonaAccountMapSQL().addMapping(personaID, accountID, isAlive);
			plugin.getAccountHandler().getLoadedAccount(accountID).swapToPersona(p, personaID, saveCurrentPersona);
		}

		if (data.containsKey("location")) {
			Location loc = (Location) data.get("location");
			if (!LocationUtil.isClose(p, loc, 1.0D)) {
				p.teleport(loc);
			}
		}

		Persona persona = new Persona(plugin, personaID, accountID, prefix, nickName, personaInvData, isAlive , activeSkinID);
		plugin.getPersonaHandler().playerObjectToID.put(p, personaID);
		plugin.getPersonaHandler().loadedPersonas.put(personaID, persona);

		return persona;
	}

	// CHECKING //
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
			return RPPersonas.TERTIARY_COLOR + player.getName() + "\n" + pers.getFormattedBasicInfo();
		} else {
			return "Unable to find loaded persona for the given player.";
		}
	}

	// UNLOADING //
	public void unloadPersonas(int accountID, Player p) {
		List<Integer> personas = plugin.getPersonaAccountMapSQL().getPersonasOf(accountID, true);
		personas.addAll(plugin.getPersonaAccountMapSQL().getPersonasOf(accountID, false));
		for (int i : personas) {
			if (loadedPersonas.containsKey(i)) {
				if (PersonaDisableListener.isPlayerEnabled(p)) {
					loadedPersonas.get(i).queueSave(p);
				}
				if (playerObjectToID.values().toArray().length > 1) {
					playerObjectToID.remove(p);
				} else {
					unloadPersona(i, p);
				}
			}
		}
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
	public void updateActiveSkin(int personaID, int skinID) {
		if (loadedPersonas.containsKey(personaID)) {
			loadedPersonas.get(personaID).updateSkin(skinID);
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
}
