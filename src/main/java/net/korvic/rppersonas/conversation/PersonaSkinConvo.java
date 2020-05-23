package net.korvic.rppersonas.conversation;

import co.lotc.core.util.MessageUtil;
import co.lotc.core.util.MojangCommunicator;
import com.google.gson.JsonObject;
import net.korvic.rppersonas.RPPersonas;
import net.korvic.rppersonas.sql.SkinsSQL;
import net.korvic.rppersonas.sql.extras.DataMapFilter;
import net.md_5.bungee.api.chat.BaseComponent;
import net.md_5.bungee.api.chat.TextComponent;
import org.bukkit.ChatColor;
import org.bukkit.conversations.*;
import org.bukkit.entity.Player;

import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class PersonaSkinConvo extends BaseConvo {

	public PersonaSkinConvo(RPPersonas plugin) {
		super(plugin);
	}

	@Override
	public Prompt getFirstPrompt(Map<Object, Object> data) {
		return new SkinNamePrompt();
	}

	// Skin Name //
	public static class SkinNamePrompt extends ValidatingPrompt {

		@Override
		public String getPromptText(ConversationContext context) {
			return "\n" + RPPersonas.PRIMARY_DARK + "Type in a name for this skin now." +
				   PersonaCreationConvo.NOTE + RPPersonas.PRIMARY_DARK + "A name is limited to letters(A-z), spaces, quotations(' \"), and dashes(-).\n";
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
			return new ConfirmNamePrompt(input);
		}
	}

	// Confirm Skin Name //
	private static class ConfirmNamePrompt extends BooleanPrompt {
		private String name;

		public ConfirmNamePrompt(String input) {
			this.name = input;
		}

		@Override
		public String getPromptText(ConversationContext context) {
			Player p = (Player) context.getForWhom();
			BaseComponent confirmation = new TextComponent("\n" + RPPersonas.PRIMARY_DARK + "You have entered " + RPPersonas.SECONDARY_LIGHT + name + RPPersonas.PRIMARY_DARK + " for the skin name.\n" + RPPersonas.PRIMARY_DARK + "Is this correct?\n" +
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
				context.setSessionData("name", name);
				registerSkin(context);
			} else {
				return new SkinNamePrompt();
			}
			return null;
		}
	}

	private static Prompt registerSkin(ConversationContext context) {
		Player p = (Player) context.getForWhom();
		p.spigot().sendMessage(new TextComponent(RPPersonas.PRIMARY_DARK + "" + ChatColor.BOLD + "Adding your skin now..."));

		int accountID = RPPersonas.get().getUuidAccountMapSQL().getAccountID(p.getUniqueId());
		String name = (String) context.getAllSessionData().get("name");
		String texture = null;
		String signature = null;

		try {
			JsonObject skinObject = MojangCommunicator.requestSkin(p.getUniqueId());
			texture = skinObject.get("value").getAsString();
			signature = skinObject.get("signature").getAsString();
		} catch (Exception e) {
			if (RPPersonas.DEBUGGING) {
				e.printStackTrace();
			}
		}

		if (accountID > 0 && texture != null && name.length() > 0) {
			DataMapFilter data = new DataMapFilter();
			data.put(SkinsSQL.ACCOUNTID, accountID)
				.put(SkinsSQL.NAME, name)
				.put(SkinsSQL.TEXTURE, texture)
				.put(SkinsSQL.SIGNATURE, signature);
			RPPersonas.get().getSkinsSQL().registerOrUpdate(data);
			p.spigot().sendMessage(new TextComponent(RPPersonas.PRIMARY_DARK + "" + ChatColor.BOLD + "Successfully added your skin to your account."));
		} else {
			p.spigot().sendMessage(new TextComponent(RPPersonas.PRIMARY_DARK + "" + ChatColor.BOLD + "Unable to add a skin to your account."));
		}

		return Prompt.END_OF_CONVERSATION;
	}
}
