package net.korvic.rppersonas.personas;

import co.lotc.core.bukkit.menu.Menu;
import co.lotc.core.bukkit.util.InventoryUtil;
import co.lotc.core.bukkit.util.LocationUtil;
import co.lotc.core.util.DataMapFilter;
import com.destroystokyo.paper.Title;
import net.korvic.rppersonas.RPPersonas;
import net.korvic.rppersonas.conversation.PersonaCreationConvo;
import net.korvic.rppersonas.death.Altar;
import net.korvic.rppersonas.events.PlayerChangePersonaEvent;
import net.korvic.rppersonas.events.PlayerCreatePersonaEvent;
import net.korvic.rppersonas.kits.Kit;
import net.korvic.rppersonas.sql.PersonaAccountsMapSQL;
import net.korvic.rppersonas.sql.PersonasSQL;
import net.korvic.rppersonas.statuses.DisabledStatus;
import net.korvic.rppersonas.statuses.StatusEntry;
import org.bukkit.*;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.*;

public class PersonaHandler {

	private static RPPersonas plugin;
	private final Map<Integer, Persona> loadedPersonas = new HashMap<>(); // personaID, persona
	private final Map<Player, Integer> playerObjectToID = new HashMap<>(); // player, personaID
	private static final Map<Player, Persona> skipSave = new HashMap<>(); // List of personas to skip saving (i.e. disabled)
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
		new DisabledStatus(null).applyEffectSync(p, (byte) 0);

		Persona pers = null;
		String welcomeText = "";
		if (first) {
			welcomeText = RPPersonas.PRIMARY_DARK + "" + ChatColor.BOLD + "Welcome!";
		} else {
			pers = plugin.getPersonaHandler().getLoadedPersona(p);
			if (pers != null) {
				pers.queueSave(p);
				skipSave.put(p, pers);
			}
		}
		Title title = new Title(welcomeText,
								RPPersonas.SECONDARY_LIGHT + "Type your Persona's name to continue.",
								20, 60*20, 20);

		Persona finalPers = pers;
		new BukkitRunnable() {
			@Override
			public void run() {
				new DisabledStatus(title).applyEffect(p, (byte) 0);
				if (plugin.getSpawnLocation() != null) {
					p.teleportAsync(plugin.getSpawnLocation());
				}
				p.getInventory().clear();

				Map<Object, Object> data = new HashMap<>();
				data.put(PersonaAccountsMapSQL.ACCOUNTID, accountID);
				data.put(PersonasSQL.ALIVE, true);
				data.put(PersonasSQL.LIVES, RPPersonas.DEFAULT_LIVES);
				data.put(PersonasSQL.PLAYTIME, 0L);
				data.put(PersonasSQL.FRESH, new Object());
				data.put(PersonasSQL.LOCATION, plugin.getSpawnLocation());

				if (finalPers != null) {
					data.put("oldpersona", finalPers);
				}

				if (first) {
					data.put(PersonasSQL.FIRST, new Object());
				}

				new PersonaCreationConvo(plugin).startConvo(p, data, !first);
			}
		}.runTask(plugin);
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
				new BukkitRunnable() {
					@Override
					public void run() {
						p.getInventory().setContents(arrayItems);
					}
				}.runTaskLater(plugin, 10);
			} else {
				new BukkitRunnable() {
					@Override
					public void run() {
						p.getInventory().clear();
					}
				}.runTaskLater(plugin, 10);
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
		double finalHealth = health;
		new BukkitRunnable() {
			@Override
			public void run() {
				p.setHealth(finalHealth);
			}
		}.runTask(plugin);

		int hunger = 20;
		if (data.containsKey(PersonasSQL.HUNGER)) {
			hunger = (int) data.get(PersonasSQL.HUNGER);
		}
		int finalHunger = hunger;
		new BukkitRunnable() {
			@Override
			public void run() {
				p.setFoodLevel(finalHunger);
			}
		}.runTask(plugin);

		int activeSkinID = 0;
		if (data.containsKey(PersonasSQL.SKINID)) {
			activeSkinID = (int) data.get(PersonasSQL.SKINID);
		}

		boolean isAlive = true;
		if (data.containsKey(PersonasSQL.ALIVE)) {
			isAlive = (boolean) data.get(PersonasSQL.ALIVE);
		}

		if (data.containsKey(PersonasSQL.FRESH)) {
			plugin.getServer().getPluginManager().callEvent(new PlayerCreatePersonaEvent());
			new BukkitRunnable() {
				@Override
				public void run() {
					p.setSaturation(20); // Give the player 20 saturation if they're a new persona so they can run around a bit more.
				}
			}.runTask(plugin);
			data.put(PersonasSQL.PERSONAID, personaID);

			plugin.getPersonasSQL().registerOrUpdate(data);
			plugin.getPersonaAccountMapSQL().registerOrUpdate(data);

			plugin.getPersonaHandler().swapToPersona(p, accountID, personaID, saveCurrentPersona);
		}

		if (data.containsKey(PersonasSQL.LOCATION)) {
			Location loc = (Location) data.get(PersonasSQL.LOCATION);
			if (!LocationUtil.isClose(p, loc, 1.0D)) {
				new BukkitRunnable() {
					@Override
					public void run() {
						p.teleport(loc);
					}
				}.runTask(plugin);
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
					new BukkitRunnable() {
						@Override
						public void run() {
							p.teleport(altar.getTPLocation());
							p.getLocation().getWorld().playSound(altar.getTPLocation(), Sound.BLOCK_PORTAL_TRAVEL, 1, 1);
							// TODO - Create respawn animation
						}
					}.runTask(plugin);
				}

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
			}
		}

		List<StatusEntry> entries = plugin.getStatusSQL().getPersonaStatuses(personaID);
		persona.getActiveStatuses().addAll(entries);
		persona.refreshStatuses();

		if (data.containsKey(PersonasSQL.BACKGROUND)) {
			Kit kit = (Kit) data.get(PersonasSQL.BACKGROUND);
			if (kit != null) {
				for (ItemStack item : kit.getItems()) {
					if (item != null) {
						new BukkitRunnable() {
							@Override
							public void run() {
								InventoryUtil.addOrDropItem(p, item);
							}
						}.runTaskLater(plugin, 20);
					}
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

		PersonaSkin.refreshPlayerSync(p);

		p.teleportAsync(plugin.getPersonasSQL().getLocation(personaID));
		plugin.getServer().getPluginManager().callEvent(new PlayerChangePersonaEvent());
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

	public void queueSaveAllPersonas() {
		for (Persona pers : loadedPersonas.values()) {
			if (pers != null && !isSkipped(pers)) {
				pers.queueSave();
			}
		}
	}

	// DELETE //
	public void deletePersona(int personaID) {
		unloadPersona(personaID, false);
		try {
			plugin.getPersonasSQL().deletePersona(personaID);
			plugin.getPersonaAccountMapSQL().deleteEntry(personaID);
			plugin.getLanguageSQL().purgeAll(personaID);
		} catch (Exception e) {
			if (RPPersonas.DEBUGGING) {
				e.printStackTrace();
			}
		}
	}

	public void deleteSkin(int skinID) {
		try {
			plugin.getSkinsSQL().deleteSkin(skinID);
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
						Inventory inv = pers.getUsingPlayer().getOpenInventory().getTopInventory();
						if (inv.getHolder() instanceof Menu) {
							pers.getUsingPlayer().closeInventory();
						}
						pers.clearStatusEntry(entry);
					}
				}
			}
		}.runTaskTimer(plugin, 0, 20);
	}
}
