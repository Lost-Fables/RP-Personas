package net.korvic.rppersonas.personas;

import com.comphenix.protocol.wrappers.WrappedSignedProperty;
import net.korvic.rppersonas.RPPersonas;

import java.util.Map;

public class PersonaSkin {

	private int skinID;
	private String name;
	private String texture;
	private WrappedSignedProperty mojangData;

	public static PersonaSkin getFromID(int skinID) {
		if (skinID > 0) {
			Map<Object, Object> skinData = RPPersonas.get().getSkinsSQL().getData(skinID);
			if (skinData.containsKey("name") && skinData.containsKey("texture") && skinData.containsKey("signature")) {
				return new PersonaSkin(skinID, (String) skinData.get("name"), (String) skinData.get("texture"), (String) skinData.get("signature"));
			}
		}
		return null;
	}

	public PersonaSkin(int skinID, String name, String texture, String signature) {
		this.skinID = skinID;
		this.name = name;
		this.texture = texture;

		this.mojangData = new WrappedSignedProperty("textures", texture, signature);
	}

	public int getSkinID() {
		return skinID;
	}

	public String getName() {
		return name;
	}

	public String getTextureValue() {
		return texture;
	}

	public WrappedSignedProperty getMojangData() {
		return mojangData;
	}

}
