package net.korvic.rppersonas.sql;

import net.korvic.rppersonas.RPPersonas;
import net.korvic.rppersonas.personas.PersonaHandler;
import net.korvic.rppersonas.sql.extras.DataMapFilter;
import net.korvic.rppersonas.sql.extras.Errors;

import java.sql.*;
import java.util.*;
import java.util.logging.Level;

public class PersonaAccountsMapSQL extends BaseSQL {

	private static final String SQLTableName = "rppersonas_persona_account_map";

	public PersonaAccountsMapSQL(RPPersonas plugin) {
		if (BaseSQL.plugin == null) {
			BaseSQL.plugin = plugin;
		}

		String SQLTable = "CREATE TABLE IF NOT EXISTS " + SQLTableName + " (\n" +
						  "    PersonaID INT NOT NULL PRIMARY KEY,\n" +
						  "    AccountID INT NOT NULL,\n" +
						  "    Alive TINYINT NOT NULL,\n" +
						  "    ActiveUUID TEXT\n" +
						  ");";
		load(SQLTable, SQLTableName);
	}

	@Override
	protected boolean customStatement() {
		connection = getSQLConnection();
		try {
			String stmt;
			stmt = "SELECT * FROM " + SQLTableName + " WHERE PersonaID=(SELECT MAX(PersonaID) FROM " + SQLTableName + ");";
			PreparedStatement ps = connection.prepareStatement(stmt);
			ResultSet rs = ps.executeQuery();
			if (rs.next()) {
				PersonaHandler.updateHighestPersonaID(rs.getInt("PersonaID"));
			}
			close(ps, rs);
		} catch (SQLException ex) {
			plugin.getLogger().log(Level.SEVERE, "Unable to retreive connection", ex);
		}

		return true;
	}

	protected void addDataMappings() {
		DataMapFilter.addFilter("accountid", "accountid", Integer.class);
	}

	// Inserts a new mapping for a persona.
	public void addOrUpdateMapping(int personaID, int accountID, boolean alive, UUID uuid) {
		plugin.getSaveQueue().addToQueue(getSaveStatement(personaID, accountID, alive, uuid));
	}

	public PreparedStatement getSaveStatement(int personaID, int accountID, boolean alive, UUID uuid) {
		Map<Object, Object> data = new HashMap<>();
		data.put("personaid", personaID);
		data.put("accountid", accountID);
		data.put("alive", alive);
		data.put("uuid", uuid);

		return getSaveStatement(data);
	}

	private PreparedStatement getSaveStatement(Map<Object, Object> data) {
		PreparedStatement replaceStatement = null;
		try {
			PreparedStatement grabStatement = null;
			Connection conn = null;
			conn = getSQLConnection();

			grabStatement = conn.prepareStatement("SELECT * FROM " + SQLTableName + " WHERE PersonaID='" + data.get("personaid") + "'");
			ResultSet result = grabStatement.executeQuery();
			boolean resultPresent = result.next();

			conn = getSQLConnection();
			replaceStatement = conn.prepareStatement("REPLACE INTO " + SQLTableName + " (PersonaID,AccountID,Alive,ActiveUUID) VALUES(?,?,?,?)");


			// Required
			replaceStatement.setInt(1, (int) data.get("personaid"));

			if (data.containsKey("accountid")) {
				replaceStatement.setInt(2, (int) data.get("accountid"));
			} else if (resultPresent) {
				replaceStatement.setInt(2, result.getInt("AccountID"));
			} else {
				replaceStatement.setInt(2, 0);
			}

			if (data.containsKey("alive")) {
				replaceStatement.setBoolean(3, (boolean) data.get("alive"));
			} else if (resultPresent) {
				replaceStatement.setBoolean(3, result.getBoolean("Alive"));
			} else {
				replaceStatement.setBoolean(3, true);
			}

			if (data.containsKey("uuid")) {
				if (data.get("uuid") != null) {
					replaceStatement.setString(4, ((UUID) data.get("uuid")).toString());
				} else {
					replaceStatement.setString(4, null);
				}
			} else if (resultPresent) {
				replaceStatement.setString(4, result.getString("ActiveUUID"));
			} else {
				replaceStatement.setString(4, null);
			}

			grabStatement.close();
		} catch (Exception e) {
			if (RPPersonas.DEBUGGING) {
				e.printStackTrace();
			}
		}
		return replaceStatement;
	}

	public PreparedStatement getDeleteStatement(int personaID) throws SQLException {
		Connection conn = null;
		PreparedStatement deleteStatement = null;
		conn = getSQLConnection();
		deleteStatement = conn.prepareStatement("DELETE FROM " + SQLTableName + " WHERE PersonaID='" + personaID + "'");
		return deleteStatement;
	}

	// Removes a persona mapping.
	public void removePersona(int personaID) {
		Connection conn = null;
		PreparedStatement ps = null;

		try {
			conn = getSQLConnection();
			String stmt;
			stmt = "DELETE FROM " + SQLTableName + " WHERE PersonaID='" + personaID + "';";
			ps = conn.prepareStatement(stmt);
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

	// Removes all persona mappings for an account.
	public void removeAccount(int accountID) {
		Connection conn = null;
		PreparedStatement ps = null;

		try {
			conn = getSQLConnection();
			String stmt;
			stmt = "DELETE FROM " + SQLTableName + " WHERE AccountID='" + accountID + "';";
			ps = conn.prepareStatement(stmt);
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

	// Retrieves a list of persona IDs for a given account.
	public Map<Integer, UUID> getPersonasOf(int accountID, boolean alive) {
		Connection conn = null;
		PreparedStatement ps = null;
		ResultSet rs = null;

		try {
			conn = getSQLConnection();
			String stmt;
			byte aliveBoolean = (byte) 0;
			if (alive) {
				aliveBoolean = (byte) 1;
			}
			stmt = "SELECT * FROM " + SQLTableName + " WHERE AccountID='" + accountID + "' AND Alive='" + aliveBoolean + "';";

			ps = conn.prepareStatement(stmt);
			rs = ps.executeQuery();

			Map<Integer, UUID> result = new HashMap<>();
			while (rs.next()) {
				String uuid = rs.getString("ActiveUUID");
				if (uuid != null) {
					result.put(rs.getInt("PersonaID"), UUID.fromString(uuid));
				} else {
					result.put(rs.getInt("PersonaID"), null);
				}
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

	public int getCurrentPersonaID(UUID uuid) {
		Connection conn = null;
		PreparedStatement ps = null;
		ResultSet rs = null;

		try {
			conn = getSQLConnection();
			String stmt;
			stmt = "SELECT * FROM " + SQLTableName + " WHERE ActiveUUID='" + uuid.toString() + "';";

			ps = conn.prepareStatement(stmt);
			rs = ps.executeQuery();

			int result = 0;
			while (rs.next()) {
				if (result <= 0) {
					result = rs.getInt("PersonaID");
				} else {
					addOrUpdateMapping(rs.getInt("PersonaID"), rs.getInt("AccountID"), rs.getBoolean("Alive"), null);
					if (RPPersonas.DEBUGGING) {
						plugin.getLogger().warning("Multiple personas found with the same UUID. Fixing now...");
					}
				}
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
