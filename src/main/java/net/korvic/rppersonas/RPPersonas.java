package net.korvic.rppersonas;

import net.korvic.rppersonas.accounts.AccountHandler;
import net.korvic.rppersonas.listeners.JoinQuitListener;
import net.korvic.rppersonas.sql.AccountsSQL;
import org.bukkit.plugin.java.JavaPlugin;

public final class RPPersonas extends JavaPlugin {

	public static final boolean DEBUGGING = false;

	private static RPPersonas instance;
	private AccountHandler accountHandler;
	private AccountsSQL database;

	@Override
	public void onEnable() {
		getServer().getPluginManager().registerEvents(new JoinQuitListener(this), this);

		accountHandler = new AccountHandler(this);

		database = new AccountsSQL(this);

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

	public AccountsSQL getSQL() {
		return database;
	}
}
