package net.korvic.rppersonas.personas;

import co.lotc.core.util.MessageUtil;
import com.google.common.collect.Maps;
import net.korvic.rppersonas.RPPersonas;
import net.md_5.bungee.api.chat.*;
import org.bukkit.ChatColor;
import org.bukkit.conversations.*;
import org.bukkit.entity.Player;

import java.util.List;
import java.util.Map;

public class PersonaHandler {

	private RPPersonas plugin;
	private Map<Integer, Persona> loadedPersonas; // personaID , persona
	private final ConversationFactory factory;
	private int highestPersonaID = 0;

	public PersonaHandler(RPPersonas plugin) {
		this.plugin = plugin;
		factory = new ConversationFactory(plugin)
				.thatExcludesNonPlayersWithMessage("Console does not participate in dialogues.")
				.withPrefix(new Prefix())
				.withModality(true);
	}

	public void updateHighestPersonaID(int personaID) {
		if (personaID >= highestPersonaID) {
			highestPersonaID = personaID + 1;
		}
	}

	public void createFirstPersona(Player p, int accountID) {
		Map<Object, Object> data = Maps.newHashMap();
		data.put("slot", 0);
		data.put("first", new Object());

		p.sendTitle(RPPersonas.PREFIX + ChatColor.BOLD + "Welcome!",
					RPPersonas.ALT_COLOR + "Type your Persona's name to continue.",
					20, 60*20, 20);

		factory.withFirstPrompt(new StartingPrompt())
			   .withInitialSessionData(data)
			   .buildConversation(p)
			   .begin();
	}

	public void createPersona(Player p, int accountID) {
		Map<Object, Object> data = Maps.newHashMap();
		data.put("slot", 0);
		data.put("first", new Object());

		p.sendTitle(null,
					RPPersonas.ALT_COLOR + "Type your Persona's name to continue.",
					20, 60*20, 20);

		factory.withFirstPrompt(new PersonaNamePrompt(false))
			   .withInitialSessionData(data)
			   .buildConversation(p)
			   .begin();
	}

	public void loadPersona(int accountID, int personaID) {

	}

	public Persona getPersona(int personaID) {
		return loadedPersonas.get(personaID);
	}

	public void unloadPersonas(int accountID) {
		List<Integer> personas = plugin.getPersAccMapSQL().getPersonasOf(accountID, true);
		personas.addAll(plugin.getPersAccMapSQL().getPersonasOf(accountID, false));
		for (int i : personas) {
			unloadPersona(i);
		}
	}

	public void unloadPersona(int personaID) {
		loadedPersonas.remove(personaID);
	}

	//// Creation Dialog Prompts ////

	private static final String SPACE = "  ";
	private static final String DIVIDER = RPPersonas.ALT_COLOR + "\n===================================================\n" + ChatColor.RESET;
	private static final String NOTE = RPPersonas.ALT_COLOR + ChatColor.BOLD + "\nNote: " + ChatColor.RESET;

	// Intro //
	private static class StartingPrompt extends MessagePrompt {

		@Override
		public String getPromptText(ConversationContext context) {
			return RPPersonas.PREFIX + ChatColor.BOLD + "   ► Welcome to Forgotten Fables! ◄   " +
				   DIVIDER +
				   RPPersonas.ALT_COLOR + "Let's get you started with the " + ChatColor.BOLD + "persona" + RPPersonas.ALT_COLOR + " you'll be playing. For the following questions, simply type a reply in chat, or use the buttons to select your answers." + ChatColor.RESET;
		}

		@Override
		protected Prompt getNextPrompt(ConversationContext context) {
			return new PersonaNamePrompt(false);
		}
	}

	// Persona Name //
	private static class PersonaNamePrompt extends ValidatingPrompt {
		private boolean returnToEnd;

		public PersonaNamePrompt(boolean returnToEnd) {
			this.returnToEnd = returnToEnd;
		}

		@Override
		public String getPromptText(ConversationContext context) {
			return RPPersonas.PREFIX + "Type in the name for your persona now." +
				   NOTE + RPPersonas.PREFIX + "A name is limited to letters(A-z), spaces, quotations(' \"), and dashes(-).";
		}

		@Override
		protected boolean isInputValid(ConversationContext context, String input) {
			Player p = (Player) context.getForWhom();

			if (input.startsWith("/")) return false;
			if (input.matches(".*[^A-Za-zÀ-ÿ \\-'\"].*")) return false;
			return ( (p.hasPermission("rppersonas.longname") && input.length() <= 64) || input.length() <= 32);
		}

		@Override
		public Prompt acceptValidatedInput(ConversationContext context, String input) {
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
			BaseComponent confirmation = new TextComponent(RPPersonas.PREFIX + "You have entered " + RPPersonas.ALT_COLOR + name + RPPersonas.PREFIX + " as your character name. Is this correct?\n" +
														   DIVIDER);

			confirmation.addExtra(MessageUtil.CommandButton("Yes", "Yes", "Click to select!"));
			confirmation.addExtra(SPACE);
			confirmation.addExtra(MessageUtil.CommandButton("No", "No", "Click to select!"));

			p.spigot().sendMessage(confirmation);
			return "";
		}

		@Override
		protected Prompt acceptValidatedInput(ConversationContext context, boolean input) {
			if (input) {
				context.setSessionData("name", name);
				if (returnToEnd) {
					return new PersonaConfirmPrompt();
				} else {
					return new PersonaRacePrompt(false);
				}
			} else {
				return new PersonaNamePrompt(returnToEnd);
			}
		}
	}

	// Race Selection //
	private static class PersonaRacePrompt extends ValidatingPrompt {
		private boolean returnToEnd;

		public PersonaRacePrompt(boolean returnToEnd) {
			this.returnToEnd = returnToEnd;
		}

		@Override
		public String getPromptText(ConversationContext context) {
			Player p = (Player) context.getForWhom();

			BaseComponent races = new TextComponent(RPPersonas.PREFIX + "Pick your main race: " + DIVIDER);

			for (PersonaRace race : PersonaRace.values()) {
				if (p.hasPermission("rppersonas.race." + race.getName().toLowerCase())) {
					races.addExtra(MessageUtil.CommandButton(race.getName(), race.getName(), "Click to see subraces"));
					races.addExtra(SPACE);
				}
			}

			MessageUtil.addNewlines(races);
			p.spigot().sendMessage(races);

			return "";
		}


		@Override
		protected boolean isInputValid(ConversationContext context, String input) {
			PersonaRace race = PersonaRace.getByName(input);
			return race != null;
		}

		@Override
		protected Prompt acceptValidatedInput(ConversationContext context, String input) {
			return new PickSubracePrompt(input, returnToEnd);
		}
	}

	// Subrace Selection //
	private static class PickSubracePrompt extends ValidatingPrompt {
		private PersonaRace race = null;
		private boolean returnToEnd;

		private PickSubracePrompt(String string, boolean returnToEnd){
			try {
				this.race = PersonaRace.getByName(string);
				this.returnToEnd = returnToEnd;
			} catch (Exception e) {
				if (RPPersonas.DEBUGGING) {
					e.printStackTrace();
				}
			}
		}

		@Override
		public String getPromptText(ConversationContext context) {
			if (race != null) {
				Player p = (Player) context.getForWhom();

				BaseComponent subraces = new TextComponent(RPPersonas.PREFIX + "Pick your subrace: " + DIVIDER +
														   MessageUtil.CommandButton("Back", "Back", "Click to return to main races") + SPACE);

				for (PersonaSubRace race : this.race.getSubRaces()) {
					if (p.hasPermission("rppersonas.race." + race.getName().toLowerCase())) {
						subraces.addExtra(MessageUtil.CommandButton(race.getName(), race.getName(), "Click to select subrace"));
						subraces.addExtra(SPACE);
					}
				}

				MessageUtil.addNewlines(subraces);
				p.spigot().sendMessage(subraces);
				return "";
			}
			return ChatColor.DARK_RED + "Error: " + ChatColor.RED + "Main Race not found. Please contact a technician.";
		}

		@Override
		protected boolean isInputValid(ConversationContext context, String input) {
			if ("back".equalsIgnoreCase(input)) return true;
			PersonaSubRace subrace = PersonaSubRace.getByName(input);
			return subrace != null;
		}

		@Override
		protected Prompt acceptValidatedInput(ConversationContext context, String input) {
			if ("back".equalsIgnoreCase(input)) return new PersonaRacePrompt(returnToEnd);
			context.setSessionData("race", input);
			if (returnToEnd) {
				return new PersonaConfirmPrompt();
			} else {
				return new PickGenderPrompt();
			}
		}
	}

	// Gender //
	private static class PickGenderPrompt extends FixedSetPrompt {

		@Override
		public String getPromptText(ConversationContext context) {
			Player p = (Player) context.getForWhom();

			BaseComponent genders = new TextComponent(RPPersonas.PREFIX + "Please select the Gender of your Persona." +
													  DIVIDER);

			for (PersonaGender g : PersonaGender.values()) {
				genders.addExtra(MessageUtil.CommandButton(g.getName(), g.getName(), "Click to select"));
				genders.addExtra(SPACE);
			}

			p.spigot().sendMessage(genders);
			return "";
		}

		@Override
		protected boolean isInputValid(ConversationContext context, String input) {
			PersonaGender gender = PersonaGender.getByName(input);
			return gender != null;
		}

		@Override
		public Prompt acceptValidatedInput(ConversationContext context, String input) {
			context.setSessionData("gender", input);
			return new PersonaConfirmPrompt();
		}
	}

	private static class PersonaConfirmPrompt extends BooleanPrompt {

		@Override
		public String getPromptText(ConversationContext context) {
			Player p = (Player) context.getForWhom();
			BaseComponent confirmation = new TextComponent(RPPersonas.PREFIX + "Let's review your persona details..." + RPPersonas.ALT_COLOR +
														   "\nName: " + context.getSessionData("name") +
														   "\nRace: " + context.getSessionData("race") +
														   "\nGender: " + context.getSessionData("gender") + RPPersonas.PREFIX +
														   "\nDoes everything look to be in order?" +
														   DIVIDER);

			confirmation.addExtra(MessageUtil.CommandButton("Yes", "Yes", "Click to select"));
			confirmation.addExtra(SPACE);
			confirmation.addExtra(MessageUtil.CommandButton("No", "No", "Click to select"));

			p.spigot().sendMessage(confirmation);

			return "";
		}

		@Override
		protected Prompt acceptValidatedInput(ConversationContext context, boolean input) {
			if (input) {
				return Prompt.END_OF_CONVERSATION;
			} else {
				return new ReturnUpPrompt();
			}
		}

	}

	// Gender //
	private static class ReturnUpPrompt extends FixedSetPrompt {

		private static String[] SECTION = new String[]{ "Name", "Race", "Gender", "Done" };

		@Override
		public String getPromptText(ConversationContext context) {
			Player p = (Player) context.getForWhom();

			BaseComponent options = new TextComponent(RPPersonas.PREFIX + "Which part is wrong?" +
													  DIVIDER);
			for (String s : SECTION) {
				options.addExtra(MessageUtil.CommandButton(s, s, "Click to select"));
				options.addExtra(SPACE);
			}

			p.spigot().sendMessage(options);
			return "";
		}

		@Override
		protected boolean isInputValid(ConversationContext context, String input) {
			for (String s : SECTION) {
				if (s.equalsIgnoreCase(input)) {
					return true;
				}
			}
			return false;
		}

		@Override
		public Prompt acceptValidatedInput(ConversationContext context, String input) {
			if (input.equalsIgnoreCase("Name")) {
				return new PersonaNamePrompt(true);
			} else if (input.equalsIgnoreCase("Race")) {
				return new PersonaRacePrompt(true);
			} else if (input.equalsIgnoreCase("Gender")) {
				return new PickGenderPrompt();
			} else if (input.equalsIgnoreCase("Done")) {
				return Prompt.END_OF_CONVERSATION;
			} else {
				return new ReturnUpPrompt();
			}
		}
	}

	public static class Prefix implements ConversationPrefix {
		@Override
		public String getPrefix(ConversationContext context) {
			return "" + RPPersonas.PREFIX;
		}
	}

}
