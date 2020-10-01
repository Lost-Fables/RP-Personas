package net.korvic.rppersonas.players;

import lombok.Getter;
import net.korvic.rppersonas.RPPersonas;
import org.bukkit.entity.Player;

import java.util.*;

/**
 * The handling class for account based transactions. This includes personas, playtime, vote counts, etc.
 */
public class Account {

	////////////////
	//// STATIC ////
	////////////////

	private static Map<Integer, Account> loadedAccounts = new HashMap<>();
	private static List<Integer> loadBlocked = new ArrayList<>();

	/**
	 * @param player The player who's account you want.
	 * @return An Account object which represents the Lost Fables account for the given player.
	 */
	public static Account getAccount(Player player) {
		return getAccount(RPPersonas.get().getUuidAccountMapSQL().getAccountID(player.getUniqueId()));
	}

	/**
	 * @param accountID The Lost Fables account ID
	 * @return An Account object which represents the given Lost Fables account ID.
	 */
	public static Account getAccount(int accountID) {
		Account account = null;
		if (accountID > 0 && !loadBlocked.contains(accountID)) {
			account = loadedAccounts.get(accountID);
			if (account == null) {
				account = new Account(accountID);
				loadedAccounts.put(accountID, account);
			}
		}
		return account;
	}

	/**
	 * @param accountID Forcefully unload the given account ID and all linked Personas. This may kick players
	 *                  back to the main menu and/or to the lobby itself.
	 */
	public static void unloadAccount(int accountID) {
		loadBlocked.add(accountID);
		Account a = loadedAccounts.get(accountID);
		if (a != null) {
			a.unloadPersonas();
			loadedAccounts.remove(accountID);
		}
		loadBlocked.remove(accountID);
	}

	/**
	 * @param accountID Remove any stray data that's no longer in active use for the given account ID.
	 */
	public static void cleanup(int accountID) {
		Account account = loadedAccounts.get(accountID);
		if (account != null) {
			account.cleanup();
		}
	}

	/**
	 * Remove any stray data that's no longer in active use for all loaded accounts.
	 */
	public static void cleanupAll() {
		for (int accountID : loadedAccounts.keySet()) {
			cleanup(accountID);
		}
	}

	//////////////////
	//// INSTANCE ////
	//////////////////

	// ID
	@Getter private int accountID;

	// Linked UUIDs
	private ArrayList<UUID> assignedUUIDs = new ArrayList<>();

	// Persona IDs
	private List<Integer> livePersonaIDs = new ArrayList<>();
	private List<Integer> deadPersonaIDs = new ArrayList<>();

	private Account(int accountID) {
		this.accountID = accountID;
		this.assignedUUIDs.addAll(RPPersonas.get().getUuidAccountMapSQL().getUUIDsOf(accountID));

		this.livePersonaIDs.addAll(RPPersonas.get().getPersonaAccountMapSQL().getPersonasOf(accountID, true).keySet());
		this.deadPersonaIDs.addAll(RPPersonas.get().getPersonaAccountMapSQL().getPersonasOf(accountID, false).keySet());

		Collections.sort(livePersonaIDs);
		Collections.sort(deadPersonaIDs);
	}

	private void unloadPersonas() {
		for (int personaID : getPersonaIDs()) {
			Persona.unloadPersona(personaID);
		}
	}

	/**
	 * @return A clone of the assigned UUIDs list.
	 */
	@SuppressWarnings("unchecked")
	public List<UUID> getUUIDs() {
		return (List<UUID>) assignedUUIDs.clone();
	}

	/**
	 * @return A sorted list of live then dead persona IDs, each grouping in ascending order.
	 */
	public List<Integer> getPersonaIDs() {
		List<Integer> output = new ArrayList<>();
		output.addAll(livePersonaIDs);
		output.addAll(deadPersonaIDs);
		return output;
	}

	/**
	 * Remove any stray data that's no longer in active use.
	 */
	public void cleanup() {
		for (int personaID : getPersonaIDs()) {
			Persona.cleanup(personaID);
		}
	}

}
