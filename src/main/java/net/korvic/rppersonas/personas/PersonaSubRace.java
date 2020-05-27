package net.korvic.rppersonas.personas;

import lombok.Getter;

public enum PersonaSubRace {

	// HUMANS
	ARMUSIAN("Armusian", 90, PersonaRace.HUMAN, new PersonaLanguage[] { PersonaLanguage.COMMON, PersonaLanguage.ARMUSIAN }),
	CARRIBARD("Carribard", 90, PersonaRace.HUMAN, new PersonaLanguage[] { PersonaLanguage.COMMON, PersonaLanguage.CARRIB }),
	GRAVICAN("Gravican", 90, PersonaRace.HUMAN, new PersonaLanguage[] { PersonaLanguage.COMMON, PersonaLanguage.GRAVICAN });

	// DWARVES


	// REHK


	// ELVES


	@Getter private String name;
	@Getter private int maxAge;
	@Getter private PersonaRace parentRace;
	@Getter private PersonaLanguage[] defaultLanguages;

	PersonaSubRace(String name, int maxAge, PersonaRace parentRace, PersonaLanguage[] languages) {
		this.name = name;
		this.maxAge = maxAge;
		this.parentRace = parentRace;
		this.defaultLanguages = languages;
	}

	public static PersonaSubRace getByName(String name) {
		for (PersonaSubRace subrace : values()) {
			if (subrace.getName().equalsIgnoreCase(name)) {
				return subrace;
			}
		}
		return null;
	}
}
