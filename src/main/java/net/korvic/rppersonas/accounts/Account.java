package net.korvic.rppersonas.accounts;

import net.korvic.rppersonas.RPPersonas;
import net.korvic.rppersonas.personas.PersonaHandler;
import org.bukkit.entity.Player;

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

		if (activePersonaID > 0) {
			this.activePersonaID = activePersonaID;
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

}
