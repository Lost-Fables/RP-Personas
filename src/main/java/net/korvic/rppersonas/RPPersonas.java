package net.korvic.rppersonas;

import co.lotc.core.bukkit.command.Commands;
import net.korvic.rppersonas.accounts.AccountHandler;
import net.korvic.rppersonas.commands.AccountCommands;
import net.korvic.rppersonas.commands.PersonaCommands;
import net.korvic.rppersonas.listeners.JoinQuitListener;
import net.korvic.rppersonas.personas.PersonaHandler;
import net.korvic.rppersonas.sql.*;
import org.bukkit.ChatColor;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitRunnable;

public final class RPPersonas extends JavaPlugin {

	public static final String PREFIX = ChatColor.DARK_AQUA + "";
	public static final String ALT_COLOR = ChatColor.WHITE + "";
	public static final boolean DEBUGGING = true;
	public static final long BASE_LONG_VALUE = Long.MAX_VALUE/2L;
	public static final int DEFAULT_PERSONAS = 2;
	public static final String PERMISSION_START = "rppersonas";

	public static FileConfiguration config;
	private static RPPersonas instance;

	// Handlers
	private AccountHandler accountHandler;
	private PersonaHandler personaHandler;
	private UnregisteredHandler unregisteredHandler;

	// SQL
	private SaveQueue saveQueue;
	private UUIDAccountMapSQL uuidAccountMap;
	private AccountsSQL accounts;
	private PersonaAccountsMapSQL personaAccountMap;
	private PersonasSQL personas;
	private CurrencySQL currency;
	private SkinsSQL skins;

	@Override
	public void onEnable() {
		instance = this;

		// Get a copy of the default config if it doesn't already exist.
		saveDefaultConfig();
		config = getConfig();

		boolean sqlSuccessful = true;
		// Initiate SQL connections
		try {
			setupDatabases();
		} catch (Exception e) {
			sqlSuccessful = false;
			this.getLogger().severe("FATAL: Failed to initiate SQL database. Please check your credentials.");
			if (DEBUGGING) {
				e.printStackTrace();
			}
		}

		if (sqlSuccessful) {
			// Start Auto-Save every 30 mins
			new BukkitRunnable() {
				@Override
				public void run() {
					personaHandler.queueSavingAll();
					//accountHandler.queueSavingAll();
				}
			}.runTaskTimerAsynchronously(this, 0, 36000);

			// Register our Listeners
			getServer().getPluginManager().registerEvents(new JoinQuitListener(this), this);

			// Register our handlers
			accountHandler = new AccountHandler(this);
			personaHandler = new PersonaHandler(this);
			unregisteredHandler = new UnregisteredHandler(this);

			// Build our commands
			Commands.build(getCommand("account"), () -> new AccountCommands(this));
			Commands.build(getCommand("persona"), () -> new PersonaCommands(this));
		} else {
			this.onDisable();
		}
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
		uuidAccountMap = new UUIDAccountMapSQL(this);
		accounts = new AccountsSQL(this);
		personaAccountMap = new PersonaAccountsMapSQL(this);
		personas = new PersonasSQL(this);
		currency = new CurrencySQL(this);
		skins = new SkinsSQL(this);

		uuidAccountMap.load();
		accounts.load();
		personaAccountMap.load();
		personas.load();
		currency.load();
		skins.load();

		saveQueue = new SaveQueue(this, config.getInt("saving.ticks"), config.getInt("saving.amount"), config.getInt("saving.percent"));
	}

	public UUIDAccountMapSQL getUUIDAccountMapSQL() {
		return uuidAccountMap;
	}
	public AccountsSQL getAccountsSQL() {
		return accounts;
	}
	public PersonaAccountsMapSQL getPersonaAccountMapSQL() {
		return personaAccountMap;
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
	public SaveQueue getSaveQueue() {
		return saveQueue;
	}
}
