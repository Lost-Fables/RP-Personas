package net.korvic.rppersonas.personas;

import co.lotc.core.util.MessageUtil;
import net.korvic.rppersonas.RPPersonas;
import net.md_5.bungee.api.chat.BaseComponent;
import net.md_5.bungee.api.chat.TextComponent;
import org.bukkit.ChatColor;
import org.bukkit.conversations.*;
import org.bukkit.entity.Player;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class PersonaCreationDialog {
	//// Creation Dialog Prompts ////

	public static final String BUTTON_SPACE = "  ";
	public static final String DIVIDER = "\n" + RPPersonas.ALT_COLOR + "================================\n" + ChatColor.RESET;
	private static final String NOTE = RPPersonas.ALT_COLOR + ChatColor.BOLD + "\nNote: " + ChatColor.RESET;

	public static final long MONTH_IN_MILLIS = 1000L * 60 * 60 * 24 * 30;

	// Intro //
	protected static class StartingPrompt extends MessagePrompt {

		@Override
		public String getPromptText(ConversationContext context) {
			return RPPersonas.PREFIX + ChatColor.BOLD + "   ► Welcome to Lost Fables! ◄   " +
				   DIVIDER +
				   RPPersonas.ALT_COLOR + "Let's get you started with the " + ChatColor.BOLD + "persona" + RPPersonas.ALT_COLOR + " you'll be playing. For the following questions, simply type a reply in chat, or use the buttons to select your answers." + ChatColor.RESET;
		}

		@Override
		protected Prompt getNextPrompt(ConversationContext context) {
			return new PersonaNamePrompt(false);
		}
	}

	// Persona Name //
	protected static class PersonaNamePrompt extends ValidatingPrompt {
		private boolean returnToEnd;

		public PersonaNamePrompt(boolean returnToEnd) {
			this.returnToEnd = returnToEnd;
		}

		@Override
		public String getPromptText(ConversationContext context) {
			return RPPersonas.PREFIX + "Type in the name for your persona now." +
				   NOTE + RPPersonas.PREFIX + "A name is limited to letters(A-z), spaces, quotations(' \"), and dashes(-).\n";
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

			return ( (p.hasPermission(RPPersonas.PERMISSION_START + ".longname") && input.length() <= 64) || input.length() <= 32);
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
			BaseComponent confirmation = new TextComponent(RPPersonas.PREFIX + "You have entered " + RPPersonas.ALT_COLOR + name + RPPersonas.PREFIX + " as your character name. Is this correct?" +
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
					races.addExtra(BUTTON_SPACE);
				}
			}

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
		private String correctedOutput = null;

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

				BaseComponent subraces = new TextComponent(RPPersonas.PREFIX + "Pick your subrace: " + DIVIDER);
				subraces.addExtra(MessageUtil.CommandButton("Back", "Back", "Click to return to main races"));
				subraces.addExtra(BUTTON_SPACE);

				for (PersonaSubRace race : this.race.getSubRaces()) {
					if (p.hasPermission("rppersonas.race." + race.getName().toLowerCase())) {
						subraces.addExtra(MessageUtil.CommandButton(race.getName(), race.getName(), "Click to select subrace"));
						subraces.addExtra(BUTTON_SPACE);
					}
				}

				p.spigot().sendMessage(subraces);
				return "";
			}
			return ChatColor.DARK_RED + "Error: " + ChatColor.RED + "Main Race not found. Please contact a technician.";
		}

		@Override
		protected boolean isInputValid(ConversationContext context, String input) {
			if ("back".equalsIgnoreCase(input)) return true;
			PersonaSubRace subrace = PersonaSubRace.getByName(input);
			if (subrace != null) {
				correctedOutput = subrace.getName();
				return true;
			} else {
				return false;
			}
		}

		@Override
		protected Prompt acceptValidatedInput(ConversationContext context, String input) {
			if ("back".equalsIgnoreCase(input)) return new PersonaRacePrompt(returnToEnd);
			context.setSessionData("race", correctedOutput);
			if (returnToEnd) {
				return new PersonaConfirmPrompt();
			} else {
				return new PersonaAgePrompt(false);
			}
		}
	}

	// Age //
	protected static class PersonaAgePrompt extends ValidatingPrompt {
		private boolean returnToEnd;
		private int age = 1;

		public PersonaAgePrompt(boolean returnToEnd) {
			this.returnToEnd = returnToEnd;
		}

		@Override
		public String getPromptText(ConversationContext context) {
			return RPPersonas.PREFIX + "Type in the age of your persona now." +
				   NOTE + RPPersonas.PREFIX + "This is measured in Ages, not Eras. Enter the number only.\n";
		}

		@Override
		protected boolean isInputValid(ConversationContext context, String input) {
			Player p = (Player) context.getForWhom();

			if (input.startsWith("/")) return false;
			if (input.matches(".*[^0-9].*")) return false;

			age = Integer.parseInt(input);

			return (p.hasPermission("rppersonas.ignoreage") || withinRaceLimits(age, (String) context.getSessionData("race")));
		}

		@Override
		public Prompt acceptValidatedInput(ConversationContext context, String input) {
			return new ConfirmAgePrompt(age, returnToEnd);
		}

		private boolean withinRaceLimits(int age, String race) {
			boolean output = false;
			try {
				PersonaSubRace subRace = PersonaSubRace.getByName(race);
				output = (age < subRace.getMaxAge());
			} catch (Exception e) {
				RPPersonas.get().getLogger().severe("Player managed to reach age selection with invalid subrace: " + race);
				if (RPPersonas.DEBUGGING) {
					e.printStackTrace();
				}
			}
			return output;
		}
	}

	// Confirm Age //
	private static class ConfirmAgePrompt extends BooleanPrompt {
		private int age;
		private boolean returnToEnd;

		public ConfirmAgePrompt(int input, boolean returnToEnd) {
			this.age = input;
			this.returnToEnd = returnToEnd;
		}

		@Override
		public String getPromptText(ConversationContext context) {
			Player p = (Player) context.getForWhom();
			BaseComponent confirmation = new TextComponent(RPPersonas.PREFIX + "You have entered " + RPPersonas.ALT_COLOR + age + RPPersonas.PREFIX + " as your character age (" + age/4 + " Eras). Is this correct?" +
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
				context.setSessionData("visualage", age);
				context.setSessionData("age", getBornMillis(age));
				if (returnToEnd) {
					return new PersonaConfirmPrompt();
				} else {
					return new PersonaGenderPrompt();
				}
			} else {
				return new PersonaAgePrompt(returnToEnd);
			}
		}

		private long getBornMillis(int age) {
			// TODO - Make year conversion method.
			return (RPPersonas.BASE_LONG_VALUE + System.currentTimeMillis()) - (age * 3 * MONTH_IN_MILLIS);
		}
	}

	// Gender //
	private static class PersonaGenderPrompt extends FixedSetPrompt {
		private String correctedOutput = null;

		@Override
		public String getPromptText(ConversationContext context) {
			Player p = (Player) context.getForWhom();

			BaseComponent genders = new TextComponent(RPPersonas.PREFIX + "Please select the Gender of your Persona." +
													  DIVIDER);

			for (PersonaGender g : PersonaGender.values()) {
				genders.addExtra(MessageUtil.CommandButton(g.getName(), g.getName(), "Click to select"));
				genders.addExtra(BUTTON_SPACE);
			}

			p.spigot().sendMessage(genders);
			return "";
		}

		@Override
		protected boolean isInputValid(ConversationContext context, String input) {
			PersonaGender gender = PersonaGender.getByName(input);
			if (gender != null) {
				correctedOutput = gender.getName();
				return true;
			} else {
				return false;
			}
		}

		@Override
		public Prompt acceptValidatedInput(ConversationContext context, String input) {
			context.setSessionData("gender", correctedOutput);
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
														   "\nAge: " + context.getSessionData("visualage") + " Ages; (" + (((int) context.getSessionData("visualage"))/4) + " Eras)" +
														   "\nGender: " + context.getSessionData("gender") + RPPersonas.PREFIX +
														   "\nDoes everything look to be in order?" +
														   DIVIDER);

			confirmation.addExtra(MessageUtil.CommandButton("Yes", "Yes", "Click to select"));
			confirmation.addExtra(BUTTON_SPACE);
			confirmation.addExtra(MessageUtil.CommandButton("No", "No", "Click to select"));

			p.spigot().sendMessage(confirmation);
			return "";
		}

		@Override
		protected Prompt acceptValidatedInput(ConversationContext context, boolean input) {
			if (input) {
				return registerPersona(context);
			} else {
				return new ReturnUpPrompt();
			}
		}

	}

	// Gender //
	private static class ReturnUpPrompt extends FixedSetPrompt {

		private static String[] SECTION = new String[]{ "Name", "Race", "Age", "Gender", "Done" };

		@Override
		public String getPromptText(ConversationContext context) {
			Player p = (Player) context.getForWhom();

			BaseComponent options = new TextComponent(RPPersonas.PREFIX + "Which part is wrong?" +
													  DIVIDER);
			for (String s : SECTION) {
				options.addExtra(MessageUtil.CommandButton(s, s, "Click to select"));
				options.addExtra(BUTTON_SPACE);
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
			} else if (input.equalsIgnoreCase("Age")) {
				return new PersonaAgePrompt(true);
			} else if (input.equalsIgnoreCase("Gender")) {
				return new PersonaGenderPrompt();
			} else if (input.equalsIgnoreCase("Done")) {
				return registerPersona(context);
			} else {
				return new ReturnUpPrompt();
			}
		}
	}

	private static Prompt registerPersona(ConversationContext context) {
		Player p = (Player) context.getForWhom();
		p.spigot().sendMessage(new TextComponent(RPPersonas.PREFIX + ChatColor.BOLD + "Registering your persona now!"));
		PersonaHandler.registerPersona(context.getAllSessionData(), p);
		PersonaHandler.clearBlindness(p);

		return Prompt.END_OF_CONVERSATION;
	}
}
