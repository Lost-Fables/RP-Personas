package net.korvic.rppersonas.altars;

import net.korvic.rppersonas.RPPersonas;
import org.bukkit.Location;

import java.util.HashMap;
import java.util.Set;

public class AltarHandler {

	private RPPersonas plugin;
	private HashMap<String, Altar> allAltars = new HashMap<>();

	public AltarHandler(RPPersonas plugin) {
		this.plugin = plugin;
	}

	public Altar getAltar(String label) {
		return allAltars.get(label);
	}

	public Set<String> getAltarList() {
		return allAltars.keySet();
	}

	public Altar createAltar(String label, Location loc) {
		Altar output = Altar.createAltar(label, loc);
		allAltars.put(label, output);
		return output;
	}

}
