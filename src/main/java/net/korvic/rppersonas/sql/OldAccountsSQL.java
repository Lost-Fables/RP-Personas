package net.korvic.rppersonas.sql;

import net.korvic.rppersonas.RPPersonas;
import net.korvic.rppersonas.sql.util.DataMapFilter;
import net.korvic.rppersonas.sql.util.Errors;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Level;

public class OldAccountsSQL extends BaseSQL {

	private static final String SQL_TABLE_NAME = "rppersonas_accounts";

	public static final String ACCOUNTID = "accountid";
	public static final String OLD_ACCOUNTID = "old-accountid";
	public static final String DISCORDID = "discordid";
	public static final String PLAYTIME = "playtime";
	public static final String VOTES = "votes";

	public OldAccountsSQL(RPPersonas plugin) {
		if (BaseSQL.plugin == null) {
			BaseSQL.plugin = plugin;
		}

		String SQLTable = "CREATE TABLE IF NOT EXISTS " + SQL_TABLE_NAME + " (\n" +
						  "    AccountID INT NOT NULL PRIMARY KEY,\n" +
						  "    DiscordID TEXT,\n" +
						  "    Playtime BIGINT NOT NULL,\n" +
						  "    Votes SMALLINT NOT NULL\n" +
						  ");";
		this.load(SQLTable, SQL_TABLE_NAME);
	}

	@Override
	protected boolean customStatement() {
		return false;
	}

	protected void addDataMappings() {
		DataMapFilter.addFilter(ACCOUNTID, ACCOUNTID, Integer.class);
		DataMapFilter.addFilter(OLD_ACCOUNTID, OLD_ACCOUNTID, Integer.class);
		DataMapFilter.addFilter(DISCORDID, DISCORDID, String.class);
		DataMapFilter.addFilter(PLAYTIME, PLAYTIME, Long.class);
		DataMapFilter.addFilter(VOTES, VOTES, Short.class);
	}

	// Checks if this account is already registered.
	public boolean isRegistered(int accountID) {
		Connection conn = null;
		PreparedStatement ps = null;
		ResultSet rs = null;

		try {
			conn = getSQLConnection();
			String stmt;
			stmt = "SELECT * FROM " + SQL_TABLE_NAME + " WHERE AccountID='" + accountID + "';";

			ps = conn.prepareStatement(stmt);
			rs = ps.executeQuery();

			boolean result = false;
			if (rs.next()) {
				result = true;
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
		return false;
	}

	// Gets our stored data for this account.
	public Map<String, Object> getData(int accountid) {
		Connection conn = null;
		PreparedStatement ps = null;
		conn = getSQLConnection();

		try {
			ps = conn.prepareStatement("SELECT * FROM " + SQL_TABLE_NAME + " WHERE AccountID='" + accountid + "'");
			ResultSet rs = ps.executeQuery();

			Map<String, Object> data = new HashMap<>();

			if (rs.next()) {
				data.put(ACCOUNTID, accountid);
				data.put(DISCORDID, rs.getString("DiscordID"));
				data.put(PLAYTIME, rs.getLong("Playtime"));
				data.put(VOTES, rs.getShort("Votes"));
			}

			return data;
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

	// Updates or Inserts a new mapping for an account.
	public void registerOrUpdate(DataMapFilter data) {
		if (data.containsKey(ACCOUNTID)) {
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

		int currentAccount = (int) ((data.containsKey(OLD_ACCOUNTID)) ? data.get(OLD_ACCOUNTID) : data.get(ACCOUNTID));
		grabStatement = conn.prepareStatement("SELECT * FROM " + SQL_TABLE_NAME + " WHERE AccountID='" + currentAccount + "'");
		ResultSet result = grabStatement.executeQuery();
		boolean resultPresent = result.next();

		conn = getSQLConnection();
		replaceStatement = conn.prepareStatement("REPLACE INTO " + SQL_TABLE_NAME + " (AccountID,DiscordID,Playtime,Votes) VALUES(?,?,?,?)");


		// Required
		replaceStatement.setInt(1, (int) data.get(ACCOUNTID));

		if (data.containsKey(DISCORDID)) {
			replaceStatement.setString(2, (String) data.get(DISCORDID));
		} else if (resultPresent) {
			replaceStatement.setString(2, result.getString("DiscordID"));
		} else {
			replaceStatement.setString(2, null);
		}

		if (data.containsKey(PLAYTIME)) {
			long playtime = (long) data.get(PLAYTIME);
			if (resultPresent) {
				playtime += result.getLong("Playtime");
			}
			replaceStatement.setLong(3, playtime);
		} else if (resultPresent) {
			replaceStatement.setLong(3, result.getLong("Playtime"));
		} else {
			replaceStatement.setLong(3, 0);
		}

		if (data.containsKey(VOTES)) {
			replaceStatement.setShort(4, (short) data.get(VOTES));
		} else if (resultPresent) {
			replaceStatement.setShort(4, result.getShort("Votes"));
		} else {
			replaceStatement.setShort(4, (short) 0);
		}

		grabStatement.close();
		return replaceStatement;
	}

	public void incrementVotes(int accountID) {
		DataMapFilter data = new DataMapFilter();
		data.putAll(getData(accountID))
			.put(VOTES, ((short) data.get(VOTES)) + 1);
		try {
			plugin.getSaveQueue().addToQueue(getSaveStatement(data));
		} catch (Exception e) {
			if (RPPersonas.DEBUGGING) {
				e.printStackTrace();
			}
		}
	}

	public void moveAllAccounts(int from, int to) {
		Connection conn = getSQLConnection();
		PreparedStatement grabStatement = null;
		PreparedStatement deleteStatement = null;
		try {
			grabStatement = conn.prepareStatement("SELECT * FROM " + SQL_TABLE_NAME + " WHERE AccountID='" + from + "'");

			ResultSet result = grabStatement.executeQuery();

			if (result.next()) {
				DataMapFilter data = new DataMapFilter();
				data.put(ACCOUNTID, to)
					.put(OLD_ACCOUNTID, from);
				getSaveStatement(data).executeUpdate();

				conn = getSQLConnection();
				deleteStatement = conn.prepareStatement("DELETE FROM " + SQL_TABLE_NAME + " WHERE AccountID='" + from + "'");
				deleteStatement.executeUpdate();
			}

			result.close();
		} catch (SQLException ex) {
			plugin.getLogger().log(Level.SEVERE, Errors.sqlConnectionExecute(), ex);
		} finally {
			try {
				if (grabStatement != null)
					grabStatement.close();
				if (deleteStatement != null) {
					deleteStatement.close();
				}
			} catch (SQLException ex) {
				plugin.getLogger().log(Level.SEVERE, Errors.sqlConnectionClose(), ex);
			}
		}
	}
}
