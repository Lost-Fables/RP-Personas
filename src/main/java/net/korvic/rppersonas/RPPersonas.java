package net.korvic.rppersonas;

import net.korvic.rppersonas.accounts.AccountHandler;
import net.korvic.rppersonas.listeners.JoinQuitListener;
import net.korvic.rppersonas.sql.*;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.Currency;

public final class RPPersonas extends JavaPlugin {

	public static final boolean DEBUGGING = false;

	private static RPPersonas instance;
	private AccountHandler accountHandler;

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
}
