package net.korvic.rppersonas.personas.modification;

import co.lotc.core.util.MessageUtil;
import net.korvic.rppersonas.RPPersonas;
import net.korvic.rppersonas.personas.aspects.PersonaGender;
import net.korvic.rppersonas.personas.PersonaHandler;
import net.korvic.rppersonas.personas.aspects.PersonaRace;
import net.korvic.rppersonas.personas.aspects.PersonaSubRace;
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
	public static final String DIVIDER = RPPersonas.SECONDARY_DARK + "================================\n" + ChatColor.RESET;
	public static final String NOTE = RPPersonas.SECONDARY_DARK + "" + ChatColor.BOLD + "\nNote: " + ChatColor.RESET;

	// Intro //
	public static class StartingPrompt extends MessagePrompt {

		@Override
		public String getPromptText(ConversationContext context) {
			return RPPersonas.PRIMARY_DARK + "" + ChatColor.BOLD + "   ► Welcome to Lost Fables! ◄   \n" +
				   DIVIDER +
				   RPPersonas.SECONDARY_DARK + "Let's get you started with the " + ChatColor.BOLD + "persona" + RPPersonas.SECONDARY_DARK + " you'll be playing. For the following questions, simply type a reply in chat, or use the buttons to select your answers." + ChatColor.RESET;
		}

		@Override
		protected Prompt getNextPrompt(ConversationContext context) {
			return new PersonaNamePrompt(false, true);
		}
	}

	// Persona Name //
	public static class PersonaNamePrompt extends ValidatingPrompt {
		private boolean returnToEnd;
		private boolean firstPersona;

		public PersonaNamePrompt(boolean returnToEnd, boolean firstPersona) {
			this.returnToEnd = returnToEnd;
			this.firstPersona = firstPersona;
		}

		@Override
		public String getPromptText(ConversationContext context) {
			String output = RPPersonas.PRIMARY_DARK + "Type in the name for your persona now." +
							NOTE + RPPersonas.SECONDARY_DARK + ChatColor.ITALIC + "A name is limited to letters(A-z), spaces, quotations(' \"), and dashes(-).\n";
			if (!firstPersona) {
				output  +=  NOTE + RPPersonas.SECONDARY_DARK + ChatColor.ITALIC + "Type 'CANCEL' at any time to exit persona creation.";
			}
			return output;
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
			return new ConfirmNamePrompt(input, returnToEnd, firstPersona);
		}
	}

	// Confirm Persona Name //
	private static class ConfirmNamePrompt extends BooleanPrompt {
		private String name;
		private boolean returnToEnd;
		private boolean firstPersona;

		public ConfirmNamePrompt(String input, boolean returnToEnd, boolean firstPersona) {
			this.name = input;
			this.returnToEnd = returnToEnd;
			this.firstPersona = firstPersona;
		}

		@Override
		public String getPromptText(ConversationContext context) {
			Player p = (Player) context.getForWhom();
			BaseComponent confirmation = new TextComponent("\n" +
														   RPPersonas.PRIMARY_DARK + "You have entered " + RPPersonas.SECONDARY_DARK + name + RPPersonas.PRIMARY_DARK + " as your character name.\n" +
														   RPPersonas.PRIMARY_DARK + "Is this correct?\n" +
														   DIVIDER);

			confirmation.addExtra(MessageUtil.CommandButton("Yes", "Yes", "Click to select!", RPPersonas.SECONDARY_LIGHT, RPPersonas.PRIMARY_LIGHT));
			confirmation.addExtra(BUTTON_SPACE);
			confirmation.addExtra(MessageUtil.CommandButton("No", "No", "Click to select!", RPPersonas.SECONDARY_LIGHT, RPPersonas.PRIMARY_LIGHT));

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
				return new PersonaNamePrompt(returnToEnd, firstPersona);
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

			BaseComponent races = new TextComponent("\n" + RPPersonas.PRIMARY_DARK + "Pick your main race: \n" + DIVIDER);

			for (PersonaRace race : PersonaRace.values()) {
				if (p.hasPermission("rppersonas.race." + race.getName().toLowerCase())) {
					races.addExtra(MessageUtil.CommandButton(race.getName(), race.getName(), "Click to see subraces", RPPersonas.SECONDARY_LIGHT, RPPersonas.PRIMARY_LIGHT));
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

				BaseComponent subraces = new TextComponent("\n" + RPPersonas.PRIMARY_DARK + "Pick your subrace: \n" + DIVIDER);
				subraces.addExtra(MessageUtil.CommandButton("Back", "Back", "Click to return to main races", RPPersonas.SECONDARY_LIGHT, RPPersonas.PRIMARY_LIGHT));
				subraces.addExtra(BUTTON_SPACE);

				for (PersonaSubRace race : this.race.getSubRaces()) {
					if (p.hasPermission("rppersonas.race." + race.getName().toLowerCase())) {
						subraces.addExtra(MessageUtil.CommandButton(race.getName(), race.getName(), "Click to select subrace", RPPersonas.SECONDARY_LIGHT, RPPersonas.PRIMARY_LIGHT));
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
			return "\n" + RPPersonas.PRIMARY_DARK + "Type in the age of your persona now." +
				   NOTE + RPPersonas.PRIMARY_DARK + "This is measured in Ages, not Eras. Enter the number only.\n";
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
			BaseComponent confirmation = new TextComponent("\n" +
														   RPPersonas.PRIMARY_DARK + "You have entered " + RPPersonas.SECONDARY_DARK + age + " Ages " +
														   RPPersonas.PRIMARY_DARK + "for your persona (" + RPPersonas.SECONDARY_DARK + age / 4 + " Eras" +
														   RPPersonas.PRIMARY_DARK + "). Is this correct?\n" + RPPersonas.SECONDARY_DARK +
														   DIVIDER);

			confirmation.addExtra(MessageUtil.CommandButton("Yes", "Yes", "Click to select!", RPPersonas.SECONDARY_LIGHT, RPPersonas.PRIMARY_LIGHT));
			confirmation.addExtra(BUTTON_SPACE);
			confirmation.addExtra(MessageUtil.CommandButton("No", "No", "Click to select!", RPPersonas.SECONDARY_LIGHT, RPPersonas.PRIMARY_LIGHT));

			p.spigot().sendMessage(confirmation);
			return "";
		}

		@Override
		protected Prompt acceptValidatedInput(ConversationContext context, boolean input) {
			if (input) {
				context.setSessionData("age", RPPersonas.getMillisFromAge(age));
				if (returnToEnd) {
					return new PersonaConfirmPrompt();
				} else {
					return new PersonaGenderPrompt();
				}
			} else {
				return new PersonaAgePrompt(returnToEnd);
			}
		}
	}

	// Gender //
	private static class PersonaGenderPrompt extends FixedSetPrompt {
		private String correctedOutput = null;

		@Override
		public String getPromptText(ConversationContext context) {
			Player p = (Player) context.getForWhom();

			BaseComponent genders = new TextComponent("\n" + RPPersonas.PRIMARY_DARK + "Please select the Gender of your Persona.\n" +
													  DIVIDER);

			for (PersonaGender g : PersonaGender.values()) {
				genders.addExtra(MessageUtil.CommandButton(g.getName(), g.getName(), "Click to select", RPPersonas.SECONDARY_LIGHT, RPPersonas.PRIMARY_LIGHT));
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
			BaseComponent confirmation = new TextComponent("\n" +
														   RPPersonas.PRIMARY_DARK + "Let's review your persona details...\n" +
														   RPPersonas.PRIMARY_DARK + "Name: " + RPPersonas.SECONDARY_DARK + context.getSessionData("name") + "\n" +
														   RPPersonas.PRIMARY_DARK + "Race: " + RPPersonas.SECONDARY_DARK + context.getSessionData("race") + "\n" +
														   RPPersonas.PRIMARY_DARK + "Age: " + RPPersonas.SECONDARY_DARK + RPPersonas.getRelativeTimeString((long) context.getSessionData("age")) + "\n" +
														   RPPersonas.PRIMARY_DARK + "Gender: " + RPPersonas.SECONDARY_DARK + context.getSessionData("gender") + "\n" +
														   RPPersonas.PRIMARY_DARK + "Does everything look to be in order?\n" +
														   DIVIDER);

			confirmation.addExtra(MessageUtil.CommandButton("Yes", "Yes", "Click to select", RPPersonas.SECONDARY_LIGHT, RPPersonas.PRIMARY_LIGHT));
			confirmation.addExtra(BUTTON_SPACE);
			confirmation.addExtra(MessageUtil.CommandButton("No", "No", "Click to select", RPPersonas.SECONDARY_LIGHT, RPPersonas.PRIMARY_LIGHT));

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

			BaseComponent options = new TextComponent("\n" + RPPersonas.PRIMARY_DARK + "Which part is wrong?\n" +
													  DIVIDER);
			for (String s : SECTION) {
				options.addExtra(MessageUtil.CommandButton(s, s, "Click to select", RPPersonas.SECONDARY_LIGHT, RPPersonas.PRIMARY_LIGHT));
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
				return new PersonaNamePrompt(true, false);
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
		p.spigot().sendMessage(new TextComponent(RPPersonas.PRIMARY_DARK + "" + ChatColor.BOLD + "Registering your persona now..."));
		PersonaHandler.registerPersona(context.getAllSessionData(), p, false);
		PersonaDisableListener.enablePlayer(p);
		p.spigot().sendMessage(new TextComponent(RPPersonas.PRIMARY_DARK + "" + ChatColor.BOLD + "Registration complete."));



		return Prompt.END_OF_CONVERSATION;
	}
}
