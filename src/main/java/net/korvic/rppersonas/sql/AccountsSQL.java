package net.korvic.rppersonas.sql;

import net.korvic.rppersonas.RPPersonas;

import java.sql.*;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Level;

public class AccountsSQL extends BaseSQL {

	private static final String SQLTableName = "rppersonas_accounts";

	public AccountsSQL(RPPersonas plugin) {
		if (BaseSQL.plugin == null) {
			BaseSQL.plugin = plugin;
		}

		String SQLTable = "CREATE TABLE IF NOT EXISTS " + SQLTableName + " (\n" +
						  "    AccountID INT NOT NULL PRIMARY KEY,\n" +
						  "    DiscordID TEXT,\n" +
						  "    Playtime BIGINT NOT NULL,\n" +
						  "    Votes SMALLINT NOT NULL\n" +
						  ");";
		this.load(SQLTable, SQLTableName);
	}

	@Override
	protected boolean customStatement() {
		return false;
	}

	// Checks if this account is already registered.
	public boolean isRegistered(int accountID) {
		Connection conn = null;
		PreparedStatement ps = null;
		ResultSet rs = null;

		try {
			conn = getSQLConnection();
			String stmt;
			stmt = "SELECT * FROM " + SQLTableName + " WHERE AccountID='" + accountID + "';";

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
	public Map<Object, Object> getData(int accountid) {
		Connection conn = null;
		PreparedStatement ps = null;
		conn = getSQLConnection();

		try {
			ps = conn.prepareStatement("SELECT * FROM " + SQLTableName + " WHERE AccountID='" + accountid + "'");
			ResultSet rs = ps.executeQuery();

			Map<Object, Object> data = new HashMap<>();

			if (rs.next()) {
				data.put("accountid", accountid);
				data.put("discordid", rs.getString("DiscordID"));
				data.put("playtime", rs.getLong("Playtime"));
				data.put("votes", rs.getShort("Votes"));
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
	public void registerOrUpdate(Map<Object, Object> data) {
		if (data.containsKey("accountid")) {
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

		grabStatement = conn.prepareStatement("SELECT * FROM " + SQLTableName + " WHERE AccountID='" + data.get("accountid") + "'");
		ResultSet result = grabStatement.executeQuery();
		boolean resultPresent = result.next();

		conn = getSQLConnection();
		replaceStatement = conn.prepareStatement("REPLACE INTO " + SQLTableName + " (AccountID,DiscordID,Playtime,Votes) VALUES(?,?,?,?)");


		// Required
		replaceStatement.setInt(1, (int) data.get("accountid"));

		if (data.containsKey("discordid")) {
			replaceStatement.setString(2, (String) data.get("discordid"));
		} else if (resultPresent) {
			replaceStatement.setString(2, result.getString("DiscordID"));
		} else {
			replaceStatement.setString(2, null);
		}

		if (data.containsKey("playtime")) {
			long playtime = (long) data.get("playtime");
			if (resultPresent) {
				playtime += result.getLong("Playtime");
			}
			replaceStatement.setLong(3, playtime);
		} else if (resultPresent) {
			replaceStatement.setLong(3, result.getLong("Playtime"));
		} else {
			replaceStatement.setLong(3, 0);
		}

		if (data.containsKey("votes")) {
			replaceStatement.setShort(4, (short) data.get("votes"));
		} else if (resultPresent) {
			replaceStatement.setShort(4, result.getShort("Votes"));
		} else {
			replaceStatement.setShort(4, (short) 0);
		}

		grabStatement.close();
		return replaceStatement;
	}

	public void incrementVotes(int accountID) {
		Map<Object, Object> oldData = getData(accountID);
		Map<Object, Object> newData = new HashMap<>();
		newData.put("accountid", accountID);
		newData.put("votes", ((short) oldData.get("votes")) + 1);
		try {
			plugin.getSaveQueue().addToQueue(getSaveStatement(newData));
		} catch (Exception e) {
			if (RPPersonas.DEBUGGING) {
				e.printStackTrace();
			}
		}
	}
}
