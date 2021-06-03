package net.korvic.rppersonas;

import co.lotc.core.bukkit.command.Commands;
import co.lotc.core.bukkit.util.PlayerUtil;
import lombok.Getter;
import me.clip.placeholderapi.PlaceholderAPI;
import net.korvic.rppersonas.accounts.Account;
import net.korvic.rppersonas.accounts.AccountHandler;
import net.korvic.rppersonas.accounts.UnregisteredHandler;
import net.korvic.rppersonas.commands.*;
import net.korvic.rppersonas.death.Altar;
import net.korvic.rppersonas.death.AltarHandler;
import net.korvic.rppersonas.death.CorpseHandler;
import net.korvic.rppersonas.death.DeathHandler;
import net.korvic.rppersonas.kits.Kit;
import net.korvic.rppersonas.kits.KitHandler;
import net.korvic.rppersonas.listeners.*;
import net.korvic.rppersonas.listeners.StatusEventListener;
import net.korvic.rppersonas.personas.PersonaHandler;
import net.korvic.rppersonas.listeners.SkinDisplayListener;
import net.korvic.rppersonas.personas.PersonaLanguage;
import net.korvic.rppersonas.personas.PersonaSubRace;
import net.korvic.rppersonas.resurrection.RezHandler;
import net.korvic.rppersonas.sql.*;
import net.korvic.rppersonas.sql.util.SaveTracker;
import net.korvic.rppersonas.statuses.*;
import net.md_5.bungee.api.ChatColor;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.PluginManager;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitRunnable;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.stream.Collectors;

public final class RPPersonas extends JavaPlugin {

	// CONSTANTS //
	public static final ChatColor PRIMARY_LIGHT = ChatColor.AQUA;
	public static final ChatColor PRIMARY_DARK = ChatColor.DARK_AQUA;
	public static final ChatColor SECONDARY_LIGHT = ChatColor.WHITE;
	public static final ChatColor SECONDARY_DARK = ChatColor.GRAY;
	public static final ChatColor TERTIARY = ChatColor.DARK_GRAY;
	public static final long BASE_LONG_VALUE = Long.MAX_VALUE/2L;
	public static final String PERMISSION_START = "rppersonas";
	public static final long DAY_IN_MILLIS = 1000L * 60 * 60 * 24;

	public static boolean DEBUGGING;
	public static long AUTO_SAVE_MINS;
	public static int DEFAULT_PERSONAS;
	public static int DEFAULT_SKINS;
	public static int DEFAULT_LIVES;
	public static int DEFAULT_REZ_LIVES;

	@Getter private static RPPersonas instance;
	public static FileConfiguration config;
	public static Date ANOMA_DATE = new Date();
	static {
		SimpleDateFormat sdf = new SimpleDateFormat("dd-M-yyyy hh:mm:ss");
		String anomaDate = "06-06-2020 23:00:00";
		try {
			ANOMA_DATE = sdf.parse(anomaDate);
		} catch (ParseException e) {
			e.printStackTrace();
		}
		ANOMA_DATE.setTime(ANOMA_DATE.getTime() + BASE_LONG_VALUE);
	}

	// Handlers
	@Getter private AccountHandler accountHandler;
	@Getter private PersonaHandler personaHandler;
	@Getter private DeathHandler deathHandler;
	@Getter private CorpseHandler corpseHandler;
	@Getter private AltarHandler altarHandler;
	@Getter private UnregisteredHandler unregisteredHandler;
	@Getter private KitHandler kitHandler;
	@Getter private RezHandler rezHandler;

	// SQL
	@Getter private UUIDAccountMapSQL uuidAccountMapSQL;
	@Getter private AccountsSQL accountsSQL;
	@Getter private PersonaAccountsMapSQL personaAccountMapSQL;
	@Getter private PersonasSQL personasSQL;
	@Getter private CurrencySQL currencySQL;
	@Getter private SkinsSQL skinsSQL;
	@Getter private DeathSQL deathSQL;
	@Getter private CorpseSQL corpseSQL;
	@Getter private AltarSQL altarsSQL;
	@Getter private StatusSQL statusSQL;
	@Getter private LanguageSQL languageSQL;
	@Getter private KarmaSQL karmaSQL;
	@Getter private RezAppSQL rezAppSQL;

	// Default Locations
	@Getter private Location spawnLocation;
	@Getter private Location deathLocation;

	@Override
	public void onEnable() {
		instance = this;

		// Get a copy of the default config if it doesn't already exist
		saveDefaultConfig();
		config = getConfig();

		boolean sqlSuccessful = true;
		// Initiate SQL connections
		try {
			setupDatabases();
		} catch (Exception e) {
			sqlSuccessful = false;
			this.getLogger().severe("FATAL: Failed to initiate SQL database. Please check your credentials.");
			e.printStackTrace();
		}

		if (sqlSuccessful) {
			// Load data from our configs
			loadFromConfig();

			// Start Auto-Save every x mins
			new BukkitRunnable() {
				@Override
				public void run() {
					JoinQuitListener.refreshAllAccountPlaytime();
					corpseHandler.saveAllCorpses();
					personaHandler.queueSaveAllPersonas();
				}
			}.runTaskTimerAsynchronously(this, 0, AUTO_SAVE_MINS * 60 * 20);

			// Start auto-clean for nameplate scoreboards
			BoardManager.toggleAutoClean();

			// Register our Listeners
			PluginManager manager = getServer().getPluginManager();
			manager.registerEvents(new JoinQuitListener(this), this);
			manager.registerEvents(new StatusEventListener(this), this);
			manager.registerEvents(new InspectListener(this), this);
			manager.registerEvents(new EnderListener(this), this);
			manager.registerEvents(new CorpseListener(this), this);
			manager.registerEvents(new KitListener(this), this);

			// If Votifier is online
			if (getServer().getPluginManager().isPluginEnabled("Votifier")) {
				getServer().getPluginManager().registerEvents(new VoteListener(this), this);
			}

			// Packet Listener for skins
			SkinDisplayListener.listen();

			// Register our handlers
			accountHandler = new AccountHandler(this);
			personaHandler = new PersonaHandler(this);
			deathHandler = new DeathHandler(this);
			corpseHandler = new CorpseHandler(this);
			altarHandler = new AltarHandler(this);
			unregisteredHandler = new UnregisteredHandler(this);
			rezHandler = new RezHandler(this);

			// Load up existing altars, corpses, and rez apps. Must be done after the handlers are created
			altarsSQL.loadAltars();
			corpseSQL.loadCorpses();
			rezAppSQL.loadApps();

			// Register parameters
			registerParameters();

			// Register our Placeholders if PlaceholderAPI is enabled
			if(Bukkit.getPluginManager().getPlugin("PlaceholderAPI") != null) {
				new Placeholders(this).register();
			}

			// Build our commands
			Commands.build(getCommand("account"), () -> new AccountCommands(this));
			Commands.build(getCommand("persona"), () -> new PersonaCommands(this));
			Commands.build(getCommand("rpp"), () -> new RPPCommands(this));

			// Register statuses we want on the list of statuses
			new SpeedStatus().registerStatus();
			new SlowStatus().registerStatus();
			new SickStatus().registerStatus();
			new BlindStatus().registerStatus();
		}
	}

	@Override
	public void onDisable() {
		if (personaHandler != null) {
			personaHandler.queueSaveAllPersonas();
		}
	}

	// Get Plugin Instance
	public static RPPersonas get() {
		return instance;
	}

	// SQL //
	private void setupDatabases() throws Exception {
		String host = RPPersonas.config.getString("mysql.host");
		int port = RPPersonas.config.getInt("mysql.port");
		String database = RPPersonas.config.getString("mysql.database");
		String user = RPPersonas.config.getString("mysql.user");
		String password = RPPersonas.config.getString("mysql.password");
		String flags = RPPersonas.config.getString("mysql.flags");
		BaseSQL.init(host, port, database, flags, user, password);

		uuidAccountMapSQL = new UUIDAccountMapSQL(this);
		accountsSQL = new AccountsSQL(this);
		personaAccountMapSQL = new PersonaAccountsMapSQL(this);
		personasSQL = new PersonasSQL(this);
		currencySQL = new CurrencySQL(this);
		skinsSQL = new SkinsSQL(this);
		deathSQL = new DeathSQL(this);
		corpseSQL = new CorpseSQL(this);
		altarsSQL = new AltarSQL(this);
		statusSQL = new StatusSQL(this);
		languageSQL = new LanguageSQL(this);
		karmaSQL = new KarmaSQL(this);
		rezAppSQL = new RezAppSQL(this);
	}

	// Spawn & Death Location
	public void setSpawnLocation(Location loc) {
		if (loc != null) {
			spawnLocation = loc;
			updateConfigForSpawn(loc);
		}
	}

	public void setDeathLocation(Location loc) {
		if (loc != null) {
			deathLocation = loc;
			updateConfigForDeath(loc);
		}
	}

	// CONFIG //
	private void loadFromConfig() {
		// Default Values
		DEBUGGING = config.getBoolean("debugging");
		AUTO_SAVE_MINS = Math.max(config.getLong("saving.mins"), 1);
		DEFAULT_PERSONAS = Math.max(config.getInt("defaults.personas-per-mc-account"), 1);
		DEFAULT_LIVES = Math.max(config.getInt("defaults.lives-per-persona"), 1);
		DEFAULT_REZ_LIVES = Math.max(config.getInt("defaults.lives-per-rez"), 1);
		DEFAULT_SKINS = Math.max(config.getInt("defaults.skins-per-account"), 0);

		// Save Tracker
		SaveTracker.setPlugin(this);

		// Spawn
		{
			String spawnWorldName = config.getString("spawn.world");
			if (spawnWorldName != null && Bukkit.getWorld(spawnWorldName) != null) {
				String facing = config.getString("spawn.facing");
				if (facing != null) {
					spawnLocation = new Location(Bukkit.getWorld(spawnWorldName), config.getDouble("spawn.x"), config.getDouble("spawn.y"), config.getDouble("spawn.z"), getYawFromFacing(facing), 0);
				}
			}
		}

		// Death
		{
			String spawnWorldName = config.getString("death.world");
			if (spawnWorldName != null && Bukkit.getWorld(spawnWorldName) != null) {
				String facing = config.getString("death.facing");
				if (facing != null) {
					deathLocation = new Location(Bukkit.getWorld(spawnWorldName), config.getDouble("death.x"), config.getDouble("death.y"), config.getDouble("death.z"), getYawFromFacing(facing), 0);
				}
			}
		}

		// Kits
		{
			ConfigurationSection section = config.getConfigurationSection("kits");
			if (section != null) {
				kitHandler = new KitHandler(this);
				for (String kitName : section.getKeys(false)) {
					List<ItemStack> list;
					try {
						list = (List<ItemStack>) config.getList("kits." + kitName);
					} catch (Exception e) {
						list = new ArrayList<>();
						if (DEBUGGING) {
							e.printStackTrace();
						}
					}
					Kit kit = new Kit(kitName, list);
					kitHandler.addKit(kit);
				}
			}
		}
	}

	// KITS
	public void updateConfigForKit(Kit kit) {
		if (kit != null) {
			config = getConfig();
			config.set("kits." + kit.getName(), kit.getItems());
			saveConfig();
		}
	}

	public void deleteConfigForKit(Kit kit) {
		if (kit != null) {
			config = getConfig();
			config.set("kits." + kit.getName(), null);
			saveConfig();
		}
	}

	// SPAWN AND DEATH
	private void updateConfigForSpawn(Location loc) {
		updateConfigForSpawnOrDeath(loc, false);
	}

	private void updateConfigForDeath(Location loc) {
		updateConfigForSpawnOrDeath(loc, true);
	}

	private void updateConfigForSpawnOrDeath(Location loc, boolean death) {
		if (loc != null && loc.getWorld() != null) {
			String type = "spawn";
			if (death) {
				type = "death";
			}
			config = getConfig();
			config.set(type + ".world", loc.getWorld().getName());
			config.set(type + ".x", loc.getBlockX() + 0.5d);
			config.set(type + ".y", loc.getBlockY() + 0.5d);
			config.set(type + ".z", loc.getBlockZ() + 0.5d);

			config.set(type + ".facing", getFacingFromYaw(loc.getYaw()));
			saveConfig();
		}
	}

	// PARAMETERS //
	private void registerParameters() {
		Commands.defineArgumentType(Altar.class)
				.defaultName("Altar")
				.defaultError("Failed to find an altar by that name.")
				.completer(() -> altarHandler.getAltarNameList())
				.mapperWithSender((sender, name) -> altarHandler.getAltar(name))
				.register();

		Commands.defineArgumentType(Status.class)
				.defaultName("Status")
				.defaultError("Failed to find a status by that name.")
				.completer(Status::getRegisteredStatusNames)
				.mapperWithSender((sender, status) -> Status.getByName(status))
				.register();

		Commands.defineArgumentType(Kit.class)
				.defaultName("Kit")
				.defaultError("Failed to find a kit by that name.")
				.completer(() -> kitHandler.getKitNameList())
				.mapperWithSender((sender, kit) -> kitHandler.getKit(kit))
				.register();

		Commands.defineArgumentType(PersonaLanguage.class)
				.defaultName("Language")
				.defaultError("Failed to find a language by that name.")
				.completer(PersonaLanguage::getNames)
				.mapperWithSender((sender, lang) -> PersonaLanguage.getByName(lang))
				.register();

		Commands.defineArgumentType(PersonaSubRace.class)
				.defaultName("Race")
				.defaultError("Failed to find a race by that name.")
				.completer(PersonaSubRace::getNames)
				.mapperWithSender((sender, race) -> PersonaSubRace.getByName(race))
				.register();

		Commands.defineArgumentType(Account.class)
				.defaultName("Player or Account")
				.defaultError("Unable to find an account for that user or number.")
				.completer((s,$) -> {
					return Arrays.stream(getServer().getOnlinePlayers().toArray()).map(object -> ((Player) object).getName()).collect(Collectors.toList());
				})
				.mapperWithSender((sender, account) -> {
					try {
						int accountID = Integer.parseInt(account);
						return accountHandler.getAccountForcefully(accountID);
					} catch (NumberFormatException nfe) {
						UUID uuid = PlayerUtil.getPlayerUUID(account);
						if (uuid != null) {
							Player player = Bukkit.getPlayer(uuid);
							return accountHandler.getAccountForcefully(player);
						}
					}
					return null;
				})
				.register();
	}

	public static float getYawFromFacing(String facing) {
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

	public static String getFacingFromYaw(float yaw) {
		int halfwayGap = 45;
		String output = "north";
		if (yaw < 180-halfwayGap) {
			if (yaw >= 90 - halfwayGap) {
				output = "west";
			} else if (yaw >= -halfwayGap) {
				output = "south";
			} else if (yaw >= -90 - halfwayGap) {
				output = "east";
			}
		}
		return output;
	}

	public static String getPrefixColor(Player p) {
		String output = "";

		if (Bukkit.getPluginManager().isPluginEnabled("PlaceholderAPI") &&
			Bukkit.getPluginManager().isPluginEnabled("Rollit")) {
			output = PlaceholderAPI.setPlaceholders(Bukkit.getOfflinePlayer(p.getUniqueId()), "%rollit_prefix%");
		}

		return output;
	}
}
