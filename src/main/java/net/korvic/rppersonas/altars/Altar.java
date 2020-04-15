package net.korvic.rppersonas.altars;

import net.korvic.rppersonas.RPPersonas;
import org.bukkit.Location;
import org.bukkit.block.Block;

import java.util.HashMap;

public class Altar {

	private static int maxAltarID = 1;

	private int altarID;
	private String label;
	private Block altarBase;
	private Location teleportLocation;
	private String iconID;

	private boolean isReviving = false;

	// STATIC //
	public static Altar createAltar(String label, Location loc) {
		int altarID = maxAltarID;
		updateMaxAltarID(altarID);
		String iconID = formatIconID(altarID);

		HashMap<Object, Object> data = new HashMap<>();
		data.put("altarid", altarID);
		data.put("iconid", iconID);
		data.put("label", label);
		data.put("location", loc);
		RPPersonas.get().getAltarSQL().registerOrUpdate(data);

		return new Altar(altarID, label, loc, iconID);
	}

	public static Altar loadAltar(int altarID, String label, Location loc, String iconID) {
		return new Altar(altarID, label, loc, iconID);
	}

	private static void updateMaxAltarID(int id) {
		if (id >= maxAltarID) {
			maxAltarID = id + 1;
		}
	}

	private static String formatIconID(int id) {
		return "RPPersonas_" + id;
	}

	public static int getMaxAltarID() {
		return getMaxAltarID();
	}

	// INSTANCE //
	private Altar(int altarID, String label, Location loc, String iconID) {
		this.altarID = altarID;
		this.label = label;
		this.altarBase = loc.getBlock();
		this.teleportLocation = loc.add(0.5, 1, 0.5);
		this.iconID = iconID;
	}

	public int getAltarID() {
		return altarID;
	}

	public String getLabel() {
		return label;
	}

	public Block getAltarBlock() {
		return altarBase;
	}

	public Location getTPLocation() {
		return teleportLocation;
	}

	public String getIconID() {
		return iconID;
	}

	public boolean isReviving() {
		return isReviving;
	}

	public void setReviving(boolean bool) {
		this.isReviving = bool;
	}

}
