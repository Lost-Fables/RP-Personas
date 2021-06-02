package net.korvic.rppersonas.sql;

import net.korvic.rppersonas.RPPersonas;
import net.korvic.rppersonas.sql.util.DataMapFilter;
import net.korvic.rppersonas.sql.util.Errors;

import java.sql.*;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Level;

public class SkinsSQL extends BaseSQL {

	private static final String SQL_TABLE_NAME = "rppersonas_saved_skins";
	
	public static final String SKINID = "skinid";
	public static final String ACCOUNTID = "accountid";
	public static final String NAME = "name";
	public static final String TEXTURE = "texture";
	public static final String SIGNATURE = "signature";

	private int highestSkinID = 1;

	public SkinsSQL(RPPersonas plugin) {
		if (BaseSQL.plugin == null) {
			BaseSQL.plugin = plugin;
		}

		String SQLTable = "CREATE TABLE IF NOT EXISTS " + SQL_TABLE_NAME + " (\n" +
						  "    SkinID INT NOT NULL PRIMARY KEY,\n" +
						  "    AccountID INT NOT NULL,\n" +
						  "    Name TEXT NOT NULL,\n" +
						  "    Texture TEXT NOT NULL,\n" +
						  "    Signature TEXT NOT NULL\n" +
						  ");";
		load(SQLTable, SQL_TABLE_NAME);
	}

	@Override
	protected boolean customStatement() {
		connection = getSQLConnection();
		try {
			String stmt;
			stmt = "SELECT * FROM " + SQL_TABLE_NAME + " WHERE SkinID=(SELECT MAX(SkinID) FROM " + SQL_TABLE_NAME + ");";
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

	protected void addDataMappings() {
		DataMapFilter.addFilter(SKINID, SKINID, Integer.class);
		DataMapFilter.addFilter(ACCOUNTID, ACCOUNTID, Integer.class);
		DataMapFilter.addFilter(NAME, NAME, String.class);
		DataMapFilter.addFilter(TEXTURE, TEXTURE, String.class);
		DataMapFilter.addFilter(SIGNATURE, SIGNATURE, String.class);
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
			stmt = "SELECT * FROM " + SQL_TABLE_NAME + " WHERE AccountID='" + accountID + "';";

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

	public void registerOrUpdate(DataMapFilter data) {
		data.put(SKINID, highestSkinID);
		updateHighestSkinID(highestSkinID);
		try {
			plugin.getSaveQueue().executeWithNotification(getSaveStatement(data));
		} catch (SQLException ex) {
			plugin.getLogger().log(Level.SEVERE, Errors.sqlConnectionExecute(), ex);
		}
	}

	public PreparedStatement getSaveStatement(DataMapFilter data) throws SQLException {
		Connection conn = null;
		PreparedStatement grabStatement = null;
		PreparedStatement replaceStatement = null;
		conn = getSQLConnection();

		grabStatement = conn.prepareStatement("SELECT * FROM " + SQL_TABLE_NAME + " WHERE SkinID='" + data.get(SKINID) + "'");
		ResultSet result = grabStatement.executeQuery();
		boolean resultPresent = result.next();

		conn = getSQLConnection();
		replaceStatement = conn.prepareStatement("REPLACE INTO " + SQL_TABLE_NAME + " (SkinID,AccountID,Name,Texture,Signature) VALUES(?,?,?,?,?)");


		// Required
		replaceStatement.setInt(1, (int) data.get(SKINID));

		if (data.containsKey(ACCOUNTID)) {
			replaceStatement.setInt(2, (int) data.get(ACCOUNTID));
		} else if (resultPresent) {
			replaceStatement.setInt(2, result.getInt("AccountID"));
		} else {
			replaceStatement.setInt(2, 0);
		}

		if (data.containsKey(NAME)) {
			replaceStatement.setString(3, (String) data.get(NAME));
		} else if (resultPresent) {
			replaceStatement.setString(3, result.getString("Name"));
		} else {
			replaceStatement.setString(3, null);
		}

		if (data.containsKey(TEXTURE)) {
			replaceStatement.setString(4, (String) data.get(TEXTURE));
		} else if (resultPresent) {
			replaceStatement.setString(4, result.getString("Texture"));
		} else {
			replaceStatement.setString(4, null);
		}

		if (data.containsKey(SIGNATURE)) {
			replaceStatement.setString(5, (String) data.get(SIGNATURE));
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
		deleteStatement = conn.prepareStatement("DELETE FROM " + SQL_TABLE_NAME + " WHERE SkinID='" + skinID + "'");
		return deleteStatement;
	}

	public Map<Object, Object> getData(int skinID) {
		Connection conn = null;
		PreparedStatement ps = null;
		ResultSet rs = null;

		try {
			conn = getSQLConnection();
			String stmt;
			stmt = "SELECT * FROM " + SQL_TABLE_NAME + " WHERE SkinID='" + skinID + "';";

			ps = conn.prepareStatement(stmt);
			rs = ps.executeQuery();

			Map<Object, Object> output = new HashMap<>();
			if (rs.next()) {
				output.put(SKINID, skinID);
				output.put(ACCOUNTID, rs.getInt("AccountID"));
				output.put(NAME, rs.getString("Name"));
				output.put(TEXTURE, rs.getString("Texture"));
				output.put(SIGNATURE, rs.getString("Signature"));
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

	public void moveAllAccounts(int from, int to) {
		Connection conn = getSQLConnection();
		PreparedStatement grabStatement = null;
		try {
			grabStatement = conn.prepareStatement("SELECT * FROM " + SQL_TABLE_NAME + " WHERE AccountID='" + from + "'");

			ResultSet result = grabStatement.executeQuery();

			while (result.next()) {
				DataMapFilter data = new DataMapFilter();
				data.put(SKINID, result.getInt("SkinID"))
					.put(ACCOUNTID, to);
				registerOrUpdate(data);
			}

			result.close();
		} catch (SQLException ex) {
			plugin.getLogger().log(Level.SEVERE, Errors.sqlConnectionExecute(), ex);
		} finally {
			try {
				if (grabStatement != null)
					grabStatement.close();
			} catch (SQLException ex) {
				plugin.getLogger().log(Level.SEVERE, Errors.sqlConnectionClose(), ex);
			}
		}
	}
}
