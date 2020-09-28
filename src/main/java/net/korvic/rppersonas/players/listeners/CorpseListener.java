package net.korvic.rppersonas.players.listeners;

import co.lotc.core.bukkit.util.InventoryUtil;
import co.lotc.core.bukkit.util.ItemUtil;
import net.korvic.rppersonas.RPPersonas;
import net.korvic.rppersonas.players.conversation.BaseConvo;
import net.korvic.rppersonas.players.conversation.RezConfirmConvo;
import net.korvic.rppersonas.players.death.Altar;
import net.korvic.rppersonas.players.death.Corpse;
import net.korvic.rppersonas.players.death.CorpseHandler;
import net.korvic.rppersonas.players.death.CorpseHolder;
import net.korvic.rppersonas.sql.PersonasSQL;
import net.korvic.rppersonas.players.statuses.DisabledStatus;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.inventory.InventoryClickEvent;
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
			e.getPlayer().getLocation().getWorld().playSound(e.getPlayer().getLocation(), Sound.BLOCK_CHORUS_FLOWER_DEATH, 1.0f, 1.0f);

			// Get Corpse item and null out the one in the player's inventory
			ItemStack corpseItem = e.getItem();
			Corpse corpse = plugin.getCorpseHandler().getCorpse(ItemUtil.getCustomTag(corpseItem, CorpseHandler.CORPSE_KEY));
			if (corpse != null) {
				if (!InventoryUtil.isEmpty(corpse.getInventory())) {
					e.getPlayer().openInventory(corpse.getInventory());
				} else {
					e.getPlayer().sendMessage(RPPersonas.PRIMARY_DARK + "The corpse has nothing on it...");
				}
			} else {
				e.getPlayer().sendMessage(RPPersonas.PRIMARY_DARK + "The corpse has rotted away...");
			}
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

			// If placed against an altar, take the corpse item during the rez prompt.
			// Otherwise inform player to ruin the corpse if they wish to place it.
			Player p = e.getPlayer();
			Altar altar = plugin.getAltarHandler().getAltarOfBlock(e.getBlockAgainst());

			if (altar != null) {
				new DisabledStatus(null).applyEffect(p, (byte) 0); // TODO make this not TP you to spawn on jump.
				ItemStack corpseItem = e.getItemInHand();
				p.getInventory().setItem(getIndexFromInventory(p.getInventory(), e.getItemInHand()), null);

				Corpse corpse = plugin.getCorpseHandler().getCorpse(ItemUtil.getCustomTag(corpseItem, CorpseHandler.CORPSE_KEY));
				Map<String, Object> personaData = plugin.getPersonasSQL().getFullInfo(corpse.getPersonaID());
				if (personaData.containsKey(PersonasSQL.ALIVE) && !((boolean) personaData.get(PersonasSQL.ALIVE))) {
					if (personaData.containsKey(PersonasSQL.LIVES) && ((int) personaData.get(PersonasSQL.LIVES) > 0)) {
						resConfirm(p, corpse, altar, personaData);
					} else {
						p.sendMessage(RPPersonas.PRIMARY_DARK + "That persona no longer has any lives left.\n" +
									  RPPersonas.PRIMARY_DARK + "They will need to make a resurrection application to be revived.");
						new DisabledStatus(null).clearEffect(p);
						InventoryUtil.addOrDropItem(p, corpseItem);
					}
				} else {
					p.sendMessage(RPPersonas.PRIMARY_DARK + "That persona is currently alive.");
					new DisabledStatus(null).clearEffect(p);
					InventoryUtil.addOrDropItem(p, corpseItem);
				}
			} else {
				e.getPlayer().sendMessage(RPPersonas.PRIMARY_DARK + "You may only place a ruined corpse.\n" +
										  RPPersonas.PRIMARY_DARK + "Use " + RPPersonas.SECONDARY_DARK + "/persona RuinCorpse " + RPPersonas.PRIMARY_DARK + "to enable placing.\n" +
										  BaseConvo.NOTE + RPPersonas.PRIMARY_DARK + "A ruined corpse can no longer be resurrected.");
			}
		}
	}

	@EventHandler (ignoreCancelled=true)
	public void onAltarBreak(BlockBreakEvent e) {
		Altar altar = plugin.getAltarHandler().getAltarOfBlock(e.getBlock());
		if (altar != null) {
			e.setCancelled(true);
			e.getPlayer().sendMessage(RPPersonas.PRIMARY_DARK + "Please remove the altar " + RPPersonas.SECONDARY_DARK + altar.getLabel() + RPPersonas.PRIMARY_DARK + " before breaking this block.");
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

	private void resConfirm(Player p, Corpse corpse, Altar altar, Map<String, Object> data) {
		Map<Object, Object> newData = new HashMap<>();
		newData.put("corpse", corpse);
		newData.put("altar", altar);
		newData.putAll(data);

		new RezConfirmConvo(plugin).startConvo(p, newData, false);
	}

}
