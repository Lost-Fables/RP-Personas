package net.korvic.rppersonas.personas;

import co.lotc.core.util.MessageUtil;
import net.korvic.rppersonas.RPPersonas;
import net.md_5.bungee.api.chat.BaseComponent;
import net.md_5.bungee.api.chat.TextComponent;
import org.bukkit.ChatColor;
import org.bukkit.conversations.BooleanPrompt;
import org.bukkit.conversations.ConversationContext;
import org.bukkit.conversations.Prompt;
import org.bukkit.conversations.ValidatingPrompt;
import org.bukkit.entity.Player;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class PersonaDeleteDialog {

	// Confirm Deletion //
	public static class DeletePersonaPrompt extends BooleanPrompt {
		private String name;
		private int personaID;

		public DeletePersonaPrompt(int personaID) {
			this.name = RPPersonas.get().getPersonasSQL().getName(personaID);
			this.personaID = personaID;
		}

		@Override
		public String getPromptText(ConversationContext context) {
			Player p = (Player) context.getForWhom();
			BaseComponent confirmation = new TextComponent("\n" + RPPersonas.PRIMARY_DARK + "You have are about to " + RPPersonas.SECONDARY_LIGHT + ChatColor.BOLD + "permanently kill" + RPPersonas.PRIMARY_DARK + " your persona '" + name + "'." +
														   "\n" + RPPersonas.PRIMARY_DARK + "Are you sure you want to do this?\n" +
														   PersonaCreationDialog.DIVIDER);

			confirmation.addExtra(MessageUtil.CommandButton("Yes", "Yes", "Click to select!", RPPersonas.SECONDARY_LIGHT, RPPersonas.PRIMARY_LIGHT));
			confirmation.addExtra(PersonaCreationDialog.BUTTON_SPACE);
			confirmation.addExtra(MessageUtil.CommandButton("No", "No", "Click to select!", RPPersonas.SECONDARY_LIGHT, RPPersonas.PRIMARY_LIGHT));

			p.spigot().sendMessage(confirmation);
			return "";
		}

		@Override
		protected Prompt acceptValidatedInput(ConversationContext context, boolean input) {
			if (input) {
				return new ConfirmDeletePrompt(name, personaID);
			} else {
				return Prompt.END_OF_CONVERSATION;
			}
		}
	}

	// Double Confirm Deletion //
	private static class ConfirmDeletePrompt extends ValidatingPrompt {
		private String name;
		private int personaID;

		public ConfirmDeletePrompt(String name, int personaID) {
			this.name = name;
			this.personaID = personaID;
		}

		@Override
		public String getPromptText(ConversationContext context) {
			return "\n" + RPPersonas.PRIMARY_DARK + "If you're certain, please type '" + name + "'.";
		}

		@Override
		protected boolean isInputValid(ConversationContext context, String input) {
			Player p = (Player) context.getForWhom();

			if (input.startsWith("/")) {
				return false;
			}

			return input.equalsIgnoreCase(name);
		}

		@Override
		public Prompt acceptValidatedInput(ConversationContext context, String input) {
			RPPersonas.get().getPersonaHandler().deletePersona(personaID);
			Player p = (Player) context.getForWhom();
			p.spigot().sendMessage(new TextComponent(RPPersonas.PRIMARY_DARK + "Persona successfully deleted."));
			return Prompt.END_OF_CONVERSATION;
		}
	}

}
