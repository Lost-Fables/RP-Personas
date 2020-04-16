package net.korvic.rppersonas.death;

import co.lotc.core.bukkit.util.ItemUtil;
import org.bukkit.Material;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

public class Corpse {

	private int id;
	private String name;
	private Inventory inv;
	private long created;
	private ItemStack itemstack;

	public Corpse(int id, String name, String texture, Inventory inv, long created) {
		this.id = id;
		this.name = name;
		this.inv = inv;
		this.created = created;

		ItemStack item = new ItemStack(Material.PLAYER_HEAD);
		if (texture != null) {
			ItemUtil.getSkullFromTexture(texture);
		}

		ItemMeta meta = item.getItemMeta();
		meta.setDisplayName(name);
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
	public ItemStack getItem() {
		return itemstack;
	}

}
