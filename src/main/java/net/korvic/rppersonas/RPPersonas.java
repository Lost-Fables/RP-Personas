package net.korvic.rppersonas;

import co.lotc.core.bukkit.command.Commands;
import net.korvic.rppersonas.accounts.AccountHandler;
import net.korvic.rppersonas.commands.RegisterCommands;
import net.korvic.rppersonas.listeners.JoinQuitListener;
import net.korvic.rppersonas.personas.PersonaHandler;
import net.korvic.rppersonas.sql.*;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.Currency;

public final class RPPersonas extends JavaPlugin {

	public static final boolean DEBUGGING = false;

	private static RPPersonas instance;

	// Handlers
	private AccountHandler accountHandler;
	private PersonaHandler personaHandler;
	private UnregisteredHandler unregisteredHandler;

	// SQL
	private UUIDAccountMapSQL uuidAccountMap;
	private AccountsSQL accounts;
	private PersonaAccountsMapSQL persAccMap;
	private PersonasSQL personas;
	private CurrencySQL currency;
	private SkinsSQL skins;

	@Override
	public void onEnable() {
		// Set this as itself.
		instance = this;

		// Register our Listeners
		getServer().getPluginManager().registerEvents(new JoinQuitListener(instance), instance);

		// Register our handlers
		accountHandler = new AccountHandler(instance);
		personaHandler = new PersonaHandler(instance);
		unregisteredHandler = new UnregisteredHandler(instance);

		// Initiate SQL connections
		setupDatabases();

		// Build our commands
		Commands.build(getCommand("register"), () -> new RegisterCommands(instance));
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
	public UnregisteredHandler getUnregisteredHandler() {
		return unregisteredHandler;
	}

	private void setupDatabases() {
		uuidAccountMap = new UUIDAccountMapSQL(instance);
		accounts = new AccountsSQL(instance);
		persAccMap = new PersonaAccountsMapSQL(instance);
		personas = new PersonasSQL(instance);
		currency = new CurrencySQL(instance);
		skins = new SkinsSQL(instance);

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
