package net.korvic.rppersonas.personas;

public enum PersonaGender {
	MALE(1),
	FEMALE(2),
	OTHER(0);

	public final int id;

	PersonaGender(int id) {
		this.id = id;
	}
}
