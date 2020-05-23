package net.korvic.rppersonas.personas;

import co.lotc.core.bukkit.util.InventoryUtil;
import lombok.Getter;
import net.korvic.rppersonas.RPPersonas;
import net.korvic.rppersonas.BoardManager;
import net.korvic.rppersonas.conversation.BaseConvo;
import net.korvic.rppersonas.sql.PersonaAccountsMapSQL;
import net.korvic.rppersonas.sql.PersonasSQL;
import net.korvic.rppersonas.sql.StatusSQL;
import net.korvic.rppersonas.sql.extras.DataMapFilter;
import net.korvic.rppersonas.statuses.Status;
import net.korvic.rppersonas.statuses.StatusEntry;
import net.korvic.rppersonas.time.TimeManager;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryType;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class Persona {

	private RPPersonas plugin;

	@Getter private Player usingPlayer;
	@Getter private int personaID;
	@Getter private int accountID;
	@Getter private String prefix;
	@Getter private String nickName;
	@Getter private String[] namePieces = new String[3];
	@Getter private Inventory enderChest;
	@Getter private boolean isAlive;
	@Getter private PersonaSkin activeSkin = null;
	@Getter private List<StatusEntry> activeStatuses = new ArrayList<>();

	private String inventory;

	public Persona(RPPersonas plugin, Player usingPlayer, int personaID, int accountID, String prefix, String nickName, String personaInvData, String personaEnderData, boolean isAlive, int activeSkinID) {
		this.plugin = plugin;

		this.usingPlayer = usingPlayer;

		this.personaID = personaID;
		this.accountID = accountID;
		this.prefix = prefix;
		setNickName(usingPlayer, nickName);
		this.inventory = personaInvData;

		this.enderChest = Bukkit.createInventory(new PersonaEnderHolder(), InventoryType.ENDER_CHEST, nickName + "'s Stash");
		ItemStack[] items = deserializeToArray(personaEnderData);
		if (items != null) {
			this.enderChest.setContents(items);
		}

		this.isAlive = isAlive;
		this.activeSkin = PersonaSkin.getFromID(activeSkinID);
	}

	// GET //
	public String getChatName() {
		if (prefix != null) {
			return "[" + prefix + "] " + nickName;
		} else {
			return nickName;
		}
	}
	public int getActiveSkinID() {
		if (activeSkin != null) {
			return activeSkin.getSkinID();
		} else {
			return 0;
		}
	}

	public Map<String, Object> getLoadedInfo() {
		Map<String, Object> output = new HashMap<>();

		output.put(PersonaAccountsMapSQL.ACCOUNTID, accountID);
		output.put(PersonasSQL.PERSONAID, personaID);
		output.put(PersonasSQL.ALIVE, isAlive);
		output.put(PersonasSQL.INVENTORY, inventory);
		if (enderChest != null) {
			output.put(PersonasSQL.ENDERCHEST, InventoryUtil.serializeItems(enderChest));
		}
		output.put(PersonasSQL.NICKNAME, nickName);
		output.put(PersonasSQL.PREFIX, prefix);

		if (activeSkin != null) {
			output.put(PersonasSQL.SKINID, activeSkin.getSkinID());
		} else {
			output.put(PersonasSQL.SKINID, 0);
		}

		return output;
	}

	public Map<String, Object> getBasicInfo() {
		Map<String, Object> output = plugin.getPersonasSQL().getBasicPersonaInfo(personaID);

		output.put(PersonasSQL.PERSONAID, personaID);

		return output;
	}

	public String getFormattedBasicInfo() {
		Map<String, Object> data = getBasicInfo();

		String output = BaseConvo.DIVIDER +
						RPPersonas.PRIMARY_DARK + "Persona ID: " + RPPersonas.SECONDARY_LIGHT + String.format("%06d", (int) data.get(PersonasSQL.PERSONAID)) + "\n";
		if (data.containsKey(PersonasSQL.NICKNAME)) {
			output += RPPersonas.PRIMARY_DARK + "Nickname: " + RPPersonas.SECONDARY_LIGHT + data.get(PersonasSQL.NICKNAME) + "\n";
		}
		output += RPPersonas.PRIMARY_DARK + "Name: " + RPPersonas.SECONDARY_LIGHT + data.get(PersonasSQL.NAME) + "\n" +
				  RPPersonas.PRIMARY_DARK + "Age: " + RPPersonas.SECONDARY_LIGHT + TimeManager.getRelativeTimeString((long) data.get(PersonasSQL.AGE)) + "\n" +
				  RPPersonas.PRIMARY_DARK + "Race: " + RPPersonas.SECONDARY_LIGHT + data.get(PersonasSQL.RACE) + "\n" +
				  RPPersonas.PRIMARY_DARK + "Gender: " + RPPersonas.SECONDARY_LIGHT + data.get(PersonasSQL.GENDER) + "\n";
		if (data.containsKey(PersonasSQL.DESCRIPTION)) {
			output += RPPersonas.PRIMARY_DARK + "Description: " + RPPersonas.SECONDARY_LIGHT + data.get(PersonasSQL.DESCRIPTION) + "\n";
		}
		output += BaseConvo.DIVIDER;

		return output;
	}

	public ItemStack[] getInventory() {
		return deserializeToArray(inventory);
	}

	private ItemStack[] deserializeToArray(String inventory) {
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
	public void queueSave() {
		queueSave(usingPlayer, null);
	}

	public void queueSave(Player p) {
		queueSave(p, null);
	}

	public void queueSave(DataMapFilter data) {
		queueSave(usingPlayer, data);
	}

	public void queueSave(Player p, DataMapFilter data) {
		this.inventory = InventoryUtil.serializeItems(p.getInventory());
		try {
			DataMapFilter newData = new DataMapFilter();
			newData.putAll(getLoadedInfo());
			if (data != null) {
				newData.putAllData(data);
			}
			newData.put(PersonasSQL.LOCATION, p.getLocation())
				   .put(PersonasSQL.HEALTH, p.getHealth())
				   .put(PersonasSQL.HUNGER, p.getFoodLevel());
			
			plugin.getPersonasSQL().registerOrUpdate(newData);
		} catch (Exception e) {
			if (RPPersonas.DEBUGGING) {
				e.printStackTrace();
			}
		}
	}

	public void unloadPersona(boolean keepLinked) {
		plugin.getPersonaHandler().unloadPersona(this, keepLinked);
	}

	// SET //
	public void setNickName(Player p, String name) {
		if (name.length() > 0) {
			this.nickName = name;
		} else {
			this.nickName = (String) getBasicInfo().get(PersonasSQL.NAME);
		}

		namePieces = new String[3];

		String personaName = this.nickName;
		int maxSize = 16;
		int pieces = (int) Math.ceil(((double) personaName.length()) / maxSize);

		if (pieces > 1) {
			for (int i = 0; i < pieces && i < namePieces.length; i++) {
				int end = Math.min(maxSize * (i + 1), personaName.length());
				namePieces[i] = personaName.substring(maxSize * i, end);
			}
		} else {
			namePieces[1] = personaName;
		}

		queueSave(p);

		BoardManager.addPlayer(usingPlayer, namePieces);
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
		if (data.containsKey(PersonasSQL.DESCRIPTION)) {
			desc.append((String) data.get(PersonasSQL.DESCRIPTION));
		}

		for (String s : description) {
			if (desc.length() > 0) {
				desc.append(" ");
			}
			desc.append(s);
		}

		DataMapFilter newData = new DataMapFilter();
		newData.put(PersonasSQL.DESCRIPTION, desc.toString());

		queueSave(p, newData);
		return desc.toString();
	}

	public void clearDescription(Player p) {
		DataMapFilter data = new DataMapFilter();
		data.put(PersonasSQL.DESCRIPTION, null);
		queueSave(p, data);
	}

	public void setSkin(int skinID) {
		this.activeSkin = PersonaSkin.getFromID(skinID);
		if (usingPlayer != null) {
			PersonaSkin.refreshPlayer(usingPlayer);
		}
	}

	// STATUS //
	public boolean hasStatus(Status status) {
		return hasStatus(status.getName());
	}

	public boolean hasStatus(String name) {
		for (StatusEntry entry : activeStatuses) {
			if (entry.getStatus().getName().equalsIgnoreCase(name)) {
				return true;
			}
		}
		return false;
	}

	public StatusEntry getStatusEntryFor(Status status) {
		return getStatusEntryFor(status.getName());
	}

	public StatusEntry getStatusEntryFor(String name) {
		for (StatusEntry entry : activeStatuses) {
			if (entry.getStatus().getName().equalsIgnoreCase(name)) {
				return entry;
			}
		}
		return null;
	}

	public void addStatus(Status status, byte severity, long duration) {
		long expiration = System.currentTimeMillis() + duration;

		activeStatuses.add(new StatusEntry(status, severity, expiration, true));
		status.applyEffect(usingPlayer, severity);

		DataMapFilter data = new DataMapFilter();
		data.put(StatusSQL.PERSONAID, personaID)
			.put(StatusSQL.STATUS, status)
			.put(StatusSQL.SEVERITY, severity)
			.put(StatusSQL.EXPIRATION, expiration);

		plugin.getStatusSQL().saveStatus(data);
	}

	public void disableStatus(Status status) {
		disableStatus(status.getName());
	}

	public void disableStatus(String name) {
		for (StatusEntry entry : activeStatuses) {
			if (entry.getStatus().getName().equalsIgnoreCase(name)) {
				entry.setEnabled(false);
				entry.getStatus().clearEffect(usingPlayer);
				refreshStatuses();
				break;
			}
		}
	}

	public void refreshStatuses() {
		for (StatusEntry entry : activeStatuses) {
			if (entry.isEnabled()) {
				entry.getStatus().refreshEffect(usingPlayer, entry.getSeverity());
			}
		}
	}

	public void clearStatus(Status status) {
		clearStatus(status.getName());
	}

	public void clearStatus(String name) {
		StatusEntry entryForRemoval = null;
		for (StatusEntry entry : activeStatuses) {
			if (entry.getStatus().getName().equalsIgnoreCase(name)) {
				entryForRemoval = entry;
				break;
			}
		}

		if (entryForRemoval != null) {
			activeStatuses.remove(entryForRemoval);
			entryForRemoval.getStatus().clearEffect(usingPlayer);
			refreshStatuses();
		}
	}

	public void clearAllStatuses() {
		for (StatusEntry entry : activeStatuses) {
			entry.getStatus().clearEffect(usingPlayer);
		}
		activeStatuses.clear();
	}

	public void setAlive(boolean isAlive) {
		this.isAlive = isAlive;
	}
}
