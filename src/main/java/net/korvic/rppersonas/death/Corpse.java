package net.korvic.rppersonas.death;

import co.lotc.core.bukkit.util.InventoryUtil;
import co.lotc.core.bukkit.util.ItemUtil;
import co.lotc.core.util.DataMapFilter;
import lombok.Getter;
import net.korvic.rppersonas.RPPersonas;
import net.korvic.rppersonas.sql.CorpseSQL;
import org.bukkit.Material;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.ArrayList;
import java.util.List;

public class Corpse {

	@Getter	private int id;
	@Getter private String name;
	@Getter private Inventory inventory;
	@Getter private long created;
	@Getter private int personaID;
	@Getter private ItemStack item;
	@Getter private String texture;

	public Corpse(int id, String name, String texture, Inventory inventory, long created, int personaID) {
		this.id = id;
		this.name = name;
		this.inventory = inventory;
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
		lore.add(RPPersonas.SECONDARY_DARK + "Take this to a resurrection altar");
		lore.add(RPPersonas.SECONDARY_DARK + "to bring them back to life.");
		lore.add("");
		lore.add(RPPersonas.SECONDARY_DARK + "Crouch + Right Click to open.");

		ItemMeta meta = item.getItemMeta();
		meta.setDisplayName(name);
		meta.setLore(lore);
		item.setItemMeta(meta);

		ItemUtil.setCustomTag(item, CorpseHandler.CORPSE_KEY, id + ":" + created);

		this.item = item;
	}

	public void save() {
		DataMapFilter data = new DataMapFilter();
		data.put(CorpseSQL.CORPSEID, id)
			.put(CorpseSQL.NAME, name)
			.put(CorpseSQL.INVENTORY, InventoryUtil.serializeItems(inventory))
			.put(CorpseSQL.CREATED, created)
			.put(CorpseSQL.PERSONAID, personaID)
			.put(CorpseSQL.TEXTURE, texture);
		RPPersonas.get().getCorpseSQL().registerOrUpdate(data);
	}

}
