package net.korvic.rppersonas.personas;

public enum PersonaSubRace {

	// HUMANS
	ARMUSIAN("Armusian", PersonaRace.HUMAN),
	CARRIBAR("Carribar", PersonaRace.HUMAN),
	GRAVICAN("Gravican", PersonaRace.HUMAN);

	private String name;
	private PersonaRace parentRace;

	PersonaSubRace(String name, PersonaRace parentRace) {
		this.name = name;
		this.parentRace = parentRace;
	}

	public String getName() {
		return name;
	}

	public PersonaRace getParentRace() {
		return parentRace;
	}

}
