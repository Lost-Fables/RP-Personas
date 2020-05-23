package net.korvic.rppersonas.conversation;

import co.lotc.core.bukkit.util.InventoryUtil;
import co.lotc.core.util.MessageUtil;
import net.korvic.rppersonas.RPPersonas;
import net.korvic.rppersonas.death.Altar;
import net.korvic.rppersonas.death.Corpse;
import net.korvic.rppersonas.personas.Persona;
import net.korvic.rppersonas.sql.PersonasSQL;
import net.korvic.rppersonas.sql.util.DataMapFilter;
import net.korvic.rppersonas.statuses.DisabledStatus;
import net.md_5.bungee.api.chat.BaseComponent;
import net.md_5.bungee.api.chat.TextComponent;
import org.bukkit.Bukkit;
import org.bukkit.conversations.BooleanPrompt;
import org.bukkit.conversations.ConversationContext;
import org.bukkit.conversations.Prompt;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.Map;
import java.util.UUID;

public class ResurrectionConfirmConvo extends BaseConvo {

	public ResurrectionConfirmConvo(RPPersonas plugin) {
		super(plugin);
	}

	@Override
	public Prompt getFirstPrompt(Map<Object, Object> data) {
		if (data.containsKey("corpse")) {
			return new ConfirmResPrompt((Corpse) data.get("corpse"));
		} else {
			return null;
		}
	}

	public class ConfirmResPrompt extends BooleanPrompt {

		private Corpse corpse;

		public ConfirmResPrompt(Corpse corpse) {
			this.corpse = corpse;
		}

		@Override
		public String getPromptText(ConversationContext context) {
			Player p = (Player) context.getForWhom();
			String name;
			if (context.getAllSessionData().containsKey(PersonasSQL.NICKNAME)) {
				name = (String) context.getAllSessionData().get(PersonasSQL.NICKNAME);
			} else {
				name = (String) context.getAllSessionData().get(PersonasSQL.NAME);
			}

			BaseComponent confirmation = new TextComponent("\n" + RPPersonas.PRIMARY_DARK + "You are about to resurrect " + name + ". Are you sure you want to do this?\n" +
														   PersonaCreationConvo.DIVIDER);

			confirmation.addExtra(MessageUtil.CommandButton("Yes", "Yes", "Click to select!", RPPersonas.SECONDARY_LIGHT, RPPersonas.PRIMARY_LIGHT));
			confirmation.addExtra(PersonaCreationConvo.BUTTON_SPACE);
			confirmation.addExtra(MessageUtil.CommandButton("No", "No", "Click to select!", RPPersonas.SECONDARY_LIGHT, RPPersonas.PRIMARY_LIGHT));

			p.spigot().sendMessage(confirmation);
			return "";
		}

		@Override
		protected Prompt acceptValidatedInput(ConversationContext context, boolean input) {
			if (input) {
				applyResurrection(context.getAllSessionData());
				((Player) context.getForWhom()).spigot().sendMessage(new TextComponent(RPPersonas.PRIMARY_DARK + "The persona will be resurrected the next time they're active."));
			} else {
				InventoryUtil.addOrDropItem((Player) context.getForWhom(), corpse.getItem());
			}
			new DisabledStatus(null).clearEffect((Player) context.getForWhom());
			return Prompt.END_OF_CONVERSATION;
		}

		private void applyResurrection(Map<Object, Object> data) {
			Altar altar = (Altar) data.get("altar");
			Corpse corpse = (Corpse) data.get("corpse");
			int personaID = (int) data.get(PersonasSQL.PERSONAID);

			DataMapFilter personaData = new DataMapFilter();
			personaData.put(PersonasSQL.PERSONAID, personaID);
			personaData.put(PersonasSQL.LIVES, ((int) data.get(PersonasSQL.LIVES) - 1));
			personaData.put(PersonasSQL.ALTARID, altar.getAltarID());
			personaData.put(PersonasSQL.CORPSEINV, InventoryUtil.serializeItems(corpse.getInventory()));

			plugin.getPersonasSQL().registerOrUpdate(personaData);
			plugin.getPersonaAccountMapSQL().registerOrUpdate(personaData);
			plugin.getCorpseSQL().deleteByCorpseID(corpse.getID());

			Persona pers = plugin.getPersonaHandler().getLoadedPersona(personaID);
			if (pers != null && pers.getUsingPlayer().isOnline()) {
				pers.getUsingPlayer().sendMessage(RPPersonas.PRIMARY_DARK + "Your soul is being pulled back to it's body...");
				new BukkitRunnable() {
					private int passes = 0;

					@Override
					public void run() {
						if (plugin.getSaveQueue().isEmpty() || passes > 3) {
							if (pers.getUsingPlayer().isOnline()) {
								plugin.getPersonaHandler().swapToPersona(pers.getUsingPlayer(), pers.getAccountID(), personaID, false); // Reload the force-saved data from above.
								pers.getUsingPlayer().teleportAsync(altar.getTPLocation());
							}
							this.cancel();
						} else {
							passes++;
						}
					}
				}.runTaskTimer(plugin, 20, 20);
			} else {
				int accountID = plugin.getPersonaAccountMapSQL().getAccountOf(personaID);
				for (UUID uuid : plugin.getUuidAccountMapSQL().getUUIDsOf(accountID)) {
					Player p = Bukkit.getPlayer(uuid);
					if (p != null && p.isOnline()) {
						p.sendMessage(RPPersonas.PRIMARY_DARK + "Your persona " + RPPersonas.SECONDARY_DARK + (String) data.get(PersonasSQL.NAME) + RPPersonas.PRIMARY_DARK + " has been resurrected.");
					}
				}
			}
		}

	}

}
