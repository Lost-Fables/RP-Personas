package net.korvic.rppersonas.sql;

import net.korvic.rppersonas.RPPersonas;
import org.bukkit.scheduler.BukkitRunnable;

import java.sql.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;

public class SkinsSQL {

	public static Connection connection;

	private RPPersonas plugin;
	private String SQLTable;
	private String SQLTableName = "rppersonas_saved_skins";
	private int highestSkinID = 1;

	public SkinsSQL(RPPersonas plugin) {
		this.plugin = plugin;
		SQLTable = "CREATE TABLE IF NOT EXISTS " + SQLTableName + " (\n" +
				   "    SkinID INT NOT NULL PRIMARY KEY,\n" +
				   "    AccountID INT NOT NULL,\n" +
				   "    Name TEXT NOT NULL,\n" +
				   "    Texture TEXT NOT NULL\n" +
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
			stmt = "SELECT * FROM " + SQLTableName + " WHERE SkinID=(SELECT MAX(SkinID) FROM " + SQLTableName + ");";
			PreparedStatement ps = connection.prepareStatement(stmt);
			ResultSet rs = ps.executeQuery();
			if (rs.next()) {
				updateHighestSkinID(rs.getInt("SkinID"));
			}
			close(ps, rs);
		} catch (SQLException ex) {
			plugin.getLogger().log(Level.SEVERE, "Unable to retreive connection", ex);
		}
	}

	private void updateHighestSkinID(int skinID) {
		if (skinID >= highestSkinID) {
			highestSkinID = skinID + 1;
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

	// Retrieves the amount of tokens a player has, as per our database.
	public Map<Integer, String> getSkinNames(int accountID) {
		Connection conn = null;
		PreparedStatement ps = null;
		ResultSet rs = null;

		try {
			conn = getSQLConnection();
			String stmt;
			stmt = "SELECT * FROM " + SQLTableName + " WHERE AccountID='" + accountID + "';";

			ps = conn.prepareStatement(stmt);
			rs = ps.executeQuery();

			Map<Integer, String> result = new HashMap<>();
			while (rs.next()) {
				result.put(rs.getInt("SkinID"), rs.getString("Name"));
			}
			return result;
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

	public String getTexture(int skinID) {
		Connection conn = null;
		PreparedStatement ps = null;
		ResultSet rs = null;

		try {
			conn = getSQLConnection();
			String stmt;
			stmt = "SELECT Texture FROM " + SQLTableName + " WHERE SkinID='" + skinID + "';";

			ps = conn.prepareStatement(stmt);
			rs = ps.executeQuery();

			String output = null;
			if (rs.next()) {
				output = rs.getString("Texture");
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

	public void addSkin(int accountID, String texture, String name) {
		if (texture != null && accountID > 0) {
			PreparedStatement ps = null;
			Map<Object, Object> data = new HashMap<>();
			data.put("texture", texture);
			data.put("name", name);
			data.put("accountid", accountID);
			data.put("skinid", highestSkinID);
			updateHighestSkinID(highestSkinID);
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

		grabStatement = conn.prepareStatement("SELECT * FROM " + SQLTableName + " WHERE SkinID='" + data.get("skinid") + "'");
		ResultSet result = grabStatement.executeQuery();
		boolean resultPresent = result.next();

		conn = getSQLConnection();
		replaceStatement = conn.prepareStatement("REPLACE INTO " + SQLTableName + " (SkinID,AccountID,Name,Texture) VALUES(?,?,?,?)");


		// Required
		replaceStatement.setInt(1, (int) data.get("skinid"));

		if (data.containsKey("accountid")) {
			replaceStatement.setInt(2, (int) data.get("accountid"));
		} else if (resultPresent) {
			replaceStatement.setInt(2, result.getInt("AccountID"));
		} else {
			replaceStatement.setInt(2, 0);
		}

		if (data.containsKey("name")) {
			replaceStatement.setString(3, (String) data.get("name"));
		} else if (resultPresent) {
			replaceStatement.setString(3, result.getString("Name"));
		} else {
			replaceStatement.setString(3, null);
		}

		if (data.containsKey("texture")) {
			replaceStatement.setString(3, (String) data.get("texture"));
		} else if (resultPresent) {
			replaceStatement.setString(3, result.getString("Texture"));
		} else {
			replaceStatement.setString(3, null);
		}

		grabStatement.close();
		return replaceStatement;
	}
}
