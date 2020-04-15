package net.korvic.rppersonas;

import co.lotc.core.bukkit.command.Commands;
import net.korvic.rppersonas.accounts.AccountHandler;
import net.korvic.rppersonas.altars.AltarHandler;
import net.korvic.rppersonas.commands.AccountCommands;
import net.korvic.rppersonas.commands.AltarCommands;
import net.korvic.rppersonas.commands.PersonaCommands;
import net.korvic.rppersonas.listeners.EnderListener;
import net.korvic.rppersonas.listeners.InspectListener;
import net.korvic.rppersonas.listeners.JoinQuitListener;
import net.korvic.rppersonas.listeners.VoteListener;
import net.korvic.rppersonas.personas.modification.PersonaDisableListener;
import net.korvic.rppersonas.personas.PersonaHandler;
import net.korvic.rppersonas.personas.aspects.PlayerDisplayListener;
import net.korvic.rppersonas.sql.*;
import net.md_5.bungee.api.ChatColor;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitRunnable;

public final class RPPersonas extends JavaPlugin {

	// CONSTANTS //
	public static final ChatColor PRIMARY_LIGHT = ChatColor.AQUA;
	public static final ChatColor PRIMARY_DARK = ChatColor.DARK_AQUA;
	public static final ChatColor SECONDARY_LIGHT = ChatColor.WHITE;
	public static final ChatColor SECONDARY_DARK = ChatColor.GRAY;
	public static final boolean DEBUGGING = true;
	public static final long BASE_LONG_VALUE = Long.MAX_VALUE/2L;
	public static final int DEFAULT_PERSONAS = 2;
	public static final String PERMISSION_START = "rppersonas";
	public static final long DAY_IN_MILLIS = 1000L * 60 * 60 * 24;
	public static final long MONTH_IN_MILLIS = DAY_IN_MILLIS * 30;
	public static final long YEAR_IN_MILLIS = DAY_IN_MILLIS * 365;

	public static FileConfiguration config;
	private static RPPersonas instance;

	// Handlers
	private AccountHandler accountHandler;
	private PersonaHandler personaHandler;
	private AltarHandler altarHandler;
	private UnregisteredHandler unregisteredHandler;

	// SQL
	private SaveQueue saveQueue;
	private UUIDAccountMapSQL uuidAccountMap;
	private AccountsSQL accounts;
	private PersonaAccountsMapSQL personaAccountMap;
	private PersonasSQL personas;
	private CurrencySQL currency;
	private SkinsSQL skins;
	private AltarSQL altars;

	// Default Location
	private Location spawnLocation;

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
			// Load data from our configs.
			loadFromConfig();

			// Start Auto-Save every 30 mins
			new BukkitRunnable() {
				@Override
				public void run() {
					JoinQuitListener.refreshAllAccountPlaytime();
					personaHandler.queueSavingAll();
					//accountHandler.queueSavingAll();
				}
			}.runTaskTimerAsynchronously(this, 0, 36000);

			// Register our Listeners
			getServer().getPluginManager().registerEvents(new JoinQuitListener(this), this);
			getServer().getPluginManager().registerEvents(new PersonaDisableListener(this), this);
			getServer().getPluginManager().registerEvents(new InspectListener(this), this);
			getServer().getPluginManager().registerEvents(new EnderListener(this), this);

			// If Votifier is online
			if (getServer().getPluginManager().isPluginEnabled("Votifier")) {
				getServer().getPluginManager().registerEvents(new VoteListener(this), this);
			}

			// Packet Listener for skins
			PlayerDisplayListener.listen();

			// Register our handlers
			accountHandler = new AccountHandler(this);
			personaHandler = new PersonaHandler(this);
			altarHandler = new AltarHandler(this);
			unregisteredHandler = new UnregisteredHandler(this);

			// Build our commands
			Commands.build(getCommand("account"), () -> new AccountCommands(this));
			Commands.build(getCommand("persona"), () -> new PersonaCommands(this));
			Commands.build(getCommand("altar"), () -> new AltarCommands(this)); // TODO move this under staff commands
		} else {
			this.onDisable();
		}
	}

	@Override
	public void onDisable() {
		saveQueue.completeAllSaves();
		PersonaDisableListener.enableAll();
	}

	// SQL //
	private void setupDatabases() {
		uuidAccountMap = new UUIDAccountMapSQL(this);
		accounts = new AccountsSQL(this);
		personaAccountMap = new PersonaAccountsMapSQL(this);
		personas = new PersonasSQL(this);
		currency = new CurrencySQL(this);
		skins = new SkinsSQL(this);
		altars = new AltarSQL(this);
	}

	// CONFIG //
	private void loadFromConfig() {
		saveQueue = new SaveQueue(this, config.getInt("saving.ticks"), config.getInt("saving.amount"), config.getInt("saving.percent"));
		String world = config.getString("spawn.world");
		if (world != null && Bukkit.getWorld(world) != null) {
			String facing = config.getString("spawn.facing");
			if (facing != null) {
				spawnLocation = new Location(Bukkit.getWorld(world), config.getDouble("spawn.x"), config.getDouble("spawn.y"), config.getDouble("spawn.z"), getYawFromFacing(facing), 0);
			}
		}
	}

	private float getYawFromFacing(String facing) {
		float output = 0;
		if (facing.equalsIgnoreCase("west")) {
			output = 90;
		} else if (facing.equalsIgnoreCase("north")) {
			output = 180;
		} else if (facing.equalsIgnoreCase("east")) {
			output = -90;
		}
		return output;
	}

	// GET //
	public static RPPersonas get() {
		return instance;
	}
	public AccountHandler getAccountHandler() {
		return accountHandler;
	}
	public PersonaHandler getPersonaHandler() {
		return personaHandler;
	}
	public AltarHandler getAltarHandler() {
		return altarHandler;
	}
	public UnregisteredHandler getUnregisteredHandler() {
		return unregisteredHandler;
	}

	public Location getSpawnLocation() {
		return spawnLocation;
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
	public AltarSQL getAltarSQL() {
		return altars;
	}
	public SaveQueue getSaveQueue() {
		return saveQueue;
	}

	// TIME //
	public static long getCurrentTime() {
		return (BASE_LONG_VALUE + System.currentTimeMillis());
	}

	public static long getMillisFromAge(int ages) {
		return (getCurrentTime() - (ages * 3 * MONTH_IN_MILLIS));
	}
	public static long getMillisFromEra(int eras) {
		return (getCurrentTime() - (eras * YEAR_IN_MILLIS));
	}

	public static int getRelativeAges(long millis) {
		return (int) (((getCurrentTime() - millis) / MONTH_IN_MILLIS) / 3);
	}
	public static int getRelativeEras(long millis) {
		return (int) ((getCurrentTime() - millis) / YEAR_IN_MILLIS);
	}
	public static String getRelativeTimeString(long millis) {
		return (getRelativeAges(millis) + " Ages; (" + getRelativeEras(millis) + " Eras)");
	}
}
