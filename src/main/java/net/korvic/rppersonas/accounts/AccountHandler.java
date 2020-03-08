package net.korvic.rppersonas.accounts;

import net.korvic.rppersonas.RPPersonas;

import java.util.ArrayList;
import java.util.List;

public class AccountHandler {

	private RPPersonas plugin;
	private List<Account> loadedAccounts;

	private static final String MULTIPLE_ACCOUNTS_WARN = "Found multiple accounts with the ID ";

	public AccountHandler(RPPersonas plugin) {
		this.plugin = plugin;
		loadedAccounts = new ArrayList<>();
	}

	public Account getAccount(int id) {
		Account output = null;
		Account[] a = (Account[]) loadedAccounts.stream().filter(account -> account.getAccountID() == id).toArray()[0];
		if (a.length == 1) {
			output = a[0];
		}
		if (a.length > 1) {
			plugin.getServer().getLogger().warning(MULTIPLE_ACCOUNTS_WARN + id);
		}
		return output;
	}

	public Account loadAccount(int id) {
		Account a = Account.createAccount(id, new ArrayList<>(), new ArrayList<>(), new ArrayList<>(), null);
		if (a != null) {
			loadedAccounts.add(a);
		}
		return a;
	}

	public boolean unloadAccount(int id) {
		Account a = getAccount(id);
		if (a != null) {
			loadedAccounts.remove(a);
			return true;
		} else {
			return false;
		}
	}

}
