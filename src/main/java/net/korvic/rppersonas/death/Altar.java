package net.korvic.rppersonas.death;

import net.korvic.rppersonas.RPPersonas;
import net.korvic.rppersonas.sql.AltarSQL;
import net.korvic.rppersonas.sql.extras.DataMapFilter;
import org.bukkit.Location;
import org.bukkit.block.Block;

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
		Altar altar = RPPersonas.get().getAltarHandler().getAltarOfBlock(loc.clone().subtract(0, 1, 0).getBlock());
		if (altar == null) {
			int altarID = maxAltarID;
			updateMaxAltarID(altarID);

			String iconID = formatIconID(altarID);
			altar = new Altar(altarID, label, loc, iconID);

			DataMapFilter data = new DataMapFilter();
			data.put(AltarSQL.ALTARID, altarID)
				.put(AltarSQL.ICONID, iconID)
				.put(AltarSQL.NAME, label)
				.put(AltarSQL.LOCATION, altar.getTPLocation());
			RPPersonas.get().getAltarsSQL().registerOrUpdate(data);

			// TODO add altar icon to Dynmap
		}
		return altar;
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
		this.altarBase = loc.clone().subtract(0, 1, 0).getBlock();
		this.teleportLocation = loc.add(0.5, 0, 0.5);
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
