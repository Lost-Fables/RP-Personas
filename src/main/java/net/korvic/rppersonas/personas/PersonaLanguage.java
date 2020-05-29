package net.korvic.rppersonas.personas;

import lombok.Getter;

import java.util.ArrayList;
import java.util.List;

public enum PersonaLanguage {

	ANC_COMMON("Ancient Common", "[AC]"),
	COMMON("Common", ""),
	SIGN("Sign Language", "[SL]"),

	GRAVICAN("Gravican", "[GR]"),
	ARMUSIAN("Armusian", "[MS]"),
	CARRIB("Carrib", "[CB]"),

	ANC_REHKISH("Ancient Rehkish", "[AR]"),
	REHKISH("Rehkish", "[R]"),

	ANC_ELVEN("Ancient Elven", "[AE]"),
	CLASS_ELVEN("Classical Elven", "[E]"),
	NEW_ELVEN("New Elven", "[NE]"),

	ANC_YAZYK("Ancient Yazyk", "[AY]"),
	YAZYK("Yazyk", "[Y]"),

	DEMONIC("Demonic", "[D]"),
	PRIMORDIAL("Primordial", "[P]"),
	CELESTIAL("Celestial", "[C]"),
	DRUII_TONGUE("Druii Tongue", "[DT]"),
	HIEROPHANT("Hierophant", "[H]"),
	TALIDURIAN("Talidurian", "[T]"),

	PYRAN("Pyran", "[PY]"),
	AQUAN("Aquan", "[AQ]"),
	ILLUMAN("Illuman", "[IL]"),
	STYGAN("Stygan", "[SY]"),

	JAVALI("Javali", "[JV]"),
	MAKSHA("Maksha", "[MK]"),
	GAHORIAN("Gahorian", "[GH]"),
	VERIVAN("Verivan", "[VR]"),

	DRAKAN("Drakan", "[DK]"),
	FEY_SPEAK("Fey Speak", "[FS]"),
	FAR_TONGUE("Far Tongue", "[FT]"),
	HORDESH("Hordesh", "[HD]");

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
