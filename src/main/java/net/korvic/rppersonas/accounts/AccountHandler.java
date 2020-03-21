package net.korvic.rppersonas.accounts;

import net.korvic.rppersonas.RPPersonas;
import net.korvic.rppersonas.personas.PersonaGender;
import org.bukkit.entity.Player;

import java.util.HashMap;
import java.util.Map;

public class AccountHandler {

	private RPPersonas plugin;
	private Map<Integer, Account> loadedAccounts; // accountID , account

	private static final String MULTIPLE_ACCOUNTS_WARN = "Found multiple accounts with the ID ";

	public AccountHandler(RPPersonas plugin) {
		this.plugin = plugin;
		loadedAccounts = new HashMap<>();
	}

	public Account getLoadedAccount(int accountID) {
		return loadedAccounts.get(accountID);
	}

	public int getActivePersona(int accountID) {
		Account account = getLoadedAccount(accountID);
		if (account != null) {
			return account.getActivePersonaID();
		} else {
			return plugin.getAccountsSQL().getActivePersonaID(accountID);
		}
	}

	public Account loadAccount(Player p, int accountID, int activePersonaID, boolean saveCurrentPersona) {
		Account a = Account.createAccount(p, accountID, activePersonaID, saveCurrentPersona);
		if (!loadedAccounts.containsValue(a)) {
			loadedAccounts.put(a.getAccountID(), a);
		}
		return a;
	}

	public void unloadAccount(int accountID) {
		loadedAccounts.remove(accountID);
	}
}
