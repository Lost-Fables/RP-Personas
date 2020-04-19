package net.korvic.rppersonas.sql;

import net.korvic.rppersonas.RPPersonas;
import net.korvic.rppersonas.personas.Persona;
import net.korvic.rppersonas.personas.pieces.PersonaGender;
import net.korvic.rppersonas.personas.pieces.PersonaSubRace;
import org.bukkit.Bukkit;
import org.bukkit.Location;

import java.sql.*;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Level;

public class PersonasSQL extends BaseSQL {

	private static final String SQLTableName = "rppersonas_personas";

	public PersonasSQL(RPPersonas plugin) {
		if (BaseSQL.plugin == null) {
			BaseSQL.plugin = plugin;
		}

		String SQLTable = "CREATE TABLE IF NOT EXISTS " + SQLTableName + " (\n" +
						  "    PersonaID INT NOT NULL PRIMARY KEY,\n" +
						  "    Alive BIT NOT NULL,\n" +
						  "    Name TEXT NOT NULL,\n" +
						  "    Gender TEXT NOT NULL,\n" +
						  "    Age BIGINT NOT NULL,\n" +
						  "    Race TEXT NOT NULL,\n" +
						  "    Lives INT NOT NULL,\n" +
						  "    Playtime BIGINT NOT NULL,\n" +

						  "    LocationWorld TEXT NOT NULL,\n" +
						  "    LocationX DOUBLE NOT NULL,\n" +
						  "    LocationY DOUBLE NOT NULL,\n" +
						  "    LocationZ DOUBLE NOT NULL,\n" +

						  "    Health DOUBLE NOT NULL,\n" +
						  "    Hunger INTEGER NOT NULL,\n" +

						  "    Inventory TEXT,\n" +
						  "    EnderChest TEXT,\n" +
						  "    NickName TEXT,\n" +
						  "    Prefix TEXT,\n" +
						  "    ActiveSkinID INT,\n" +
						  "    Description TEXT\n" +
						  ");";
		load(SQLTable, SQLTableName);
	}

	@Override
	protected boolean customStatement() {
		return false;
	}

	// Inserts a new mapping for a persona.
	public void registerOrUpdate(Map<Object, Object> data) {
		if (data.containsKey("personaid")) {
			PreparedStatement ps = null;
			try {
				ps = getSaveStatement(data);
				ps.executeUpdate();
			} catch (SQLException ex) {
				plugin.getLogger().log(Level.SEVERE, Errors.sqlConnectionExecute(), ex);
			} finally {
				try {
					if (ps != null)
						ps.close();
				} catch (SQLException ex) {
					plugin.getLogger().log(Level.SEVERE, Errors.sqlConnectionClose(), ex);
				}
			}
		}
	}

	public PreparedStatement getSaveStatement(Map<Object, Object> data) throws SQLException {
		Connection conn = null;
		PreparedStatement grabStatement = null;
		PreparedStatement replaceStatement = null;
		conn = getSQLConnection();

		grabStatement = conn.prepareStatement("SELECT * FROM " + SQLTableName + " WHERE PersonaID='" + data.get("personaid") + "'");
		ResultSet result = grabStatement.executeQuery();
		boolean resultPresent = result.next();

		conn = getSQLConnection();
		replaceStatement = conn.prepareStatement("REPLACE INTO " + SQLTableName + " (PersonaID,Alive,Name,Gender,Age,Race,Lives,Playtime,LocationWorld,LocationX,LocationY,LocationZ,Health,Hunger,Inventory,EnderChest,NickName,Prefix,ActiveSkinID,Description) VALUES(?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?)");


		// Required
		replaceStatement.setInt(1, (int) data.get("personaid"));

		if (data.containsKey("alive")) {
			replaceStatement.setBoolean(2, (boolean) data.get("alive"));
		} else if (resultPresent) {
			replaceStatement.setBoolean(2, result.getBoolean("Alive"));
		} else {
			replaceStatement.setBoolean(2, true);
		}

		if (data.containsKey("name")) {
			replaceStatement.setString(3, (String) data.get("name"));
		} else if (resultPresent) {
			replaceStatement.setString(3, result.getString("Name"));
		} else {
			replaceStatement.setString(3, "Lost Name");
		}

		if (data.containsKey("gender")) {
			replaceStatement.setString(4, (String) data.get("gender"));
		} else if (resultPresent) {
			replaceStatement.setString(4, result.getString("Gender"));
		} else {
			replaceStatement.setString(4, PersonaGender.values()[0].getName());
		}

		if (data.containsKey("age")) {
			replaceStatement.setLong(5, (long) data.get("age"));
		} else if (resultPresent) {
			replaceStatement.setLong(5, result.getLong("Age"));
		} else {
			replaceStatement.setLong(5, 0);
		}

		if (data.containsKey("race")) {
			replaceStatement.setString(6, (String) data.get("race"));
		} else if (resultPresent) {
			replaceStatement.setString(6, result.getString("Race"));
		} else {
			replaceStatement.setString(6, PersonaSubRace.values()[0].getName());
		}

		if (data.containsKey("lives")) {
			replaceStatement.setInt(7, (int) data.get("lives"));
		} else if (resultPresent) {
			replaceStatement.setInt(7, result.getInt("Lives"));
		} else {
			replaceStatement.setInt(7, 3);
		}

		if (data.containsKey("playtime")) {
			replaceStatement.setLong(8, (long) data.get("playtime"));
		} else if (resultPresent) {
			replaceStatement.setLong(8, result.getLong("Playtime"));
		} else {
			replaceStatement.setLong(8, 0);
		}


		// Location
		Location loc = null;
		if (data.containsKey("location")) {
			loc = (Location) data.get("location");
		} else if (!resultPresent) {
			loc = RPPersonas.get().getSpawnLocation();
		}

		if (loc != null) {
			replaceStatement.setString(9, loc.getWorld().getName());
			replaceStatement.setDouble(10, loc.getX());
			replaceStatement.setDouble(11, loc.getY());
			replaceStatement.setDouble(12, loc.getZ());
		} else {
			replaceStatement.setString(9, result.getString("LocationWorld"));
			replaceStatement.setDouble(10, result.getDouble("LocationX"));
			replaceStatement.setDouble(11, result.getDouble("LocationY"));
			replaceStatement.setDouble(12, result.getDouble("LocationZ"));
		}

		//Health & Hunger
		if (data.containsKey("health")) {
			replaceStatement.setDouble(13, (double) data.get("health"));
		} else if (resultPresent) {
			replaceStatement.setDouble(13, result.getDouble("Health"));
		} else {
			replaceStatement.setDouble(13, 20.0);
		}

		if (data.containsKey("hunger")) {
			replaceStatement.setInt(14, (int) data.get("hunger"));
		} else if (resultPresent) {
			replaceStatement.setInt(14, result.getInt("Hunger"));
		} else {
			replaceStatement.setInt(14, 20);
		}

		// Optional
		if (data.containsKey("inventory")) {
			replaceStatement.setString(15, (String) data.get("inventory"));
		} else if (resultPresent) {
			replaceStatement.setString(15, result.getString("Inventory"));
		} else {
			replaceStatement.setString(15, null);
		}

		if (data.containsKey("enderchest")) {
			replaceStatement.setString(16, (String) data.get("enderchest"));
		} else if (resultPresent) {
			replaceStatement.setString(16, result.getString("EnderChest"));
		} else {
			replaceStatement.setString(16, null);
		}

		if (data.containsKey("nickname")) {
			replaceStatement.setString(17, (String) data.get("nickname"));
		} else if (resultPresent) {
			replaceStatement.setString(17, result.getString("NickName"));
		} else {
			replaceStatement.setString(17, null);
		}

		if (data.containsKey("prefix")) {
			replaceStatement.setString(18, (String) data.get("prefix"));
		} else if (resultPresent) {
			replaceStatement.setString(18, result.getString("Prefix"));
		} else {
			replaceStatement.setString(18, null);
		}

		if (data.containsKey("skinid")) {
			replaceStatement.setInt(19, (int) data.get("skinid"));
		} else if (resultPresent) {
			replaceStatement.setInt(19, result.getInt("ActiveSkinID"));
		} else {
			replaceStatement.setInt(19, 0);
		}

		if (data.containsKey("description")) {
			replaceStatement.setString(20, (String) data.get("description"));
		} else if (resultPresent) {
			replaceStatement.setString(20, result.getString("Description"));
		} else {
			replaceStatement.setString(20, null);
		}

		grabStatement.close();
		return replaceStatement;
	}

	public PreparedStatement getDeleteStatement(int personaID) throws SQLException {
		Connection conn = null;
		PreparedStatement deleteStatement = null;
		conn = getSQLConnection();
		deleteStatement = conn.prepareStatement("DELETE FROM " + SQLTableName + " WHERE PersonaID='" + personaID + "'");
		return deleteStatement;
	}

	public void unlinkSkin(int skinID) {
		try {
			Connection conn = getSQLConnection();
			PreparedStatement grabStatement = conn.prepareStatement("SELECT * FROM " + SQLTableName + " WHERE ActiveSkinID='" + skinID + "'");
			ResultSet result = grabStatement.executeQuery();

			while (result.next()) {
				int personaID = result.getInt("PersonaID");

				Persona pers = plugin.getPersonaHandler().getLoadedPersona(personaID);
				if (pers != null) {
					pers.setSkin(0);
				}

				Map<Object, Object> data = new HashMap<>();
				data.put("personaid", personaID);
				data.put("skinid", 0);

				RPPersonas.get().getSaveQueue().addToQueue(getSaveStatement(data));
			}
		} catch (Exception e) {
			if (RPPersonas.DEBUGGING) {
				e.printStackTrace();
			}
		}
	}

	// GET INFO //
	public Map<String, Object> getBasicPersonaInfo(int personaID) {
		Connection conn = null;
		PreparedStatement ps = null;
		ResultSet rs = null;

		try {
			conn = getSQLConnection();
			String stmt;
			stmt = "SELECT NickName, Name, Age, Race, Gender, Description, PersonaID FROM " + SQLTableName + " WHERE PersonaID='" + personaID + "';";

			ps = conn.prepareStatement(stmt);
			rs = ps.executeQuery();

			Map<String, Object> output = new HashMap<>();

			if (rs.next()) {
				if (rs.getString("NickName") != null && rs.getString("NickName").length() > 0) {
					output.put("nickname", rs.getString("NickName"));
				}
				output.put("name", rs.getString("Name"));
				output.put("age", rs.getLong("Age"));
				output.put("race", rs.getString("Race"));
				output.put("gender", rs.getString("Gender"));

				if (rs.getString("Description") != null && rs.getString("Description").length() > 0) {
					output.put("description", rs.getString("Description"));
				}
			}

			return output;
		} catch (SQLException ex) {
			plugin.getLogger().log(Level.SEVERE, Errors.sqlConnectionExecute(), ex);
		} finally {
			try {
				if (ps != null)
					ps.close();
			} catch (SQLException ex) {
				plugin.getLogger().log(Level.SEVERE, Errors.sqlConnectionClose(), ex);
			}
		}
		return null;
	}

	public Map<String, Object> getLoadingInfo(int personaID) {
		Connection conn = null;
		PreparedStatement ps = null;
		ResultSet rs = null;

		try {
			conn = getSQLConnection();
			String stmt;
			stmt = "SELECT Prefix, NickName, Name, Inventory, EnderChest, LocationWorld, LocationX, LocationY, LocationZ, Health, Hunger, ActiveSkinID, Alive FROM " + SQLTableName + " WHERE PersonaID='" + personaID + "';";

			ps = conn.prepareStatement(stmt);
			rs = ps.executeQuery();

			Map<String, Object> output = new HashMap<>();

			if (rs.next()) {
				if (rs.getString("NickName") != null && rs.getString("NickName").length() > 0) {
					output.put("nickname", rs.getString("NickName"));
				}
				output.put("name", rs.getString("Name"));
				output.put("inventory", rs.getString("Inventory"));
				output.put("enderchest", rs.getString("EnderChest"));

				String world = rs.getString("LocationWorld");
				if (world != null && Bukkit.getWorld(world) != null) {
					output.put("location", new Location(Bukkit.getWorld(world), rs.getDouble("LocationX"), rs.getDouble("LocationY"), rs.getDouble("LocationZ")));
				}

				output.put("health", rs.getDouble("Health"));
				output.put("hunger", rs.getInt("Hunger"));
				output.put("skinid", rs.getInt("ActiveSkinID"));

				if (rs.getShort("Alive") > 0) {
					output.put("alive", new Object());
				}
			}

			return output;
		} catch (SQLException ex) {
			plugin.getLogger().log(Level.SEVERE, Errors.sqlConnectionExecute(), ex);
		} finally {
			try {
				if (ps != null)
					ps.close();
			} catch (SQLException ex) {
				plugin.getLogger().log(Level.SEVERE, Errors.sqlConnectionClose(), ex);
			}
		}
		return null;
	}

	public Map<String, Object> getFullInfo(int personaID) {
		Connection conn = null;
		PreparedStatement ps = null;
		ResultSet rs = null;

		try {
			conn = getSQLConnection();
			String stmt;
			stmt = "SELECT * FROM " + SQLTableName + " WHERE PersonaID='" + personaID + "';";

			ps = conn.prepareStatement(stmt);
			rs = ps.executeQuery();

			Map<String, Object> output = new HashMap<>();

			if (rs.next()) {
				output.put("alive", rs.getBoolean("Alive"));
				output.put("name", rs.getString("Name"));
				output.put("gender", rs.getString("Gender"));
				output.put("age", rs.getLong("Age"));
				output.put("race", rs.getString("Race"));
				output.put("lives", rs.getInt("Lives"));
				output.put("playtime", rs.getLong("Playtime"));

				if (rs.getString("Inventory") != null && rs.getString("Inventory").length() > 0) {
					output.put("inventory", rs.getString("Inventory"));
				}
				if (rs.getString("NickName") != null && rs.getString("NickName").length() > 0) {
					output.put("nickname", rs.getString("NickName"));
				}
				if (rs.getInt("ActiveSkinID") > 0) {
					output.put("skinid", rs.getInt("ActiveSkinID"));
				}
				if (rs.getString("Description") != null && rs.getString("Description").length() > 0) {
					output.put("description", rs.getString("Description"));
				}
			}

			return output;
		} catch (SQLException ex) {
			plugin.getLogger().log(Level.SEVERE, Errors.sqlConnectionExecute(), ex);
		} finally {
			try {
				if (ps != null)
					ps.close();
			} catch (SQLException ex) {
				plugin.getLogger().log(Level.SEVERE, Errors.sqlConnectionClose(), ex);
			}
		}
		return null;
	}

	public int getActiveSkinID(int personaID) {
		Connection conn = null;
		PreparedStatement ps = null;
		ResultSet rs = null;

		try {
			conn = getSQLConnection();
			String stmt;
			stmt = "SELECT ActiveSkinID FROM " + SQLTableName + " WHERE PersonaID='" + personaID + "';";

			ps = conn.prepareStatement(stmt);
			rs = ps.executeQuery();

			int output = -1;

			if (rs.next()) {
				output = rs.getInt("ActiveSKinID");
			}

			return output;
		} catch (SQLException ex) {
			plugin.getLogger().log(Level.SEVERE, Errors.sqlConnectionExecute(), ex);
		} finally {
			try {
				if (ps != null)
					ps.close();
			} catch (SQLException ex) {
				plugin.getLogger().log(Level.SEVERE, Errors.sqlConnectionClose(), ex);
			}
		}
		return 0;
	}

	public String getCurrentName(int personaID) {
		Connection conn = null;
		PreparedStatement ps = null;
		ResultSet rs = null;

		try {
			conn = getSQLConnection();
			String stmt;
			stmt = "SELECT NickName, Name FROM " + SQLTableName + " WHERE PersonaID='" + personaID + "';";

			ps = conn.prepareStatement(stmt);
			rs = ps.executeQuery();

			String output = null;

			if (rs.next()) {
				output = rs.getString("NickName");
				if (output == null || output.length() <= 0) {
					output = rs.getString("Name");
				}
			}

			return output;
		} catch (SQLException ex) {
			plugin.getLogger().log(Level.SEVERE, Errors.sqlConnectionExecute(), ex);
		} finally {
			try {
				if (ps != null)
					ps.close();
			} catch (SQLException ex) {
				plugin.getLogger().log(Level.SEVERE, Errors.sqlConnectionClose(), ex);
			}
		}
		return null;
	}

	public String getName(int personaID) {
		Connection conn = null;
		PreparedStatement ps = null;
		ResultSet rs = null;

		try {
			conn = getSQLConnection();
			String stmt;
			stmt = "SELECT Name FROM " + SQLTableName + " WHERE PersonaID='" + personaID + "';";

			ps = conn.prepareStatement(stmt);
			rs = ps.executeQuery();

			String output = null;

			if (rs.next()) {
				output = rs.getString("Name");
			}

			return output;
		} catch (SQLException ex) {
			plugin.getLogger().log(Level.SEVERE, Errors.sqlConnectionExecute(), ex);
		} finally {
			try {
				if (ps != null)
					ps.close();
			} catch (SQLException ex) {
				plugin.getLogger().log(Level.SEVERE, Errors.sqlConnectionClose(), ex);
			}
		}
		return null;
	}

	public long getAge(int personaID) {
		Connection conn = null;
		PreparedStatement ps = null;
		ResultSet rs = null;

		try {
			conn = getSQLConnection();
			String stmt;
			stmt = "SELECT Age FROM " + SQLTableName + " WHERE PersonaID='" + personaID + "';";

			ps = conn.prepareStatement(stmt);
			rs = ps.executeQuery();

			long output = -1;

			if (rs.next()) {
				output = rs.getLong("Age");
			}

			return output;
		} catch (SQLException ex) {
			plugin.getLogger().log(Level.SEVERE, Errors.sqlConnectionExecute(), ex);
		} finally {
			try {
				if (ps != null)
					ps.close();
			} catch (SQLException ex) {
				plugin.getLogger().log(Level.SEVERE, Errors.sqlConnectionClose(), ex);
			}
		}
		return 0;
	}

	public String getRace(int personaID) {
		Connection conn = null;
		PreparedStatement ps = null;
		ResultSet rs = null;

		try {
			conn = getSQLConnection();
			String stmt;
			stmt = "SELECT Race FROM " + SQLTableName + " WHERE PersonaID='" + personaID + "';";

			ps = conn.prepareStatement(stmt);
			rs = ps.executeQuery();

			String output = null;

			if (rs.next()) {
				output = rs.getString("Race");
			}

			return output;
		} catch (SQLException ex) {
			plugin.getLogger().log(Level.SEVERE, Errors.sqlConnectionExecute(), ex);
		} finally {
			try {
				if (ps != null)
					ps.close();
			} catch (SQLException ex) {
				plugin.getLogger().log(Level.SEVERE, Errors.sqlConnectionClose(), ex);
			}
		}
		return null;
	}

	public String getGender(int personaID) {
		Connection conn = null;
		PreparedStatement ps = null;
		ResultSet rs = null;

		try {
			conn = getSQLConnection();
			String stmt;
			stmt = "SELECT Gender FROM " + SQLTableName + " WHERE PersonaID='" + personaID + "';";

			ps = conn.prepareStatement(stmt);
			rs = ps.executeQuery();

			String output = null;

			if (rs.next()) {
				output = rs.getString("Gender");
			}

			return output;
		} catch (SQLException ex) {
			plugin.getLogger().log(Level.SEVERE, Errors.sqlConnectionExecute(), ex);
		} finally {
			try {
				if (ps != null)
					ps.close();
			} catch (SQLException ex) {
				plugin.getLogger().log(Level.SEVERE, Errors.sqlConnectionClose(), ex);
			}
		}
		return null;
	}

	public Location getLocation(int personaID) {
		Connection conn = null;
		PreparedStatement ps = null;
		ResultSet rs = null;

		try {
			conn = getSQLConnection();
			String stmt;
			stmt = "SELECT LocationWorld,LocationX,LocationY,LocationZ FROM " + SQLTableName + " WHERE PersonaID='" + personaID + "';";

			ps = conn.prepareStatement(stmt);
			rs = ps.executeQuery();

			Location output = null;

			if (rs.next()) {
				String world = rs.getString("LocationWorld");
				if (world != null && Bukkit.getWorld(world) != null) {
					output = new Location(Bukkit.getWorld(world), rs.getDouble("LocationX"), rs.getDouble("LocationY"), rs.getDouble("LocationZ"));
				}
			}

			return output;
		} catch (SQLException ex) {
			plugin.getLogger().log(Level.SEVERE, Errors.sqlConnectionExecute(), ex);
		} finally {
			try {
				if (ps != null)
					ps.close();
			} catch (SQLException ex) {
				plugin.getLogger().log(Level.SEVERE, Errors.sqlConnectionClose(), ex);
			}
		}
		return null;
	}

}
