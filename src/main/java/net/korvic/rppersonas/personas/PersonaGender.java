package net.korvic.rppersonas.personas;

public enum PersonaGender {
	MALE(1, "Male"),
	FEMALE(2, "Female"),
	OTHER(0, "Other");

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
