package net.korvic.rppersonas.personas.pieces;

public enum PersonaGender {
	OTHER(0, "Other"),
	MALE(1, "Male"),
	FEMALE(2, "Female");

	private final int id;
	private final String name;

	PersonaGender(int id, String name) {
		this.id = id;
		this.name = name;
	}

	public int getID() {
		return id;
	}

	public String getName() {
		return name;
	}

	public static PersonaGender getByName(String name) {
		for (PersonaGender gender : values()) {
			if (gender.getName().equalsIgnoreCase(name)) {
				return gender;
			}
		}
		return null;
	}

}
