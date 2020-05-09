package net.korvic.rppersonas.sql;

import net.korvic.rppersonas.RPPersonas;
import net.korvic.rppersonas.sql.extras.DataMapFilter;
import net.korvic.rppersonas.sql.extras.Errors;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.World;

import java.sql.*;
import java.util.Map;
import java.util.logging.Level;

public class AltarSQL extends BaseSQL {

	private static final String SQL_TABLE_NAME = "rppersonas_altars";

	public static final String ALTARID = "altarid";
	public static final String NAME = "name";
	public static final String LOCATION = "location";
	public static final String ICONID = "iconid";

	public AltarSQL(RPPersonas plugin) {
		if (BaseSQL.plugin == null) {
			BaseSQL.plugin = plugin;
		}

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
		load(SQLTable, SQL_TABLE_NAME);
	}

	@Override
	protected boolean customStatement() {
		return false;
	}

	protected void addDataMappings() {
		DataMapFilter.addFilter(ALTARID, ALTARID, Integer.class);
		DataMapFilter.addFilter(NAME, NAME, String.class);
		DataMapFilter.addFilter(LOCATION, LOCATION, Location.class);
		DataMapFilter.addFilter(ICONID, ICONID, String.class);
	}

	public void loadAltars() {
		connection = getSQLConnection();
		try {
			String stmt = "SELECT * FROM " + SQL_TABLE_NAME + ";";
			PreparedStatement ps = connection.prepareStatement(stmt);
			ResultSet rs = ps.executeQuery();

			while (rs.next()) {
				World world = Bukkit.getWorld(rs.getString("World"));
				if (world != null) {
					Location loc = new Location(world, rs.getDouble("LocationX"), rs.getDouble("LocationY"), rs.getDouble("LocationZ"), rs.getInt("LocationYaw"), 0);
					plugin.getAltarHandler().loadAltar(rs.getInt("AltarID"), rs.getString("Name"), loc, rs.getString("IconID"));
				}
			}

			close(ps, rs);
		} catch (SQLException ex) {
			plugin.getLogger().log(Level.SEVERE, "Unable to retreive connection", ex);
		}
	}

	public void registerOrUpdate(DataMapFilter data) {
		if (data.containsKey(ALTARID)) {
			try {
				plugin.getSaveQueue().addToQueue(getSaveStatement(data));
			} catch (SQLException ex) {
				plugin.getLogger().log(Level.SEVERE, Errors.sqlConnectionExecute(), ex);
			}
		}
	}

	public PreparedStatement getSaveStatement(DataMapFilter data) throws SQLException {
		Connection conn = null;
		PreparedStatement grabStatement = null;
		PreparedStatement replaceStatement = null;
		conn = getSQLConnection();

		grabStatement = conn.prepareStatement("SELECT * FROM " + SQL_TABLE_NAME + " WHERE AltarID='" + data.get(ALTARID) + "'");
		ResultSet result = grabStatement.executeQuery();
		boolean resultPresent = result.next();

		conn = getSQLConnection();
		replaceStatement = conn.prepareStatement("REPLACE INTO " + SQL_TABLE_NAME + " (AltarID,Name,World,LocationX,LocationY,LocationZ,LocationYaw,IconID) VALUES(?,?,?,?,?,?,?,?)");


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

		grabStatement.close();
		return replaceStatement;
	}

}
