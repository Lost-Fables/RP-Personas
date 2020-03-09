package net.korvic.rppersonas.accounts;

import net.korvic.rppersonas.RPPersonas;

import java.util.List;
import java.util.Map;
import java.util.UUID;

public class Account {
	private int accountID;
	private int activePersonaID;


	// ACCOUNT CREATION //

	protected static Account createAccount(int accountID, int activePersonaID) {
		if (RPPersonas.get().getAccountHandler().getAccount(accountID) == null) {
			return new Account(accountID, activePersonaID);
		} else {
			return null;
		}
	}

	private Account(int accountID, int activePersonaID) {
		this.accountID = accountID;
		this.activePersonaID = activePersonaID;
	}

	// GETTERS //

	public int getAccountID() {
		return accountID;
	}

	public List<Integer> getPersonaIDs() {
		// Use accountID to get list of personaIDs from AccountPersonaMapping
		return null;
	}

	public List<Integer> getDeadPersonaIDs() {
		// Use getPersonaIDs then check each persona to see if dead
		return null;
	}

	public List<String> getSkinNames() {
		// Use accountID to get a list of skins. Stream names to output list.
		return null;
	}

	public List<UUID> getUUIDs() {
		// Use accountID to get a list of UUIDs from the UUID-Account map
		return null;
	}

}
