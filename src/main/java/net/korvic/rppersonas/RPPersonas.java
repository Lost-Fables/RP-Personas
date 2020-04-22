package net.korvic.rppersonas;

import co.lotc.core.bukkit.command.Commands;
import net.korvic.rppersonas.accounts.AccountHandler;
import net.korvic.rppersonas.accounts.UnregisteredHandler;
import net.korvic.rppersonas.death.Altar;
import net.korvic.rppersonas.death.AltarHandler;
import net.korvic.rppersonas.commands.AccountCommands;
import net.korvic.rppersonas.commands.AltarCommands;
import net.korvic.rppersonas.commands.PersonaCommands;
import net.korvic.rppersonas.death.CorpseHandler;
import net.korvic.rppersonas.death.DeathHandler;
import net.korvic.rppersonas.listeners.*;
import net.korvic.rppersonas.listeners.StatusEventListener;
import net.korvic.rppersonas.personas.PersonaHandler;
import net.korvic.rppersonas.listeners.PlayerDisplayListener;
import net.korvic.rppersonas.sql.*;
import net.korvic.rppersonas.sql.extras.SaveQueue;
import net.md_5.bungee.api.ChatColor;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.plugin.PluginManager;
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
	public static final long WEEK_IN_MILLIS = DAY_IN_MILLIS * 7;
	public static final long MONTH_IN_MILLIS = DAY_IN_MILLIS * 30;
	public static final long YEAR_IN_MILLIS = DAY_IN_MILLIS * 365;

	public static FileConfiguration config;
	private static RPPersonas instance;

	// Handlers
	private AccountHandler accountHandler;
	private PersonaHandler personaHandler;
	private DeathHandler deathHandler;
	private CorpseHandler corpseHandler;
	private AltarHandler altarHandler;
	private UnregisteredHandler unregisteredHandler;

	// SQL
	private SaveQueue saveQueueSQL;
	private UUIDAccountMapSQL uuidAccountMapSQL;
	private AccountsSQL accountsSQL;
	private PersonaAccountsMapSQL personaAccountMapSQL;
	private PersonasSQL personasSQL;
	private CurrencySQL currencySQL;
	private SkinsSQL skinsSQL;
	private DeathSQL deathSQL;
	private CorpseSQL corpseSQL;
	private AltarSQL altarsSQL;

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
					corpseHandler.saveAllCorpses();
					personaHandler.saveAllPersonas();
				}
			}.runTaskTimerAsynchronously(this, 0, 36000);

			// Register our Listeners
			PluginManager manager = getServer().getPluginManager();
			manager.registerEvents(new JoinQuitListener(this), this);
			manager.registerEvents(new StatusEventListener(this), this);
			manager.registerEvents(new InspectListener(this), this);
			manager.registerEvents(new EnderListener(this), this);
			manager.registerEvents(new CorpseListener(this), this);

			// If Votifier is online
			if (getServer().getPluginManager().isPluginEnabled("Votifier")) {
				getServer().getPluginManager().registerEvents(new VoteListener(this), this);
			}

			// Packet Listener for skins
			PlayerDisplayListener.listen();

			// Register our handlers
			accountHandler = new AccountHandler(this);
			personaHandler = new PersonaHandler(this);
			deathHandler = new DeathHandler(this);
			corpseHandler = new CorpseHandler(this);
			altarHandler = new AltarHandler(this);
			unregisteredHandler = new UnregisteredHandler(this);

			// Load up existing altars & corpses. Must be done after the alter handler is created.
			altarsSQL.loadAltars();
			corpseSQL.loadCorpses();

			// Register parameters
			registerParameters();

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
		saveQueueSQL.completeAllSaves();
		BaseSQL.cancelConnectionMaintainer();
	}

	// SQL //
	private void setupDatabases() {
		uuidAccountMapSQL = new UUIDAccountMapSQL(this);
		accountsSQL = new AccountsSQL(this);
		personaAccountMapSQL = new PersonaAccountsMapSQL(this);
		personasSQL = new PersonasSQL(this);
		currencySQL = new CurrencySQL(this);
		skinsSQL = new SkinsSQL(this);
		deathSQL = new DeathSQL(this);
		corpseSQL = new CorpseSQL(this);
		altarsSQL = new AltarSQL(this);
	}

	// CONFIG //
	private void loadFromConfig() {
		saveQueueSQL = new SaveQueue(this, config.getInt("saving.ticks"), config.getInt("saving.amount"), config.getInt("saving.percent"));
		String world = config.getString("spawn.world");
		if (world != null && Bukkit.getWorld(world) != null) {
			String facing = config.getString("spawn.facing");
			if (facing != null) {
				spawnLocation = new Location(Bukkit.getWorld(world), config.getDouble("spawn.x"), config.getDouble("spawn.y"), config.getDouble("spawn.z"), getYawFromFacing(facing), 0);
			}
		}
	}

	// PARAMETERS //
	private void registerParameters() {

		Commands.defineArgumentType(Altar.class)
				.defaultName("Altar")
				.defaultError("Failed to find an altar by that name.")
				.completer(() -> altarHandler.getAltarList())
				.mapperWithSender((sender, name) -> altarHandler.getAltar(name))
				.register();

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
	public DeathHandler getDeathHandler() {
		return deathHandler;
	}
	public CorpseHandler getCorpseHandler() {
		return corpseHandler;
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
		return uuidAccountMapSQL;
	}
	public AccountsSQL getAccountsSQL() {
		return accountsSQL;
	}
	public PersonaAccountsMapSQL getPersonaAccountMapSQL() {
		return personaAccountMapSQL;
	}
	public PersonasSQL getPersonasSQL() {
		return personasSQL;
	}
	public CurrencySQL getCurrencySQL() {
		return currencySQL;
	}
	public SkinsSQL getSkinsSQL() {
		return skinsSQL;
	}
	public DeathSQL getDeathSQL() {
		return deathSQL;
	}
	public CorpseSQL getCorpseSQL() {
		return corpseSQL;
	}
	public AltarSQL getAltarSQL() {
		return altarsSQL;
	}
	public SaveQueue getSaveQueue() {
		return saveQueueSQL;
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
