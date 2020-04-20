package net.korvic.rppersonas.sql;

import net.korvic.rppersonas.RPPersonas;
import net.korvic.rppersonas.sql.extras.DataBuffer;
import net.korvic.rppersonas.sql.extras.Errors;
import org.bukkit.entity.Player;

import java.sql.*;
import java.util.*;
import java.util.logging.Level;

public class UUIDAccountMapSQL extends BaseSQL {

	private static final String SQL_TABLE_NAME = "rppersonas_uuid_account_map";
	
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
		DataBuffer.addMapping(PLAYER_UUID, PLAYER_UUID, UUID.class);
		DataBuffer.addMapping(ACCOUNTID, ACCOUNTID, Integer.class);
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
			ps = conn.prepareStatement("REPLACE INTO " + SQL_TABLE_NAME + " (UUID,AccountID) VALUES(?,?)");
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
