package net.korvic.rppersonas.personas;

import net.korvic.rppersonas.RPPersonas;

import java.util.HashMap;
import java.util.Map;

public class Persona {

	private RPPersonas plugin;
	private int personaID;
	private int accountID;
	private String prefix;
	private String nickName;
	private PersonaInventory inv;
	private boolean isAlive;
	private int activeSkinID;

	public Persona(RPPersonas plugin, int personaID, int accountID, String prefix, String nickName, String personaInvData, boolean isAlive, int activeSkinID) {
		this.personaID = personaID;
		this.accountID = accountID;
		this.prefix = prefix;
		this.nickName = nickName;
		this.inv = new PersonaInventory(personaInvData);
		this.isAlive = isAlive;
		this.activeSkinID = activeSkinID;
	}

	public Map<String, Object> getDeepInfo() {
		Map<String, Object> output = new HashMap<>();

		output.put("personaID", personaID);
		output.put("accountID", accountID);
		output.put("prefix", prefix);
		output.put("nickname", nickName);
		output.put("inventory", inv);
		output.put("alive", isAlive);
		output.put("skinid", activeSkinID);

		return output;
	}

	public Map<String, Object> getBasicInfo() {
		Map<String, Object> output = plugin.getPersonasSQL().getBasicPersonaInfo(personaID);

		output.put("personaID", personaID);

		return output;
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
