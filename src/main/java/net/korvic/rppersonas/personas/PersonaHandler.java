package net.korvic.rppersonas.personas;

import net.korvic.rppersonas.RPPersonas;

import java.util.List;
import java.util.Map;

public class PersonaHandler {

	private RPPersonas plugin;
	private Map<Integer, Persona> loadedPersonas;

	public PersonaHandler(RPPersonas plugin) {
		this.plugin = plugin;
	}

	public void createPersona(int accountID) {

	}

	public void loadPersona(int personaID) {

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

}
