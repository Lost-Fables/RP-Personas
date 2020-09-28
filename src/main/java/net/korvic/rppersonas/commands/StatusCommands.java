package net.korvic.rppersonas.commands;

import co.lotc.core.bukkit.menu.Menu;
import co.lotc.core.bukkit.menu.MenuAction;
import co.lotc.core.bukkit.menu.MenuAgent;
import co.lotc.core.bukkit.menu.MenuUtil;
import co.lotc.core.bukkit.menu.icon.Button;
import co.lotc.core.bukkit.menu.icon.Icon;
import co.lotc.core.command.annotate.Arg;
import co.lotc.core.command.annotate.Cmd;
import co.lotc.core.command.annotate.Range;
import co.lotc.core.util.MessageUtil;
import net.korvic.rppersonas.RPPersonas;
import net.korvic.rppersonas.personas.OldPersona;
import net.korvic.rppersonas.statuses.Status;
import net.korvic.rppersonas.statuses.StatusEntry;
import net.md_5.bungee.api.chat.ClickEvent;
import net.md_5.bungee.api.chat.TextComponent;
import org.apache.commons.lang.time.DurationFormatUtils;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.ArrayList;
import java.util.List;

public class StatusCommands extends BaseCommand {

	private RPPersonas plugin;

	public StatusCommands(RPPersonas plugin) {
		this.plugin = plugin;
	}

	@Cmd(value="Apply a set status to yourself.", permission=RPPersonas.PERMISSION_START + ".status.apply")
	public void apply(CommandSender sender,
					  @Arg(value="Status", description="The status you wish to apply.") Status status,
					  @Arg(value="Severity", description="The strength of the effect.") @Range(min=1, max=255) int severity,
					  @Arg(value="Duration", description="The length in seconds that the effect will last.") int duration) {
		if (sender instanceof Player) {
			OldPersona pers = plugin.getPersonaHandler().getLoadedPersona((Player) sender);
			if (pers != null) {
				pers.addStatus(status, (byte) severity, 1000 * duration);
			} else {
				msg(RPPersonas.PRIMARY_DARK + "Please make sure your forum account is linked before modifying your persona!");
			}
		} else {
			msg("Stahp it, console.");
		}
	}

	@Cmd(value="Apply a set status to another player.", permission=RPPersonas.PERMISSION_START + ".status.apply.other")
	public void applyother(CommandSender sender,
						   Player player,
						   @Arg(value="Status", description="The status you wish to apply.") Status status,
						   @Arg(value="Severity", description="The strength of the effect.") @Range(min=1, max=255) int severity,
						   @Arg(value="Duration", description="The length in seconds that the effect will last.") int duration) {
		OldPersona pers = plugin.getPersonaHandler().getLoadedPersona(player);
		if (pers != null) {
			pers.addStatus(status, (byte) severity, 1000 * duration);
		} else {
			msg(RPPersonas.PRIMARY_DARK + "Please make sure the player has a linked forum account before modifying their persona!");
		}
	}

	@Cmd(value="Clear a status effect from yourself.", permission=RPPersonas.PERMISSION_START + ".status.clear")
	public void clear(CommandSender sender, Status status) {
		if (sender instanceof Player) {
			OldPersona pers = plugin.getPersonaHandler().getLoadedPersona((Player) sender);
			if (pers != null) {
				pers.clearStatus(status);
			} else {
				msg(RPPersonas.PRIMARY_DARK + "Please make sure your forum account is linked before modifying your persona!");
			}
		} else {
			msg("Stahp it, console.");
		}
	}

	@Cmd(value="Clear a status effect from another player.", permission=RPPersonas.PERMISSION_START + ".status.clear.other")
	public void clearother(CommandSender sender, Player player, Status status) {
		OldPersona pers = plugin.getPersonaHandler().getLoadedPersona(player);
		if (pers != null) {
			pers.clearStatus(status);
		} else {
			msg(RPPersonas.PRIMARY_DARK + "Please make sure the player's forum account is linked before modifying their persona!");
		}
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
		OldPersona pers = plugin.getPersonaHandler().getLoadedPersona(p);
		buildMainMenu(null, pers).openSession(p);

	}

	// MAIN MENU //
	public static Menu buildMainMenu(Menu menu, OldPersona pers) {
		List<Icon> icons = new ArrayList<>();

		// Available Button
		icons.add(new Button() {
			@Override
			public ItemStack getItemStack(MenuAgent menuAgent) {
				ItemStack item = new ItemStack(Material.ENDER_EYE);
				ItemMeta meta = item.getItemMeta();
				meta.setDisplayName(RPPersonas.PRIMARY_DARK + "" + ChatColor.BOLD + "Add Status");

				List<String> lore = new ArrayList<>();
				lore.add(RPPersonas.SECONDARY_DARK + "Click here to browse all statuses.");

				meta.setLore(lore);
				item.setItemMeta(meta);

				return item;
			}

			@Override
			public void click(MenuAction menuAction) {
				buildAvailableStatusMenu(menuAction.getMenuAgent().getMenu(), pers).openSession(menuAction.getPlayer());
			}
		});

		// Active Button
		icons.add(new Button() {
			@Override
			public ItemStack getItemStack(MenuAgent menuAgent) {
				ItemStack item = new ItemStack(Material.TOTEM_OF_UNDYING);
				ItemMeta meta = item.getItemMeta();
				meta.setDisplayName(RPPersonas.PRIMARY_DARK + "" + ChatColor.BOLD + "Current Statuses");

				List<String> lore = new ArrayList<>();
				lore.add(RPPersonas.SECONDARY_DARK + "Click here to browse active statuses.");

				meta.setLore(lore);
				item.setItemMeta(meta);

				return item;
			}

			@Override
			public void click(MenuAction menuAction) {
				buildActiveStatusMenu(menuAction.getMenuAgent().getMenu(), pers).openSession(menuAction.getPlayer());
			}
		});

		// Build Menu
		if (menu != null) {
			return Menu.fromIcons(menu, ChatColor.BOLD + "Status Effects", icons);
		} else {
			return Menu.fromIcons(ChatColor.BOLD + "Status Effects", icons);
		}
	}

	// AVAILABLE STATUSES //
	private static Menu buildAvailableStatusMenu(Menu menu, OldPersona pers) {
		List<Icon> icons = new ArrayList<>();
		for (Status status : Status.getStatuses()) {
			icons.add(buildAvailableStatusIcon(status, pers));
		}
		return MenuUtil.createMultiPageMenu(menu, ChatColor.BOLD + "Available Statuses", icons).get(0);
	}

	private static Icon buildAvailableStatusIcon(Status status, OldPersona pers) {
		return new Button() {
			@Override
			public ItemStack getItemStack(MenuAgent menuAgent) {
				ItemStack item = new ItemStack(status.getMaterial());
				ItemMeta meta = item.getItemMeta();
				meta.setDisplayName(status.getColor() + "" + status.getIcon() + " " + RPPersonas.SECONDARY_DARK + status.getName());

				List<String> lore = new ArrayList<>();

				lore.add(RPPersonas.SECONDARY_DARK + "Click to apply this to your persona.");

				lore.add("");

				// Add description broken down into 40 width pieces.
				double charsPerLine = 40d;
				int pages = (int) Math.ceil(status.getDescription().length()/charsPerLine);
				for (int i = 1; i <= pages; i++) {
					int j = (int) ((i - 1) * charsPerLine);
					int k = (int) (i * charsPerLine);

					if (status.getDescription().length() < k) {
						k = status.getDescription().length();
					}
					lore.add(RPPersonas.SECONDARY_DARK + status.getDescription().substring(j, k));
				}

				meta.setLore(lore);
				item.setItemMeta(meta);
				return item;
			}

			@Override
			public void click(MenuAction menuAction) {
				TextComponent message = new TextComponent();
				message.setClickEvent(new ClickEvent(ClickEvent.Action.SUGGEST_COMMAND, "/persona status apply " + status.getName()));
				message.setText(RPPersonas.PRIMARY_DARK + "Click here to auto fill the status command, or use " + RPPersonas.SECONDARY_DARK + "/persona status apply" + RPPersonas.PRIMARY_DARK + " to get started.");
				message.setHoverEvent(MessageUtil.hoverEvent("Click here!"));

				menuAction.getPlayer().closeInventory();
				menuAction.getPlayer().sendMessage(message);
			}
		};
	}

	// ACTIVE STATUSES //
	private static Menu buildActiveStatusMenu(Menu menu, OldPersona pers) {
		List<Icon> icons = new ArrayList<>();
		for (StatusEntry entry : pers.getActiveStatuses()) {
			icons.add(buildActiveStatusIcon(menu, pers, entry));
		}
		return MenuUtil.createMultiPageMenu(menu, ChatColor.BOLD + "Active Statuses", icons).get(0);
	}

	private static Icon buildActiveStatusIcon(Menu menu, OldPersona pers, StatusEntry entry) {
		Status status = entry.getStatus();
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
					if (entry.isEnabled()) {
						active += ChatColor.GREEN + "" + ChatColor.BOLD + "Active " + RPPersonas.SECONDARY_DARK;
					} else {
						active += ChatColor.RED + "" + ChatColor.BOLD + "Inactive " + RPPersonas.SECONDARY_DARK;
					}
					lore.add(active + "Click to toggle this status effect on or off.");
				}

				if (menuAgent.getPlayer().hasPermission(RPPersonas.PERMISSION_START + ".status.clear")) {
					lore.add(RPPersonas.SECONDARY_DARK + "" + ChatColor.BOLD + "Right Click to clear this status effect completely.");
				}

				lore.add("");

				long expiryTime = entry.getExpiration() - System.currentTimeMillis();
				lore.add(RPPersonas.SECONDARY_DARK + "Expires in: " + RPPersonas.SECONDARY_LIGHT + ChatColor.ITALIC + DurationFormatUtils.formatDuration(expiryTime, "HH:mm:ss"));

				lore.add("");

				// Add description broken down into 40 width pieces.
				double charsPerLine = 40d;
				int pages = (int) Math.ceil(status.getDescription().length()/charsPerLine);
				for (int i = 1; i <= pages; i++) {
					int j = (int) ((i - 1) * charsPerLine);
					int k = (int) (i * charsPerLine);

					if (status.getDescription().length() < k) {
						k = status.getDescription().length();
					}
					lore.add(RPPersonas.SECONDARY_DARK + status.getDescription().substring(j, k));
				}

				meta.setLore(lore);
				item.setItemMeta(meta);
				return item;
			}

			@Override
			public void click(MenuAction menuAction) {
				ClickType click = menuAction.getClick();

				if ((click.equals(ClickType.RIGHT) || click.equals(ClickType.SHIFT_RIGHT)) &&
					menuAction.getPlayer().hasPermission(RPPersonas.PERMISSION_START + ".status.clear")) {
					pers.clearStatusEntry(entry);
				} else {
					if (status.isToggleable()) {
						entry.setEnabled(!entry.isEnabled());
					}

					if (entry.isEnabled()) {
						status.applyEffect(menuAction.getPlayer(), entry.getSeverity());
					} else {
						pers.disableStatusEntry(entry);
					}
				}

				buildActiveStatusMenu(menu, pers).openSession(menuAction.getPlayer());
			}
		};
	}

}
