package net.korvic.rppersonas.personas;

import lombok.Getter;

public enum PersonaRace {

	HUMAN(    "Human",     new PersonaSubRace[] { PersonaSubRace.ARMUSIAN, PersonaSubRace.CARRIBARD, PersonaSubRace.GRAVICAN, PersonaSubRace.GIANTBLOOD, PersonaSubRace.AKAGIJIN }),
	DWARF(    "Dwarf",     new PersonaSubRace[] { PersonaSubRace.CAVE_DWARF, PersonaSubRace.HILL_DWARF, PersonaSubRace.DEEP_DWARF }),
	REHK(     "Rehk",      new PersonaSubRace[] { PersonaSubRace.REHK_UR, PersonaSubRace.REHK_UG }),
	ELF(      "Elf",       new PersonaSubRace[] { PersonaSubRace.SPRING_ELF, PersonaSubRace.SUMMER_ELF, PersonaSubRace.AUTUMN_ELF, PersonaSubRace.WINTER_ELF }),
	BEASTFOLK("Beastfolk", new PersonaSubRace[] { PersonaSubRace.JAVALI, PersonaSubRace.VERIVAN, PersonaSubRace.MAKSHA, PersonaSubRace.GAHORI, PersonaSubRace.UVARI }),
	OTHER(    "Other",     new PersonaSubRace[] { PersonaSubRace.EMPYREAN, PersonaSubRace.DEMONKIN, PersonaSubRace.GOLEM, PersonaSubRace.KALTERMEN, PersonaSubRace.TIANDIREN });

	@Getter private final String name;
	@Getter private final PersonaSubRace[] subRaceList;

	PersonaRace(String name, PersonaSubRace[] subRaces) {
		this.name = name;
		this.subRaceList = subRaces;
	}

	public String getSafeName() {
		return this.name.replace(' ', '_');
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
