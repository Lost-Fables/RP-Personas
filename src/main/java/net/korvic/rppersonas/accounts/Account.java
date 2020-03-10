package net.korvic.rppersonas.accounts;

import net.korvic.rppersonas.RPPersonas;

import java.util.*;

public class Account {
	private static int highestAccountID;
	private int accountID;
	private int activePersonaID;
	private static RPPersonas plugin;


	// ACCOUNT CREATION //

	protected static Account createFreshAccount() {
		return new Account(highestAccountID, -1);
	}

	protected static Account createActiveAccount(int accountID, int activePersonaID) {
		if (RPPersonas.get().getAccountHandler().getAccount(accountID) == null) {
			return new Account(accountID, activePersonaID);
		} else {
			return null;
		}
	}

	private Account(int accountID, int activePersonaID) {
		this.accountID = accountID;
		plugin = RPPersonas.get();

		if (activePersonaID > 0) {
			this.activePersonaID = activePersonaID;
		}

		if (accountID >= highestAccountID) {
			highestAccountID = accountID + 1;
		}
	}

	// GETTERS //

	public int getAccountID() {
		return accountID;
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
