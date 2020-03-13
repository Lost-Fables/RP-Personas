package net.korvic.rppersonas.personas;

public class Persona {
	private int personaID;
	private int accountID;
	private String prefix;
	private String nickName;
	private PersonaInventory inv;
	private boolean isAlive;
	private int activeSkinID;

	public Persona(int personaID, int accountID, String prefix, String nickName, String personaInvData, boolean isAlive, int activeSkinID) {
		this.personaID = personaID;
		this.accountID = accountID;
		this.prefix = prefix;
		this.nickName = nickName;
		this.inv = new PersonaInventory(personaInvData);
		this.isAlive = isAlive;
		this.activeSkinID = activeSkinID;
	}

	/*
	 * Store all above info.
	 * Store official name, pull as nickName if there is no nickName
	 * Store desc as String
	 * Store Deaths and Revives as int
	 * Store Money and Bank as truncated Float to 2 decimals
	 * Store Playtime as long (milliseconds)
	 * Store Gender as int referring to PersonaGender
	 */
}
