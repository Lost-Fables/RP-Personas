package net.korvic.rppersonas.sql;

import co.lotc.core.util.DataMapFilter;
import net.korvic.rppersonas.RPPersonas;
import net.korvic.rppersonas.sql.util.Errors;
import net.korvic.rppersonas.sql.util.SaveTracker;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.World;

import java.sql.*;
import java.util.logging.Level;

public class AltarSQL extends BaseSQL {

	private static final String SQL_TABLE_NAME = TABLE_PREFIX + "altars";

	public static final String ALTARID = "altarid";
	public static final String NAME = "name";
	public static final String LOCATION = "location";
	public static final String ICONID = "iconid";

	public AltarSQL(RPPersonas plugin) {
		String SQLTable = "CREATE TABLE IF NOT EXISTS " + SQL_TABLE_NAME + " (\n" +
						  "    AltarID INT NOT NULL PRIMARY KEY,\n" +
						  "    Name TEXT NOT NULL,\n" +
						  "    World TEXT NOT NULL,\n" +
						  "    LocationX INT NOT NULL,\n" +
						  "    LocationY INT NOT NULL,\n" +
						  "    LocationZ INT NOT NULL,\n" +
						  "    LocationYaw INT NOT NULL,\n" +
						  "    IconID TEXT\n" +
						  ");";
		createTable(SQLTable);
		addDataMappings();
	}

	protected void addDataMappings() {
		DataMapFilter.addFilter(ALTARID, Integer.class);
		DataMapFilter.addFilter(NAME, String.class);
		DataMapFilter.addFilter(LOCATION, Location.class);
		DataMapFilter.addFilter(ICONID, String.class);
	}

	public void loadAltars() {
		try (Connection conn = getSQLConnection();
			 PreparedStatement ps = conn.prepareStatement("SELECT * FROM " + SQL_TABLE_NAME + ";");
			 ResultSet rs = ps.executeQuery();) {
			while (rs.next()) {
				World world = Bukkit.getWorld(rs.getString("World"));
				if (world != null) {
					Location loc = new Location(world, rs.getDouble("LocationX"), rs.getDouble("LocationY"), rs.getDouble("LocationZ"), rs.getInt("LocationYaw"), 0);
					plugin.getAltarHandler().loadAltar(rs.getInt("AltarID"), rs.getString("Name"), loc, rs.getString("IconID"));
				}
			}
		} catch (SQLException ex) {
			plugin.getLogger().log(Level.SEVERE, "Unable to retreive connection", ex);
		}
	}

	public void registerOrUpdate(DataMapFilter data) {
		if (data.containsKey(ALTARID)) {
			getSaveStatement(data);
		}
	}

	public void getSaveStatement(DataMapFilter data) {
		try (Connection conn2 = getSQLConnection();
			 PreparedStatement grabStatement = conn2.prepareStatement("SELECT * FROM " + SQL_TABLE_NAME + " WHERE AltarID='" + data.get(ALTARID) + "'");
			 ResultSet result = grabStatement.executeQuery();) {

			boolean resultPresent = result.next();

			try (Connection conn = getSQLConnection();
				PreparedStatement replaceStatement = conn.prepareStatement("REPLACE INTO " + SQL_TABLE_NAME + " (AltarID,Name,World,LocationX,LocationY,LocationZ,LocationYaw,IconID) VALUES(?,?,?,?,?,?,?,?)");) {

				// Required
				replaceStatement.setInt(1, (int) data.get(ALTARID));

				if (data.containsKey(NAME)) {
					replaceStatement.setString(2, (String) data.get(NAME));
				} else if (resultPresent) {
					replaceStatement.setString(2, result.getString("Name"));
				} else {
					replaceStatement.setString(2, null);
				}

				if (data.containsKey(LOCATION)) {
					Location loc = (Location) data.get(LOCATION);
					replaceStatement.setString(3, loc.getWorld().getName());
					replaceStatement.setInt(4, loc.getBlockX());
					replaceStatement.setInt(5, loc.getBlockY());
					replaceStatement.setInt(6, loc.getBlockZ());
					replaceStatement.setInt(7, (int) loc.getYaw());
				} else if (resultPresent) {
					replaceStatement.setString(3, result.getString("World"));
					replaceStatement.setInt(4, result.getInt("LocationX"));
					replaceStatement.setInt(5, result.getInt("LocationY"));
					replaceStatement.setInt(6, result.getInt("LocationZ"));
					replaceStatement.setInt(7, result.getInt("LocationYaw"));
				} else {
					replaceStatement.setString(3, null);
					replaceStatement.setInt(4, 0);
					replaceStatement.setInt(5, 0);
					replaceStatement.setInt(6, 0);
					replaceStatement.setInt(7, 0);
				}

				if (data.containsKey(ICONID)) {
					replaceStatement.setString(8, (String) data.get(ICONID));
				} else if (resultPresent) {
					replaceStatement.setString(8, result.getString("IconID"));
				} else {
					replaceStatement.setString(8, null);
				}
				SaveTracker.executeWithTracker(replaceStatement);
			} catch (SQLException ex) {
				plugin.getLogger().log(Level.SEVERE, Errors.sqlConnectionExecute(), ex);
			}
		} catch (SQLException ex) {
			plugin.getLogger().log(Level.SEVERE, Errors.sqlConnectionExecute(), ex);
		}
	}

}
