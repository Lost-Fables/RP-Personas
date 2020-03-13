package net.korvic.rppersonas.personas;

import net.korvic.rppersonas.RPPersonas;
import org.bukkit.ChatColor;
import org.bukkit.conversations.ConversationFactory;
import org.bukkit.entity.Player;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class PersonaHandler {

	private static RPPersonas plugin;
	private Map<Integer, Persona> loadedPersonas; // personaID , persona
	private static int highestPersonaID = 0;

	public PersonaHandler(RPPersonas plugin) {
		PersonaHandler.plugin = plugin;
	}

	public static void updateHighestPersonaID(int personaID) {
		if (personaID >= highestPersonaID) {
			highestPersonaID = personaID + 1;
		}
	}

	// CREATION //
	public static void createPersona(Player p, int accountID, boolean first) {
		blindPlayer(p);

		Map<Object, Object> data = new HashMap<>();
		String title = null;
		data.put("accountid", accountID);
		data.put("alive", new Object());

		if (first) {
			title = RPPersonas.PREFIX + ChatColor.BOLD + "Welcome!";
		}

		p.sendTitle(title,
					RPPersonas.ALT_COLOR + "Type your Persona's name to continue.",
					20, 60*20, 20);

		ConversationFactory factory = getFreshFactory();
		factory.withInitialSessionData(data);
		if (first) {
			factory.withFirstPrompt(new PersonaCreationDialog.StartingPrompt());
			//factory.addConversationAbandonedListener(new FirstPersonaAbandonListener());
		} else {
			factory.withFirstPrompt(new PersonaCreationDialog.PersonaNamePrompt(false));
		}
		factory.buildConversation(p).begin();
	}

	public static void registerPersona(Map<Object, Object> data) {
		int personaID = highestPersonaID;
		if (data.containsKey("personaid")) {
			personaID = (int) data.get("personaid");
		}
		updateHighestPersonaID(personaID);

		int accountID = (int) data.get("accountid");

		String prefix = null;
		if (data.containsKey("prefix")) {
			prefix = (String) data.get("prefix");
		}

		String nickName = null;
		if (data.containsKey("nickname")) {
			nickName = (String) data.get("nickname");
		} else if (data.containsKey("name")) {
			nickName = (String) data.get("name");
		}

		String personaInvData = null;
		if (data.containsKey("inventory")) {
			personaInvData = (String) data.get("inventory");
		}

		int activeSkinID = 0;
		if (data.containsKey("skinid")) {
			activeSkinID = (int) data.get("skinid");
		}

		if (data.containsKey("fresh")) {
			plugin.getPersonasSQL().register(data);
		}
		Persona persona = new Persona(personaID, accountID, prefix, nickName, personaInvData, data.containsKey("alive"), activeSkinID);
	}

	// CHECKING //
	public Persona getPersona(int personaID) {
		return loadedPersonas.get(personaID);
	}

	// UNLOADING //
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

	// EFFECTS //
	private static void blindPlayer(Player p) {
		p.addPotionEffect(new PotionEffect(PotionEffectType.SLOW, 100000, 255));
		p.addPotionEffect(new PotionEffect(PotionEffectType.BLINDNESS, 100000, 255));
	}

	private void clearBlindness(Player p) {
		p.removePotionEffect(PotionEffectType.SLOW);
		p.removePotionEffect(PotionEffectType.BLINDNESS);
	}

	// FACTORY //

	private static ConversationFactory getFreshFactory() {
		return new ConversationFactory(plugin)
				.thatExcludesNonPlayersWithMessage("Console does not participate in dialogues.")
				.withModality(true);
	}

}
