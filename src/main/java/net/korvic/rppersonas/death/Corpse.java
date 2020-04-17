package net.korvic.rppersonas.death;

import co.lotc.core.bukkit.util.ItemUtil;
import net.korvic.rppersonas.RPPersonas;
import org.bukkit.Material;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class Corpse {

	private int id;
	private String name;
	private Inventory inv;
	private long created;
	private ItemStack itemstack;
	private String texture;

	public Corpse(int id, String name, String texture, Inventory inv, long created) {
		this.id = id;
		this.name = name;
		this.inv = inv;
		this.created = created;
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

		ItemUtil.decorate(item, name, String.valueOf(lore));
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
	public String getTexture() {
		return texture;
	}
	public ItemStack getItem() {
		return itemstack;
	}

	public void save() {
		Map<Object, Object> data = new HashMap<>();
		data.put("corpseid", id);
		data.put("name", name);
		data.put("inventory", inv);
		data.put("created", created);
		data.put("texture", texture);
		RPPersonas.get().getCorpseSQL().registerOrUpdate(data);
	}

}
