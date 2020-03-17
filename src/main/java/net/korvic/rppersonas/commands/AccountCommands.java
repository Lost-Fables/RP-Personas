package net.korvic.rppersonas.commands;

import co.lotc.core.bukkit.menu.Menu;
import co.lotc.core.bukkit.menu.MenuAction;
import co.lotc.core.bukkit.menu.MenuAgent;
import co.lotc.core.bukkit.menu.MenuUtil;
import co.lotc.core.bukkit.menu.icon.Button;
import co.lotc.core.bukkit.menu.icon.Icon;
import co.lotc.core.bukkit.menu.icon.Slot;
import co.lotc.core.bukkit.util.ItemUtil;
import co.lotc.core.command.annotate.Arg;
import co.lotc.core.command.annotate.Cmd;
import co.lotc.core.util.MessageUtil;
import co.lotc.core.util.TimeUtil;
import net.korvic.rppersonas.RPPersonas;
import net.korvic.rppersonas.sql.PersonasSQL;
import net.md_5.bungee.api.chat.ClickEvent;
import net.md_5.bungee.api.chat.HoverEvent;
import net.md_5.bungee.api.chat.TextComponent;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.inventory.meta.SkullMeta;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class AccountCommands extends BaseCommand {

	private RPPersonas plugin;
	private static final String PLAYER_ONLY = RPPersonas.PREFIX + "This command only works for players!";
	private static final String FORUM_LINK_SUCCESS = RPPersonas.PREFIX + "Successfully linked your forum account!";
	private static final String ALREADY_REGISTERED = RPPersonas.PREFIX + "Your account is already linked!";
	private static final String FORUM_LINK_REQUIRED = RPPersonas.PREFIX + "You need to link your account with " + RPPersonas.ALT_COLOR + "/account link FORUM_ID " + RPPersonas.PREFIX + "to use that.";

	public AccountCommands(RPPersonas plugin) {
		this.plugin = plugin;
	}

	@Cmd(value="Send a registration message to your forum account.", permission="rppersonas.accepted")
	public void link(CommandSender sender,
						 @Arg(value="Forum ID", description="Your forum account ID.") int id) {
		if (sender instanceof Player) {
			Player p = (Player) sender;
			if (plugin.getUUIDAccountMapSQL().getAccountID(p.getUniqueId()) <= 0) {
				//TODO - Send user a message on the forums for them to confirm their Forum ID that will instead run the lines below.
				plugin.getUUIDAccountMapSQL().addMapping(id, p);
				msg(FORUM_LINK_SUCCESS);
			} else {
				msg(ALREADY_REGISTERED);
			}
		} else {
			msg(PLAYER_ONLY);
		}
	}

	@Cmd(value="Start the register process for someone else.", permission="rppersonas.helper")
	public void other(@Arg(value="The Player", description="The player you're helping register.") Player p,
					  @Arg(value="Forum ID", description="The forum ID of the other player.") int id) {
		link((CommandSender) p, id);
	}

	@Cmd(value="Open a menu to manage your account.", permission="rppersonas.use")
	public void menu(CommandSender sender) {
		if (sender instanceof Player) {
			Player p = (Player) sender;
			int accountID = plugin.getUUIDAccountMapSQL().getAccountID(p.getUniqueId());
			if (accountID > 0) {
				new AccountMenu().buildMainMenu(accountID).openSession(p);
			} else {
				msg(FORUM_LINK_REQUIRED);
			}
		} else {
			msg(PLAYER_ONLY);
		}
	}

	private class AccountMenu {
		// MENUS //

		private static final String STAT_HEAD = "eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvNmM3ZDM1YzdmNWMyODQ5ZDFlMjM4OTZlYmFiMjQ0ZDM0ZWYwZGFmZWRkODkxOTc0OTQ2MWI3ZDE1Y2MxZjA0In19fQ==";
		private static final String DISCORD_HEAD = "eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvYTNiMTgzYjE0OGI5YjRlMmIxNTgzMzRhZmYzYjViYjZjMmMyZGJiYzRkNjdmNzZhN2JlODU2Njg3YTJiNjIzIn19fQ";
		private static final String SKINS_HEAD = "eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvNjI3NGUxNjA1MjMzNDI1MDkxZjdiMjgzN2E0YmI4ZjRjODA0ZGFjODBkYjllNGY1OTlmNTM1YzAzYWZhYjBmOCJ9fX0=";
		private static final String NO_SET_SKIN = "eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvNTM1OWQ5MTI3NzI0MmZjMDFjMzA5YWNjYjg3YjUzM2YxOTI5YmUxNzZlY2JhMmNkZTYzYmY2MzVlMDVlNjk5YiJ9fX0=";
		private static final String DEAD_PERSONA = "eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvMmYyNGVkNjg3NTMwNGZhNGExZjBjNzg1YjJjYjZhNmE3MjU2M2U5ZjNlMjRlYTU1ZTE4MTc4NDUyMTE5YWE2NiJ9fX0=";

		private static final String NO_PLAYTIME = "Nothing yet!";

		private Menu homeMenu = null;

		private Menu buildMainMenu(int accountID) {
			ArrayList<Icon> icons = new ArrayList<>();
			icons.add(getStatisticsIcon(accountID));
			icons.add(getDiscordIcon(accountID));
			icons.add(getSkinsIcon(accountID));
			icons.add(getPersonasIcon(accountID));

			homeMenu = Menu.fromIcons(ChatColor.BOLD + "Account Management", icons);
			return homeMenu;
		}

		private Icon getStatisticsIcon(int accountID) {
			return new Button() {
				@Override
				public ItemStack getItemStack(MenuAgent menuAgent) {
					ItemStack item = ItemUtil.getSkullFromTexture(STAT_HEAD);
					ItemMeta meta = item.getItemMeta();
					meta.setDisplayName(RPPersonas.PREFIX + ChatColor.BOLD + "Stats");

					ArrayList<String> lore = new ArrayList<>();
					lore.add(RPPersonas.ALT_COLOR + ChatColor.ITALIC + "Votes: " + ChatColor.RESET + RPPersonas.ALT_COLOR + plugin.getAccountsSQL().getVotes(accountID));

					long timeSpent = plugin.getAccountsSQL().getPlaytime(accountID);

					String playtime;
					if (timeSpent > 0) {
						playtime = TimeUtil.printBrief(timeSpent).toPlainText();
					} else {
						playtime = NO_PLAYTIME;
					}

					lore.add(RPPersonas.ALT_COLOR + ChatColor.ITALIC + "Playtime: " + ChatColor.RESET + RPPersonas.ALT_COLOR + playtime);

					meta.setLore(lore);
					item.setItemMeta(meta);
					return item;
				}

				@Override
				public void click(MenuAction menuAction) {
				}
			};
		}

		private Icon getDiscordIcon(int accountID) {
			return new Button() {
				@Override
				public ItemStack getItemStack(MenuAgent menuAgent) {
					ItemStack item = ItemUtil.getSkullFromTexture(DISCORD_HEAD);
					ItemMeta meta = item.getItemMeta();
					meta.setDisplayName(RPPersonas.PREFIX + ChatColor.BOLD + "Discord");

					ArrayList<String> lore = new ArrayList<>();
					lore.add(RPPersonas.ALT_COLOR + ChatColor.ITALIC + "Click here to get a discord link.");

					String discordTag = plugin.getAccountsSQL().getDiscordInfo(accountID);
					if (discordTag != null && discordTag.length() > 0) {
						lore.add(RPPersonas.ALT_COLOR + ChatColor.ITALIC + "Linked To: " + ChatColor.RESET + RPPersonas.ALT_COLOR + discordTag);
					}

					meta.setLore(lore);
					item.setItemMeta(meta);
					return item;
				}

				@Override
				public void click(MenuAction menuAction) {
					TextComponent message = new TextComponent(RPPersonas.PREFIX + ChatColor.BOLD + "→ Click here to open Discord! ←");
					message.setClickEvent(new ClickEvent(ClickEvent.Action.OPEN_URL, "https://discord.gg/MnCMWGR"));
					message.setHoverEvent(MessageUtil.hoverEvent("Click!"));
					menuAction.getPlayer().sendMessage(message);
				}
			};
		}

		private Icon getSkinsIcon(int accountID) {
			return new Button() {
				@Override
				public ItemStack getItemStack(MenuAgent menuAgent) {
					ItemStack item = ItemUtil.getSkullFromTexture(SKINS_HEAD);
					ItemMeta meta = item.getItemMeta();
					meta.setDisplayName(RPPersonas.PREFIX + ChatColor.BOLD + "Saved Skins");

					ArrayList<String> lore = new ArrayList<>();
					lore.add(RPPersonas.ALT_COLOR + ChatColor.ITALIC + "Browse and Manage your stored skins.");

					meta.setLore(lore);
					item.setItemMeta(meta);
					return item;
				}

				@Override
				public void click(MenuAction menuAction) {
					menuAction.getPlayer().sendMessage("Open skins menu...");
				}
			};
		}

		private Icon getPersonasIcon(int accountID) {
			return new Button() {
				@Override
				public ItemStack getItemStack(MenuAgent menuAgent) {
					ItemStack item = new ItemStack(Material.PLAYER_HEAD);
					SkullMeta meta = (SkullMeta) item.getItemMeta();
					meta.setOwningPlayer(menuAgent.getPlayer());
					meta.setDisplayName(RPPersonas.PREFIX + ChatColor.BOLD + "Personas");

					ArrayList<String> lore = new ArrayList<>();
					lore.add(RPPersonas.ALT_COLOR + ChatColor.ITALIC + "Browse and Manage your Personas.");

					meta.setLore(lore);
					item.setItemMeta(meta);
					return item;
				}

				@Override
				public void click(MenuAction menuAction) {
					getPersonasListMenu(accountID).get(0).openSession(menuAction.getPlayer());
				}
			};
		}

		private List<Menu> getPersonasListMenu(int accountID) {
			ArrayList<Icon> icons = new ArrayList<>();

			for (int personaID : plugin.getPersonaAccountMapSQL().getPersonasOf(accountID, true)) {
				icons.add(new Button() {
					@Override
					public ItemStack getItemStack(MenuAgent menuAgent) {
						int skinID = plugin.getPersonasSQL().getActiveSkinID(personaID);
						Map<String, Object> data = plugin.getPersonasSQL().getBasicPersonaInfo(personaID);
						ItemStack item;
						if (skinID > 0) {
							item = ItemUtil.getSkullFromTexture(plugin.getSkinsSQL().getTexture(skinID));
						} else {
							item = ItemUtil.getSkullFromTexture(NO_SET_SKIN);
						}

						ItemMeta meta = item.getItemMeta();
						String currentName;
						if (data.containsKey("nickname")) {
							currentName = (String) data.get("nickname");
						} else {
							currentName = (String) data.get("name");
						}
						meta.setDisplayName(RPPersonas.PREFIX + ChatColor.BOLD + currentName);

						ArrayList<String> lore = new ArrayList<>();
						lore.add(RPPersonas.ALT_COLOR + ChatColor.ITALIC + "Persona ID: " + ChatColor.RESET + RPPersonas.ALT_COLOR + personaID);
						lore.add(RPPersonas.ALT_COLOR + ChatColor.ITALIC + "Name: " + ChatColor.RESET + RPPersonas.ALT_COLOR + data.get("name"));
						lore.add(RPPersonas.ALT_COLOR + ChatColor.ITALIC + "Age: " + ChatColor.RESET + RPPersonas.ALT_COLOR + data.get("age"));
						lore.add(RPPersonas.ALT_COLOR + ChatColor.ITALIC + "Race: " + ChatColor.RESET + RPPersonas.ALT_COLOR + data.get("race"));
						lore.add(RPPersonas.ALT_COLOR + ChatColor.ITALIC + "Gender: " + ChatColor.RESET + RPPersonas.ALT_COLOR + data.get("gender"));

						meta.setLore(lore);
						item.setItemMeta(meta);
						return item;
					}

					@Override
					public void click(MenuAction menuAction) {
						menuAction.getPlayer().sendMessage("Opening Persona...");
					}
				});
			}

			for (int personaID : plugin.getPersonaAccountMapSQL().getPersonasOf(accountID, false)) {
				icons.add(new Button() {
					@Override
					public ItemStack getItemStack(MenuAgent menuAgent) {
						int skinID = plugin.getPersonasSQL().getActiveSkinID(personaID);
						Map<String, Object> data = plugin.getPersonasSQL().getBasicPersonaInfo(personaID);
						ItemStack item;
						if (skinID > 0) {
							item = ItemUtil.getSkullFromTexture(plugin.getSkinsSQL().getTexture(skinID));
						} else {
							item = ItemUtil.getSkullFromTexture(DEAD_PERSONA);
						}

						ItemMeta meta = item.getItemMeta();
						String currentName;
						if (data.containsKey("nickname")) {
							currentName = (String) data.get("nickname");
						} else {
							currentName = (String) data.get("name");
						}
						meta.setDisplayName(RPPersonas.PREFIX + ChatColor.BOLD + currentName + "(Dead)");

						ArrayList<String> lore = new ArrayList<>();
						lore.add(RPPersonas.ALT_COLOR + ChatColor.ITALIC + "Persona ID: " + ChatColor.RESET + RPPersonas.ALT_COLOR + personaID);
						lore.add(RPPersonas.ALT_COLOR + ChatColor.ITALIC + "Name: " + ChatColor.RESET + RPPersonas.ALT_COLOR + data.get("name"));
						lore.add(RPPersonas.ALT_COLOR + ChatColor.ITALIC + "Age: " + ChatColor.RESET + RPPersonas.ALT_COLOR + data.get("age"));
						lore.add(RPPersonas.ALT_COLOR + ChatColor.ITALIC + "Race: " + ChatColor.RESET + RPPersonas.ALT_COLOR + data.get("race"));
						lore.add(RPPersonas.ALT_COLOR + ChatColor.ITALIC + "Gender: " + ChatColor.RESET + RPPersonas.ALT_COLOR + data.get("gender"));

						meta.setLore(lore);
						item.setItemMeta(meta);
						return item;
					}

					@Override
					public void click(MenuAction menuAction) {
						menuAction.getPlayer().sendMessage("Opening Persona...");
					}
				});
			}

			return MenuUtil.createMultiPageMenu(homeMenu, ChatColor.BOLD + "Personas", icons);
		}
	}
}
