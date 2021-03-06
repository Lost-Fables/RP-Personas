package net.korvic.rppersonas.personas;

import lombok.Getter;

import java.util.ArrayList;
import java.util.List;

public enum PersonaLanguage {

	// Common
	ANC_COMMON("Ancient_Common", "[AC]"),
	COMMON("Common", ""),
	SIGN("Sign_Language", "[SL]"),

	// Human
	GRAVICAN("Gravican", "[GR]"),
	ARMUSIAN("Armusian", "[MS]"),
	CARRIB("Carrib", "[CB]"),
	JOTUNNTUNGA( "Jötunntunga", "[JT]"),
	TIANDIWEN( "Tiandiwen", "[TD]"),
	AKAGIGO( "Akagigo", "[AG]"),
	KALTESPRACH( "Kaltesprach", "[KS]"),

	// Rehk
	ANC_REHKISH("Ancient_Rehkish", "[AR]"),
	REHKISH("Rehkish", "[R]"),

	// Elf
	ANC_ELVEN("Ancient_Elven", "[AE]"),
	CLASS_ELVEN("Classical_Elven", "[E]"),
	NEW_ELVEN("New_Elven", "[NE]"),

	// Dwarf
	ANC_YAZYK("Ancient_Yazyk", "[AY]"),
	YAZYK("Yazyk", "[Y]"),

	// Lost Languages
	DEMONIC("Demonic", "[D]"),
	PRIMORDIAL("Primordial", "[P]"),
	CELESTIAL("Celestial", "[C]"),
	DRUII_TONGUE("Druii_Tongue", "[DT]"),
	HIEROPHANT("Hierophant", "[H]"),
	TALIDURIAN("Talidurian", "[T]"),
	PRAVUM("Pravum", "[PR]"),

	PYRAN("Pyran", "[PY]"),
	AQUAN("Aquan", "[AQ]"),
	ILLUMAN("Illuman", "[IL]"),
	STYGAN("Stygan", "[SY]"),

	// Beastfolk
	JAVALI("Javali", "[JV]"),
	MAKSHA("Maksha", "[MK]"),
	GAHORIAN("Gahorian", "[GH]"),
	VERIVAN("Verivan", "[VR]"),
	UVARI("Uvari", "[UI]"),

	DRAKAN("Drakan", "[DK]"),
	FEY_SPEAK("Fey_Speak", "[FS]"),
	FAR_TONGUE("Far_Tongue", "[FT]"),
	HORDESH("Hordesh", "[HD]"),

	POLYGLOT("Polyglot", "[PG]");

	@Getter private String name;
	@Getter private String tag;

	PersonaLanguage(String name, String tag) {
		this.name = name;
		this.tag = tag;
	}

	public static List<String> getNames() {
		List<String> list = new ArrayList<>();
		for (PersonaLanguage lang : values()) {
			list.add(lang.getName());
		}
		return list;
	}

	public static PersonaLanguage getByName(String name) {
		for (PersonaLanguage lang : values()) {
			if (name.equalsIgnoreCase(lang.getName())) {
				return lang;
			}
		}
		return null;
	}

}
