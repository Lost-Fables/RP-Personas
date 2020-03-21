package net.korvic.rppersonas.accounts;

import net.korvic.rppersonas.RPPersonas;
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

	public Account getAccount(int accountID) {
		return loadedAccounts.get(accountID);
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
