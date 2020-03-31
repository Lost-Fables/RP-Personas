package net.korvic.rppersonas.personas;

import co.lotc.core.bukkit.util.InventoryUtil;
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
import org.bukkit.inventory.ItemStack;

import java.lang.reflect.InvocationTargetException;
import java.sql.PreparedStatement;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class Persona {

	private RPPersonas plugin;

	private Player usingPlayer;

	private int personaID;
	private int accountID;
	private String prefix;
	private String nickName;
	private String inventory;
	private boolean isAlive;
	private PersonaSkin activeSkin = null;

	public Persona(RPPersonas plugin, Player usingPlayer, int personaID, int accountID, String prefix, String nickName, String personaInvData, boolean isAlive, int activeSkinID) {
		this.plugin = plugin;

		this.usingPlayer = usingPlayer;

		this.personaID = personaID;
		this.accountID = accountID;
		this.prefix = prefix;
		this.nickName = nickName;
		this.inventory = personaInvData;
		this.isAlive = isAlive;
		this.activeSkin = PersonaSkin.getFromID(activeSkinID);
	}

	// GET //
	public Player getUsingPlayer() {
		return usingPlayer;
	}
	public int getPersonaID() {
		return personaID;
	}
	public int getAccountID() {
		return accountID;
	}
	public String getPrefix() {
		return prefix;
	}
	public String getNickName() {
		return nickName;
	}
	public boolean isAlive() {
		return isAlive;
	}
	public PersonaSkin getActiveSkin() {
		return activeSkin;
	}
	public int getActiveSkinID() {
		if (activeSkin != null) {
			return activeSkin.getSkinID();
		} else {
			return 0;
		}
	}

	public Map<Object, Object> getLoadedInfo() {
		Map<Object, Object> output = new HashMap<>();

		output.put("accountid", accountID);
		output.put("personaid", personaID);
		output.put("alive", isAlive);
		output.put("inventory", inventory);
		output.put("nickname", nickName);
		output.put("prefix", prefix);

		if (activeSkin != null) {
			output.put("skinid", activeSkin.getSkinID());
		} else {
			output.put("skinid", 0);
		}

		return output;
	}

	public Map<String, Object> getBasicInfo() {
		Map<String, Object> output = plugin.getPersonasSQL().getBasicPersonaInfo(personaID);

		output.put("personaid", personaID);

		return output;
	}

	public String getFormattedBasicInfo() {
		Map<String, Object> data = getBasicInfo();

		String output = PersonaCreationDialog.DIVIDER +
						RPPersonas.PRIMARY_DARK + "Persona ID: " + RPPersonas.SECONDARY_LIGHT + String.format("%06d", (int) data.get("personaid")) + "\n";
		if (data.containsKey("nickname")) {
			output += RPPersonas.PRIMARY_DARK + "Nickname: " + RPPersonas.SECONDARY_LIGHT + data.get("nickname") + "\n";
		}
		output += RPPersonas.PRIMARY_DARK + "Name: " + RPPersonas.SECONDARY_LIGHT + data.get("name") + "\n" +
				  RPPersonas.PRIMARY_DARK + "Age: " + RPPersonas.SECONDARY_LIGHT + RPPersonas.getRelativeTimeString((long) data.get("age")) + "\n" +
				  RPPersonas.PRIMARY_DARK + "Race: " + RPPersonas.SECONDARY_LIGHT + data.get("race") + "\n" +
				  RPPersonas.PRIMARY_DARK + "Gender: " + RPPersonas.SECONDARY_LIGHT + data.get("gender") + "\n";
		if (data.containsKey("description")) {
			output += RPPersonas.PRIMARY_DARK + "Description: " + RPPersonas.SECONDARY_LIGHT + data.get("description") + "\n";
		}
		output += PersonaCreationDialog.DIVIDER;

		return output;
	}

	public ItemStack[] getInventory() {
		if (inventory != null) {
			List<ItemStack> items = InventoryUtil.deserializeItems(inventory);
			ItemStack[] arrayItems = new ItemStack[items.size()];
			for (int i = 0; i < arrayItems.length; i++) {
				arrayItems[i] = items.get(i);
			}
			return arrayItems;
		} else {
			return null;
		}
	}

	// SAVE //
	public void queueSave(Player p) {
		queueSave(p, null);
	}

	public void queueSave(Player p, Map<Object, Object> data) {
		this.inventory = InventoryUtil.serializeItems(p.getInventory());
		try {
			Map<Object, Object> newData = getLoadedInfo();
			if (data != null) {
				newData.putAll(data);
			}
			newData.put("location", p.getLocation());
			newData.put("health", p.getHealth());
			newData.put("hunger", p.getFoodLevel());
			PreparedStatement ps = plugin.getPersonasSQL().getSaveStatement(newData);
			plugin.getSaveQueue().addToQueue(ps);
		} catch (Exception e) {
			if (RPPersonas.DEBUGGING) {
				e.printStackTrace();
			}
		}
	}

	public void unloadPersona() {
		plugin.getPersonaHandler().unloadPersona(this);
	}

	// SET //
	public void setNickName(Player p, String name) {
		if (name.length() > 0) {
			this.nickName = name;
		} else {
			this.nickName = (String) getBasicInfo().get("name");
		}
		queueSave(p);
	}

	public void setPrefix(Player p, String prefix) {
		if (prefix.length() > 0) {
			this.prefix = prefix;
		} else {
			this.prefix = null;
		}
		queueSave(p);
	}

	public String addToDescription(Player p, String[] description) {
		Map<String, Object> data = getBasicInfo();
		StringBuilder desc = new StringBuilder();
		if (data.containsKey("description")) {
			desc.append((String) data.get("description"));
		}

		for (String s : description) {
			if (desc.length() > 0) {
				desc.append(" ");
			}
			desc.append(s);
		}

		Map<Object, Object> newData = new HashMap<>();
		newData.put("description", desc.toString());

		queueSave(p, newData);
		return desc.toString();
	}

	public void clearDescription(Player p) {
		Map<Object, Object> data = new HashMap<>();
		data.put("description", null);
		queueSave(p, data);
	}

	public void setSkin(int skinID) {
		this.activeSkin = PersonaSkin.getFromID(skinID);
		if (usingPlayer != null) {
			PersonaSkin.refreshPlayer(usingPlayer);
		}
	}
}
