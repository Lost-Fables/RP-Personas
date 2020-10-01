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
		Account a = null;
		if (accountID > 0 && !loadBlocked.contains(accountID)) {
			a = loadedAccounts.get(accountID);
			if (a == null) {
				a = new Account(accountID);
				loadedAccounts.put(accountID, a);
			}
		}
		return a;
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

	//////////////////
	//// INSTANCE ////
	//////////////////

	@Getter private int accountID;
	private ArrayList<UUID> assignedUUIDs = new ArrayList<>();
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

}
