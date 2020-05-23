package net.korvic.rppersonas.personas;

import co.lotc.core.bukkit.util.InventoryUtil;
import co.lotc.core.bukkit.util.LocationUtil;
import com.destroystokyo.paper.Title;
import net.korvic.rppersonas.RPPersonas;
import net.korvic.rppersonas.conversation.PersonaCreationConvo;
import net.korvic.rppersonas.death.Altar;
import net.korvic.rppersonas.sql.PersonaAccountsMapSQL;
import net.korvic.rppersonas.sql.PersonasSQL;
import net.korvic.rppersonas.sql.util.DataMapFilter;
import net.korvic.rppersonas.statuses.DisabledStatus;
import net.korvic.rppersonas.statuses.EtherealStatus;
import net.korvic.rppersonas.statuses.Status;
import net.korvic.rppersonas.statuses.StatusEntry;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.*;

public class PersonaHandler {

	private static RPPersonas plugin;
	private Map<Integer, Persona> loadedPersonas = new HashMap<>(); // personaID, persona
	private Map<Player, Integer> playerObjectToID = new HashMap<>(); // player, personaID
	private static Map<Player, Persona> skipSave = new HashMap<>(); // List of personas to skip saving (i.e. disabled)
	private static int highestPersonaID = 1;

	public PersonaHandler(RPPersonas plugin) {
		PersonaHandler.plugin = plugin;
		startStatusRunnable();
	}

	public static void updateHighestPersonaID(int personaID) {
		if (personaID >= highestPersonaID) {
			highestPersonaID = personaID + 1;
		}
	}

	// CREATION //
	public static void createPersona(Player p, int accountID, boolean first) {
		new DisabledStatus(null).applyEffect(p, (byte) 0);

		String welcomeText = "";
		if (first) {
			welcomeText = RPPersonas.PRIMARY_DARK + "" + ChatColor.BOLD + "Welcome!";
		} else if (plugin.getPersonaHandler().getLoadedPersona(p) != null) {
			plugin.getPersonaHandler().getLoadedPersona(p).queueSave(p);
			skipSave.put(p, plugin.getPersonaHandler().getLoadedPersona(p));
		}
		Title title = new Title(welcomeText,
								RPPersonas.SECONDARY_LIGHT + "Type your Persona's name to continue.",
								20, 60*20, 20);

		new DisabledStatus(title).applyEffect(p, (byte) 0);
		p.teleportAsync(plugin.getSpawnLocation());
		p.getInventory().clear();

		Map<Object, Object> data = new HashMap<>();
		data.put(PersonaAccountsMapSQL.ACCOUNTID, accountID);
		data.put(PersonasSQL.ALIVE, true);
		data.put(PersonasSQL.LIVES, 3);
		data.put(PersonasSQL.PLAYTIME, 0L);
		data.put(PersonasSQL.FRESH, new Object());
		data.put(PersonasSQL.LOCATION, plugin.getSpawnLocation());

		if (first) {
			data.put(PersonasSQL.FIRST, new Object());
		}

		new PersonaCreationConvo(plugin).startConvo(p, data, !first);
	}

	public Persona loadPersona(Player p, int accountID, int personaID, boolean saveCurrentPersona) {
		DataMapFilter personaData = new DataMapFilter();
		personaData.putAll(plugin.getPersonasSQL().getLoadingInfo(personaID))
				   .put(PersonasSQL.PERSONAID, personaID)
				   .put(PersonaAccountsMapSQL.ACCOUNTID, accountID);
		return registerPersona(personaData, p, saveCurrentPersona);
	}

	public static Persona registerPersona(DataMapFilter data, Player p, boolean saveCurrentPersona) {
		int personaID = highestPersonaID;
		if (data.containsKey(PersonasSQL.PERSONAID)) {
			personaID = (int) data.get(PersonasSQL.PERSONAID);
		}
		updateHighestPersonaID(personaID);

		int accountID = (int) data.get(PersonaAccountsMapSQL.ACCOUNTID);

		String prefix = null;
		if (data.containsKey(PersonasSQL.PREFIX)) {
			prefix = (String) data.get(PersonasSQL.PREFIX);
		}

		String nickName = null;
		if (data.containsKey(PersonasSQL.NICKNAME)) {
			nickName = (String) data.get(PersonasSQL.NICKNAME);
		} else if (data.containsKey(PersonasSQL.NAME)) {
			nickName = (String) data.get(PersonasSQL.NAME);
		}

		String personaInvData = null;
		if (data.containsKey(PersonasSQL.INVENTORY)) {
			personaInvData = (String) data.get(PersonasSQL.INVENTORY);

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
		if (data.containsKey(PersonasSQL.ENDERCHEST)) {
			personaEnderData = (String) data.get(PersonasSQL.ENDERCHEST);
		}

		double health = 20.0;
		if (data.containsKey(PersonasSQL.HEALTH)) {
			health = (double) data.get(PersonasSQL.HEALTH);
		}
		p.setHealth(health);

		int hunger = 20;
		if (data.containsKey(PersonasSQL.HUNGER)) {
			hunger = (int) data.get(PersonasSQL.HUNGER);
		}
		p.setFoodLevel(hunger);

		int activeSkinID = 0;
		if (data.containsKey(PersonasSQL.SKINID)) {
			activeSkinID = (int) data.get(PersonasSQL.SKINID);
		}

		boolean isAlive = false;
		if (data.containsKey(PersonasSQL.ALIVE)) {
			isAlive = (boolean) data.get(PersonasSQL.ALIVE);
		}

		if (data.containsKey(PersonasSQL.FRESH)) {
			p.setSaturation(20); // Give the player 20 saturation if they're a new persona so they can run around a bit more.
			data.put(PersonasSQL.PERSONAID, personaID);

			plugin.getPersonasSQL().registerOrUpdate(data);
			plugin.getPersonaAccountMapSQL().registerOrUpdate(data);

			plugin.getPersonaHandler().swapToPersona(p, accountID, personaID, saveCurrentPersona);
		}

		if (data.containsKey(PersonasSQL.LOCATION)) {
			Location loc = (Location) data.get(PersonasSQL.LOCATION);
			if (!LocationUtil.isClose(p, loc, 1.0D)) {
				p.teleport(loc);
			}
		}

		Persona persona = new Persona(plugin, p, personaID, accountID, prefix, nickName, personaInvData, personaEnderData, isAlive, activeSkinID);
		plugin.getPersonaHandler().playerObjectToID.put(p, personaID);
		plugin.getPersonaHandler().loadedPersonas.put(personaID, persona);

		if (!isAlive) {
			if (data.containsKey(PersonasSQL.ALTARID) && ((int) data.get(PersonasSQL.ALTARID)) > 0) {
				persona.setAlive(true);

				Altar altar = plugin.getAltarHandler().getAltar((int) data.get(PersonasSQL.ALTARID));
				if (altar != null) {
					p.teleport(altar.getTPLocation());
					// TODO - Create respawn animation
				}

				Status.clearStatus(EtherealStatus.NAME, p);
				new EtherealStatus().clearEffect(p);

				if (data.containsKey(PersonasSQL.CORPSEINV) && data.get(PersonasSQL.CORPSEINV) != null) {
					ItemStack[] items = InventoryUtil.deserializeItemsToArray((String) data.get(PersonasSQL.CORPSEINV));
					for (ItemStack item : items) {
						if (item != null) {
							InventoryUtil.addOrDropItem(p, item);
						}
					}
				}

				data.put(PersonasSQL.ALIVE, true);
				data.put(PersonasSQL.ALTARID, 0);
				data.put(PersonasSQL.CORPSEINV, null);

				plugin.getPersonasSQL().registerOrUpdate(data);
				plugin.getPersonaAccountMapSQL().registerOrUpdate(data);
			} else {
				if (!persona.hasStatus(EtherealStatus.NAME)) {
					persona.addStatus(new EtherealStatus(), (byte) -1, Long.MAX_VALUE);
				}
			}
		}

		return persona;
	}

	// GET //
	public Persona getLoadedPersona(String personaName) {
		for (Persona pers : loadedPersonas.values()) {
			if (pers.getNickName().equals(personaName) || pers.getNamePieces()[1].equals(personaName)) {
				return pers;
			}
		}
		return null;
	}

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
				DataMapFilter data = new DataMapFilter();
				data.put(PersonaAccountsMapSQL.PERSONAID, originalPersona.getPersonaID())
					.put(PersonaAccountsMapSQL.ACCOUNTID, accountID)
					.put(PersonaAccountsMapSQL.ACTIVEUUID, null);
				plugin.getPersonaAccountMapSQL().registerOrUpdate(data);
			}
			unloadPersona(originalPersona, false);
		}

		DataMapFilter data = new DataMapFilter();
		data.put(PersonaAccountsMapSQL.PERSONAID, personaID)
			.put(PersonaAccountsMapSQL.ACCOUNTID, accountID)
			.put(PersonaAccountsMapSQL.ACTIVEUUID, p.getUniqueId());
		plugin.getPersonaAccountMapSQL().registerOrUpdate(data);

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
			DataMapFilter data = new DataMapFilter();
			data.put(PersonaAccountsMapSQL.PERSONAID, pers.getPersonaID())
				.put(PersonaAccountsMapSQL.ACCOUNTID, pers.getAccountID())
				.put(PersonaAccountsMapSQL.ACTIVEUUID, uuid);
			plugin.getPersonaAccountMapSQL().registerOrUpdate(data);
			removeFromMemory(pers.getPersonaID(), pers.getUsingPlayer());
		}
	}

	private void removeFromMemory(int personaID, Player p) {
		loadedPersonas.remove(personaID);
		playerObjectToID.remove(p);
	}

	// UPDATE //
	public void updateActiveSkin(int personaID, int skinID, Player p) {
		if (loadedPersonas.containsKey(personaID)) {
			p.closeInventory();
			loadedPersonas.get(personaID).setSkin(skinID);
		}

		DataMapFilter data = new DataMapFilter();
		data.put(PersonasSQL.PERSONAID, personaID);
		data.put(PersonasSQL.SKINID, skinID);
		plugin.getPersonasSQL().registerOrUpdate(data);
	}

	public void saveAllPersonas() {
		for (Player p : playerObjectToID.keySet()) {
			Persona pers = loadedPersonas.get(playerObjectToID.get(p));
			if (!isSkipped(pers)) {
				pers.queueSave(p);
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

	// SKIP SAVE //
	public static void startSkipping(Player p, Persona pers) {
		skipSave.put(p, pers);
	}

	public static void stopSkipping(Player p) {
		skipSave.remove(p);
	}

	public static boolean isSkipped(Persona pers) {
		return skipSave.containsValue(pers);
	}

	// STATUS UPDATES //
	private void startStatusRunnable() {
		new BukkitRunnable(){
			@Override
			public void run() {
				for (Persona pers : loadedPersonas.values()) {
					List<StatusEntry> entriesToClear = new ArrayList<>();
					for (StatusEntry entry : pers.getActiveStatuses()) {
						if (entry.getExpiration() <= System.currentTimeMillis()) {
							entriesToClear.add(entry);
						}
					}
					for (StatusEntry entry : entriesToClear) {
						pers.clearStatusEntry(entry);
					}
				}
			}
		}.runTaskTimer(plugin, 0, 20);
	}
}
