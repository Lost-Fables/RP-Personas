package net.korvic.rppersonas;

import net.korvic.rppersonas.accounts.AccountHandler;
import net.korvic.rppersonas.listeners.JoinQuitListener;
import net.korvic.rppersonas.personas.PersonaHandler;
import net.korvic.rppersonas.sql.*;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.Currency;

public final class RPPersonas extends JavaPlugin {

	public static final boolean DEBUGGING = false;

	private static RPPersonas instance;
	private AccountHandler accountHandler;
	private PersonaHandler personaHandler;

	// SQL
	private UUIDAccountMapSQL uuidAccountMap;
	private AccountsSQL accounts;
	private PersonaAccountsMapSQL persAccMap;
	private PersonasSQL personas;
	private CurrencySQL currency;
	private SkinsSQL skins;

	@Override
	public void onEnable() {
		getServer().getPluginManager().registerEvents(new JoinQuitListener(this), this);

		accountHandler = new AccountHandler(this);
		personaHandler = new PersonaHandler(this);

		setupDatabases();

		instance = this;
	}

	@Override
	public void onDisable() {
		// Plugin shutdown logic
	}

	public static RPPersonas get() {
		return instance;
	}
	public AccountHandler getAccountHandler() {
		return accountHandler;
	}
	public PersonaHandler getPersonaHandler() {
		return personaHandler;
	}

	private void setupDatabases() {
		uuidAccountMap = new UUIDAccountMapSQL(this);
		accounts = new AccountsSQL(this);
		persAccMap = new PersonaAccountsMapSQL(this);
		personas = new PersonasSQL(this);
		currency = new CurrencySQL(this);
		skins = new SkinsSQL(this);

		uuidAccountMap.load();
		accounts.load();
		persAccMap.load();
		personas.load();
		currency.load();
		skins.load();
	}

	public UUIDAccountMapSQL getUUIDAccountMapSQL() {
		return uuidAccountMap;
	}
	public AccountsSQL getAccountsSQL() {
		return accounts;
	}
	public PersonaAccountsMapSQL getPersAccMapSQL() {
		return persAccMap;
	}
	public PersonasSQL getPersonasSQL() {
		return personas;
	}
	public CurrencySQL getCurrencySQL() {
		return currency;
	}
	public SkinsSQL getSkinsSQL() {
		return skins;
	}
}
