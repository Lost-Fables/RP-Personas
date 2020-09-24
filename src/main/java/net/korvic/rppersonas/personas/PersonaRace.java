package net.korvic.rppersonas.personas;

import lombok.Getter;

public enum PersonaRace {

	HUMAN("Human", new PersonaSubRace[] { PersonaSubRace.ARMUSIAN, PersonaSubRace.CARRIBARD, PersonaSubRace.GRAVICAN }),
	DWARF("Dwarf", new PersonaSubRace[] { PersonaSubRace.CAVE_DWARF, PersonaSubRace.HILL_DWARF, PersonaSubRace.DEEP_DWARF }),
	REHK("Rehk", new PersonaSubRace[] { PersonaSubRace.REHK_UR, PersonaSubRace.REHK_UG }),
	ELF("Elf", new PersonaSubRace[] { PersonaSubRace.SPRING_ELF, PersonaSubRace.SUMMER_ELF, PersonaSubRace.AUTUMN_ELF, PersonaSubRace.WINTER_ELF }),
	BEASTFOLK("Beastfolk", new PersonaSubRace[] { PersonaSubRace.JAVALI });

	@Getter private String name;
	@Getter private PersonaSubRace[] subRaceList;

	PersonaRace(String name, PersonaSubRace[] subRaces) {
		this.name = name;
		this.subRaceList = subRaces;
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
