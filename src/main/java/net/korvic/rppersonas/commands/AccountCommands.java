package net.korvic.rppersonas.commands;

import co.lotc.core.bukkit.menu.Menu;
import co.lotc.core.bukkit.menu.MenuAction;
import co.lotc.core.bukkit.menu.MenuAgent;
import co.lotc.core.bukkit.menu.MenuUtil;
import co.lotc.core.bukkit.menu.icon.Button;
import co.lotc.core.bukkit.menu.icon.Icon;
import co.lotc.core.bukkit.util.ItemUtil;
import co.lotc.core.bukkit.util.PermissionsUtil;
import co.lotc.core.command.annotate.Arg;
import co.lotc.core.command.annotate.Cmd;
import co.lotc.core.command.annotate.Default;
import co.lotc.core.util.MessageUtil;
import co.lotc.core.util.TimeUtil;
import net.korvic.rppersonas.RPPersonas;
import net.korvic.rppersonas.players.conversation.PersonaDeleteConvo;
import net.korvic.rppersonas.players.conversation.RezAppConvo;
import net.korvic.rppersonas.players.personas.OldPersona;
import net.korvic.rppersonas.players.personas.PersonaHandler;
import net.korvic.rppersonas.players.personas.PersonaSkin;
import net.korvic.rppersonas.players.conversation.PersonaSkinConvo;
import net.korvic.rppersonas.sql.AccountsSQL;
import net.korvic.rppersonas.sql.PersonasSQL;
import net.korvic.rppersonas.sql.util.DataMapFilter;
import net.korvic.rppersonas.time.TimeManager;
import net.md_5.bungee.api.chat.ClickEvent;
import net.md_5.bungee.api.chat.TextComponent;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.inventory.meta.SkullMeta;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.*;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;

public class AccountCommands extends BaseCommand {

	private RPPersonas plugin;
	private static final String PLAYER_ONLY = RPPersonas.PRIMARY_DARK + "This command only works for players!";
	private static final String ACCOUNT_LINK_SUCCESS = RPPersonas.PRIMARY_DARK + "Your accounts are now linked together!";
	private static final String DISCORD_LINK_SUCCESS = RPPersonas.PRIMARY_DARK + "Successfully linked your Discord account!";
	private static final String DISCORD_UNLINKED_SUCCESS = RPPersonas.PRIMARY_DARK + "Successfully removed the Discord link on your account.";
	private static final String ALREADY_REGISTERED = RPPersonas.PRIMARY_DARK + "Your account is already linked!";
	private static final String FORUM_LINK_REQUIRED = RPPersonas.PRIMARY_DARK + "You need to link your account with " + RPPersonas.SECONDARY_LIGHT + "/account ForumLink FORUM_ID " + RPPersonas.PRIMARY_DARK + "to use that.";

	public AccountCommands(RPPersonas plugin) {
		this.plugin = plugin;
	}

	@Cmd(value = "Link your alternate accounts to the same Lost Fables account.", permission = RPPersonas.PERMISSION_START + ".link")
	public void linkalt (CommandSender sender, Player other) {
		if (sender instanceof Player) {
			Player p = (Player) sender;
			if (!p.equals(other)) {
				int accountID = plugin.getUuidAccountMapSQL().getAccountID(p.getUniqueId());
				if (accountID > 0 && other != null) {
					plugin.getAccountHandler().attemptLink(p, other);
					msg(RPPersonas.PRIMARY_DARK + "Use " + RPPersonas.SECONDARY_LIGHT + "/account altaccept " + p.getName() + RPPersonas.PRIMARY_DARK + "to finalize the link.\n"
						+ RPPersonas.PRIMARY_DARK + "All linked accounts take full responsibility for the actions of one-another.");
				} else {
					msg(RPPersonas.PRIMARY_DARK + "You don't have a linked account, yourself! Make an application on the forums.");
				}
			} else {
				msg(RPPersonas.PRIMARY_DARK + "You cannot link to yourself!");
			}
		} else {
			msg(PLAYER_ONLY);
		}
	}

	@Cmd(value = "Accept an account link request.", permission = RPPersonas.PERMISSION_START + ".link")
	public void altaccept (CommandSender sender, Player other) {
		if (sender instanceof Player) {
			Player p = (Player) sender;
			if (!p.equals(other)) {
				plugin.getAccountHandler().finalizeLink(other, p);
				msg(ACCOUNT_LINK_SUCCESS);
				other.sendMessage(ACCOUNT_LINK_SUCCESS);
			} else {
				msg(RPPersonas.PRIMARY_DARK + "You can't link to yourself!");
			}
		}
	}

	@Cmd(value = "Attach your discord information to your account.", permission = RPPersonas.PERMISSION_START + ".accepted")
	public void linkdiscord (CommandSender sender,
							@Arg(value="DiscordID#0000", description="Your personal Discord ID.") @Default(value="") String discordID) {
		if (sender instanceof Player) {
			//TODO - Hook into Discord bot to send a message to them to confirm the link.

			String discordOutput = null;
			if (discordID.length() > 0) {
				discordOutput = discordID;
			}

			DataMapFilter data = new DataMapFilter();
			data.put(AccountsSQL.ACCOUNTID, plugin.getUuidAccountMapSQL().getAccountID(((Player) sender).getUniqueId()))
				.put(AccountsSQL.DISCORDID, discordOutput);

			plugin.getAccountsSQL().registerOrUpdate(data);

			if (discordOutput != null) {
				msg(DISCORD_LINK_SUCCESS);
			} else {
				msg(DISCORD_UNLINKED_SUCCESS);
			}
		} else {
			msg(PLAYER_ONLY);
		}
	}

	@Cmd(value = "Open a menu to manage your account.", permission = RPPersonas.PERMISSION_START + ".accepted")
	public void menu(CommandSender sender) {
		if (sender instanceof Player) {
			Player p = (Player) sender;
			int accountID = plugin.getUuidAccountMapSQL().getAccountID(p.getUniqueId());
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
		private static final String DEAD_PERSONA = "eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvMmYyNGVkNjg3NTMwNGZhNGExZjBjNzg1YjJjYjZhNmE3MjU2M2U5ZjNlMjRlYTU1ZTE4MTc4NDUyMTE5YWE2NiJ9fX0=";
		private static final String GRAY_STEVE = "eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvZjI5MjY1Y2M1ZjEwMzg0OTQzODJlNjQ2N2FkOGQ3YzlhMjI1NzNlYzM2MzYyYmQ0OTE5MmZkNDM0YjUxYzkyIn19fQ==";

		private static final String NO_PLAYTIME = "Nothing yet!";

		private Menu homeMenu = null;

		private Menu buildMainMenu(int accountID) {
			ArrayList<Icon> icons = new ArrayList<>();
			Map<String, Object> data = plugin.getAccountsSQL().getData(accountID);
			icons.add(getStatisticsIcon(data));
			icons.add(getDiscordIcon(data));
			icons.add(getSkinsIcon(data));
			icons.add(getPersonasIcon(accountID));
			icons.add(getStatusIcon());

			homeMenu = Menu.fromIcons(ChatColor.BOLD + "Account Management", icons);
			return homeMenu;
		}


		// STATS //
		private Icon getStatisticsIcon(Map<String, Object> data) {
			return new Button() {
				@Override
				public ItemStack getItemStack(MenuAgent menuAgent) {
					ItemStack item = ItemUtil.getSkullFromTexture(STAT_HEAD);
					ItemMeta meta = item.getItemMeta();
					meta.setDisplayName(RPPersonas.PRIMARY_DARK + "" + ChatColor.BOLD + "Stats");

					ArrayList<String> lore = new ArrayList<>();
					lore.add(RPPersonas.SECONDARY_LIGHT + "Votes: " + RPPersonas.SECONDARY_DARK + (short) data.get("votes"));

					long timeSpent = (long) data.get("playtime");

					String playtime;
					if (timeSpent > 0) {
						playtime = TimeUtil.printBrief(timeSpent).toPlainText();
					} else {
						playtime = NO_PLAYTIME;
					}

					lore.add(RPPersonas.SECONDARY_LIGHT + "Playtime: " + RPPersonas.SECONDARY_DARK + playtime);

					meta.setLore(lore);
					item.setItemMeta(meta);
					return item;
				}

				@Override
				public void click(MenuAction menuAction) {
				}
			};
		}


		// DISCORD //
		private Icon getDiscordIcon(Map<String, Object> data) {
			return new Button() {
				@Override
				public ItemStack getItemStack(MenuAgent menuAgent) {
					ItemStack item = ItemUtil.getSkullFromTexture(DISCORD_HEAD);
					ItemMeta meta = item.getItemMeta();
					meta.setDisplayName(RPPersonas.PRIMARY_DARK + "" + ChatColor.BOLD + "Discord");

					ArrayList<String> lore = new ArrayList<>();
					lore.add(RPPersonas.SECONDARY_LIGHT + "" + ChatColor.ITALIC + "Click here to get a discord invite.");
					lore.add(RPPersonas.SECONDARY_LIGHT + "" + ChatColor.ITALIC + "Use /account discordlink to link your discord.");

					String discordTag = (String) data.get("discordid");
					if (discordTag != null && discordTag.length() > 0) {
						lore.add("");
						lore.add(RPPersonas.SECONDARY_LIGHT + "Linked To: " + RPPersonas.SECONDARY_DARK + discordTag);
					}

					meta.setLore(lore);
					item.setItemMeta(meta);
					return item;
				}

				@Override
				public void click(MenuAction menuAction) {
					TextComponent message = new TextComponent(RPPersonas.PRIMARY_DARK + "" + ChatColor.BOLD + "→ Click here to open Discord! ←");
					message.setClickEvent(new ClickEvent(ClickEvent.Action.OPEN_URL, "https://discord.gg/MnCMWGR"));
					message.setHoverEvent(MessageUtil.hoverEvent("Click!"));
					menuAction.getPlayer().sendMessage(message);
				}
			};
		}


		// SKINS //
		private Icon getSkinsIcon(Map<String, Object> data) {
			return new Button() {
				@Override
				public ItemStack getItemStack(MenuAgent menuAgent) {
					ItemStack item = ItemUtil.getSkullFromTexture(SKINS_HEAD);
					ItemMeta meta = item.getItemMeta();
					meta.setDisplayName(RPPersonas.PRIMARY_DARK + "" + ChatColor.BOLD + "Saved Skins");

					ArrayList<String> lore = new ArrayList<>();
					lore.add(RPPersonas.SECONDARY_LIGHT + "" + ChatColor.ITALIC + "Browse and Manage your stored skins.");

					meta.setLore(lore);
					item.setItemMeta(meta);
					return item;
				}

				@Override
				public void click(MenuAction menuAction) {
					int maxSkins = PermissionsUtil.getMaxPermission(menuAction.getPlayer().getUniqueId(), RPPersonas.PERMISSION_START + ".personaslots", RPPersonas.DEFAULT_SKINS);
					getSkinsListMenu(data, maxSkins, menuAction.getPlayer()).get(0).openSession(menuAction.getPlayer());
				}
			};
		}

		private List<Menu> getSkinsListMenu(Map<String, Object> data, int maxSkins, Player player) {
			Map<Integer, String> skinData = plugin.getSkinsSQL().getSkinNames((int) data.get("accountid"));
			int currentSkinCount = 0;

			ArrayList<Icon> icons = new ArrayList<>();
			PersonaSkin currentSkin = null;
			if (plugin.getPersonaHandler().getLoadedPersona(player) != null) {
				currentSkin = plugin.getPersonaHandler().getLoadedPersona(player).getActiveSkin();
			}

			for (int skinID : skinData.keySet()) {
				boolean isActive = (currentSkin != null) && (skinID == currentSkin.getSkinID());
				PersonaSkin finalCurrentSkin = currentSkin;

				icons.add(new Button() {
					@Override
					public ItemStack getItemStack(MenuAgent menuAgent) {
						String texture;
						if (isActive) {
							texture = finalCurrentSkin.getTextureValue();
						} else {
							texture = (String) plugin.getSkinsSQL().getData(skinID).get("texture");
						}

						ItemStack item = ItemUtil.getSkullFromTexture(texture);
						ItemMeta meta = item.getItemMeta();

						meta.setDisplayName(RPPersonas.PRIMARY_DARK + "" + ChatColor.BOLD + skinData.get(skinID));
						ArrayList<String> lore = new ArrayList<>();
						if (isActive) {
							lore.add(RPPersonas.SECONDARY_LIGHT + "" + ChatColor.ITALIC + "Current active skin. Right Click to delete.");
						} else {
							lore.add(RPPersonas.SECONDARY_LIGHT + "" + ChatColor.ITALIC + "Left Click to use this skin. Right Click to delete.");
						}

						meta.setLore(lore);
						item.setItemMeta(meta);
						return item;
					}

					@Override
					public void click(MenuAction menuAction) {
						ClickType clickType = menuAction.getClick();

						if (clickType.equals(ClickType.LEFT) || clickType.equals(ClickType.SHIFT_LEFT)) {
							int personaID = plugin.getPersonaHandler().getLoadedPersona(player).getPersonaID();
							plugin.getPersonaHandler().updateActiveSkin(personaID, skinID, menuAction.getPlayer());
							menuAction.getPlayer().sendMessage(RPPersonas.PRIMARY_DARK + "Persona skin updated!");

						} else if (clickType.equals(ClickType.RIGHT) || clickType.equals(ClickType.SHIFT_RIGHT)) {
							Player p = menuAction.getPlayer();
							p.closeInventory();
							p.sendMessage(RPPersonas.PRIMARY_DARK + "Deleting skin...");
							plugin.getPersonaHandler().deleteSkin(skinID);
							plugin.getPersonasSQL().unlinkSkin(skinID);
							p.sendMessage(RPPersonas.PRIMARY_DARK + "Skin deleted.");
						}
					}
				});
				currentSkinCount++;
			}

			if (currentSkinCount < maxSkins) {
				int finalCurrentPersonaCount = currentSkinCount;
				icons.add(new Button() {
					@Override
					public ItemStack getItemStack(MenuAgent menuAgent) {
						ItemStack item = ItemUtil.getSkullFromTexture(GRAY_STEVE);
						ItemMeta meta = item.getItemMeta();

						meta.setDisplayName(RPPersonas.PRIMARY_DARK + "" + ChatColor.BOLD + "Unused Skins (" + (maxSkins - finalCurrentPersonaCount) + ")");
						ArrayList<String> lore = new ArrayList<>();
						lore.add(RPPersonas.SECONDARY_LIGHT + "" + ChatColor.ITALIC + "Click here to save your current skin to your account.");

						meta.setLore(lore);
						item.setItemMeta(meta);
						return item;
					}

					@Override
					public void click(MenuAction menuAction) {
						Player p = menuAction.getPlayer();
						p.closeInventory();

						Map<Object, Object> data = new HashMap<>();
						data.put("accountid", plugin.getUuidAccountMapSQL().getAccountID(p.getUniqueId()));

						new PersonaSkinConvo(plugin).startConvo(p, data, true);
					}
				});
			}

			icons.add(new Button() {
				@Override
				public ItemStack getItemStack(MenuAgent menuAgent) {
					ItemStack item = new ItemStack(Material.PLAYER_HEAD);
					ItemMeta meta = item.getItemMeta();
					meta.setDisplayName(RPPersonas.PRIMARY_DARK + "" + ChatColor.BOLD + "Default Skin");

					ArrayList<String> lore = new ArrayList<>();
					lore.add(RPPersonas.SECONDARY_LIGHT + "" + ChatColor.ITALIC + "Click to reset to your default Minecraft skin.");

					meta.setLore(lore);
					item.setItemMeta(meta);
					return item;
				}

				@Override
				public void click(MenuAction menuAction) {
					int personaID = plugin.getPersonaHandler().getLoadedPersona(player).getPersonaID();
					plugin.getPersonaHandler().updateActiveSkin(personaID, 0, menuAction.getPlayer());
					menuAction.getPlayer().sendMessage(RPPersonas.PRIMARY_DARK + "Persona skin reset!");
				}
			});

			return MenuUtil.createMultiPageMenu(homeMenu, ChatColor.BOLD + "Stored Skins", icons);
		}


		// PERSONAS //
		private Icon getPersonasIcon(int accountID) {
			final boolean[] clicked = { false };

			return new Button() {
				@Override
				public ItemStack getItemStack(MenuAgent menuAgent) {
					ItemStack item = new ItemStack(Material.PLAYER_HEAD);
					SkullMeta meta = (SkullMeta) item.getItemMeta();
					meta.setOwningPlayer(menuAgent.getPlayer());
					meta.setDisplayName(RPPersonas.PRIMARY_DARK + "" + ChatColor.BOLD + "Personas");

					ArrayList<String> lore = new ArrayList<>();
					lore.add(RPPersonas.SECONDARY_LIGHT + "" + ChatColor.ITALIC + "Browse and Manage your Personas.");

					meta.setLore(lore);
					item.setItemMeta(meta);
					return item;
				}

				@Override
				public void click(MenuAction menuAction) {
					if (!clicked[0]) {
						clicked[0] = true;
						Player p = menuAction.getPlayer();
						p.sendMessage(RPPersonas.PRIMARY_DARK + "" + ChatColor.BOLD + "Loading Personas...");
						int totalAccounts = plugin.getUuidAccountMapSQL().getUUIDsOf(p).size();
						AtomicInteger amount = new AtomicInteger(0);
						AtomicInteger passes = new AtomicInteger(0);
						AtomicBoolean finished = PermissionsUtil.getTotalPermission(amount, p.getUniqueId(), RPPersonas.PERMISSION_START + ".personaslots");

						new BukkitRunnable() {
							private boolean opened = false;

							@Override
							public void run() {
								if (!opened) {
									if (finished.get()) {
										int maxPersonas = amount.get() + (totalAccounts * RPPersonas.DEFAULT_PERSONAS);
										List<Menu> personasMenu = getPersonasListMenu(accountID, maxPersonas);
										if (personasMenu.size() > 0) {
											personasMenu.get(0).openSession(menuAction.getPlayer());
											opened = true;
										}
									} else {
										if (passes.get() > 10) {
											opened = true;
										} else {
											passes.addAndGet(1);
										}
									}
								} else {
									if (!this.isCancelled()) {
										this.cancel();
									}
								}
							}
						}.runTaskTimer(plugin, 2, 2);
					}
				}
			};
		}

		private List<Menu> getPersonasListMenu(int accountID, int maxPersonas) {
			List<Icon> icons = new ArrayList<>();
			Map<Integer, UUID> livePersonas = plugin.getPersonaAccountMapSQL().getPersonasOf(accountID, true);
			int currentPersonaCount = 0;

			// LIVE PERSONAS //
			for (int personaID : livePersonas.keySet()) {
				icons.add(new Button() {
					private String currentName;

					@Override
					public ItemStack getItemStack(MenuAgent menuAgent) {
						Map<String, Object> data = plugin.getPersonasSQL().getBasicPersonaInfo(personaID);

						PersonaSkin skin = null;
						if (plugin.getPersonaHandler().getLoadedPersona(menuAgent.getPlayer()) != null) {
							skin = plugin.getPersonaHandler().getLoadedPersona(menuAgent.getPlayer()).getActiveSkin();
						}

						ItemStack item;
						if (skin != null) {
							item = ItemUtil.getSkullFromTexture(skin.getTextureValue());
						} else {
							item = new ItemStack(Material.PLAYER_HEAD);
						}

						ItemMeta meta = item.getItemMeta();
						if (data.containsKey("nickname")) {
							currentName = (String) data.get("nickname");
						} else {
							currentName = (String) data.get("name");
						}
						meta.setDisplayName(RPPersonas.PRIMARY_DARK + "" + ChatColor.BOLD + currentName);

						ArrayList<String> lore = new ArrayList<>();
						if (livePersonas.get(personaID) != null) {
							lore.add(RPPersonas.SECONDARY_LIGHT + "Persona currently in use.");
						} else {
							lore.add(RPPersonas.SECONDARY_LIGHT + "Left Click to use, Right Click to delete.");
						}

						lore.add("");
						lore.add(RPPersonas.SECONDARY_LIGHT + "Persona ID: " + RPPersonas.SECONDARY_DARK + String.format("%06d", personaID));
						lore.add(RPPersonas.SECONDARY_LIGHT + "Name: " + RPPersonas.SECONDARY_DARK + data.get("name"));
						lore.add(RPPersonas.SECONDARY_LIGHT + "Age: " + RPPersonas.SECONDARY_DARK + TimeManager.getRelativeTimeString((long) data.get("age")));
						lore.add(RPPersonas.SECONDARY_LIGHT + "Race: " + RPPersonas.SECONDARY_DARK + data.get("race"));
						lore.add(RPPersonas.SECONDARY_LIGHT + "Gender: " + RPPersonas.SECONDARY_DARK + data.get("gender"));

						meta.setLore(lore);
						item.setItemMeta(meta);
						return item;
					}

					@Override
					public void click(MenuAction menuAction) {
						if (livePersonas.get(personaID) == null) {
							ClickType click = menuAction.getClick();

							if (click.equals(ClickType.LEFT) || click.equals(ClickType.SHIFT_LEFT)) {
								menuAction.getPlayer().closeInventory();
								plugin.getPersonaHandler().swapToPersona(menuAction.getPlayer(), accountID, personaID, true);
								menuAction.getPlayer().sendMessage(RPPersonas.PRIMARY_DARK + "You are now playing as " + RPPersonas.SECONDARY_DARK + currentName + RPPersonas.PRIMARY_DARK + ".");

							} else if (click.equals(ClickType.RIGHT) || click.equals(ClickType.SHIFT_RIGHT)) {
								menuAction.getPlayer().closeInventory();

								Map<Object, Object> data = new HashMap<>();
								data.put(PersonasSQL.PERSONAID, personaID);
								new PersonaDeleteConvo(plugin).startConvo(menuAction.getPlayer(), data, true);
							}
						}
					}
				});
				currentPersonaCount++;
			}

			// DEAD PERSONAS //
			Map<Integer, UUID> deadPersonas = plugin.getPersonaAccountMapSQL().getPersonasOf(accountID, false);
			for (int personaID : deadPersonas.keySet()) {
				icons.add(new Button() {
					@Override
					public ItemStack getItemStack(MenuAgent menuAgent) {
						Map<String, Object> data = plugin.getPersonasSQL().getBasicPersonaInfo(personaID);
						PersonaSkin skin = plugin.getPersonaHandler().getLoadedPersona(menuAgent.getPlayer()).getActiveSkin();
						ItemStack item;
						if (skin != null) {
							item = ItemUtil.getSkullFromTexture(skin.getTextureValue());
						} else {
							item = ItemUtil.getSkullFromTexture(DEAD_PERSONA);
						}

						ItemMeta meta = item.getItemMeta();
						String currentName;
						if (data.containsKey(PersonasSQL.NICKNAME)) {
							currentName = (String) data.get(PersonasSQL.NICKNAME);
						} else {
							currentName = (String) data.get(PersonasSQL.NAME);
						}
						meta.setDisplayName(RPPersonas.PRIMARY_DARK + "" + ChatColor.BOLD + currentName + " (Dead)");

						ArrayList<String> lore = new ArrayList<>();

						if (deadPersonas.get(personaID) != null) {
							lore.add(RPPersonas.SECONDARY_LIGHT + "Persona currently in use.");
							if (!plugin.getRezAppSQL().hasApplied(personaID)) {
								lore.add(RPPersonas.SECONDARY_LIGHT + "Left Click to open a resurrection app.");
							}
						} else {
							lore.add(RPPersonas.SECONDARY_LIGHT + "Left Click to use, Right Click to delete.");
						}

						lore.add("");
						lore.add(RPPersonas.SECONDARY_LIGHT + "Persona ID: " + RPPersonas.SECONDARY_DARK + String.format("%06d", personaID));
						lore.add(RPPersonas.SECONDARY_LIGHT + "Name: " + RPPersonas.SECONDARY_DARK + data.get(PersonasSQL.NAME));
						lore.add(RPPersonas.SECONDARY_LIGHT + "Age: " + RPPersonas.SECONDARY_DARK + TimeManager.getRelativeTimeString((long) data.get(PersonasSQL.AGE)));
						lore.add(RPPersonas.SECONDARY_LIGHT + "Race: " + RPPersonas.SECONDARY_DARK + data.get(PersonasSQL.RACE));
						lore.add(RPPersonas.SECONDARY_LIGHT + "Gender: " + RPPersonas.SECONDARY_DARK + data.get(PersonasSQL.GENDER));

						meta.setLore(lore);
						item.setItemMeta(meta);
						return item;
					}

					@Override
					public void click(MenuAction menuAction) {
						ClickType click = menuAction.getClick();
						Map<String, Object> personaData = plugin.getPersonasSQL().getBasicPersonaInfo(personaID);

						if (click.equals(ClickType.LEFT) || click.equals(ClickType.SHIFT_LEFT)) {
							if (deadPersonas.get(personaID) == null) {
								menuAction.getPlayer().closeInventory();
								plugin.getPersonaHandler().swapToPersona(menuAction.getPlayer(), accountID, personaID, true);
								menuAction.getPlayer().sendMessage(RPPersonas.PRIMARY_DARK + "You are now playing as " + RPPersonas.SECONDARY_DARK + personaData.get(PersonasSQL.NAME) + RPPersonas.PRIMARY_DARK + ".");

							} else if (!plugin.getRezAppSQL().hasApplied(personaID)) {
								menuAction.getPlayer().closeInventory();
								new RezAppConvo(plugin).startConvo(menuAction.getPlayer(), null, true);
							}
						} else if (click.equals(ClickType.RIGHT) || click.equals(ClickType.SHIFT_RIGHT)) {
							menuAction.getPlayer().closeInventory();

							Map<Object, Object> deleteData = new HashMap<>();
							deleteData.put(PersonasSQL.PERSONAID, personaID);
							new PersonaDeleteConvo(plugin).startConvo(menuAction.getPlayer(), deleteData, true);
						}
					}
				});
				currentPersonaCount++;
			}

			// CREATE PERSONAS //
			if (currentPersonaCount < maxPersonas) {
				int finalCurrentPersonaCount = currentPersonaCount;
				icons.add(new Button() {
					@Override
					public ItemStack getItemStack(MenuAgent menuAgent) {
						ItemStack item = ItemUtil.getSkullFromTexture(GRAY_STEVE);
						ItemMeta meta = item.getItemMeta();

						meta.setDisplayName(RPPersonas.PRIMARY_DARK + "" + ChatColor.BOLD + "Unused Personas (" + (maxPersonas - finalCurrentPersonaCount) + ")");
						ArrayList<String> lore = new ArrayList<>();
						lore.add(RPPersonas.SECONDARY_LIGHT + "" + ChatColor.ITALIC + "Click here to make a new persona!");

						meta.setLore(lore);
						item.setItemMeta(meta);
						return item;
					}

					@Override
					public void click(MenuAction menuAction) {
						menuAction.getPlayer().closeInventory();
						PersonaHandler.createPersona(menuAction.getPlayer(), accountID, false);
					}
				});
			}

			return MenuUtil.createMultiPageMenu(homeMenu, ChatColor.BOLD + "Personas", icons);
		}

		// STATUS
		private Icon getStatusIcon() {
			return new Button() {
				@Override
				public ItemStack getItemStack(MenuAgent menuAgent) {
					ItemStack item = new ItemStack(Material.ENCHANTED_GOLDEN_APPLE);
					ItemMeta meta = item.getItemMeta();
					meta.setDisplayName(RPPersonas.PRIMARY_DARK + "" + ChatColor.BOLD + "Status Effects");

					List<String> lore = new ArrayList<>();
					lore.add(RPPersonas.SECONDARY_DARK + "Add or modify active statuses.");

					meta.setLore(lore);
					item.setItemMeta(meta);
					return item;
				}

				@Override
				public void click(MenuAction menuAction) {
					OldPersona pers = plugin.getPersonaHandler().getLoadedPersona(menuAction.getPlayer());
					StatusCommands.buildMainMenu(homeMenu, pers).openSession(menuAction.getPlayer());
				}
			};
		}

	}

}
