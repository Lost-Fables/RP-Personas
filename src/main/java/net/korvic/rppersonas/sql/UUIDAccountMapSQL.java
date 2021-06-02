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
		String SQLTable = "CREATE TABLE IF NOT EXISTS " + SQL_TABLE_NAME + " (\n" +
						  "    UUID VARCHAR(255) NOT NULL PRIMARY KEY,\n" +
						  "    AccountID INT NOT NULL\n" +
						  ");";
		createTable(SQLTable);
	}

	protected void addDataMappings() {
		DataMapFilter.addFilter(PLAYER, PLAYER, Player.class);
		DataMapFilter.addFilter(PLAYER_UUID, PLAYER_UUID, UUID.class);
		DataMapFilter.addFilter(ACCOUNTID, ACCOUNTID, Integer.class);
	}

	public void registerOrUpdate(DataMapFilter data) {
		if (data.containsKey(PLAYER)) {
			Player p = (Player) data.get(PLAYER);
			try (PreparedStatement stmt = getSaveStatement(data);){
				plugin.getUnregisteredHandler().remove(p);
				plugin.getSaveQueue().executeWithNotification(stmt);
				plugin.getAccountHandler().loadAccount(p, (int) data.get(ACCOUNTID), 0, true);
			} catch (Exception e) {
				if (RPPersonas.DEBUGGING) {
					e.printStackTrace();
				}
			}
		} else if (data.containsKey(PLAYER_UUID)) {
			try (PreparedStatement stmt = getSaveStatement(data);) {
				plugin.getSaveQueue().executeWithNotification(stmt);
			} catch (Exception e) {
				if (RPPersonas.DEBUGGING) {
					e.printStackTrace();
				}
			}
		}
	}

	private PreparedStatement getSaveStatement(DataMapFilter data) throws SQLException {
		try (Connection conn = getSQLConnection();
			 PreparedStatement replaceStatement = conn.prepareStatement("REPLACE INTO " + SQL_TABLE_NAME + " (UUID,AccountID) VALUES(?,?)");) {

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
		} catch (Exception e) {
			if (RPPersonas.DEBUGGING) {
				e.printStackTrace();
			}
		}
		return null;
	}

	public List<UUID> getUUIDsOf(Player player) {
		return getUUIDsOf(getAccountID(player.getUniqueId()));
	}

	// Retrieves the amount of tokens a player has, as per our database.
	public List<UUID> getUUIDsOf(int accountID) {
		String stmt = "SELECT * FROM " + SQL_TABLE_NAME + " WHERE AccountID='" + accountID + "';";
		try (Connection conn = getSQLConnection();
			 PreparedStatement ps = conn.prepareStatement(stmt);
			 ResultSet rs = ps.executeQuery();) {
			List<UUID> result = new ArrayList<>();
			while (rs.next()) {
				result.add(UUID.fromString(rs.getString("UUID")));
			}
			return result;
		} catch (SQLException ex) {
			plugin.getLogger().log(Level.SEVERE, Errors.sqlConnectionExecute(), ex);
		}
		return null;
	}

	public int getAccountID(UUID uuid) {
		String stmt = "SELECT * FROM " + SQL_TABLE_NAME + " WHERE UUID='" + uuid.toString() + "';";
		try (Connection conn = getSQLConnection();
			 PreparedStatement ps = conn.prepareStatement(stmt);
			 ResultSet rs = ps.executeQuery();) {
			int result = 0;
			if (rs.next()) {
				result = rs.getInt("AccountID");
			}
			if (rs.next()) {
				plugin.getLogger().warning("Found duplicate account maps for UUID: " + uuid.toString());
			}
			return result;
		} catch (SQLException ex) {
			plugin.getLogger().log(Level.SEVERE, Errors.sqlConnectionExecute(), ex);
		}
		return 0;
	}

	public void moveAllAccounts(int from, int to) {

		try (Connection conn = getSQLConnection();
			 PreparedStatement grabStatement = conn.prepareStatement("SELECT * FROM " + SQL_TABLE_NAME + " WHERE AccountID='" + from + "'");
			 ResultSet result = grabStatement.executeQuery();) {

			while (result.next()) {
				DataMapFilter data = new DataMapFilter();
				data.put(PLAYER_UUID, UUID.fromString(result.getString("UUID")))
					.put(ACCOUNTID, to);
				registerOrUpdate(data);
			}
		} catch (SQLException ex) {
			plugin.getLogger().log(Level.SEVERE, Errors.sqlConnectionExecute(), ex);
		}
	}
}
