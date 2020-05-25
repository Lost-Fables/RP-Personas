package net.korvic.rppersonas.kits;

import co.lotc.core.bukkit.menu.Menu;
import co.lotc.core.bukkit.menu.MenuAction;
import co.lotc.core.bukkit.menu.MenuAgent;
import co.lotc.core.bukkit.menu.MenuUtil;
import co.lotc.core.bukkit.menu.icon.Button;
import co.lotc.core.bukkit.menu.icon.Icon;
import lombok.Getter;
import net.korvic.rppersonas.RPPersonas;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.entity.Item;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.ArrayList;
import java.util.List;

public class KitHandler {

	private RPPersonas plugin;
	@Getter private List<Kit> allKits = new ArrayList<>();
	public static final int KIT_SIZE = 2*9;

	public KitHandler(RPPersonas plugin) {
		this.plugin = plugin;
	}

	public void addKit(Kit kit) {
		if (getKit(kit.getName()) == null) {
			allKits.add(kit);
		}
	}

	public void editKit(Player p, Kit kit) {
		if (kit != null) {
			Inventory inv = Bukkit.createInventory(new KitEditHolder(kit), KIT_SIZE);
			for (ItemStack item : kit.getItems()) {
				if (item != null) {
					inv.addItem(item);
				}
			}
			p.openInventory(inv);
		}
	}

	public void deleteKit(Kit kit) {
		allKits.remove(kit);
	}

	public Kit getKit(String name) {
		for (Kit kit : allKits) {
			if (kit.getName().equalsIgnoreCase(name)) {
				return kit;
			}
		}
		return null;
	}

	public List<String> getKitNameList() {
		List<String> names = new ArrayList<>();
		for (Kit kit : allKits) {
			names.add(kit.getName());
		}
		return names;
	}

	// KIT BROWSER MENU

	public class KitBrowser {

		private Menu homeMenu;

		public Menu getBrowserMenu() {
			List<Icon> icons = new ArrayList<>();

			for (Kit kit : allKits) {
				icons.add(buildKitIcon(kit));
			}

			homeMenu = Menu.fromIcons(ChatColor.BOLD + "Choose Your Background", icons);
			return homeMenu;
		}

		public Icon buildKitIcon(Kit kit) {
			return new Button() {
				@Override
				public ItemStack getItemStack(MenuAgent menuAgent) {
					ItemStack item = new ItemStack(kit.getIcon());
					ItemMeta meta = item.getItemMeta();
					meta.setDisplayName(RPPersonas.PRIMARY_DARK + "" + ChatColor.BOLD + kit.getName());

					List<String> lore = new ArrayList<>();
					lore.add(RPPersonas.SECONDARY_DARK + "Click here to preview the items.");
					meta.setLore(lore);

					item.setItemMeta(meta);
					return item;
				}

				@Override
				public void click(MenuAction menuAction) {
					getKitPreview(kit).openSession(menuAction.getPlayer());
				}
			};
		}

		public Menu getKitPreview(Kit kit) {
			List<Icon> icons = new ArrayList<>();

			for (ItemStack item : kit.getItems()) {
				icons.add(new Button() {
					@Override
					public ItemStack getItemStack(MenuAgent menuAgent) {
						return item;
					}

					@Override
					public void click(MenuAction menuAction) {}
				});
			}

			return MenuUtil.createMultiPageMenu(homeMenu, ChatColor.BOLD + kit.getName(), icons).get(0);
		}

	}

}
