package net.korvic.rppersonas.sql;

import net.korvic.rppersonas.RPPersonas;
import org.bukkit.scheduler.BukkitRunnable;

import java.sql.*;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Level;

public class PersonasSQL {

	public static Connection connection;

	private RPPersonas plugin;
	private String SQLTable;
	private String SQLTableName = "rppersonas_personas";

	public PersonasSQL(RPPersonas plugin) {
		this.plugin = plugin;
		SQLTable = "CREATE TABLE IF NOT EXISTS " + SQLTableName + " (\n" +
				   "    PersonaID INT NOT NULL PRIMARY KEY,\n" +
				   "    Alive TINYINT NOT NULL,\n" +
				   "    Name TEXT NOT NULL,\n" +
				   "    Gender TEXT NOT NULL,\n" +
				   "    Age BIGINT NOT NULL,\n" +
				   "    Race TEXT NOT NULL,\n" +
				   "    Lives INT NOT NULL,\n" +
				   "    Playtime BIGINT NOT NULL,\n" +

				   "    Inventory TEXT,\n" +
				   "    NickName TEXT,\n" +
				   "    Prefix TEXT,\n" +
				   "    ActiveSkinID INT,\n" +
				   "    Description TEXT\n" +
				   ");";
	}

	public void load() {
		connection = getSQLConnection();
		try {
			Statement s = connection.createStatement();
			s.execute(SQLTable);
			s.close();
		} catch (SQLException e) {
			e.printStackTrace();
		}
		initialize();
		runConnectionMaintainer();
	}

	public void initialize(){
		connection = getSQLConnection();
		try {
			String stmt;
			stmt = "SELECT * FROM " + SQLTableName + ";";
			PreparedStatement ps = connection.prepareStatement(stmt);
			ResultSet rs = ps.executeQuery();
			close(ps, rs);
		} catch (SQLException ex) {
			plugin.getLogger().log(Level.SEVERE, "Unable to retreive connection", ex);
		}
	}

	public Connection getSQLConnection() {
		String host = RPPersonas.config.getString("mysql.host");
		String port = RPPersonas.config.getString("mysql.port");
		String database = RPPersonas.config.getString("mysql.database");
		String user = RPPersonas.config.getString("mysql.user");
		String password = RPPersonas.config.getString("mysql.password");

		try {
			if (connection != null && !connection.isClosed()) {
				return connection;
			}
			String url = "jdbc:mysql://" + host + ":" + port + "/" + database + "?useSSL=false";
			return DriverManager.getConnection(url, user, password);
		} catch (SQLException ex) {
			if (RPPersonas.DEBUGGING) {
				plugin.getLogger().log(Level.SEVERE, "MySQL exception on initialize", ex);
			}
		}

		return null;
	}

	private void runConnectionMaintainer() {
		(new BukkitRunnable() {
			@Override
			public void run() {
				try {
					if (connection != null && !connection.isClosed()) {
						connection.createStatement().execute("SELECT 1");
					}
				} catch (SQLException e) {
					connection = getSQLConnection();
				}
			}
		}).runTaskTimerAsynchronously(plugin, 60 * 20, 60 * 20);
	}

	private void close(PreparedStatement ps, ResultSet rs){
		try {
			if (ps != null)
				ps.close();
			if (rs != null)
				rs.close();
		} catch (SQLException ex) {
			Errors.close(plugin, ex);
		}
	}

	// Inserts a new mapping for a persona.
	public void register(Map<Object, Object> data) {
		Connection conn = null;
		PreparedStatement ps = null;
		try {
			conn = getSQLConnection();
			byte aliveByte = (byte) 0;
			if (data.containsKey("alive")) {
				aliveByte = (byte) 1;
			}

			ps = conn.prepareStatement("REPLACE INTO " + SQLTableName + " (PersonaID,Alive,Name,Gender,Age,Race,Lives,Playtime,Inventory,NickName,Prefix,ActiveSkinID,Description) VALUES(?,?,?,?,?,?,?,?,?,?,?,?,?)");

			ps.setInt(1, (int) data.get("personaid"));
			ps.setByte(2, aliveByte);
			ps.setString(3, (String) data.get("name"));
			ps.setString(4, (String) data.get("gender"));
			ps.setLong(5, (long) data.get("age"));
			ps.setString(6, (String) data.get("race"));
			ps.setInt(7, (int) data.get("lives"));
			ps.setLong(8, (long) data.get("playtime"));

			if (data.containsKey("inventory")) {
				ps.setString(9, (String) data.get("inventory"));
			} else {
				ps.setString(9, null);
			}

			if (data.containsKey("nickname")) {
				ps.setString(10, (String) data.get("nickname"));
			} else {
				ps.setString(10, null);
			}

			if (data.containsKey("prefix")) {
				ps.setString(11, (String) data.get("prefix"));
			} else {
				ps.setString(11, null);
			}

			if (data.containsKey("skinid")) {
				ps.setInt(12, (int) data.get("skinid"));
			} else {
				ps.setInt(12, -1);
			}

			if (data.containsKey("description")) {
				ps.setString(13, (String) data.get("description"));
			} else {
				ps.setString(13, null);
			}

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
				output.put("age", rs.getLong("Age")); // TODO - Grab actual age.
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
			stmt = "SELECT Prefix, NickName, Name, Inventory, ActiveSkinID, Alive FROM " + SQLTableName + " WHERE PersonaID='" + personaID + "';";

			ps = conn.prepareStatement(stmt);
			rs = ps.executeQuery();

			Map<String, Object> output = new HashMap<>();

			if (rs.next()) {
				if (rs.getString("NickName") != null && rs.getString("NickName").length() > 0) {
					output.put("nickname", rs.getString("NickName"));
				}
				output.put("name", rs.getString("Name"));
				output.put("inventory", rs.getString("Inventory"));
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

}
