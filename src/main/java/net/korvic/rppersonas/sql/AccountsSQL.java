package net.korvic.rppersonas.sql;

import co.lotc.core.util.DataMapFilter;
import net.korvic.rppersonas.RPPersonas;
import net.korvic.rppersonas.sql.util.Errors;
import net.korvic.rppersonas.sql.util.SaveTracker;

import java.sql.*;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Level;

public class AccountsSQL extends BaseSQL {

	private static final String SQL_TABLE_NAME = "rppersonas_accounts";

	public static final String ACCOUNTID = "accountid";
	public static final String OLD_ACCOUNTID = "old-accountid";
	public static final String DISCORDID = "discordid";
	public static final String PLAYTIME = "playtime";
	public static final String VOTES = "votes";

	public AccountsSQL(RPPersonas plugin) {
		String SQLTable = "CREATE TABLE IF NOT EXISTS " + SQL_TABLE_NAME + " (\n" +
						  "    AccountID INT NOT NULL PRIMARY KEY,\n" +
						  "    DiscordID TEXT,\n" +
						  "    Playtime BIGINT NOT NULL,\n" +
						  "    Votes SMALLINT NOT NULL\n" +
						  ");";
		createTable(SQLTable);
		addDataMappings();
	}

	protected void addDataMappings() {
		DataMapFilter.addFilter(ACCOUNTID, Integer.class);
		DataMapFilter.addFilter(OLD_ACCOUNTID, Integer.class);
		DataMapFilter.addFilter(DISCORDID, String.class);
		DataMapFilter.addFilter(PLAYTIME, Long.class);
		DataMapFilter.addFilter(VOTES, Short.class);
	}

	// Checks if this account is already registered.
	public boolean isRegistered(int accountID) {
		String stmt = "SELECT * FROM " + SQL_TABLE_NAME + " WHERE AccountID='" + accountID + "';";
		try (Connection conn =  getSQLConnection();
			 PreparedStatement ps = conn.prepareStatement(stmt);
			 ResultSet rs = ps.executeQuery();) {

			return rs.next();
		} catch (SQLException ex) {
			plugin.getLogger().log(Level.SEVERE, Errors.sqlConnectionExecute(), ex);
		}
		return false;
	}

	// Gets our stored data for this account.
	public Map<String, Object> getData(int accountid) {
		try (Connection conn = getSQLConnection();
			 PreparedStatement ps = conn.prepareStatement("SELECT * FROM " + SQL_TABLE_NAME + " WHERE AccountID='" + accountid + "'");
			 ResultSet rs = ps.executeQuery();) {

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
		}

		return null;
	}

	// Updates or Inserts a new mapping for an account.
	public void registerOrUpdate(DataMapFilter data) {
		if (data.containsKey(ACCOUNTID)) {
			saveData(data);
		}
	}

	public void saveData(DataMapFilter data) {
		int currentAccount = (int) ((data.containsKey(OLD_ACCOUNTID)) ? data.get(OLD_ACCOUNTID) : data.get(ACCOUNTID));
		try (Connection conn2 = getSQLConnection();
			 PreparedStatement grabStatement = conn2.prepareStatement("SELECT * FROM " + SQL_TABLE_NAME + " WHERE AccountID='" + currentAccount + "'");
			 ResultSet result = grabStatement.executeQuery();) {

			boolean resultPresent = result.next();

			try (Connection conn = getSQLConnection();
				PreparedStatement replaceStatement = conn.prepareStatement("REPLACE INTO " + SQL_TABLE_NAME + " (AccountID,DiscordID,Playtime,Votes) VALUES(?,?,?,?)");) {

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
				SaveTracker.executeWithTracker(replaceStatement);
			} catch (SQLException ex) {
				plugin.getLogger().log(Level.SEVERE, Errors.sqlConnectionExecute(), ex);
			}
		} catch (SQLException ex) {
			plugin.getLogger().log(Level.SEVERE, Errors.sqlConnectionExecute(), ex);
		}
	}

	public void incrementVotes(int accountID) {
		DataMapFilter data = new DataMapFilter();
		short votes = 0;

		Object o = data.get(VOTES);
		if (o instanceof Short) {
			votes = (short) o;
		}
		votes++;
		data.putAll(getData(accountID))
			.put(VOTES, votes);
		saveData(data);
	}

	public void moveAllAccounts(int from, int to) {
		try (Connection conn = getSQLConnection();
			 PreparedStatement grabStatement = conn.prepareStatement("SELECT * FROM " + SQL_TABLE_NAME + " WHERE AccountID='" + from + "'");
			 ResultSet result = grabStatement.executeQuery();) {

			if (result.next()) {
				DataMapFilter data = new DataMapFilter();
				data.put(ACCOUNTID, to)
					.put(OLD_ACCOUNTID, from);
				saveData(data);

				try (Connection conn2 = getSQLConnection();
					 PreparedStatement deleteStatement = conn.prepareStatement("DELETE FROM " + SQL_TABLE_NAME + " WHERE AccountID='" + from + "'");) {
					deleteStatement.executeUpdate();
				}
			}
		} catch (SQLException ex) {
			plugin.getLogger().log(Level.SEVERE, Errors.sqlConnectionExecute(), ex);
		}
	}
}
