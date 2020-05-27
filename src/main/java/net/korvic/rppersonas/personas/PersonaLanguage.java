package net.korvic.rppersonas.personas;

import lombok.Getter;

public enum PersonaLanguage {

	ANC_COMMON("Ancient Common", "[AC]"),
	COMMON("Common", ""),

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
	VERIVAN("Verivan", "[VR]");

	@Getter private String name;
	@Getter private String tag;

	PersonaLanguage(String name, String tag) {
		this.name = name;
		this.tag = tag;
	}

}
