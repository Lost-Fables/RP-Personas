package net.korvic.rppersonas.personas.aspects;

import com.comphenix.protocol.PacketType;
import com.comphenix.protocol.ProtocolLibrary;
import com.comphenix.protocol.ProtocolManager;
import com.comphenix.protocol.events.ListenerPriority;
import com.comphenix.protocol.events.PacketAdapter;
import com.comphenix.protocol.events.PacketContainer;
import com.comphenix.protocol.events.PacketEvent;
import com.comphenix.protocol.wrappers.EnumWrappers;
import com.comphenix.protocol.wrappers.PlayerInfoData;
import com.comphenix.protocol.wrappers.WrappedGameProfile;
import com.comphenix.protocol.wrappers.WrappedSignedProperty;
import com.google.common.collect.Multimap;
import net.korvic.rppersonas.RPPersonas;
import net.korvic.rppersonas.personas.Persona;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

import java.util.List;
import java.util.ListIterator;
import java.util.UUID;

public class PersonaSkinListener {

	public static void listen() {
		ProtocolManager manager = ProtocolLibrary.getProtocolManager();
		manager.addPacketListener(
				new PacketAdapter(RPPersonas.get(), ListenerPriority.LOWEST, PacketType.Play.Server.PLAYER_INFO) {
					@Override
					public void onPacketSending(PacketEvent event) {
						if (RPPersonas.DEBUGGING) {
							RPPersonas.get().getLogger().info("Skin Packet Being Sent...");
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
										int skinID = pers.getActiveSkinID();
										if (skinID > 0) {
											if (RPPersonas.DEBUGGING) {
												RPPersonas.get().getLogger().info("New Skin Found...");
											}
											changed = true;
											WrappedGameProfile newProf = new WrappedGameProfile(uuid, player.getName());
											Multimap<String, WrappedSignedProperty> properties = newProf.getProperties();

											properties.removeAll("textures");
											properties.put("textures", RPPersonas.get().getPersonaHandler().getLoadedPersona(player).getActiveSkin().getMojangData());

											playerInfo = new PlayerInfoData(newProf, playerInfo.getLatency(), playerInfo.getGameMode(), playerInfo.getDisplayName());
											iterator.set(playerInfo);
										} else if (RPPersonas.DEBUGGING) {
											RPPersonas.get().getLogger().info("Failed to find a stored skin.");
										}
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
