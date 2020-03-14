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
				   "    Inventory TEXT NOT NULL,\n" +
				   "    Lives TINYINT NOT NULL,\n" +
				   "    Playtime BIGINT NOT NULL,\n" +

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

			ps = conn.prepareStatement("REPLACE INTO " + SQLTableName + " (PersonaID,AccountID,Alive,Name,Gender,Age,Race,Inventory,Lives,Playtime,NickName,Prefix,ActiveSkinID,Description) VALUES(?,?,?,?,?,?,?,?,?,?,?,?,?,?)");

			ps.setInt(1, (int) data.get("personaid"));
			ps.setInt(2, (int) data.get("accountid"));
			ps.setByte(3, aliveByte);
			ps.setString(4, (String) data.get("name"));
			ps.setString(5, (String) data.get("gender"));
			ps.setLong(6, (Long) data.get("age"));
			ps.setString(7, (String) data.get("race"));
			ps.setString(8, (String) data.get("inventory"));
			ps.setInt(9, (int) data.get("lives"));
			ps.setLong(10, (long) data.get("playtime"));

			if (data.containsKey("nickname")) {
				ps.setString(11, (String) data.get("nickname"));
			}
			if (data.containsKey("prefix")) {
				ps.setString(12, (String) data.get("prefix"));
			}
			if (data.containsKey("skinid")) {
				ps.setInt(13, (int) data.get("skinid"));
			}
			if (data.containsKey("description")) {
				ps.setString(14, (String) data.get("description"));
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
			stmt = "SELECT Max(PersonaID) FROM " + SQLTableName + " WHERE PersonaID='" + personaID + "';";

			ps = conn.prepareStatement(stmt);
			rs = ps.executeQuery();

			Map<String, Object> output = new HashMap<>();

			if (rs.next()) {
				if (rs.getString("NickName").length() > 0) {
					output.put("nickname", rs.getString("NickName"));
				}
				output.put("name", rs.getString("Name"));
				output.put("age", rs.getLong("Age")); // TODO - Grab actual age.
				output.put("race", rs.getString("Race"));
				output.put("gender", rs.getString("Gender"));

				if (rs.getString("Description").length() > 0) {
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

}
