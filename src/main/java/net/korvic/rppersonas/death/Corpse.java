package net.korvic.rppersonas.death;

import co.lotc.core.bukkit.util.InventoryUtil;
import co.lotc.core.bukkit.util.ItemUtil;
import net.korvic.rppersonas.RPPersonas;
import net.korvic.rppersonas.sql.CorpseSQL;
import net.korvic.rppersonas.sql.extras.DataMapFilter;
import org.bukkit.Material;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.ArrayList;
import java.util.List;

public class Corpse {

	private int id;
	private String name;
	private Inventory inv;
	private long created;
	private int personaID;
	private ItemStack itemstack;
	private String texture;

	public Corpse(int id, String name, String texture, Inventory inv, long created, int personaID) {
		this.id = id;
		this.name = name;
		this.inv = inv;
		this.created = created;
		this.personaID = personaID;
		this.texture = texture;

		ItemStack item;
		if (texture != null) {
			item = ItemUtil.getSkullFromTexture(texture);
		} else {
			item = new ItemStack(Material.PLAYER_HEAD);
		}

		List<String> lore = new ArrayList<>();
		lore.add("Take this to a resurrection altar");
		lore.add("to bring them back to life.");
		lore.add("");
		lore.add("Crouch + Right Click to open.");

		ItemMeta meta = item.getItemMeta();
		meta.setDisplayName(name);
		meta.setLore(lore);
		item.setItemMeta(meta);

		ItemUtil.setCustomTag(item, CorpseHandler.CORPSE_KEY, id + ":" + created);

		this.itemstack = item;
	}

	public int getID() {
		return id;
	}
	public String getName() {
		return name;
	}
	public Inventory getInventory() {
		return inv;
	}
	public long getCreated() {
		return created;
	}
	public int getPersonaID() {
		return personaID;
	}
	public String getTexture() {
		return texture;
	}
	public ItemStack getItem() {
		return itemstack;
	}

	public void save() {
		DataMapFilter data = new DataMapFilter();
		data.put(CorpseSQL.CORPSEID, id)
			.put(CorpseSQL.NAME, name)
			.put(CorpseSQL.INVENTORY, InventoryUtil.serializeItems(inv))
			.put(CorpseSQL.CREATED, created)
			.put(CorpseSQL.PERSONAID, personaID)
			.put(CorpseSQL.TEXTURE, texture);
		RPPersonas.get().getCorpseSQL().registerOrUpdate(data);
	}

}
