package net.korvic.rppersonas.personas;

import lombok.Getter;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

public enum PersonaSubRace {

	// HUMANS
	ARMUSIAN(  "Armusian",   90, PersonaRace.HUMAN, new PersonaLanguage[] { PersonaLanguage.COMMON, PersonaLanguage.ARMUSIAN }),
	CARRIBARD( "Carribard",  90, PersonaRace.HUMAN, new PersonaLanguage[] { PersonaLanguage.COMMON, PersonaLanguage.CARRIB }),
	GRAVICAN(  "Gravican",   90, PersonaRace.HUMAN, new PersonaLanguage[] { PersonaLanguage.COMMON, PersonaLanguage.GRAVICAN }),
	GIANTBLOOD("Giantblood", 90, PersonaRace.HUMAN, new PersonaLanguage[] { PersonaLanguage.COMMON, PersonaLanguage.JOTUNNTUNGA }),
	TIANDIREN( "Tiandiren",  90, PersonaRace.HUMAN, new PersonaLanguage[] { PersonaLanguage.COMMON, PersonaLanguage.TIANDIWEN }),
	AKAGIJIN(  "Akagijin",   90, PersonaRace.HUMAN, new PersonaLanguage[] { PersonaLanguage.COMMON, PersonaLanguage.AKAGIGO }),
	KALTERMEN( "Kaltermen",  90, PersonaRace.HUMAN, new PersonaLanguage[] { PersonaLanguage.COMMON, PersonaLanguage.KALTESPRACH }),
	// DWARVES
	CAVE_DWARF("Cave Dwarf", 90, PersonaRace.DWARF, new PersonaLanguage[] { PersonaLanguage.COMMON, PersonaLanguage.YAZYK }),
	HILL_DWARF("Hill Dwarf", 90, PersonaRace.DWARF, new PersonaLanguage[] { PersonaLanguage.COMMON, PersonaLanguage.YAZYK }),
	DEEP_DWARF("Deep Dwarf", 90, PersonaRace.DWARF, new PersonaLanguage[] { PersonaLanguage.COMMON, PersonaLanguage.YAZYK }),

	// REHK
	REHK_UR("Rehk-Ur", 90, PersonaRace.REHK, new PersonaLanguage[] { PersonaLanguage.COMMON, PersonaLanguage.REHKISH }),
	REHK_UG("Rehk-Ug", 90, PersonaRace.REHK, new PersonaLanguage[] { PersonaLanguage.COMMON, PersonaLanguage.REHKISH }),

	// ELVES
	SPRING_ELF("Spring Elf", 90, PersonaRace.ELF, new PersonaLanguage[] { PersonaLanguage.COMMON, PersonaLanguage.CLASS_ELVEN }),
	SUMMER_ELF("Summer Elf", 90, PersonaRace.ELF, new PersonaLanguage[] { PersonaLanguage.COMMON, PersonaLanguage.CLASS_ELVEN }),
	AUTUMN_ELF("Autumn Elf", 90, PersonaRace.ELF, new PersonaLanguage[] { PersonaLanguage.COMMON, PersonaLanguage.NEW_ELVEN }),
	WINTER_ELF("Winter Elf", 90, PersonaRace.ELF, new PersonaLanguage[] { PersonaLanguage.COMMON, PersonaLanguage.NEW_ELVEN }),

	// BEASTFOLK
	JAVALI( "Javali",  90, PersonaRace.BEASTFOLK, new PersonaLanguage[] { PersonaLanguage.COMMON, PersonaLanguage.JAVALI }),
	VERIVAN("Verivan", 90, PersonaRace.BEASTFOLK, new PersonaLanguage[] { PersonaLanguage.COMMON, PersonaLanguage.VERIVAN }),
	MAKSHA( "Maksha",  90, PersonaRace.BEASTFOLK, new PersonaLanguage[] { PersonaLanguage.COMMON, PersonaLanguage.MAKSHA }),
	GAHORI( "Gahori",  90, PersonaRace.BEASTFOLK, new PersonaLanguage[] { PersonaLanguage.COMMON, PersonaLanguage.GAHORIAN }),
	UVARI( "Uvari",  90, PersonaRace.BEASTFOLK, new PersonaLanguage[] { PersonaLanguage.COMMON, PersonaLanguage.UVARI }),

	// RACE APP
	EMPYREAN("Empyrean", 90, PersonaRace.OTHER, new PersonaLanguage[] { PersonaLanguage.COMMON, PersonaLanguage.CELESTIAL }),
	DEMONKIN("Demonkin", 90, PersonaRace.OTHER, new PersonaLanguage[] { PersonaLanguage.COMMON, PersonaLanguage.DEMONIC }),
	GOLEM(   "Golem",    90, PersonaRace.OTHER, new PersonaLanguage[] { PersonaLanguage.COMMON });

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

	public String getSafeName() {
		return this.name.replace(' ', '_');
	}

	public static PersonaSubRace getByName(String name) {
		String properName = name.replace('_', ' ');
		for (PersonaSubRace subrace : values()) {
			if (subrace.getName().equalsIgnoreCase(properName)) {
				return subrace;
			}
		}
		return null;
	}

	public static List<String> getNames() {
		return Arrays.stream(values()).map(PersonaSubRace::getSafeName).collect(Collectors.toList());
	}
}
