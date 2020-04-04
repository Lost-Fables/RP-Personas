package net.korvic.rppersonas.accounts;

import net.korvic.rppersonas.RPPersonas;
import net.korvic.rppersonas.personas.Persona;
import net.korvic.rppersonas.personas.PersonaHandler;
import net.korvic.rppersonas.personas.PersonaSkin;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import java.util.*;

public class Account {
	private int accountID;
	private Map<Integer, Persona> loadedPersonas;
	private static RPPersonas plugin;


	// ACCOUNT CREATION //

	protected static Account createAccount(Player p, int accountID, int personaIDToSwapTo, boolean saveCurrentPersona) {
		Account a = RPPersonas.get().getAccountHandler().getLoadedAccount(accountID);
		boolean first = false;

		if (a == null) {
			a = new Account(p, accountID, saveCurrentPersona);
			first = true;
		} else if (a.getLivePersonaIDs().size() < 1) {
			first = true;
		}

		if (personaIDToSwapTo > 0) {
			plugin.getPersonaHandler().loadPersona(p, accountID, personaIDToSwapTo, saveCurrentPersona);
		} else {
			PersonaHandler.createPersona(p, accountID, first);
		}

		return a;
	}

	private Account(Player p, int accountID, boolean saveCurrentPersona) {
		this.accountID = accountID;
		plugin = RPPersonas.get();
	}

	// GETTERS //
	public int getAccountID() {
		return accountID;
	}

	public List<Persona> getLoadedPersonas() {
		return (List<Persona>) loadedPersonas.values();
	}
	public Set<Integer> getLivePersonaIDs() {
		return plugin.getPersonaAccountMapSQL().getPersonasOf(accountID, true).keySet();
	}
	public Set<Integer> getDeadPersonaIDs() {
		return plugin.getPersonaAccountMapSQL().getPersonasOf(accountID, false).keySet();
	}

	public Map<Integer, String> getSkinNames() {
		return plugin.getSkinsSQL().getSkinNames(accountID);
	}

	public List<UUID> getUUIDs() {
		return plugin.getUUIDAccountMapSQL().getUUIDsOf(accountID);
	}

	// SWAPPING //

	public void swapToPersonaIfOwned(Player p, int personaID, boolean alive, boolean saveCurrentPersona) {
		Map<Integer, UUID> personas = plugin.getPersonaAccountMapSQL().getPersonasOf(accountID, alive);
		if (personas.containsKey(personaID) && personas.get(personaID) == null) {
			swapToPersona(p, personaID, saveCurrentPersona);
		}
	}

	public void swapToPersona(Player p, int personaID, boolean saveCurrentPersona) {
		Persona originalPersona = plugin.getPersonaHandler().getLoadedPersona(p);
		if (originalPersona != null) {
			if (saveCurrentPersona) {
				originalPersona.queueSave(p);
				plugin.getSaveQueue().addToQueue(plugin.getPersonaAccountMapSQL().getSaveStatement(originalPersona.getPersonaID(), accountID, originalPersona.isAlive(), null));
			}
			plugin.getPersonaHandler().getLoadedPersona(p).unloadPersona();
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

}
