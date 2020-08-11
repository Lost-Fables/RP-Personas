package net.korvic.rppersonas.personas;

import com.comphenix.protocol.PacketType;
import com.comphenix.protocol.ProtocolLibrary;
import com.comphenix.protocol.ProtocolManager;
import com.comphenix.protocol.events.PacketContainer;
import com.comphenix.protocol.utility.MinecraftReflection;
import com.comphenix.protocol.wrappers.*;
import com.google.common.collect.Lists;
import com.google.common.hash.Hashing;
import net.korvic.rppersonas.RPPersonas;
import org.bukkit.*;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;
import org.bukkit.scheduler.BukkitRunnable;

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
		ProtocolManager manager = ProtocolLibrary.getProtocolManager();

		PacketContainer packet = manager.createPacket(PacketType.Play.Server.RESPAWN);
		Object nmsWorld = BukkitConverters.getWorldConverter().getGeneric(p.getWorld());
		Object resourceKey = WORLD_KEY_FIELD.get(nmsWorld);
		long seed = p.getWorld().getSeed();

		packet.getDimensions().write(0, p.getWorld().getEnvironment().getId()); //a
		packet.getSpecificModifier(RESOURCE_KEY_CLASS).write(1, resourceKey); //b
		packet.getLongs().write(0, Hashing.sha256().hashLong(seed).asLong()); //c
		packet.getGameModes().write(0, EnumWrappers.NativeGameMode.fromBukkit(p.getGameMode())); //d
		packet.getGameModes().write(1, EnumWrappers.NativeGameMode.fromBukkit(p.getGameMode())); //e
		packet.getBooleans().write(0, DEBUG_WORLD_FIELD.getBoolean(nmsWorld)); //f
		packet.getBooleans().write(1, p.getWorld().getWorldType() == WorldType.FLAT); //g
		packet.getBooleans().write(2, true); //h


		Location location = p.getLocation();
		PacketContainer teleport = manager.createPacket(PacketType.Play.Server.POSITION);
		teleport.getModifier().writeDefaults();
		teleport.getDoubles().write(0, location.getX());
		teleport.getDoubles().write(1, location.getY());
		teleport.getDoubles().write(2, location.getZ());
		teleport.getFloat().write(0, location.getYaw());
		teleport.getFloat().write(1, location.getPitch());
		teleport.getIntegers().writeSafely(0, -99);

		try {
			manager.sendServerPacket(p, packet);
			manager.sendServerPacket(p, teleport);
			//Some wizardry here to make the right amount of hearts show up
			if(p.getGameMode() == GameMode.ADVENTURE || p.getGameMode() == GameMode.SURVIVAL) {
				boolean toggle = p.isHealthScaled();
				p.setHealthScaled(!toggle);
				p.setHealthScale(p.getHealthScale());
				p.setHealth(p.getHealth());
				p.setHealthScaled(toggle);
			}

			//Some wizardry here to prevent unintended speedhacking
			p.setWalkSpeed(p.getWalkSpeed());

			//Redraw inventory as assumed empty on respawn
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

}

/* 1.13- Version
package net.korvic.rppersonas.personas;

import com.comphenix.protocol.PacketType;
import com.comphenix.protocol.ProtocolLibrary;
import com.comphenix.protocol.ProtocolManager;
import com.comphenix.protocol.events.PacketContainer;
import com.comphenix.protocol.wrappers.*;
import com.google.common.collect.Lists;
import net.korvic.rppersonas.RPPersonas;
import org.bukkit.Bukkit;
import org.bukkit.GameMode;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;

import java.lang.reflect.InvocationTargetException;
import java.util.List;
import java.util.Map;

public class PersonaSkin {

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

	// ProtocolLib Refreshing
	public static void refreshPlayer(Player p) {
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
			fakeRespawn(p, p.getWorld().getEnvironment());
		} catch (InvocationTargetException e) {
			e.printStackTrace();
		}
	}

	@SuppressWarnings("deprecation")
	private static void fakeRespawn(Player p, World.Environment env) {
		ProtocolManager manager = ProtocolLibrary.getProtocolManager();

		PacketContainer packet = manager.createPacket(PacketType.Play.Server.RESPAWN);
		packet.getDimensions().write(0, env.getId()); //Does matter
		packet.getDifficulties().write(0, EnumWrappers.Difficulty.valueOf(p.getWorld().getDifficulty().name()));//Doesnt matter
		packet.getGameModes().write(0, EnumWrappers.NativeGameMode.fromBukkit(p.getGameMode()));
		packet.getWorldTypeModifier().write(0, p.getWorld().getWorldType()); //Doesnt matter tbh


		Location location = p.getLocation();
		PacketContainer teleport = manager.createPacket(PacketType.Play.Server.POSITION);
		teleport.getModifier().writeDefaults();
		teleport.getDoubles().write(0, location.getX());
		teleport.getDoubles().write(1, location.getY());
		teleport.getDoubles().write(2, location.getZ());
		teleport.getFloat().write(0, location.getYaw());
		teleport.getFloat().write(1, location.getPitch());
		teleport.getIntegers().writeSafely(0, -99);

		try {
			manager.sendServerPacket(p, packet);
			manager.sendServerPacket(p, teleport);
			//Some wizardry here to make the right amount of hearts show up
			if(p.getGameMode() == GameMode.ADVENTURE || p.getGameMode() == GameMode.SURVIVAL) {
				boolean toggle = p.isHealthScaled();
				p.setHealthScaled(!toggle);
				p.setHealthScale(p.getHealthScale());
				p.setHealth(p.getHealth());
				p.setHealthScaled(toggle);
			}

			//Some wizardry here to prevent unintended speedhacking
			p.setWalkSpeed(p.getWalkSpeed());

			//Redraw inventory as assumed empty on respawn
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

}
*/
