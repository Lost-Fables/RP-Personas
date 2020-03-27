package net.korvic.rppersonas.personas;

import com.comphenix.protocol.wrappers.WrappedSignedProperty;

public class PersonaSkin {

	private WrappedSignedProperty mojangData;

	private int skinID;
	private String name;
	private String texture;

	public PersonaSkin(int skinID, String name, String texture) {
		this.skinID = skinID;
		this.name = name;
		this.texture = texture;


		this.mojangData = new WrappedSignedProperty("textures", texture, );
	}

}
