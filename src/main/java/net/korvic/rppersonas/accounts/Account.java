package net.korvic.rppersonas.accounts;

import net.korvic.rppersonas.RPPersonas;

import java.util.*;

public class Account {
	private int accountID;
	private int activePersonaID;
	private static RPPersonas plugin;


	// ACCOUNT CREATION //

	protected static Account createAccount(int accountID, int activePersonaID) {
		Account a = RPPersonas.get().getAccountHandler().getAccount(accountID);
		if (a == null) {
			a = new Account(accountID, activePersonaID);
		}
		return a;
	}

	private Account(int accountID, int activePersonaID) {
		this.accountID = accountID;
		plugin = RPPersonas.get();

		if (activePersonaID > 0) {
			this.activePersonaID = activePersonaID;
		} else {
			plugin.getPersonaHandler().createPersona(accountID);
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
		return plugin.getPersAccMapSQL().getPersonasOf(accountID, true);
	}

	public List<Integer> getDeadPersonaIDs() {
		return plugin.getPersAccMapSQL().getPersonasOf(accountID, false);
	}

	public Map<Integer, String> getSkinNames() {
		return plugin.getSkinsSQL().getSkinNames(accountID);
	}

	public List<UUID> getUUIDs() {
		return plugin.getUUIDAccountMapSQL().getUUIDsOf(accountID);
	}

}
