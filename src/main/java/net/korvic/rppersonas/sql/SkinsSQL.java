package net.korvic.rppersonas.sql;

import net.korvic.rppersonas.RPPersonas;

import java.sql.*;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Level;

public class SkinsSQL extends BaseSQL {

	private static final String SQLTableName = "rppersonas_saved_skins";
	private int highestSkinID = 1;

	public SkinsSQL(RPPersonas plugin) {
		if (BaseSQL.plugin == null) {
			BaseSQL.plugin = plugin;
		}

		String SQLTable = "CREATE TABLE IF NOT EXISTS " + SQLTableName + " (\n" +
						  "    SkinID INT NOT NULL PRIMARY KEY,\n" +
						  "    AccountID INT NOT NULL,\n" +
						  "    Name TEXT NOT NULL,\n" +
						  "    Texture TEXT NOT NULL,\n" +
						  "    Signature TEXT NOT NULL\n" +
						  ");";
		load(SQLTable, SQLTableName);
	}

	@Override
	protected boolean customStatement() {
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

		return true;
	}

	private void updateHighestSkinID(int skinID) {
		if (skinID >= highestSkinID) {
			highestSkinID = skinID + 1;
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

	public void addSkin(int accountID, String name, String texture, String signature) {
		if (accountID > 0 && texture != null && signature != null) {
			Map<Object, Object> data = new HashMap<>();
			data.put("accountid", accountID);
			data.put("name", name);
			data.put("texture", texture);
			data.put("signature", signature);
			addSkin(data);
		}
	}
	public void addSkin(Map<Object, Object> data) {
		PreparedStatement ps = null;
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

	public PreparedStatement getSaveStatement(Map<Object, Object> data) throws SQLException {
		Connection conn = null;
		PreparedStatement grabStatement = null;
		PreparedStatement replaceStatement = null;
		conn = getSQLConnection();

		grabStatement = conn.prepareStatement("SELECT * FROM " + SQLTableName + " WHERE SkinID='" + data.get("skinid") + "'");
		ResultSet result = grabStatement.executeQuery();
		boolean resultPresent = result.next();

		conn = getSQLConnection();
		replaceStatement = conn.prepareStatement("REPLACE INTO " + SQLTableName + " (SkinID,AccountID,Name,Texture,Signature) VALUES(?,?,?,?,?)");


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
			replaceStatement.setString(4, (String) data.get("texture"));
		} else if (resultPresent) {
			replaceStatement.setString(4, result.getString("Texture"));
		} else {
			replaceStatement.setString(4, null);
		}

		if (data.containsKey("signature")) {
			replaceStatement.setString(5, (String) data.get("signature"));
		} else if (resultPresent) {
			replaceStatement.setString(5, result.getString("Signature"));
		} else {
			replaceStatement.setString(5, null);
		}

		grabStatement.close();
		return replaceStatement;
	}

	public PreparedStatement getDeleteStatement(int skinID) throws SQLException {
		Connection conn = null;
		PreparedStatement deleteStatement = null;
		conn = getSQLConnection();
		deleteStatement = conn.prepareStatement("DELETE FROM " + SQLTableName + " WHERE SkinID='" + skinID + "'");
		return deleteStatement;
	}

	public Map<Object, Object> getData(int skinID) {
		Connection conn = null;
		PreparedStatement ps = null;
		ResultSet rs = null;

		try {
			conn = getSQLConnection();
			String stmt;
			stmt = "SELECT * FROM " + SQLTableName + " WHERE SkinID='" + skinID + "';";

			ps = conn.prepareStatement(stmt);
			rs = ps.executeQuery();

			Map<Object, Object> output = new HashMap<>();
			if (rs.next()) {
				output.put("skinid", skinID);
				output.put("accountid", rs.getInt("AccountID"));
				output.put("name", rs.getString("Name"));
				output.put("texture", rs.getString("Texture"));
				output.put("signature", rs.getString("Signature"));
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
