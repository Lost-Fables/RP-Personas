package net.korvic.rppersonas.personas;

import com.comphenix.protocol.PacketType;
import com.comphenix.protocol.ProtocolLibrary;
import com.comphenix.protocol.ProtocolManager;
import com.comphenix.protocol.events.PacketContainer;
import com.comphenix.protocol.injector.netty.WirePacket;
import com.comphenix.protocol.utility.MinecraftReflection;
import com.comphenix.protocol.wrappers.*;
import com.google.common.collect.Lists;
import com.google.common.hash.Hashing;
import net.korvic.rppersonas.RPPersonas;
import org.bukkit.*;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.util.Vector;

import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.List;
import java.util.Map;

public class PersonaSkin {

	// static final methods are faster, because JVM can inline them and make them accessible
	private static final Class<Object> RESOURCE_KEY_CLASS = (Class<Object>) MinecraftReflection.getMinecraftClass("ResourceKey");
	private static final Field WORLD_KEY_FIELD;
	private static final Field DEBUG_WORLD_FIELD;

	private static final boolean DISABLED_PACKETS;

	static {
		boolean methodAvailable;
		try {
			Player.class.getDeclaredMethod("hidePlayer", Plugin.class, Player.class);
			methodAvailable = true;
		} catch (NoSuchMethodException noSuchMethodEx) {
			methodAvailable = false;
		}

		boolean localDisable = false;
		Field localWorldKey = null;
		Field localDebugWorld = null;

		Method localHandleMethod = null;
		Field localInteractionField = null;
		Field localGamemode = null;

		// use standard reflection, because we cannot use the performance benefits of MethodHandles
		// MethodHandles are only clearly faster with invokeExact
		// we can use for a nested call of debug world: getDebugField(getNMSWorldFromBukkit) in a single handle
		// But for the resourceKey the return type is not known at compile time - it's an NMS class
		try {
			Class<?> nmsWorldClass = MinecraftReflection.getNmsWorldClass();
			localWorldKey = nmsWorldClass.getDeclaredField("dimensionKey");
			localWorldKey.setAccessible(true);

			localDebugWorld = nmsWorldClass.getDeclaredField("debugWorld");
			localDebugWorld.setAccessible(true);
		} catch (NoSuchFieldException reflectiveEx) {
			localDisable = true;
		}
		WORLD_KEY_FIELD = localWorldKey;
		DEBUG_WORLD_FIELD = localDebugWorld;
		DISABLED_PACKETS = localDisable;
	}

	private int skinID;
	private String name;
	private String texture;
	private WrappedSignedProperty mojangData;

	public static PersonaSkin getFromID(int skinID) {
		if (skinID > 0) {
			Map<Object, Object> skinData = RPPersonas.get().getSkinsSQL().getData(skinID);
			if (skinData.containsKey("name") && skinData.containsKey("texture") && skinData.containsKey("signature")) {
				return new PersonaSkin(skinID, (String) skinData.get("name"), (String) skinData.get("texture"), (String) skinData.get("signature"));
			}
		}
		return null;
	}

	public PersonaSkin(int skinID, String name, String texture, String signature) {
		this.skinID = skinID;
		this.name = name;
		this.texture = texture;

		this.mojangData = new WrappedSignedProperty("textures", texture, signature);
	}

	public int getSkinID() {
		return skinID;
	}

	public String getName() {
		return name;
	}

	public String getTextureValue() {
		return texture;
	}

	public WrappedSignedProperty getMojangData() {
		return mojangData;
	}

	public static void refreshOthers(Player p) {
		Bukkit.getOnlinePlayers().stream()
			  .filter(x -> (x != p))
			  .filter(x -> x.canSee(p))
			  .forEach(x -> {p.hidePlayer(RPPersonas.get(), x); p.showPlayer(RPPersonas.get(), x);});
	}

	public static void refreshPlayerSync(Player p) {
		new BukkitRunnable() {
			@Override
			public void run() {
				refreshPlayer(p);
			}
		}.runTask(RPPersonas.get());
	}
	// ProtocolLib Refreshing
	private static void refreshPlayer(Player p) {
		if (p.isInsideVehicle()) {
			p.leaveVehicle();
		}

		Bukkit.getOnlinePlayers().stream()
			  .filter(x -> (x != p))
			  .filter(x -> x.canSee(p))
			  .forEach(x -> {x.hidePlayer(RPPersonas.get(), p); x.showPlayer(RPPersonas.get(), p);});

		final ProtocolManager manager = ProtocolLibrary.getProtocolManager();

		WrappedGameProfile profile = WrappedGameProfile.fromPlayer(p); //Protocollib for version independence
		List<PlayerInfoData> playerInfoList = Lists.newArrayList();

		playerInfoList.add(new PlayerInfoData(profile,
											  1, //who cares honestly
											  EnumWrappers.NativeGameMode.fromBukkit(p.getGameMode()),
											  WrappedChatComponent.fromText(p.getDisplayName())));

		final PacketContainer packetDel = manager.createPacket(PacketType.Play.Server.PLAYER_INFO);
		final PacketContainer packetAdd = manager.createPacket(PacketType.Play.Server.PLAYER_INFO);
		packetDel.getPlayerInfoAction().write(0, EnumWrappers.PlayerInfoAction.REMOVE_PLAYER);
		packetAdd.getPlayerInfoAction().write(0, EnumWrappers.PlayerInfoAction.ADD_PLAYER);
		packetDel.getPlayerInfoDataLists().write(0, playerInfoList);
		packetAdd.getPlayerInfoDataLists().write(0, playerInfoList);

		try {
			manager.sendServerPacket(p, packetDel);
			manager.sendServerPacket(p, packetAdd);
			fakeRespawn(p);
		} catch (InvocationTargetException e) {
			e.printStackTrace();
		} catch (Exception e) {
			RPPersonas.get().getLogger().warning("Screwed up on NMS. Call a doctor.");
			e.printStackTrace();
		}
	}

	@SuppressWarnings("deprecation")
	private static void fakeRespawn(Player p) throws IllegalAccessException {
		// Grab our origin dimension and any dimension that isn't our current one (if possible).
		int originDimension = p.getWorld().getEnvironment().getId();
		int dimension = originDimension;
		for (World world : Bukkit.getWorlds()) {
			if (world != p.getWorld()) {
				dimension = world.getEnvironment().getId();
				break;
			}
		}

		Object nmsWorld = BukkitConverters.getWorldConverter().getGeneric(p.getWorld());
		Object resourceKey = WORLD_KEY_FIELD.get(nmsWorld);
		long seed = p.getWorld().getSeed();

		// Do two respawn packets as recommended by ProtocolLib
		PacketContainer firstRespawn = buildRespawn(dimension, resourceKey, nmsWorld, seed, p);
		PacketContainer secondRespawn = buildRespawn(originDimension, resourceKey, nmsWorld, seed, p);

		try {
			{
				ProtocolManager manager = ProtocolLibrary.getProtocolManager();
				manager.sendServerPacket(p, firstRespawn);
				manager.sendServerPacket(p, secondRespawn);

				// Some wizardry here to make the right amount of hearts show up
				if (p.getGameMode() == GameMode.ADVENTURE || p.getGameMode() == GameMode.SURVIVAL) {
					boolean toggle = p.isHealthScaled();
					p.setHealthScaled(!toggle);
					p.setHealthScale(p.getHealthScale());
					p.setHealth(p.getHealth());
					p.setHealthScaled(toggle);
				}
			}

			// Some wizardry here to prevent unintended speedhacking
			p.setWalkSpeed(p.getWalkSpeed());

			// Teleport the player far away to make sure they refresh chunks, then back to their new position.
			// Solves the issue of 1.14-1.15 not loading chunks fully.
			{
				RPPersonas plugin = RPPersonas.get();
				Location origin = p.getLocation();
				Location offset;

				if (origin.getWorld() != plugin.getSpawnLocation().getWorld() ||
					origin.distance(plugin.getSpawnLocation()) >= 100) {
					offset = plugin.getSpawnLocation();
				} else if (origin.getWorld() != plugin.getDeathLocation().getWorld() ||
						   origin.distance(plugin.getDeathLocation()) >= 100) {
					offset = plugin.getDeathLocation();
				} else {
					// Make sure we're teleporting relatively close to the y axis to avoid any worldborder
					// shenanigans, but still 100 blocks away in the xz direction.
					int[] xz = { origin.getBlockX(), origin.getBlockZ() };
					for (int i = 0; i < xz.length; i++) {
						if (xz[i] >= 100 || xz[i] <= -100) {
							xz[i] = 0;
						} else {
							xz[i] += 100;
						}
					}
					offset = origin.clone().add(new Vector(xz[0], 260, xz[1]));
				}
				p.teleport(offset);
				p.teleport(origin);
			}

			// Redraw inventory as assumed empty on respawn
			new BukkitRunnable() {
				@Override
				public void run() {
					p.updateInventory();
				}
			}.runTaskLater(RPPersonas.get(), 10);
		} catch (InvocationTargetException e) {
			e.printStackTrace();
		}
	}

	@SuppressWarnings("deprecation")
	private static PacketContainer buildRespawn(int dimension, Object resourceKey, Object nmsWorld, long seed, Player p) throws IllegalAccessException {
		ProtocolManager manager = ProtocolLibrary.getProtocolManager();
		PacketContainer output = manager.createPacket(PacketType.Play.Server.RESPAWN);

		output.getDimensions().write(0, dimension); //a
		output.getSpecificModifier(RESOURCE_KEY_CLASS).write(1, resourceKey); //b
		output.getLongs().write(0, Hashing.sha256().hashLong(seed).asLong()); //c
		output.getGameModes().write(0, EnumWrappers.NativeGameMode.fromBukkit(p.getGameMode())); //d
		output.getGameModes().write(1, EnumWrappers.NativeGameMode.fromBukkit(p.getGameMode())); //e
		output.getBooleans().write(0, DEBUG_WORLD_FIELD.getBoolean(nmsWorld)); //f
		output.getBooleans().write(1, p.getWorld().getWorldType() == WorldType.FLAT); //g
		output.getBooleans().write(2, true); //h

		return output;
	}

}