package net.korvic.rppersonas.death;

import co.lotc.core.bukkit.util.InventoryUtil;
import co.lotc.core.bukkit.util.ItemUtil;
import co.lotc.core.bukkit.util.PlayerUtil;
import net.korvic.rppersonas.RPPersonas;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryType;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class CorpseHandler {

	public static final String CORPSE_KEY = "rppersonas_corpse";
	private static int maxID = 1;
	private static CorpseHolder holder = new CorpseHolder();

	private final RPPersonas plugin;
	private Map<Integer, Corpse> knownCorpses = new HashMap<>();

	// INSTANCE //
	public CorpseHandler(RPPersonas plugin) {
		this.plugin = plugin;
	}

	public Corpse createCorpse(Player player) {
		Corpse output = null;
		try {
			ItemStack[] items = filterInventoryForCorpse(player);
			output = createCorpse(RPPersonas.PRIMARY_DARK + plugin.getPersonaHandler().getLoadedPersona(player).getNickName() + "'s Corpse", PlayerUtil.getPlayerTexture(player.getUniqueId()), items);
		} catch (Exception e) {
			if (RPPersonas.DEBUGGING) {
				e.printStackTrace();
			}
		}
		return output;
	}

	public Corpse createCorpse(String name, String texture, String inventory) {
		return createCorpse(name, texture, InventoryUtil.deserializeItemsToArray(inventory));
	}

	private Corpse createCorpse(String name, String texture, ItemStack[] inventory) {
		int id = maxID;
		updateMaxID(id);
		return loadCorpse(id, name, texture, inventory, System.currentTimeMillis());
	}

	public Corpse loadCorpse(int id, String name, String texture, String inventory, long created) {
		return loadCorpse(id, name, texture, InventoryUtil.deserializeItemsToArray(inventory), created);
	}

	public Corpse loadCorpse(int id, String name, String texture, ItemStack[] inventory, long created) {
		Inventory inv = Bukkit.createInventory(holder, InventoryType.PLAYER);
		if (inventory != null) {
			inv.setContents(inventory);
		}

		Corpse corpse = new Corpse(id, name, texture, inv, created);
		knownCorpses.put(id, corpse);
		updateMaxID(id);

		return corpse;
	}

	public Corpse getCorpse(String utilKey) {
		Corpse output = null;

		String[] keys = utilKey.split(":");
		if (keys.length == 2) {
			int id = Integer.parseInt(keys[0]);
			long created = Long.parseLong(keys[1]);

			output = knownCorpses.get(id);
			if (output.getCreated() != created) {
				output = null;
			}
		}
		return output;
	}

	private ItemStack[] filterInventoryForCorpse(Player player) {
		Inventory inv = player.getInventory();
		List<ItemStack> itemsToDrop = new ArrayList<>();
		List<ItemStack> itemsToKeep = new ArrayList<>();

		ItemStack[] itemsAsArray = inv.getContents();
		inv.clear();

		for (ItemStack item : itemsAsArray) {
			if (ItemUtil.hasCustomTag(item, CORPSE_KEY)) {
				itemsToKeep.add(item);
			} else {
				itemsToDrop.add(item);
			}
		}

		InventoryUtil.addItem(inv, itemsToKeep);
		return ItemUtil.itemListToArray(itemsToDrop);
	}

	// STATIC //
	public static void updateMaxID(int id) {
		if (id >= maxID) {
			maxID = id + 1;
		}
	}
}
