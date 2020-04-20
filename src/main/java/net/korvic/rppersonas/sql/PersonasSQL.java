package net.korvic.rppersonas.sql;

import co.lotc.core.bukkit.util.InventoryUtil;
import net.korvic.rppersonas.RPPersonas;
import net.korvic.rppersonas.personas.Persona;
import net.korvic.rppersonas.personas.pieces.PersonaGender;
import net.korvic.rppersonas.personas.pieces.PersonaSubRace;
import net.korvic.rppersonas.sql.extras.DataBuffer;
import net.korvic.rppersonas.sql.extras.Errors;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.inventory.Inventory;

import java.sql.*;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Level;

public class PersonasSQL extends BaseSQL {

	private static final String SQL_TABLE_NAME = "rppersonas_personas";
	
	public static final String PERSONAID = "personaid";
	public static final String ALIVE = "alive";
	public static final String NAME = "name";
	public static final String GENDER = "gender";
	public static final String AGE = "age";
	public static final String RACE = "race";
	public static final String LIVES = "lives";
	public static final String PLAYTIME = "playtime";
	public static final String LOCATION = "location";
	public static final String HEALTH = "health";
	public static final String HUNGER = "hunger";
	public static final String INVENTORY = "inventory";
	public static final String ENDERCHEST = "enderchest";
	public static final String NICKNAME = "nickname";
	public static final String PREFIX = "prefix";
	public static final String SKINID = "skinid";
	public static final String DESCRIPTION = "description";

	public PersonasSQL(RPPersonas plugin) {
		if (BaseSQL.plugin == null) {
			BaseSQL.plugin = plugin;
		}

		String SQLTable = "CREATE TABLE IF NOT EXISTS " + SQL_TABLE_NAME + " (\n" +
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
		load(SQLTable, SQL_TABLE_NAME);
	}

	@Override
	protected boolean customStatement() {
		return false;
	}

	protected void addDataMappings() {
		DataBuffer.addMapping(PERSONAID, PERSONAID, Integer.class);
		DataBuffer.addMapping(ALIVE, ALIVE, Boolean.class);
		DataBuffer.addMapping(NAME, NAME, String.class);
		DataBuffer.addMapping(GENDER, GENDER, PersonaGender.class);
		DataBuffer.addMapping(AGE, AGE, Long.class);
		DataBuffer.addMapping(RACE, RACE, PersonaSubRace.class);
		DataBuffer.addMapping(LIVES, LIVES, Integer.class);
		DataBuffer.addMapping(PLAYTIME, PLAYTIME, Long.class);
		DataBuffer.addMapping(LOCATION, LOCATION, Location.class);
		DataBuffer.addMapping(HEALTH, HEALTH, Double.class);
		DataBuffer.addMapping(HUNGER, HUNGER, Integer.class);
		DataBuffer.addMapping(INVENTORY, INVENTORY, Inventory.class);
		DataBuffer.addMapping(ENDERCHEST, ENDERCHEST, Inventory.class);
		DataBuffer.addMapping(NICKNAME, NICKNAME, String.class);
		DataBuffer.addMapping(PREFIX, PREFIX, String.class);
		DataBuffer.addMapping(SKINID, SKINID, Integer.class);
		DataBuffer.addMapping(DESCRIPTION, DESCRIPTION, String.class);
	}

	// Inserts a new mapping for a persona.
	public void registerOrUpdate(Map<Object, Object> data) {
		if (data.containsKey(PERSONAID)) {
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

		grabStatement = conn.prepareStatement("SELECT * FROM " + SQL_TABLE_NAME + " WHERE PersonaID='" + data.get(PERSONAID) + "'");
		ResultSet result = grabStatement.executeQuery();
		boolean resultPresent = result.next();

		conn = getSQLConnection();
		replaceStatement = conn.prepareStatement("REPLACE INTO " + SQL_TABLE_NAME + " (PersonaID,Alive,Name,Gender,Age,Race,Lives,Playtime,LocationWorld,LocationX,LocationY,LocationZ,Health,Hunger,Inventory,EnderChest,NickName,Prefix,ActiveSkinID,Description) VALUES(?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?)");


		// Required
		replaceStatement.setInt(1, (int) data.get(PERSONAID));

		if (data.containsKey(ALIVE)) {
			replaceStatement.setBoolean(2, (boolean) data.get(ALIVE));
		} else if (resultPresent) {
			replaceStatement.setBoolean(2, result.getBoolean("Alive"));
		} else {
			replaceStatement.setBoolean(2, true);
		}

		if (data.containsKey(NAME)) {
			replaceStatement.setString(3, (String) data.get(NAME));
		} else if (resultPresent) {
			replaceStatement.setString(3, result.getString("Name"));
		} else {
			replaceStatement.setString(3, "Lost Name");
		}

		if (data.containsKey(GENDER)) {
			replaceStatement.setString(4, ((PersonaGender) data.get(GENDER)).getName());
		} else if (resultPresent) {
			replaceStatement.setString(4, result.getString("Gender"));
		} else {
			replaceStatement.setString(4, PersonaGender.values()[0].getName());
		}

		if (data.containsKey(AGE)) {
			replaceStatement.setLong(5, (long) data.get(AGE));
		} else if (resultPresent) {
			replaceStatement.setLong(5, result.getLong("Age"));
		} else {
			replaceStatement.setLong(5, 0);
		}

		if (data.containsKey(RACE)) {
			replaceStatement.setString(6, ((PersonaSubRace) data.get(RACE)).getName());
		} else if (resultPresent) {
			replaceStatement.setString(6, result.getString("Race"));
		} else {
			replaceStatement.setString(6, PersonaSubRace.values()[0].getName());
		}

		if (data.containsKey(LIVES)) {
			replaceStatement.setInt(7, (int) data.get(LIVES));
		} else if (resultPresent) {
			replaceStatement.setInt(7, result.getInt("Lives"));
		} else {
			replaceStatement.setInt(7, 3);
		}

		if (data.containsKey(PLAYTIME)) {
			replaceStatement.setLong(8, (long) data.get(PLAYTIME));
		} else if (resultPresent) {
			replaceStatement.setLong(8, result.getLong("Playtime"));
		} else {
			replaceStatement.setLong(8, 0);
		}


		// Location
		Location loc = null;
		if (data.containsKey(LOCATION)) {
			loc = (Location) data.get(LOCATION);
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
		if (data.containsKey(HEALTH)) {
			replaceStatement.setDouble(13, (double) data.get(HEALTH));
		} else if (resultPresent) {
			replaceStatement.setDouble(13, result.getDouble("Health"));
		} else {
			replaceStatement.setDouble(13, 20.0);
		}

		if (data.containsKey(HUNGER)) {
			replaceStatement.setInt(14, (int) data.get(HUNGER));
		} else if (resultPresent) {
			replaceStatement.setInt(14, result.getInt("Hunger"));
		} else {
			replaceStatement.setInt(14, 20);
		}

		// Optional
		if (data.containsKey(INVENTORY)) {
			replaceStatement.setString(15, InventoryUtil.serializeItems((Inventory) data.get(INVENTORY)));
		} else if (resultPresent) {
			replaceStatement.setString(15, result.getString("Inventory"));
		} else {
			replaceStatement.setString(15, null);
		}

		if (data.containsKey(ENDERCHEST)) {
			replaceStatement.setString(16, InventoryUtil.serializeItems((Inventory) data.get(ENDERCHEST)));
		} else if (resultPresent) {
			replaceStatement.setString(16, result.getString("EnderChest"));
		} else {
			replaceStatement.setString(16, null);
		}

		if (data.containsKey(NICKNAME)) {
			replaceStatement.setString(17, (String) data.get(NICKNAME));
		} else if (resultPresent) {
			replaceStatement.setString(17, result.getString("NickName"));
		} else {
			replaceStatement.setString(17, null);
		}

		if (data.containsKey(PREFIX)) {
			replaceStatement.setString(18, (String) data.get(PREFIX));
		} else if (resultPresent) {
			replaceStatement.setString(18, result.getString("Prefix"));
		} else {
			replaceStatement.setString(18, null);
		}

		if (data.containsKey(SKINID)) {
			replaceStatement.setInt(19, (int) data.get(SKINID));
		} else if (resultPresent) {
			replaceStatement.setInt(19, result.getInt("ActiveSkinID"));
		} else {
			replaceStatement.setInt(19, 0);
		}

		if (data.containsKey(DESCRIPTION)) {
			replaceStatement.setString(20, (String) data.get(DESCRIPTION));
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
		deleteStatement = conn.prepareStatement("DELETE FROM " + SQL_TABLE_NAME + " WHERE PersonaID='" + personaID + "'");
		return deleteStatement;
	}

	public void unlinkSkin(int skinID) {
		try {
			Connection conn = getSQLConnection();
			PreparedStatement grabStatement = conn.prepareStatement("SELECT * FROM " + SQL_TABLE_NAME + " WHERE ActiveSkinID='" + skinID + "'");
			ResultSet result = grabStatement.executeQuery();

			while (result.next()) {
				int personaID = result.getInt("PersonaID");

				Persona pers = plugin.getPersonaHandler().getLoadedPersona(personaID);
				if (pers != null) {
					pers.setSkin(0);
				}

				Map<Object, Object> data = new HashMap<>();
				data.put(PERSONAID, personaID);
				data.put(SKINID, 0);

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
			stmt = "SELECT NickName, Name, Age, Race, Gender, Description, PersonaID FROM " + SQL_TABLE_NAME + " WHERE PersonaID='" + personaID + "';";

			ps = conn.prepareStatement(stmt);
			rs = ps.executeQuery();

			Map<String, Object> output = new HashMap<>();

			if (rs.next()) {
				if (rs.getString("NickName") != null && rs.getString("NickName").length() > 0) {
					output.put(NICKNAME, rs.getString("NickName"));
				}
				output.put(NAME, rs.getString("Name"));
				output.put(AGE, rs.getLong("Age"));
				output.put(RACE, rs.getString("Race"));
				output.put(GENDER, rs.getString("Gender"));

				if (rs.getString("Description") != null && rs.getString("Description").length() > 0) {
					output.put(DESCRIPTION, rs.getString("Description"));
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
			stmt = "SELECT Prefix, NickName, Name, Inventory, EnderChest, LocationWorld, LocationX, LocationY, LocationZ, Health, Hunger, ActiveSkinID, Alive FROM " + SQL_TABLE_NAME + " WHERE PersonaID='" + personaID + "';";

			ps = conn.prepareStatement(stmt);
			rs = ps.executeQuery();

			Map<String, Object> output = new HashMap<>();

			if (rs.next()) {
				if (rs.getString("NickName") != null && rs.getString("NickName").length() > 0) {
					output.put(NICKNAME, rs.getString("NickName"));
				}
				output.put(NAME, rs.getString("Name"));
				output.put(INVENTORY, rs.getString("Inventory"));
				output.put(ENDERCHEST, rs.getString("EnderChest"));

				String world = rs.getString("LocationWorld");
				if (world != null && Bukkit.getWorld(world) != null) {
					output.put(LOCATION, new Location(Bukkit.getWorld(world), rs.getDouble("LocationX"), rs.getDouble("LocationY"), rs.getDouble("LocationZ")));
				}

				output.put(HEALTH, rs.getDouble("Health"));
				output.put(HUNGER, rs.getInt("Hunger"));
				output.put(SKINID, rs.getInt("ActiveSkinID"));

				if (rs.getShort("Alive") > 0) {
					output.put(ALIVE, new Object());
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
			stmt = "SELECT * FROM " + SQL_TABLE_NAME + " WHERE PersonaID='" + personaID + "';";

			ps = conn.prepareStatement(stmt);
			rs = ps.executeQuery();

			Map<String, Object> output = new HashMap<>();

			if (rs.next()) {
				output.put(ALIVE, rs.getBoolean("Alive"));
				output.put(NAME, rs.getString("Name"));
				output.put(GENDER, rs.getString("Gender"));
				output.put(AGE, rs.getLong("Age"));
				output.put(RACE, rs.getString("Race"));
				output.put(LIVES, rs.getInt("Lives"));
				output.put(PLAYTIME, rs.getLong("Playtime"));

				if (rs.getString("Inventory") != null && rs.getString("Inventory").length() > 0) {
					output.put(INVENTORY, rs.getString("Inventory"));
				}
				if (rs.getString("NickName") != null && rs.getString("NickName").length() > 0) {
					output.put(NICKNAME, rs.getString("NickName"));
				}
				if (rs.getInt("ActiveSkinID") > 0) {
					output.put(SKINID, rs.getInt("ActiveSkinID"));
				}
				if (rs.getString("Description") != null && rs.getString("Description").length() > 0) {
					output.put(DESCRIPTION, rs.getString("Description"));
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
			stmt = "SELECT ActiveSkinID FROM " + SQL_TABLE_NAME + " WHERE PersonaID='" + personaID + "';";

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
			stmt = "SELECT NickName, Name FROM " + SQL_TABLE_NAME + " WHERE PersonaID='" + personaID + "';";

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
			stmt = "SELECT Name FROM " + SQL_TABLE_NAME + " WHERE PersonaID='" + personaID + "';";

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
			stmt = "SELECT Age FROM " + SQL_TABLE_NAME + " WHERE PersonaID='" + personaID + "';";

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
			stmt = "SELECT Race FROM " + SQL_TABLE_NAME + " WHERE PersonaID='" + personaID + "';";

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
			stmt = "SELECT Gender FROM " + SQL_TABLE_NAME + " WHERE PersonaID='" + personaID + "';";

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
			stmt = "SELECT LocationWorld,LocationX,LocationY,LocationZ FROM " + SQL_TABLE_NAME + " WHERE PersonaID='" + personaID + "';";

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
