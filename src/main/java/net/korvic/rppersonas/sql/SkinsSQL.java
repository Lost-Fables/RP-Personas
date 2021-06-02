package net.korvic.rppersonas.sql;

import co.lotc.core.util.DataMapFilter;
import net.korvic.rppersonas.RPPersonas;
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
		String SQLTable = "CREATE TABLE IF NOT EXISTS " + SQL_TABLE_NAME + " (\n" +
						  "    SkinID INT NOT NULL PRIMARY KEY,\n" +
						  "    AccountID INT NOT NULL,\n" +
						  "    Name TEXT NOT NULL,\n" +
						  "    Texture TEXT NOT NULL,\n" +
						  "    Signature TEXT NOT NULL\n" +
						  ");";
		createTable(SQLTable);
	}

	protected boolean customStatement() {
		String stmt = "SELECT * FROM " + SQL_TABLE_NAME + " WHERE SkinID=(SELECT MAX(SkinID) FROM " + SQL_TABLE_NAME + ");";
		try (Connection conn = getSQLConnection();
			 PreparedStatement ps = conn.prepareStatement(stmt);
			 ResultSet rs = ps.executeQuery();) {
			if (rs.next()) {
				updateHighestSkinID(rs.getInt("SkinID"));
			}
		} catch (SQLException ex) {
			plugin.getLogger().log(Level.SEVERE, "Unable to retreive connection", ex);
		}
		return true;
	}

	protected void addDataMappings() {
		DataMapFilter.addFilter(SKINID, Integer.class);
		DataMapFilter.addFilter(ACCOUNTID, Integer.class);
		DataMapFilter.addFilter(NAME, String.class);
		DataMapFilter.addFilter(TEXTURE, String.class);
		DataMapFilter.addFilter(SIGNATURE, String.class);
	}

	private void updateHighestSkinID(int skinID) {
		if (skinID >= highestSkinID) {
			highestSkinID = skinID + 1;
		}
	}

	// Retrieves the amount of tokens a player has, as per our database.
	public Map<Integer, String> getSkinNames(int accountID) {
		String stmt = "SELECT * FROM " + SQL_TABLE_NAME + " WHERE AccountID='" + accountID + "';";
		try (Connection conn = getSQLConnection();
			 PreparedStatement ps = conn.prepareStatement(stmt);
			 ResultSet rs = ps.executeQuery();) {
			Map<Integer, String> result = new HashMap<>();
			while (rs.next()) {
				result.put(rs.getInt("SkinID"), rs.getString("Name"));
			}
			return result;
		} catch (SQLException ex) {
			plugin.getLogger().log(Level.SEVERE, Errors.sqlConnectionExecute(), ex);
		}
		return null;
	}

	public void registerOrUpdate(DataMapFilter data) {
		data.put(SKINID, highestSkinID);
		updateHighestSkinID(highestSkinID);
		try (PreparedStatement stmt = getSaveStatement(data);) {
			plugin.getSaveQueue().executeWithNotification(stmt);
		} catch (SQLException ex) {
			plugin.getLogger().log(Level.SEVERE, Errors.sqlConnectionExecute(), ex);
		}
	}

	public PreparedStatement getSaveStatement(DataMapFilter data) throws SQLException {
		try (Connection conn2 = getSQLConnection();
			 PreparedStatement grabStatement = conn2.prepareStatement("SELECT * FROM " + SQL_TABLE_NAME + " WHERE SkinID='" + data.get(SKINID) + "'");
			 ResultSet result = grabStatement.executeQuery();) {

			boolean resultPresent = result.next();

			try (Connection conn = getSQLConnection();) {
				PreparedStatement replaceStatement = conn.prepareStatement("REPLACE INTO " + SQL_TABLE_NAME + " (SkinID,AccountID,Name,Texture,Signature) VALUES(?,?,?,?,?)");
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
				return replaceStatement;
			}
		}
	}

	public PreparedStatement getDeleteStatement(int skinID) throws SQLException {
		try (Connection conn = getSQLConnection();) {
			return conn.prepareStatement("DELETE FROM " + SQL_TABLE_NAME + " WHERE SkinID='" + skinID + "'");
		} catch (Exception e) {
			if (RPPersonas.DEBUGGING) {
				e.printStackTrace();
			}
		}
		return null;
	}

	public Map<Object, Object> getData(int skinID) {
		String stmt = "SELECT * FROM " + SQL_TABLE_NAME + " WHERE SkinID='" + skinID + "';";
		try (Connection conn = getSQLConnection();
			 PreparedStatement ps = conn.prepareStatement(stmt);
			 ResultSet rs = ps.executeQuery();) {

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
		}
		return null;
	}

	public void moveAllAccounts(int from, int to) {
		try (Connection conn = getSQLConnection();
			 PreparedStatement grabStatement = conn.prepareStatement("SELECT * FROM " + SQL_TABLE_NAME + " WHERE AccountID='" + from + "'");
			 ResultSet result = grabStatement.executeQuery();) {

			while (result.next()) {
				DataMapFilter data = new DataMapFilter();
				data.put(SKINID, result.getInt("SkinID"))
					.put(ACCOUNTID, to);
				registerOrUpdate(data);
			}

			result.close();
		} catch (SQLException ex) {
			plugin.getLogger().log(Level.SEVERE, Errors.sqlConnectionExecute(), ex);
		}
	}
}
