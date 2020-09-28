package net.korvic.rppersonas.players.accounts;

import net.korvic.rppersonas.RPPersonas;
import net.korvic.rppersonas.players.personas.OldPersona;
import net.korvic.rppersonas.players.personas.PersonaHandler;
import org.bukkit.entity.Player;

import java.util.*;

public class OldAccount {
	private int accountID;
	private Map<Integer, OldPersona> personas;
	private static RPPersonas plugin;


	// ACCOUNT CREATION //

	protected static OldAccount loadAccount(int accountID) {
		OldAccount account = RPPersonas.get().getAccountHandler().getLoadedAccount(accountID);

		return account;
	}

	protected static OldAccount createAccount(Player p, int accountID, int personaIDToSwapTo, boolean saveCurrentPersona) {
		OldAccount a = RPPersonas.get().getAccountHandler().getLoadedAccount(accountID);
		boolean first = false;

		if (a == null) {
			a = new OldAccount(p, accountID, saveCurrentPersona);
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

	private OldAccount(Player p, int accountID, boolean saveCurrentPersona) {
		this.accountID = accountID;
		plugin = RPPersonas.get();
	}

	// GETTERS //
	public int getAccountID() {
		return accountID;
	}

	public List<OldPersona> getPersonas() {
		return (List<OldPersona>) personas.values();
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
		return plugin.getUuidAccountMapSQL().getUUIDsOf(accountID);
	}

}
