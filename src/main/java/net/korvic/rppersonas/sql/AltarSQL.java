package net.korvic.rppersonas.sql;

import net.korvic.rppersonas.RPPersonas;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.scheduler.BukkitRunnable;

import java.sql.*;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Level;

public class AltarSQL extends SQLConnection {

	private static final String SQLTableName = "rppersonas_altars";

	public AltarSQL(RPPersonas plugin) {
		if (SQLConnection.plugin == null) {
			SQLConnection.plugin = plugin;
		}

		String SQLTable = "CREATE TABLE IF NOT EXISTS " + SQLTableName + " (\n" +
						  "    AltarID INT NOT NULL PRIMARY KEY,\n" +
						  "    Name TEXT NOT NULL,\n" +
						  "    World TEXT NOT NULL,\n" +
						  "    LocationX DOUBLE NOT NULL,\n" +
						  "    LocationY DOUBLE NOT NULL,\n" +
						  "    LocationZ DOUBLE NOT NULL,\n" +
						  "    IconID TEXT\n" +
						  ");";
		load(SQLTable, SQLTableName);
	}

	@Override
	protected boolean customStatement() {
		connection = getSQLConnection();
		try {
			String stmt;
			stmt = "SELECT * FROM " + SQLTableName + ";";
			PreparedStatement ps = connection.prepareStatement(stmt);
			ResultSet rs = ps.executeQuery();

			while (rs.next()) {
				World world = Bukkit.getWorld(rs.getString("World"));
				if (world != null) {
					Location loc = new Location(world, rs.getDouble("LocationX"), rs.getDouble("LocationY"), rs.getDouble("LocationZ"));
					plugin.getAltarHandler().loadAltar(rs.getInt("AltarID"), rs.getString("Name"), loc, rs.getString("IconID"));
				}
			}

			close(ps, rs);
		} catch (SQLException ex) {
			plugin.getLogger().log(Level.SEVERE, "Unable to retreive connection", ex);
		}

		return true;
	}

	public void registerOrUpdate(Map<Object, Object> data) {
		if (data.containsKey("altarid")) {
			try {
				plugin.getSaveQueue().addToQueue(getSaveStatement(data));
			} catch (SQLException ex) {
				plugin.getLogger().log(Level.SEVERE, Errors.sqlConnectionExecute(), ex);
			}
		}
	}

	public PreparedStatement getSaveStatement(Map<Object, Object> data) throws SQLException {
		Connection conn = null;
		PreparedStatement grabStatement = null;
		PreparedStatement replaceStatement = null;
		conn = getSQLConnection();

		grabStatement = conn.prepareStatement("SELECT * FROM " + SQLTableName + " WHERE AltarID='" + data.get("altarid") + "'");
		ResultSet result = grabStatement.executeQuery();
		boolean resultPresent = result.next();

		conn = getSQLConnection();
		replaceStatement = conn.prepareStatement("REPLACE INTO " + SQLTableName + " (AltarID,Name,World,LocationX,LocationY,LocationZ,IconID) VALUES(?,?,?,?,?,?,?)");


		// Required
		replaceStatement.setInt(1, (int) data.get("altarid"));

		if (data.containsKey("label")) {
			replaceStatement.setString(2, (String) data.get("label"));
		} else if (resultPresent) {
			replaceStatement.setString(2, result.getString("Name"));
		} else {
			replaceStatement.setString(2, null);
		}

		if (data.containsKey("location")) {
			Location loc = (Location) data.get("location");
			replaceStatement.setString(3, loc.getWorld().getName());
			replaceStatement.setDouble(4, loc.getX());
			replaceStatement.setDouble(5, loc.getY());
			replaceStatement.setDouble(6, loc.getZ());
		} else if (resultPresent) {
			replaceStatement.setString(3, result.getString("World"));
			replaceStatement.setDouble(4, result.getInt("LocationX"));
			replaceStatement.setDouble(5, result.getInt("LocationY"));
			replaceStatement.setDouble(6, result.getInt("LocationZ"));
		} else {
			replaceStatement.setString(3, null);
			replaceStatement.setDouble(4, 0);
			replaceStatement.setDouble(5, 0);
			replaceStatement.setDouble(6, 0);
		}

		if (data.containsKey("iconid")) {
			replaceStatement.setString(7, (String) data.get("iconid"));
		} else if (resultPresent) {
			replaceStatement.setString(7, result.getString("IconID"));
		} else {
			replaceStatement.setString(7, null);
		}

		grabStatement.close();
		return replaceStatement;
	}

}
