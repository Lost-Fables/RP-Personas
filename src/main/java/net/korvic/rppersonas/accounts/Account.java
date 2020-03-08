package net.korvic.rppersonas.accounts;

import net.korvic.rppersonas.RPPersonas;

import java.util.List;
import java.util.Map;
import java.util.UUID;

public class Account {
	private int accountID;
	private List<Integer> personaIDs;
	private List<Integer> deadPersonaIDs;
	private List<String> skinNames;
	private Map<UUID, String> uuids; // <UUID, Username>

	/*
	 * Store all above info.
	 * Store Forum ID as int
	 * Store Discord IDs as String
	 * Store Playtime as long (millis)
	 * Store Votes as int
	 */


	// ACCOUNT CREATION //

	protected static Account createAccount(int id, List<Integer> personaIDs, List<Integer> deadPersonaIDs, List<String> skinNames, Map<UUID, String> uuids) {
		if (RPPersonas.handler.getAccount(id) == null) {
			return new Account(id, personaIDs, deadPersonaIDs, skinNames, uuids);
		} else {
			return null;
		}
	}

	private Account(int accountID, List<Integer> personaIDs, List<Integer> deadPersonaIDs, List<String> skinNames, Map<UUID, String> uuids) {
		this.accountID = accountID;
		this.personaIDs = personaIDs;
		this.deadPersonaIDs = deadPersonaIDs;
		this.skinNames = skinNames;
		this.uuids = uuids;
	}

	// GETTERS //

	public int getAccountID() {
		return accountID;
	}

	public List<Integer> getPersonaIDs() {
		return personaIDs;
	}

	public List<Integer> getDeadPersonaIDs() {
		return deadPersonaIDs;
	}

	public List<String> getSkinNames() {
		return skinNames;
	}

	public Map<UUID, String> getUUIDs() {
		return uuids;
	}

}
