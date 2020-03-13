package net.korvic.rppersonas.personas;

import java.util.List;

public enum PersonaRace {

	HUMAN("Human", new PersonaSubRace[]{ PersonaSubRace.ARMUSIAN, PersonaSubRace.CARRIBAR, PersonaSubRace.GRAVICAN });

	private String name;
	private PersonaSubRace[] subRaceList;

	PersonaRace(String name, PersonaSubRace[] subRaces) {
		this.name = name;
		this.subRaceList = subRaces;
	}

	public String getName() {
		return name;
	}

	public PersonaSubRace[] getSubRaces() {
		return subRaceList;
	}

	public static PersonaRace getByName(String name) {
		for (PersonaRace race : values()) {
			if (race.getName().equalsIgnoreCase(name)) {
				return race;
			}
		}
		return null;
	}
}
