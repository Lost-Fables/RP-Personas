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
	private int activePersonaID;
	private static RPPersonas plugin;


	// ACCOUNT CREATION //

	protected static Account createAccount(Player p, int accountID, int activePersonaID, boolean saveCurrentPersona) {
		Account a = RPPersonas.get().getAccountHandler().getLoadedAccount(accountID);
		if (a == null) {
			a = new Account(p, accountID, activePersonaID, saveCurrentPersona);
		}
		return a;
	}

	private Account(Player p, int accountID, int activePersonaID, boolean saveCurrentPersona) {
		this.accountID = accountID;
		plugin = RPPersonas.get();

		if (!plugin.getAccountsSQL().isRegistered(accountID)) {
			Map<Object, Object> accountData = new HashMap<>();
			accountData.put("accountid", accountID);
			accountData.put("personaid", activePersonaID);
			plugin.getAccountsSQL().registerOrUpdate(accountData);
		}

		if (activePersonaID > 0) {
			this.activePersonaID = activePersonaID;
			plugin.getPersonaHandler().loadPersona(p, accountID, activePersonaID, saveCurrentPersona);
		} else {
			PersonaHandler.createPersona(p, accountID, true);
		}
	}

	// GETTERS //

	public int getAccountID() {
		return accountID;
	}

	public int getActivePersonaID() {
		return activePersonaID;
	}

	public List<Integer> getLivePersonaIDs() {
		return plugin.getPersonaAccountMapSQL().getPersonasOf(accountID, true);
	}

	public List<Integer> getDeadPersonaIDs() {
		return plugin.getPersonaAccountMapSQL().getPersonasOf(accountID, false);
	}

	public Map<Integer, String> getSkinNames() {
		return plugin.getSkinsSQL().getSkinNames(accountID);
	}

	public List<UUID> getUUIDs() {
		return plugin.getUUIDAccountMapSQL().getUUIDsOf(accountID);
	}

	// SWAPPING //

	public void swapToPersonaIfOwned(Player p, int personaID, boolean alive, boolean saveCurrentPersona) {
		if (plugin.getPersonaAccountMapSQL().getPersonasOf(accountID, alive).contains(personaID)) {
			swapToPersona(p, personaID, saveCurrentPersona);
		}
	}

	public void swapToPersona(Player p, int personaID, boolean saveCurrentPersona) {
		if (plugin.getPersonaHandler().getLoadedPersona(p) != null) {
			if (saveCurrentPersona) {
				plugin.getPersonaHandler().getLoadedPersona(p).queueSave(p);
			}
			plugin.getPersonaHandler().unloadPersona(activePersonaID, p);
		}
		this.activePersonaID = personaID;
		Map<Object, Object> data = new HashMap<>();
		data.put("accountid", accountID);
		data.put("activepersonaid", personaID);
		plugin.getAccountsSQL().registerOrUpdate(data);

		Persona pers = plugin.getPersonaHandler().loadPersona(p, accountID, personaID, saveCurrentPersona);
		ItemStack[] items = pers.getInventory();
		if (items != null) {
			p.getInventory().setContents(items);
		} else {
			p.getInventory().clear();
		}
		PersonaSkin.refreshPlayer(p);
		p.teleportAsync(plugin.getPersonasSQL().getLocation(personaID));
	}

}
