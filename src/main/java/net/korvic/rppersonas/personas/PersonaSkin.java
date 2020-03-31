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

	// ProtocolLib Refreshing
	public static void refreshPlayer(Player p) {
		Bukkit.getOnlinePlayers().stream()
			  .filter(x -> (x != p))
			  .filter(x -> x.canSee(p))
			  .forEach(x -> {x.hidePlayer(RPPersonas.get(), p); x.showPlayer(RPPersonas.get(), p);});

		final ProtocolManager manager = ProtocolLibrary.getProtocolManager();

		WrappedGameProfile profile = WrappedGameProfile.fromPlayer(p); //Protocollib for version independence
		List<PlayerInfoData> lpid = Lists.newArrayList();

		lpid.add(new PlayerInfoData(profile,
									1, //who cares honestly
									EnumWrappers.NativeGameMode.fromBukkit(p.getGameMode()),
									WrappedChatComponent.fromText(p.getDisplayName())));

		final PacketContainer packetDel = manager.createPacket(PacketType.Play.Server.PLAYER_INFO);
		final PacketContainer packetAdd = manager.createPacket(PacketType.Play.Server.PLAYER_INFO);
		packetDel.getPlayerInfoAction().write(0, EnumWrappers.PlayerInfoAction.REMOVE_PLAYER);
		packetAdd.getPlayerInfoAction().write(0, EnumWrappers.PlayerInfoAction.ADD_PLAYER);
		packetDel.getPlayerInfoDataLists().write(0, lpid);
		packetAdd.getPlayerInfoDataLists().write(0, lpid);

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
			//Some wizardry here to make the right amount of hearts how up
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
