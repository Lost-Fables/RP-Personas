package net.korvic.rppersonas;

import co.lotc.core.bukkit.command.Commands;
import lombok.Getter;
import me.clip.placeholderapi.PlaceholderAPI;
import net.korvic.rppersonas.accounts.AccountHandler;
import net.korvic.rppersonas.accounts.UnregisteredHandler;
import net.korvic.rppersonas.commands.TimeCommands;
import net.korvic.rppersonas.death.Altar;
import net.korvic.rppersonas.death.AltarHandler;
import net.korvic.rppersonas.commands.AccountCommands;
import net.korvic.rppersonas.commands.RPPCommands;
import net.korvic.rppersonas.commands.PersonaCommands;
import net.korvic.rppersonas.death.CorpseHandler;
import net.korvic.rppersonas.death.DeathHandler;
import net.korvic.rppersonas.kits.Kit;
import net.korvic.rppersonas.kits.KitHandler;
import net.korvic.rppersonas.listeners.*;
import net.korvic.rppersonas.listeners.StatusEventListener;
import net.korvic.rppersonas.personas.PersonaHandler;
import net.korvic.rppersonas.listeners.SkinDisplayListener;
import net.korvic.rppersonas.personas.PersonaLanguage;
import net.korvic.rppersonas.resurrection.RezHandler;
import net.korvic.rppersonas.sql.*;
import net.korvic.rppersonas.sql.util.SaveQueue;
import net.korvic.rppersonas.statuses.*;
import net.korvic.rppersonas.time.Season;
import net.korvic.rppersonas.time.TimeManager;
import net.luckperms.api.LuckPermsProvider;
import net.luckperms.api.context.ImmutableContextSet;
import net.luckperms.api.model.user.User;
import net.luckperms.api.node.Node;
import net.luckperms.api.node.NodeType;
import net.luckperms.api.node.types.PrefixNode;
import net.luckperms.api.query.QueryOptions;
import net.md_5.bungee.api.ChatColor;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.PluginManager;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public final class RPPersonas extends JavaPlugin {

	// CONSTANTS //
	public static final ChatColor PRIMARY_LIGHT = ChatColor.AQUA;
	public static final ChatColor PRIMARY_DARK = ChatColor.DARK_AQUA;
	public static final ChatColor SECONDARY_LIGHT = ChatColor.WHITE;
	public static final ChatColor SECONDARY_DARK = ChatColor.GRAY;
	public static final ChatColor TERTIARY = ChatColor.DARK_GRAY;
	public static final boolean DEBUGGING = false;
	public static final long BASE_LONG_VALUE = Long.MAX_VALUE/2L;
	public static final int DEFAULT_PERSONAS = 2;
	public static final int DEFAULT_SKINS = 0;
	public static final String PERMISSION_START = "rppersonas";
	public static final long DAY_IN_MILLIS = 1000L * 60 * 60 * 24;

	public static FileConfiguration config;
	private static RPPersonas instance;

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
	@Getter private SaveQueue saveQueue;
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

			// Start Auto-Save every 30 mins
			new BukkitRunnable() {
				@Override
				public void run() {
					JoinQuitListener.refreshAllAccountPlaytime();
					corpseHandler.saveAllCorpses();
					personaHandler.queueSaveAllPersonas();
				}
			}.runTaskTimerAsynchronously(this, 0, 36000);

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
			TimeCommands timeCommands = new TimeCommands(this);
			Commands.build(getCommand("account"), () -> new AccountCommands(this));
			Commands.build(getCommand("persona"), () -> new PersonaCommands(this));
			Commands.build(getCommand("rpp"), () -> new RPPCommands(this, timeCommands));
			Commands.build(getCommand("time"), () -> timeCommands);

			// Register statuses we want on the list of statuses
			new SpeedStatus().registerStatus();
			new SlowStatus().registerStatus();
			new SickStatus().registerStatus();
			new BlindStatus().registerStatus();
		}
	}

	@Override
	public void onDisable() {
		if (saveQueue != null) {
			saveQueue.stopSaving();
		}
		if (personaHandler != null) {
			personaHandler.queueSaveAllPersonas();
		}
		if (saveQueue != null) {
			saveQueue.completeAllSaves();
		}
		BaseSQL.cancelConnectionMaintainer();
	}

	// Get Plugin Instance
	public static RPPersonas get() {
		return instance;
	}

	// SQL //
	private void setupDatabases() throws Exception {
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
		// Save Queue
		saveQueue = new SaveQueue(this, config.getInt("saving.ticks"), config.getInt("saving.amount"), config.getInt("saving.percent"));

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

		// World Time
		{
			ConfigurationSection section = config.getConfigurationSection("worlds");
			if (section != null) {
				for (String worldName : section.getKeys(false)) {
					World world = Bukkit.getWorld(worldName);

					TimeManager manager = TimeManager.registerWorld(world, false);
					manager.setSeason(config.getString("worlds." + worldName + ".season"), false);
					manager.setTimeScale(config.getInt("worlds." + worldName + ".timescale"), false);

					List<String> syncedWorlds = section.getStringList(worldName + ".synced");
					for (String str : syncedWorlds) {
						World syncedWorld = Bukkit.getWorld(str);
						manager.addSyncedWorld(syncedWorld, false);
					}
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

	// TIME
	public void updateConfigForWorld(String worldName, String season, int timeScale, List<String> syncedWorlds) {
		String configPath = "worlds." + worldName;
		config = getConfig();
		if (season != null) {
			config.set(configPath + ".season", season);
		}
		if (timeScale > 20) {
			config.set(configPath + ".timescale", timeScale);
		}
		if (syncedWorlds != null) {
			config.set(configPath + ".synced", syncedWorlds);
		}
		saveConfig();
	}

	public void deleteConfigForWorld(String worldName) {
		config = getConfig();
		config.set("worlds." + worldName, null);
		saveConfig();
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

		Commands.defineArgumentType(Season.class)
				.defaultName("Season")
				.completer((s,$) -> Season.getAvailable(s))
				.mapperWithSender((sender, season) -> Season.getByName(season))
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
			output = PlaceholderAPI.setPlaceholders(p, "%rollit_prefix%");
		}

		return output;
	}
}
