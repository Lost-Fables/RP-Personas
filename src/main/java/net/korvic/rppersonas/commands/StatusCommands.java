package net.korvic.rppersonas.commands;

import co.lotc.core.bukkit.menu.Menu;
import co.lotc.core.bukkit.menu.MenuAction;
import co.lotc.core.bukkit.menu.MenuAgent;
import co.lotc.core.bukkit.menu.icon.Button;
import co.lotc.core.bukkit.menu.icon.Icon;
import co.lotc.core.command.annotate.Cmd;
import net.korvic.rppersonas.RPPersonas;
import net.korvic.rppersonas.personas.Persona;
import net.korvic.rppersonas.statuses.Status;
import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.ArrayList;
import java.util.List;

public class StatusCommands extends BaseCommand {

	private RPPersonas plugin;

	public StatusCommands(RPPersonas plugin) {
		this.plugin = plugin;
	}

	@Cmd(value="Open the status management menu.")
	public void menu(CommandSender sender) {
		if (sender instanceof Player) {
			Player p = (Player) sender;
			if (plugin.getPersonaHandler().getLoadedPersona(p) != null) {
				openStatusMenu(p);
			} else {
				msg(RPPersonas.PRIMARY_DARK + "You need to register a persona first! Be sure to link your account to get started.");
			}
		} else {
			msg(PersonaCommands.NO_CONSOLE);
		}
	}

	private void openStatusMenu(Player p) {
		buildActiveStatusMenu(plugin.getPersonaHandler().getLoadedPersona(p)).openSession(p);
	}

	private Menu buildActiveStatusMenu(Persona pers) {
		List<Icon> icons = new ArrayList<>();
		for (Status status : pers.getActiveStatuses()) {
			icons.add(buildActiveStatusIcon(status));
		}
		return Menu.fromIcons("Active Statuses", icons);
	}

	private Icon buildActiveStatusIcon(Status status) {
		return new Button() {
			@Override
			public ItemStack getItemStack(MenuAgent menuAgent) {
				ItemStack item = new ItemStack(status.getMaterial());
				ItemMeta meta = item.getItemMeta();
				meta.setDisplayName(status.getColor() + "" + status.getIcon() + " " + RPPersonas.SECONDARY_DARK + status.getName());

				List<String> lore = new ArrayList<>();

				// Toggleable text
				if (status.isToggleable()) {
					String active = "";
					if (status.isActive()) {
						active += ChatColor.GREEN + "" + ChatColor.BOLD + "Active " + RPPersonas.SECONDARY_DARK;
					} else {
						active += ChatColor.RED + "" + ChatColor.BOLD + "Inactive " + RPPersonas.SECONDARY_DARK;
					}
					lore.add(active + "Click to toggle this status on or off.");
				}

				// Add description broken down into 35 width pieces.
				double charsPerLine = 35d;
				int pages = (int) Math.ceil(status.getDescription().length()/charsPerLine);
				for (int i = 1; i <= pages; i++) {
					int j = (int) ((i - 1) * charsPerLine);
					int k = (int) (i * charsPerLine);

					if (status.getDescription().length() < k) {
						k = status.getDescription().length();
					}
					lore.add(status.getDescription().substring(j, k));
				}

				meta.setLore(lore);
				item.setItemMeta(meta);
				return item;
			}

			@Override
			public void click(MenuAction menuAction) {
				if (status.isToggleable()) {
					status.setActive(!status.isActive());
				}

				if (status.isActive()) {
					status.applyEffect(menuAction.getPlayer());
				} else {
					status.clearEffect(menuAction.getPlayer());
				}
			}
		};
	}

}
