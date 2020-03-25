package net.korvic.rppersonas.commands;

import co.lotc.core.util.MessageUtil;
import net.korvic.rppersonas.RPPersonas;
import net.korvic.rppersonas.personas.*;
import net.md_5.bungee.api.chat.BaseComponent;
import net.md_5.bungee.api.chat.TextComponent;
import org.bukkit.ChatColor;
import org.bukkit.conversations.*;
import org.bukkit.entity.Player;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class SkinNameDialog {
	//// Creation Dialog Prompts ////

	public static final String BUTTON_SPACE = "  ";
	public static final String DIVIDER = RPPersonas.SECONDARY_COLOR + "================================\n" + ChatColor.RESET;
	private static final String NOTE = RPPersonas.SECONDARY_COLOR + ChatColor.BOLD + "\nNote: " + ChatColor.RESET;

	// Persona Name //
	protected static class PersonaNamePrompt extends ValidatingPrompt {
		private boolean returnToEnd;

		public PersonaNamePrompt(boolean returnToEnd) {
			this.returnToEnd = returnToEnd;
		}

		@Override
		public String getPromptText(ConversationContext context) {
			return RPPersonas.PRIMARY_COLOR + "Type in the name for your persona now." +
				   NOTE + RPPersonas.PRIMARY_COLOR + "A name is limited to letters(A-z), spaces, quotations(' \"), and dashes(-).\n";
		}

		@Override
		protected boolean isInputValid(ConversationContext context, String input) {
			Player p = (Player) context.getForWhom();

			if (input.startsWith("/")) {
				return false;
			}

			final String regex = ".*[^A-Za-zÀ-ÿ \\-'\"].*?|\\b[^A-Z ].*?\\b";
			final Pattern pattern = Pattern.compile(regex, Pattern.MULTILINE);
			final Matcher matcher = pattern.matcher(input);
			if (matcher.find()) {
				return false;
			}

			return (p.hasPermission(RPPersonas.PERMISSION_START + ".longname") && input.length() <= 64) || input.length() <= 32;
		}

		@Override
		public Prompt acceptValidatedInput(ConversationContext context, String input) {
			((Player) context.getForWhom()).hideTitle();
			return new ConfirmNamePrompt(input, returnToEnd);
		}
	}

	// Confirm Persona Name //
	private static class ConfirmNamePrompt extends BooleanPrompt {
		private String name;
		private boolean returnToEnd;

		public ConfirmNamePrompt(String input, boolean returnToEnd) {
			this.name = input;
			this.returnToEnd = returnToEnd;
		}

		@Override
		public String getPromptText(ConversationContext context) {
			Player p = (Player) context.getForWhom();
			BaseComponent confirmation = new TextComponent(RPPersonas.PRIMARY_COLOR + "You have entered " + RPPersonas.SECONDARY_COLOR + name + RPPersonas.PRIMARY_COLOR + " as your character name. Is this correct?\n" +
														   DIVIDER);

			confirmation.addExtra(MessageUtil.CommandButton("Yes", "Yes", "Click to select!"));
			confirmation.addExtra(BUTTON_SPACE);
			confirmation.addExtra(MessageUtil.CommandButton("No", "No", "Click to select!"));

			p.spigot().sendMessage(confirmation);
			return "";
		}

		@Override
		protected Prompt acceptValidatedInput(ConversationContext context, boolean input) {
			if (input) {
				context.setSessionData("name", name);
				if (returnToEnd) {
					//return new PersonaConfirmPrompt();
				} else {
					//return new PersonaRacePrompt(false);
				}
			} else {
				return new PersonaNamePrompt(returnToEnd);
			}
			return null;
		}
	}

	private static Prompt registerPersona(ConversationContext context) {
		Player p = (Player) context.getForWhom();
		p.spigot().sendMessage(new TextComponent(RPPersonas.PRIMARY_COLOR + ChatColor.BOLD + "Registering your persona now!"));
		PersonaHandler.registerPersona(context.getAllSessionData(), p, false);
		PersonaDisableListener.enablePlayer(p);

		return Prompt.END_OF_CONVERSATION;
	}
}
