package net.korvic.rppersonas.listeners;

import net.korvic.rppersonas.RPPersonas;
import net.korvic.rppersonas.kits.Kit;
import net.korvic.rppersonas.kits.KitCreateHolder;
import net.korvic.rppersonas.kits.KitEditHolder;
import net.korvic.rppersonas.kits.KitHandler;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.inventory.ItemStack;

import java.util.Arrays;
import java.util.List;

public class KitListener implements Listener {

	RPPersonas plugin;

	public KitListener(RPPersonas plugin) {
		this.plugin = plugin;
	}

	@EventHandler
	public void onKitEditClose(InventoryCloseEvent e) {
		if (e.getInventory().getHolder() instanceof KitEditHolder) {
			KitEditHolder holder = (KitEditHolder) e.getInventory().getHolder();
			List<ItemStack> items = Arrays.asList(e.getInventory().getContents());
			holder.getKit().setItems(items);
		}
	}

	@EventHandler
	public void onKitCreateClose(InventoryCloseEvent e) {
		if (e.getInventory().getHolder() instanceof KitCreateHolder) {
			KitCreateHolder holder = (KitCreateHolder) e.getInventory().getHolder();
			Kit kit = new Kit(holder.getName(), Arrays.asList(e.getInventory().getContents()));
			plugin.getKitHandler().addKit(kit);
		}
	}

}
