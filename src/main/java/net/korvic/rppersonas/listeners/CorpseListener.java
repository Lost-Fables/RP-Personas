package net.korvic.rppersonas.listeners;

import co.lotc.core.bukkit.util.InventoryUtil;
import co.lotc.core.bukkit.util.ItemUtil;
import net.korvic.rppersonas.RPPersonas;
import net.korvic.rppersonas.death.Altar;
import net.korvic.rppersonas.death.Corpse;
import net.korvic.rppersonas.death.CorpseHandler;
import net.korvic.rppersonas.death.CorpseHolder;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryDragEvent;
import org.bukkit.event.inventory.InventoryInteractEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;

import java.util.HashMap;
import java.util.Map;

public class CorpseListener implements Listener {

	private RPPersonas plugin;

	public CorpseListener(RPPersonas plugin) {
		this.plugin = plugin;
	}

	@EventHandler
	public void onCorpseOpen(PlayerInteractEvent e) {
		if (ItemUtil.hasCustomTag(e.getItem(), CorpseHandler.CORPSE_KEY) &&
			(e.getAction().equals(Action.RIGHT_CLICK_AIR) || e.getAction().equals(Action.RIGHT_CLICK_BLOCK)) &&
			e.getPlayer().isSneaking()) {
			e.setCancelled(true);

			// Get Corpse item and null out the one in the player's inventory
			ItemStack corpseItem = e.getItem();
			Corpse corpse = plugin.getCorpseHandler().getCorpse(ItemUtil.getCustomTag(corpseItem, CorpseHandler.CORPSE_KEY));
			e.getPlayer().openInventory(corpse.getInventory());
		}
	}

	@EventHandler(ignoreCancelled = true)
	public void onCorpseTransfer(InventoryClickEvent e) {
		if (e.getInventory().getHolder() instanceof CorpseHolder) {
			if (!e.getWhoClicked().hasPermission(RPPersonas.PERMISSION_START + ".corpsebypass") &&
				e.getClickedInventory() != null && !(e.getClickedInventory().getHolder() instanceof CorpseHolder)) {
				e.setCancelled(true);
			} else {
				for (ItemStack item : InventoryUtil.getTouchedByEvent(e)) {
					if (ItemUtil.hasCustomTag(item, CorpseHandler.CORPSE_KEY)) {
						e.setCancelled(true);
						break;
					}
				}
			}
		}
	}

	@EventHandler
	public void onCorpsePlace(BlockPlaceEvent e) {
		if (ItemUtil.hasCustomTag(e.getItemInHand(), CorpseHandler.CORPSE_KEY)) {
			e.setCancelled(true);

			// Get Corpse item and null out the one in the player's inventory
			Player p = e.getPlayer();
			ItemStack corpseItem = e.getItemInHand();
			p.getInventory().setItem(getIndexFromInventory(p.getInventory(), e.getItemInHand()), null);
			Altar altar = plugin.getAltarHandler().getAltarOfBlock(e.getBlockAgainst());

			if (altar != null) {
				resConfirm(p, corpseItem, altar);
			} else {
				placeConfirm(p, corpseItem);
			}
		}
	}

	public static int getIndexFromInventory(Inventory inv, ItemStack item) {
		int index = -1;
		ItemStack[] invList = inv.getContents();
		for (int i = 0; i < invList.length; i++) {
			if (invList[i] != null && invList[i].equals(item)) {
				index = i;
				break;
			}
		}
		return index;
	}

	private void resConfirm(Player p, ItemStack corpse, Altar altar) {
		Map<Object, Object> data = new HashMap<>();
		data.put("player", p);
		data.put("corpse", corpse);
		data.put("altar", altar);

		// TODO Convo factory to confirm and start resurrection.
	}

	private void placeConfirm(Player p, ItemStack corpse) {
		Map<Object, Object> data = new HashMap<>();
		data.put("player", p);
		data.put("corpse", corpse);

		// TODO Convo factory to confirm and return placeable head item.
	}

}