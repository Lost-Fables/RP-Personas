package net.korvic.rppersonas.conversation;

import co.lotc.core.util.MessageUtil;
import lombok.Getter;
import net.korvic.rppersonas.RPPersonas;
import net.korvic.rppersonas.death.Altar;
import net.korvic.rppersonas.personas.PersonaGender;
import net.korvic.rppersonas.sql.PersonasSQL;
import net.korvic.rppersonas.sql.RezAppSQL;
import net.korvic.rppersonas.sql.util.DataMapFilter;
import net.md_5.bungee.api.chat.BaseComponent;
import net.md_5.bungee.api.chat.TextComponent;
import org.bukkit.ChatColor;
import org.bukkit.conversations.*;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class RezAppConvo extends BaseConvo {

	public RezAppConvo(RPPersonas plugin) {
		super(plugin);
	}

	@Override
	public Prompt getFirstPrompt(Map<Object, Object> data) {
		this.factory.addConversationAbandonedListener(new RezAppAbandonedListener());
		return new GreetingPrompt();
	}

	//////////////
	// Greeting //
	//////////////
	private static class GreetingPrompt extends ValidatingPrompt {

		private boolean firstPass = true;
		private boolean returned = false;
		private boolean inquired = false;

		@Override
		public String getPromptText(ConversationContext context) {
			Player p = (Player) context.getForWhom();

			String npcSpeech = null;
			if (firstPass) {
				npcSpeech = fauxChatBuilder("Ah, another soul passing on to Mevvet. Are you ready for what awaits you?");
			} else {
				npcSpeech = fauxChatBuilder("With that out of the way, are you ready for what awaits you?");
			}

			BaseComponent message = new TextComponent(fauxChatBuilder(npcSpeech) + "\n" +
													  DIVIDER);
			message.addExtra(MessageUtil.CommandButton("I've no qualms with my life ending here.", "Yes", "Click to select!", RPPersonas.SECONDARY_LIGHT, RPPersonas.PRIMARY_LIGHT));
			message.addExtra("\n");
			message.addExtra(MessageUtil.CommandButton("Not yet, my life can't end here.", "No", "Click to select!", RPPersonas.SECONDARY_LIGHT, RPPersonas.PRIMARY_LIGHT));
			message.addExtra("\n");
			message.addExtra(MessageUtil.CommandButton("I've been here before...", "Returned", "Click to select!", RPPersonas.SECONDARY_LIGHT, RPPersonas.PRIMARY_LIGHT));
			message.addExtra("\n");
			message.addExtra(MessageUtil.CommandButton("What's Mevvet?", "What", "Click to select!", RPPersonas.SECONDARY_LIGHT, RPPersonas.PRIMARY_LIGHT));

			p.spigot().sendMessage(message);
			firstPass = false;
			return "";
		}

		@Override
		protected boolean isInputValid(ConversationContext context, String input) {
			return true;
		}

		@Override
		public Prompt acceptValidatedInput(ConversationContext context, String input) {
			Player p = (Player) context.getForWhom();

			if (input.equalsIgnoreCase("Yes")) {
				p.sendMessage(fauxChatBuilder("Very well. I'll be here for the rest of eternity if you change your mind."));
				return Prompt.END_OF_CONVERSATION;
			} else if (input.equalsIgnoreCase("No")) {
				return new ReasoningPrompt();
			} else if (input.equalsIgnoreCase("Returned")) {
				String npcSpeech = "Many souls pass through. Some are brought to life by their loved ones at an altar. Surely you don't expect I remember you?";
				if (!inquired) {
					npcSpeech = "Mmm... I'm sure you have. " + npcSpeech;
				}
				returned = true;
				p.sendMessage(fauxChatBuilder(npcSpeech));
				return this;
			} else if (input.equalsIgnoreCase("What")) {
				String npcSpeech = "All you need know is that Mevvet is the place you go when you *die*.";
				if (!returned) {
					npcSpeech = "First time, then? " + npcSpeech;
				}
				inquired = true;
				p.sendMessage(fauxChatBuilder(npcSpeech));
				return this;
			}

			p.sendMessage(fauxChatBuilder("There are a plethora of souls here, I'd rather not waste time."));
			return this;
		}
	}

    ///////////////
	// Reasoning //
	///////////////
	private static class ReasoningPrompt extends ValidatingPrompt {

		private boolean firstPass = true;
		private RezAppResponses responses = new RezAppResponses();

		@Override
		public String getPromptText(ConversationContext context) {
			Player p = (Player) context.getForWhom();

			String npcSpeech = null;
			if (firstPass) {
				npcSpeech = fauxChatBuilder("Mm... For what reasons would you seek to return to the mortal plane?");
			} else {
				double value = Math.random()*5;

				if (value < 1) {
					npcSpeech = fauxChatBuilder("And...?");
				} else if (value < 2) {
					npcSpeech = fauxChatBuilder("I see.");
				} else if (value < 3) {
					npcSpeech = fauxChatBuilder("Alright.");
				} else if (value < 4) {
					npcSpeech = fauxChatBuilder("Mhm.");
				} else {
					npcSpeech = fauxChatBuilder("Go on.");
				}
			}

			BaseComponent message = new TextComponent(fauxChatBuilder(npcSpeech));

			if (!firstPass) {
				message.addExtra("\n" + DIVIDER +
								 MessageUtil.CommandButton("That's all I had.", "Done", "Click to select!", RPPersonas.SECONDARY_LIGHT, RPPersonas.PRIMARY_LIGHT));
			}

			p.spigot().sendMessage(message);
			firstPass = false;
			return "";
		}

		@Override
		protected boolean isInputValid(ConversationContext context, String input) {
			return true;
		}

		@Override
		public Prompt acceptValidatedInput(ConversationContext context, String input) {
			Player p = (Player) context.getForWhom();

			if (input.equalsIgnoreCase("Done")) {
				return new HonestyPrompt(responses);
			}

			responses.addEntry(1, input);
			return this;
		}
	}

	/////////////
	// Honesty //
	/////////////
	private static class HonestyPrompt extends ValidatingPrompt {

		private boolean firstPass = true;
		private RezAppResponses responses;

		public HonestyPrompt(RezAppResponses responses) {
			this.responses = responses;
		}

		@Override
		public String getPromptText(ConversationContext context) {
			Player p = (Player) context.getForWhom();

			String npcSpeech = null;
			if (firstPass) {
				npcSpeech = fauxChatBuilder("So you'd say you've lived an *honest* life?");
			} else {
				double value = Math.random()*5;

				if (value < 1) {
					npcSpeech = fauxChatBuilder("And...?");
				} else if (value < 2) {
					npcSpeech = fauxChatBuilder("I see.");
				} else if (value < 3) {
					npcSpeech = fauxChatBuilder("Alright.");
				} else if (value < 4) {
					npcSpeech = fauxChatBuilder("Mhm.");
				} else {
					npcSpeech = fauxChatBuilder("Go on.");
				}
			}

			BaseComponent message = new TextComponent(fauxChatBuilder(npcSpeech));

			if (!firstPass) {
				message.addExtra("\n" + DIVIDER +
								 MessageUtil.CommandButton("That's all I had.", "Done", "Click to select!", RPPersonas.SECONDARY_LIGHT, RPPersonas.PRIMARY_LIGHT));
			}

			p.spigot().sendMessage(message);
			firstPass = false;
			return "";
		}

		@Override
		protected boolean isInputValid(ConversationContext context, String input) {
			return true;
		}

		@Override
		public Prompt acceptValidatedInput(ConversationContext context, String input) {
			Player p = (Player) context.getForWhom();

			if (input.equalsIgnoreCase("Done")) {
				return new MeaningPrompt(responses);
			}

			responses.addEntry(2, input);
			return this;
		}
	}

	/////////////
	// Meaning //
	/////////////
	private static class MeaningPrompt extends ValidatingPrompt {

		private boolean firstPass = true;
		private RezAppResponses responses;

		public MeaningPrompt(RezAppResponses responses) {
			this.responses = responses;
		}

		@Override
		public String getPromptText(ConversationContext context) {
			Player p = (Player) context.getForWhom();

			String npcSpeech = null;
			if (firstPass) {
				npcSpeech = fauxChatBuilder("And what exactly do you think the meaning of your existence is? Across all the planes, what does your soul accomplish that another could not?");
			} else {
				double value = Math.random()*5;

				if (value < 1) {
					npcSpeech = fauxChatBuilder("And...?");
				} else if (value < 2) {
					npcSpeech = fauxChatBuilder("I see.");
				} else if (value < 3) {
					npcSpeech = fauxChatBuilder("Alright.");
				} else if (value < 4) {
					npcSpeech = fauxChatBuilder("Mhm.");
				} else {
					npcSpeech = fauxChatBuilder("Go on.");
				}
			}

			BaseComponent message = new TextComponent(fauxChatBuilder(npcSpeech));

			if (!firstPass) {
				message.addExtra("\n" + DIVIDER +
								 MessageUtil.CommandButton("That's all I had.", "Done", "Click to select!", RPPersonas.SECONDARY_LIGHT, RPPersonas.PRIMARY_LIGHT));
			}

			p.spigot().sendMessage(message);
			firstPass = false;
			return "";
		}

		@Override
		protected boolean isInputValid(ConversationContext context, String input) {
			return true;
		}

		@Override
		public Prompt acceptValidatedInput(ConversationContext context, String input) {
			Player p = (Player) context.getForWhom();

			if (input.equalsIgnoreCase("Done")) {
				return new AltarPrompt(responses);
			}

			responses.addEntry(3, input);
			return this;
		}
	}

	///////////
	// Altar //
	///////////
	private static class AltarPrompt extends FixedSetPrompt {

		private RezAppResponses responses;

		public AltarPrompt(RezAppResponses responses) {
			this.responses = responses;
		}

		@Override
		public String getPromptText(ConversationContext context) {
			Player p = (Player) context.getForWhom();

			BaseComponent message = new TextComponent(fauxChatBuilder("What area of Gaia do you hail from?") + "\n" + DIVIDER);

			for (String altarName : RPPersonas.get().getAltarHandler().getAltarNameList()) {
				message.addExtra(MessageUtil.CommandButton(altarName, altarName, "Click to select", RPPersonas.SECONDARY_LIGHT, RPPersonas.PRIMARY_LIGHT));
				message.addExtra(BUTTON_SPACE);
			}

			p.spigot().sendMessage(message);
			return "";
		}

		@Override
		protected boolean isInputValid(ConversationContext context, String input) {
			return RPPersonas.get().getAltarHandler().getAltar(input) != null;
		}

		@Override
		public Prompt acceptValidatedInput(ConversationContext context, String input) {
			return new ClosingPrompt(responses, RPPersonas.get().getAltarHandler().getAltar(input));
		}
	}

	///////////
	// Close //
	///////////
	public static class ClosingPrompt extends MessagePrompt {

		private RezAppResponses responses;
		private Altar altar;

		public ClosingPrompt(RezAppResponses responses, Altar altar) {
			this.responses = responses;
			this.altar = altar;
		}

		@Override
		public String getPromptText(ConversationContext context) {
			return fauxChatBuilder("I will need some time to think on this. In the meantime, make yourself at home. It's a *long* ride.");
		}

		@Override
		protected Prompt getNextPrompt(ConversationContext context) {
			RPPersonas plugin = RPPersonas.get();
			int personaID = plugin.getPersonaHandler().getLoadedPersona((Player) context.getForWhom()).getPersonaID();
			DataMapFilter data = new DataMapFilter().put(RezAppSQL.PERSONAID, personaID)
													.put(RezAppSQL.RESPONSES, responses).put(RezAppSQL.KARMA, plugin.getKarmaSQL().calculateKarma(personaID))
													.put(RezAppSQL.KILLS, plugin.getDeathSQL().getKills(personaID))
													.put(RezAppSQL.DEATHS, plugin.getDeathSQL().getDeaths(personaID))
													.put(RezAppSQL.ALTAR, altar);
			plugin.getRezAppSQL().registerOrUpdate(data);
			return Prompt.END_OF_CONVERSATION;
		}
	}

	// FAUX-CHAT BUILDER
	private static String fauxChatBuilder(String message) {
		boolean start = true;
		while (message.contains("*")) {
			if (start) {
				message = message.replaceFirst("/*", ChatColor.ITALIC + "");
			} else {
				message = message.replaceFirst("/*", RPPersonas.SECONDARY_LIGHT + "");
			}
			start = !start;
		}

		return RPPersonas.TERTIARY + "Mysterious Voice: " + RPPersonas.SECONDARY_LIGHT + "\"" + message + "\"";
	}


	// SUB-CLASSES
	public static class RezAppAbandonedListener implements ConversationAbandonedListener {
		@Override
		public void conversationAbandoned(ConversationAbandonedEvent abandonedEvent) {
			if (!abandonedEvent.gracefulExit()) {
				Player p = (Player) abandonedEvent.getContext().getForWhom();
				p.sendMessage(fauxChatBuilder("Very well. I'll be around if you change your mind."));
			}
		}

	}

	public static class RezAppResponses {

		@Getter
		private HashMap<Integer, List<String>> responses = new HashMap<>();

		public void addEntry(int questionID, String entry) {
			List<String> fullEntry = responses.computeIfAbsent(questionID, k -> new ArrayList<>());
			fullEntry.add(entry);
		}

		public List<String> getResponsesAsList(int questionID) {
			return responses.get(questionID);
		}

	}

}

