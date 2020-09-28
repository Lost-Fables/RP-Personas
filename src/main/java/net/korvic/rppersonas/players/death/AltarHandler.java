package net.korvic.rppersonas.players.death;

import net.korvic.rppersonas.RPPersonas;
import org.bukkit.Location;
import org.bukkit.block.Block;

import java.util.Collection;
import java.util.HashMap;
import java.util.Set;

public class AltarHandler {

	private final RPPersonas plugin;
	private HashMap<String, Altar> allAltars = new HashMap<>();

	public AltarHandler(RPPersonas plugin) {
		this.plugin = plugin;
	}

	public Altar getAltar(String label) {
		Altar altar = allAltars.get(label);

		if (altar == null) {
			for (String key : allAltars.keySet()) {
				if (label.equalsIgnoreCase(key)) {
					altar = allAltars.get(key);
					break;
				}
			}
		}

		return altar;
	}

	public Altar getAltar(int altarID) {
		for (Altar altar : allAltars.values()) {
			if (altar.getAltarID() == altarID) {
				return altar;
			}
		}
		return null;
	}

	public Collection<Altar> getAltarList() {
		return allAltars.values();
	}

	public Set<String> getAltarNameList() {
		return allAltars.keySet();
	}

	public Altar createAltar(String label, Location loc) {
		Altar output = getAltarOfBlock(loc.clone().subtract(0, 1, 0).getBlock());
		if (output == null && !allAltars.containsKey(label)) {
			output = Altar.createAltar(label, loc);
			allAltars.put(label, output);
		} else {
			output = null;
		}
		return output;
	}

	public Altar loadAltar(int altarID, String label, Location loc, String iconID) {
		Altar output = Altar.loadAltar(altarID, label, loc, iconID);
		allAltars.put(label, output);
		return output;
	}

	public Altar getAltarOfBlock(Block clickedBlock) {
		Altar output = null;
		for (Altar altar : allAltars.values()) {
			if (altar.getAltarBlock().equals(clickedBlock)) {
				output = altar;
				break;
			}
		}
		return output;
	}
}
