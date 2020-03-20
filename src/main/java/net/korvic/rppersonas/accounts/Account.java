package net.korvic.rppersonas.accounts;

import net.korvic.rppersonas.RPPersonas;
import net.korvic.rppersonas.personas.Persona;
import net.korvic.rppersonas.personas.PersonaHandler;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import java.util.*;

public class Account {
	private int accountID;
	private int activePersonaID;
	private static RPPersonas plugin;


	// ACCOUNT CREATION //

	protected static Account createAccount(Player p, int accountID, int activePersonaID) {
		Account a = RPPersonas.get().getAccountHandler().getAccount(accountID);
		if (a == null) {
			a = new Account(p, accountID, activePersonaID);
		}
		return a;
	}

	private Account(Player p, int accountID, int activePersonaID) {
		this.accountID = accountID;
		plugin = RPPersonas.get();

		if (!plugin.getAccountsSQL().isRegistered(accountID)) {
			Map<Object, Object> accountData = new HashMap<>();
			accountData.put("accountid", accountID);
			accountData.put("personaid", activePersonaID);
			plugin.getAccountsSQL().register(accountData);
		}

		if (activePersonaID > 0) {
			this.activePersonaID = activePersonaID;
			plugin.getPersonaHandler().loadPersona(p, accountID, activePersonaID);
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

	public void swapToPersonaIfOwned(Player p, int personaID, boolean alive) {
		if (plugin.getPersonaAccountMapSQL().getPersonasOf(accountID, alive).contains(personaID)) {
			swapToPersona(p, personaID);
		}
	}

	public void swapToPersona(Player p, int personaID) {
		if (plugin.getPersonaHandler().getLoadedPersona(p) != null) {
			plugin.getPersonaHandler().getLoadedPersona(p).queueSave(p);
			plugin.getPersonaHandler().unloadPersona(activePersonaID, p);
		}
		this.activePersonaID = personaID;
		plugin.getAccountsSQL().updateActivePersona(accountID, personaID);

		Persona pers = plugin.getPersonaHandler().loadPersona(p, accountID, personaID);
		ItemStack[] items = pers.getInventory();
		if (items != null) {
			p.getInventory().setContents(items);
		} else {
			p.getInventory().clear();
		}
		p.teleportAsync(plugin.getPersonasSQL().getLocation(personaID));
	}

}
