package net.korvic.rppersonas.sql;

import net.korvic.rppersonas.RPPersonas;
import net.korvic.rppersonas.accounts.Account;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;

import java.sql.*;
import java.util.*;
import java.util.logging.Level;

public class UUIDAccountMapSQL extends SQLConnection {

	private static final String SQLTableName = "rppersonas_uuid_account_map";

	public UUIDAccountMapSQL(RPPersonas plugin) {
		if (SQLConnection.plugin == null) {
			SQLConnection.plugin = plugin;
		}

		String SQLTable = "CREATE TABLE IF NOT EXISTS " + SQLTableName + " (\n" +
						  "    UUID VARCHAR(255) NOT NULL PRIMARY KEY,\n" +
						  "    AccountID INT NOT NULL\n" +
						  ");";
		this.load(SQLTable, SQLTableName);
	}

	@Override
	protected boolean customStatement() {
		return false;
	}

	public void addMapping(int accountID, Player p) {
		plugin.getUnregisteredHandler().remove(p);
		addMapping(accountID, p.getUniqueId());
		plugin.getAccountHandler().loadAccount(p, accountID, 0, true);
	}

	protected void addMapping(int accountID, UUID uuid) {
		Connection conn = null;
		PreparedStatement ps = null;
		try {
			conn = getSQLConnection();
			ps = conn.prepareStatement("REPLACE INTO " + SQLTableName + " (UUID,AccountID) VALUES(?,?)");
			ps.setString(1, uuid.toString());
			ps.setInt(2, accountID);
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

	// Retrieves the amount of tokens a player has, as per our database.
	public List<UUID> getUUIDsOf(int accountID) {
		Connection conn = null;
		PreparedStatement ps = null;
		ResultSet rs = null;

		try {
			conn = getSQLConnection();
			String stmt;
			stmt = "SELECT * FROM " + SQLTableName + " WHERE AccountID='" + accountID + "';";

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
			stmt = "SELECT * FROM " + SQLTableName + " WHERE UUID='" + uuid.toString() + "';";

			ps = conn.prepareStatement(stmt);
			rs = ps.executeQuery();

			int result = -1;
			if (rs.next()) {
				result = rs.getInt("AccountID");
			}
			if (rs.next()) {
				plugin.getLogger().warning("Found duplicate accounts for UUID: " + uuid.toString());
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

}
