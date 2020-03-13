package net.korvic.rppersonas.accounts;

import net.korvic.rppersonas.RPPersonas;

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

	public Account loadAccount(int accountID, int activePersonaID) {
		Account a = Account.createAccount(accountID, activePersonaID);
		if (!loadedAccounts.containsValue(a)) {
			loadedAccounts.put(a.getAccountID(), a);
		}
		return a;
	}

	public void unloadAccount(int accountID) {
		loadedAccounts.remove(accountID);
	}

}
