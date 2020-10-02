package net.korvic.rppersonas.players.personas;

import co.lotc.core.bukkit.util.InventoryUtil;
import lombok.Getter;
import lombok.Setter;
import net.korvic.rppersonas.RPPersonas;
import net.korvic.rppersonas.BoardManager;
import net.korvic.rppersonas.players.conversation.BaseConvo;
import net.korvic.rppersonas.sql.PersonaAccountsMapSQL;
import net.korvic.rppersonas.sql.PersonasSQL;
import net.korvic.rppersonas.sql.StatusSQL;
import net.korvic.rppersonas.sql.util.DataMapFilter;
import net.korvic.rppersonas.players.statuses.Status;
import net.korvic.rppersonas.players.statuses.StatusEntry;
import net.korvic.rppersonas.time.TimeManager;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryType;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class OldPersona {

	private RPPersonas plugin;

	@Getter private Player usingPlayer;
	@Getter private int personaID;
	@Getter private int accountID;
	@Getter private String prefix;
	@Getter private String nickName;
	@Getter private String[] namePieces = new String[2];
	@Getter private Inventory enderChest;
	@Getter @Setter private boolean alive;
	@Getter private PersonaSkin activeSkin = null;
	@Getter private List<StatusEntry> activeStatuses = new ArrayList<>();
	@Getter @Setter private boolean staffNameEnabled = false;

	private String inventory;

	public OldPersona(RPPersonas plugin, Player usingPlayer, int personaID, int accountID, String prefix, String nickName, String personaInvData, String personaEnderData, boolean alive, int activeSkinID) {
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

		this.alive = alive;
		this.activeSkin = PersonaSkin.getFromID(activeSkinID);
	}

	// GET //


	public Map<String, Object> getLoadedInfo() {
		Map<String, Object> output = new HashMap<>();

		output.put(PersonaAccountsMapSQL.ACCOUNTID, accountID);
		output.put(PersonasSQL.PERSONAID, personaID);
		output.put(PersonasSQL.ALIVE, alive);
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
		Map<String, Short> languages = getLanguages();

		StringBuilder output = new StringBuilder(BaseConvo.DIVIDER +
												 RPPersonas.PRIMARY_DARK + "Persona ID: " + RPPersonas.SECONDARY_LIGHT + String.format("%06d", (int) data.get(PersonasSQL.PERSONAID)) + "\n");
		if (data.containsKey(PersonasSQL.NICKNAME)) {
			output.append(RPPersonas.PRIMARY_DARK).append("Nickname: ").append(RPPersonas.SECONDARY_LIGHT).append(data.get(PersonasSQL.NICKNAME)).append("\n");
		}
		output.append(RPPersonas.PRIMARY_DARK).append("Name: ").append(RPPersonas.SECONDARY_LIGHT).append(data.get(PersonasSQL.NAME)).append("\n")
			  .append(RPPersonas.PRIMARY_DARK).append("Age: ").append(RPPersonas.SECONDARY_LIGHT).append(TimeManager.getRelativeTimeString((long) data.get(PersonasSQL.AGE))).append("\n")
			  .append(RPPersonas.PRIMARY_DARK).append("Race: ").append(RPPersonas.SECONDARY_LIGHT).append(data.get(PersonasSQL.RACE)).append("\n")
			  .append(RPPersonas.PRIMARY_DARK).append("Gender: ").append(RPPersonas.SECONDARY_LIGHT).append(data.get(PersonasSQL.GENDER)).append("\n");

		if (languages != null && languages.size() > 0) {
			StringBuilder languageLine = new StringBuilder(RPPersonas.PRIMARY_DARK + "Languages: ");
			boolean filled = false;
			for (String key : languages.keySet()) {
				if (filled) {
					languageLine.append(RPPersonas.TERTIARY).append(ChatColor.BOLD).append(" | ");
				} else {
					filled = true;
				}

				PersonaLanguage lang = PersonaLanguage.getByName(key);
				if (lang != null) {
					languageLine.append(RPPersonas.SECONDARY_LIGHT);
					if (lang.getTag().length() > 0) {
						languageLine.append(lang.getTag()).append(" ");
					}
					languageLine.append(key.replace("_", " ")).append(" ").append(RPPersonas.SECONDARY_DARK).append(languages.get(key));
				}
			}
			output.append(languageLine).append("\n");
		}

		if (data.containsKey(PersonasSQL.DESCRIPTION)) {
			output.append(RPPersonas.PRIMARY_DARK).append("Description: ").append(RPPersonas.SECONDARY_LIGHT).append(data.get(PersonasSQL.DESCRIPTION)).append("\n");
		}
		output.append(BaseConvo.DIVIDER);

		return output.toString();
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
	public void setNickName(String name) {
		setNickName(usingPlayer, name);
	}

	public void setNickName(Player p, String name) {
		if (name.length() > 0) {
			this.nickName = name;
		} else {
			this.nickName = (String) getBasicInfo().get(PersonasSQL.NAME);
		}

		namePieces = new String[2];
		String prefix = "";
		if (staffNameEnabled) {
			prefix = RPPersonas.getPrefixColor(p);
		}

		String personaName = prefix + this.nickName;
		int maxMidSize = 16;
		int maxSuffixSize = 64;

		namePieces[0] = personaName.substring(0, Math.min(maxMidSize, personaName.length()));
		if (personaName.length() > maxMidSize) {
			String suffix = prefix + personaName.substring(maxMidSize, personaName.length());
			namePieces[1] = suffix.substring(0, Math.min(maxSuffixSize, suffix.length()));
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
			PersonaSkin.refreshPlayerSync(usingPlayer);
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

	public boolean isStatusEnabled(Status status) {
		return isStatusEnabled(status.getName());
	}
	public boolean isStatusEnabled(String name) {
		for (StatusEntry entry : activeStatuses) {
			if (entry.getStatus().getName().equalsIgnoreCase(name)) {
				return entry.isEnabled();
			}
		}
		return false;
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

	public void refreshStatuses() {
		for (StatusEntry entry : activeStatuses) {
			if (entry.isEnabled()) {
				entry.getStatus().refreshEffect(usingPlayer, entry.getSeverity());
			}
		}
	}

	public void disableStatus(Status status) {
		disableStatus(status.getName());
	}
	public void disableStatus(String name) {
		for (StatusEntry entry : activeStatuses) {
			if (entry.getStatus().getName().equalsIgnoreCase(name)) {
				disableStatusEntry(entry);
			}
		}
	}
	public void disableStatusEntry(StatusEntry entry) {
		entry.setEnabled(false);
		entry.getStatus().clearEffect(usingPlayer);
		refreshStatuses();
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
			clearStatusEntry(entryForRemoval);
		}
	}
	public void clearStatusEntry(StatusEntry entry) {
		activeStatuses.remove(entry);
		entry.getStatus().clearEffect(usingPlayer);
		plugin.getStatusSQL().deleteStatus(personaID, entry);
		refreshStatuses();
	}

	public void clearAllStatuses() {
		for (StatusEntry entry : activeStatuses) {
			entry.getStatus().clearEffect(usingPlayer);
		}
		activeStatuses.clear();
	}
}
