package net.korvic.rppersonas.accounts;

import net.korvic.rppersonas.RPPersonas;
import net.korvic.rppersonas.personas.Persona;
import net.korvic.rppersonas.sql.UUIDAccountMapSQL;
import net.korvic.rppersonas.sql.util.DataMapFilter;
import org.bukkit.entity.Player;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

public class AccountHandler {

	private RPPersonas plugin;
	private Map<Integer, Account> loadedAccounts; // accountID, account
	private Map<Player, Player> awaitingLink; // Requester, Linker

	private static final String MULTIPLE_ACCOUNTS_WARN = "Found multiple accounts with the ID ";

	public AccountHandler(RPPersonas plugin) {
		this.plugin = plugin;
		loadedAccounts = new HashMap<>();
		awaitingLink = new HashMap<>();
	}

	public Account getLoadedAccount(int accountID) {
		return loadedAccounts.get(accountID);
	}

	public List<Persona> getLoadedPersonas(int accountID) {
		return loadedAccounts.get(accountID).getLoadedPersonas();
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

	public void attemptLink(Player requester, Player linker) {
		awaitingLink.put(requester, linker);
		if (linker != null && linker.isOnline()) {
			linker.sendMessage(RPPersonas.SECONDARY_LIGHT + requester.getName() + RPPersonas.PRIMARY_DARK +
							   " is requesting to link to this account. Linking accounts means that any and all actions any account does, all other linked accounts are equally responsible for, and allows you to share persona slots.\n" +
							   RPPersonas.PRIMARY_DARK + "If you wish to do this, use " + RPPersonas.SECONDARY_LIGHT + "/account altaccept " + requester.getName() + RPPersonas.PRIMARY_DARK + ".");
		}
	}

	public void finalizeLink(Player requester, Player linker) {
		if (awaitingLink.get(requester).equals(linker)) {
			awaitingLink.remove(requester);
			addLink(linker, plugin.getUuidAccountMapSQL().getAccountID(requester.getUniqueId()));
		}
	}

	public void addLink(Player player, int forumID) {
		DataMapFilter data = new DataMapFilter();
		data.put(UUIDAccountMapSQL.ACCOUNTID, forumID)
			.put(UUIDAccountMapSQL.PLAYER, player);
		plugin.getUuidAccountMapSQL().registerOrUpdate(data);
	}

	public void addLink(UUID uuid, int forumID) {
		DataMapFilter data = new DataMapFilter();
		data.put(UUIDAccountMapSQL.ACCOUNTID, forumID)
			.put(UUIDAccountMapSQL.PLAYER_UUID, uuid);
		plugin.getUuidAccountMapSQL().registerOrUpdate(data);
	}

	public Account getAccountForcefully(int accountID) {
		return null;
	}

	public Account getAccountForcefully(Player player) {
		return null;
	}
}
