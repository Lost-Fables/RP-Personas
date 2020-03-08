package net.korvic.rppersonas.personas;

import org.bukkit.inventory.Inventory;

public class Persona {
	private int personaID;
	private int accountID;
	private String prefix;
	private String nickName;
	private Inventory inv;
	private boolean isAlive;
	private int skinSlot;

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
