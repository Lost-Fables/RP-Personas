package net.korvic.rppersonas.personas.aspects;

import com.comphenix.protocol.PacketType;
import com.comphenix.protocol.ProtocolLibrary;
import com.comphenix.protocol.ProtocolManager;
import com.comphenix.protocol.events.ListenerPriority;
import com.comphenix.protocol.events.PacketAdapter;
import com.comphenix.protocol.events.PacketContainer;
import com.comphenix.protocol.events.PacketEvent;
import com.comphenix.protocol.wrappers.*;
import com.google.common.collect.Multimap;
import net.korvic.rppersonas.RPPersonas;
import net.korvic.rppersonas.personas.Persona;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

import java.util.List;
import java.util.ListIterator;
import java.util.UUID;

public class PlayerDisplayListener {

	public static void listen() {
		ProtocolManager manager = ProtocolLibrary.getProtocolManager();
		manager.addPacketListener(
				new PacketAdapter(RPPersonas.get(), ListenerPriority.LOWEST, PacketType.Play.Server.PLAYER_INFO) {
					@Override
					public void onPacketSending(PacketEvent event) {
						if (RPPersonas.DEBUGGING) {
							RPPersonas.get().getLogger().info("Player Packet Being Sent...");
						}
						PacketContainer container = event.getPacket();
						EnumWrappers.PlayerInfoAction InfoAction = container.getPlayerInfoAction().read(0);

						if (InfoAction == EnumWrappers.PlayerInfoAction.ADD_PLAYER) {
							boolean changed = false;
							List<PlayerInfoData> allPlayerInfo = container.getPlayerInfoDataLists().read(0);

							for (ListIterator<PlayerInfoData> iterator = allPlayerInfo.listIterator(); iterator.hasNext();) {
								PlayerInfoData playerInfo = iterator.next();
								UUID uuid = playerInfo.getProfile().getUUID();
								Player player = Bukkit.getPlayer(uuid);
								if (player != null) {
									Persona pers = RPPersonas.get().getPersonaHandler().getLoadedPersona(player);
									if (pers != null) {
										WrappedGameProfile profile = new WrappedGameProfile(uuid, pers.getNickName());

										if (pers.getActiveSkinID() > 0) {
											if (RPPersonas.DEBUGGING) {
												RPPersonas.get().getLogger().info("New Skin Found...");
											}

											Multimap<String, WrappedSignedProperty> properties = profile.getProperties();
											properties.removeAll("textures");
											properties.put("textures", pers.getActiveSkin().getMojangData());
										} else {
											WrappedGameProfile oldProfile = WrappedGameProfile.fromPlayer(player);
											Multimap<String, WrappedSignedProperty> oldProperties = oldProfile.getProperties();

											Multimap<String, WrappedSignedProperty> properties = profile.getProperties();
											properties.removeAll("textures");
											properties.putAll("textures", oldProperties.get("textures"));
										}

										playerInfo = new PlayerInfoData(profile, playerInfo.getLatency(), playerInfo.getGameMode(), playerInfo.getDisplayName());
										iterator.set(playerInfo);
										changed = true;
									}
								}
							}

							if (changed) {
								container.getPlayerInfoDataLists().write(0, allPlayerInfo);
								if (RPPersonas.DEBUGGING) {
									RPPersonas.get().getLogger().info("Skin Packet Sent.");
								}
							}
						}
					}
				});
	}
}
