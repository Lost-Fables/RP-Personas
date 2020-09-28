package net.korvic.rppersonas.players.conversation;

import net.korvic.rppersonas.RPPersonas;
import org.bukkit.ChatColor;
import org.bukkit.conversations.ConversationFactory;
import org.bukkit.conversations.Prompt;
import org.bukkit.entity.Player;

import java.util.Map;

public abstract class BaseConvo {

	protected RPPersonas plugin;
	protected ConversationFactory factory;

	// String Assets
	public static final String BUTTON_SPACE = "  ";
	public static final String DIVIDER = RPPersonas.SECONDARY_DARK + "================================\n" + ChatColor.RESET;
	public static final String NOTE = RPPersonas.SECONDARY_DARK + "" + ChatColor.BOLD + "\nNote: " + ChatColor.RESET;

	public BaseConvo(RPPersonas plugin) {
		this.plugin = plugin;
	}

	public void startConvo(Player p, Map<Object, Object> data, boolean abandonable) {
		this.factory = getFreshFactory();
		if (data != null) {
			factory.withInitialSessionData(data);
		}
		factory.withFirstPrompt(this.getFirstPrompt(data));
		if (abandonable) {
			addAbandoners(factory);
		}
		factory.buildConversation(p).begin();
	}

	public abstract Prompt getFirstPrompt(Map<Object, Object> data);

	// FACTORY //
	protected ConversationFactory getFreshFactory() {
		return new ConversationFactory(plugin)
				.thatExcludesNonPlayersWithMessage("Console does not participate in dialogues.")
				.withModality(true);
	}

	private static void addAbandoners(ConversationFactory factory) {
		factory.withEscapeSequence("quit")
			   .withEscapeSequence("exit")
			   .withEscapeSequence("cancel")
			   .withEscapeSequence("stop")
			   .withEscapeSequence("help");
	}
}
