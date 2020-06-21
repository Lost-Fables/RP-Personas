package net.korvic.rppersonas.listeners;

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

import java.util.ArrayList;
import java.util.List;
import java.util.ListIterator;
import java.util.UUID;

public class SkinDisplayListener {

	public static List<Player> showingMCNames = new ArrayList<>();

	public static void listen() {
		ProtocolManager manager = ProtocolLibrary.getProtocolManager();
		manager.addPacketListener(
				new PacketAdapter(RPPersonas.get(), ListenerPriority.LOWEST, PacketType.Play.Server.PLAYER_INFO) {
					@Override
					public void onPacketSending(PacketEvent event) {
						PacketContainer container = event.getPacket();
						EnumWrappers.PlayerInfoAction infoAction = container.getPlayerInfoAction().read(0);

						if (infoAction == EnumWrappers.PlayerInfoAction.ADD_PLAYER) {
							boolean changed = false;
							List<PlayerInfoData> allPlayerInfo = container.getPlayerInfoDataLists().read(0);

							for (ListIterator<PlayerInfoData> iterator = allPlayerInfo.listIterator(); iterator.hasNext();) {
								PlayerInfoData playerInfo = iterator.next();
								UUID uuid = playerInfo.getProfile().getUUID();
								Player player = Bukkit.getPlayer(uuid);
								if (player != null) {
									Persona pers = RPPersonas.get().getPersonaHandler().getLoadedPersona(player);
									if (pers != null) {
										WrappedGameProfile profile = playerInfo.getProfile();
										String name = pers.getNamePieces()[0];
										if (showingMCNames.contains(event.getPlayer())) {
											name = player.getName();
										}
										profile = profile.withName(name);

										if (pers.getActiveSkinID() > 0) {
											Multimap<String, WrappedSignedProperty> properties = profile.getProperties();
											properties.removeAll("textures");
											properties.put("textures", pers.getActiveSkin().getMojangData());
											changed = true;
										}

										playerInfo = new PlayerInfoData(profile, playerInfo.getLatency(), playerInfo.getGameMode(), WrappedChatComponent.fromText(player.getName()));
										iterator.set(playerInfo);

										if (!player.equals(event.getPlayer())) {
											changed = true;
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
