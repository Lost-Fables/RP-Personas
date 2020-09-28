package net.korvic.rppersonas.sql;

import net.korvic.rppersonas.RPPersonas;
import net.korvic.rppersonas.sql.util.DataMapFilter;
import net.korvic.rppersonas.sql.util.Errors;
import org.bukkit.entity.Player;

import java.sql.*;
import java.util.*;
import java.util.logging.Level;

public class UUIDAccountMapSQL extends BaseSQL {

	private static final String SQL_TABLE_NAME = "rppersonas_uuid_account_map";
	
	public static final String PLAYER = "player";
	public static final String PLAYER_UUID = "uuid";
	public static final String ACCOUNTID = "accountid";

	public UUIDAccountMapSQL(RPPersonas plugin) {
		if (BaseSQL.plugin == null) {
			BaseSQL.plugin = plugin;
		}

		String SQLTable = "CREATE TABLE IF NOT EXISTS " + SQL_TABLE_NAME + " (\n" +
						  "    UUID VARCHAR(255) NOT NULL PRIMARY KEY,\n" +
						  "    AccountID INT NOT NULL\n" +
						  ");";
		this.load(SQLTable, SQL_TABLE_NAME);
	}

	@Override
	protected boolean customStatement() {
		return false;
	}

	protected void addDataMappings() {
		DataMapFilter.addFilter(PLAYER, PLAYER, Player.class);
		DataMapFilter.addFilter(PLAYER_UUID, PLAYER_UUID, UUID.class);
		DataMapFilter.addFilter(ACCOUNTID, ACCOUNTID, Integer.class);
	}

	public void registerOrUpdate(DataMapFilter data) {
		if (data.containsKey(PLAYER)) {
			Player p = (Player) data.get(PLAYER);
			try {
				plugin.getUnregisteredHandler().remove(p);
				plugin.getSaveQueue().addToQueue(getSaveStatement(data));
				plugin.getAccountHandler().loadAccount(p, (int) data.get(ACCOUNTID), 0, true);
			} catch (Exception e) {
				if (RPPersonas.DEBUGGING) {
					e.printStackTrace();
				}
			}
		} else if (data.containsKey(PLAYER_UUID)) {
			try {
				plugin.getSaveQueue().addToQueue(getSaveStatement(data));
			} catch (Exception e) {
				if (RPPersonas.DEBUGGING) {
					e.printStackTrace();
				}
			}
		}
	}

	private PreparedStatement getSaveStatement(DataMapFilter data) throws SQLException {
		Connection conn = null;
		PreparedStatement replaceStatement = null;
		conn = getSQLConnection();
		replaceStatement = conn.prepareStatement("REPLACE INTO " + SQL_TABLE_NAME + " (UUID,AccountID) VALUES(?,?)");

		// Required
		if (data.containsKey(PLAYER)) {
			replaceStatement.setString(1, ((Player) data.get(PLAYER)).getUniqueId().toString());
		} else if (data.containsKey(PLAYER_UUID)) {
			replaceStatement.setString(1, ((UUID) data.get(PLAYER_UUID)).toString());
		}

		if (data.containsKey(ACCOUNTID)) {
			replaceStatement.setInt(2, (int) data.get(ACCOUNTID));
		} else {
			replaceStatement.setInt(2, 0);
		}

		return replaceStatement;
	}

	public List<UUID> getUUIDsOf(Player player) {
		return getUUIDsOf(getAccountID(player.getUniqueId()));
	}

	// Retrieves the amount of tokens a player has, as per our database.
	public List<UUID> getUUIDsOf(int accountID) {
		Connection conn = null;
		PreparedStatement ps = null;
		ResultSet rs = null;

		try {
			conn = getSQLConnection();
			String stmt;
			stmt = "SELECT * FROM " + SQL_TABLE_NAME + " WHERE AccountID='" + accountID + "';";

			ps = conn.prepareStatement(stmt);
			rs = ps.executeQuery();

			List<UUID> result = new ArrayList<>();
			while (rs.next()) {
				result.add(UUID.fromString(rs.getString("UUID")));
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

	public int getAccountID(UUID uuid) {
		Connection conn = null;
		PreparedStatement ps = null;
		ResultSet rs = null;

		try {
			conn = getSQLConnection();
			String stmt;
			stmt = "SELECT * FROM " + SQL_TABLE_NAME + " WHERE UUID='" + uuid.toString() + "';";

			ps = conn.prepareStatement(stmt);
			rs = ps.executeQuery();

			int result = -1;
			if (rs.next()) {
				result = rs.getInt("AccountID");
			}
			if (rs.next()) {
				plugin.getLogger().warning("Found duplicate account maps for UUID: " + uuid.toString());
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
		return 0;
	}

	public void moveAllAccounts(int from, int to) {
		Connection conn = getSQLConnection();
		PreparedStatement grabStatement = null;
		try {
			grabStatement = conn.prepareStatement("SELECT * FROM " + SQL_TABLE_NAME + " WHERE AccountID='" + from + "'");

			ResultSet result = grabStatement.executeQuery();

			while (result.next()) {
				DataMapFilter data = new DataMapFilter();
				data.put(PLAYER_UUID, UUID.fromString(result.getString("UUID")))
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
