package net.korvic.rppersonas.personas;

import co.lotc.core.bukkit.util.InventoryUtil;
import net.korvic.rppersonas.RPPersonas;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import java.sql.PreparedStatement;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class Persona {

	private RPPersonas plugin;
	private int personaID;
	private int accountID;
	private String prefix;
	private String nickName;
	private String inventory;
	private boolean isAlive;
	private int activeSkinID;

	public Persona(RPPersonas plugin, int personaID, int accountID, String prefix, String nickName, String personaInvData, boolean isAlive, int activeSkinID) {
		this.plugin = plugin;
		this.personaID = personaID;
		this.accountID = accountID;
		this.prefix = prefix;
		this.nickName = nickName;
		this.inventory = personaInvData;
		this.isAlive = isAlive;
		this.activeSkinID = activeSkinID;
	}

	public Map<Object, Object> getLoadedInfo() {
		Map<Object, Object> output = new HashMap<>();

		output.put("accountid", accountID);
		output.put("personaid", personaID);
		output.put("alive", isAlive);
		output.put("inventory", inventory);
		output.put("nickname", nickName);
		output.put("prefix", prefix);
		output.put("skinid", activeSkinID);

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
						RPPersonas.PRIMARY_COLOR + "Persona ID: " + RPPersonas.SECONDARY_COLOR + data.get("personaid") + "\n";
		if (data.containsKey("nickname")) {
			output += RPPersonas.PRIMARY_COLOR + "Nickname: " + RPPersonas.SECONDARY_COLOR + data.get("nickname") + "\n";
		}
		output += RPPersonas.PRIMARY_COLOR + "Name: " + RPPersonas.SECONDARY_COLOR + data.get("name") + "\n" +
				  RPPersonas.PRIMARY_COLOR + "Age: " + RPPersonas.SECONDARY_COLOR + data.get("age") + "\n" +
				  RPPersonas.PRIMARY_COLOR + "Race: " + RPPersonas.SECONDARY_COLOR + data.get("race") + "\n" +
				  RPPersonas.PRIMARY_COLOR + "Gender: " + RPPersonas.SECONDARY_COLOR + data.get("gender") + "\n";
		if (data.containsKey("description")) {
			output += RPPersonas.PRIMARY_COLOR + "Description: " + RPPersonas.SECONDARY_COLOR + data.get("description") + "\n";
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

	public void updateSkin(int skinID) {
		this.activeSkinID = skinID;
		//TODO - Update player's model to have new skin.
	}

	public void queueSave(Player p) {
		this.inventory = InventoryUtil.serializeItems(p.getInventory());
		try {
			Map<Object, Object> data = getLoadedInfo();
			data.put("location", p.getLocation());
			PreparedStatement ps = plugin.getPersonasSQL().getSaveStatement(data);
			plugin.getSaveQueue().addToQueue(ps);
		} catch (Exception e) {
			if (RPPersonas.DEBUGGING) {
				e.printStackTrace();
			}
		}
	}
}
