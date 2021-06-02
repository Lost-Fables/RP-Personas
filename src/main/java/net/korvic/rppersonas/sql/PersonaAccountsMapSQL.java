package net.korvic.rppersonas.sql;

import net.korvic.rppersonas.RPPersonas;
import net.korvic.rppersonas.personas.PersonaHandler;
import net.korvic.rppersonas.sql.util.DataMapFilter;
import net.korvic.rppersonas.sql.util.Errors;

import java.sql.*;
import java.util.*;
import java.util.logging.Level;

public class PersonaAccountsMapSQL extends BaseSQL {

	private static final String SQL_TABLE_NAME = "rppersonas_persona_account_map";

	public static final String PERSONAID = "personaid";
	public static final String ACCOUNTID = "accountid";
	public static final String ALIVE = "alive";
	public static final String ACTIVEUUID = "active_uuid";

	public PersonaAccountsMapSQL(RPPersonas plugin) {
		String SQLTable = "CREATE TABLE IF NOT EXISTS " + SQL_TABLE_NAME + " (\n" +
						  "    PersonaID INT NOT NULL PRIMARY KEY,\n" +
						  "    AccountID INT NOT NULL,\n" +
						  "    Alive BIT NOT NULL,\n" +
						  "    ActiveUUID TEXT\n" +
						  ");";
		createTable(SQLTable);
		updateData();
	}

	protected void updateData() {
		String stmt = "SELECT * FROM " + SQL_TABLE_NAME + " WHERE PersonaID=(SELECT MAX(PersonaID) FROM " + SQL_TABLE_NAME + ");";
		try (Connection conn = getSQLConnection();
			 PreparedStatement ps = conn.prepareStatement(stmt);
			 ResultSet rs = ps.executeQuery();) {

			if (rs.next()) {
				PersonaHandler.updateHighestPersonaID(rs.getInt("PersonaID"));
			}
		} catch (SQLException ex) {
			plugin.getLogger().log(Level.SEVERE, "Unable to retreive connection", ex);
		}
	}

	protected void addDataMappings() {
		DataMapFilter.addFilter(PERSONAID, PERSONAID, Integer.class);
		DataMapFilter.addFilter(ACCOUNTID, ACCOUNTID, Integer.class);
		DataMapFilter.addFilter(ALIVE, ALIVE, Boolean.class);
		DataMapFilter.addFilter(ACTIVEUUID, ACTIVEUUID, UUID.class);
	}

	// Inserts a new mapping for a persona.
	public void registerOrUpdate(DataMapFilter data) {
		if (data.containsKey(PERSONAID)) {
			try (PreparedStatement stmt = getSaveStatement(data);) {
				plugin.getSaveQueue().executeWithNotification(stmt);
			} catch (SQLException ex) {
				plugin.getLogger().log(Level.SEVERE, Errors.sqlConnectionExecute(), ex);
			}
		}
	}

	private PreparedStatement getSaveStatement(DataMapFilter data) {
		try (Connection conn2 = getSQLConnection();
			 PreparedStatement grabStatement = conn2.prepareStatement("SELECT * FROM " + SQL_TABLE_NAME + " WHERE PersonaID='" + data.get(PERSONAID) + "'");
			 ResultSet result = grabStatement.executeQuery();) {

			boolean resultPresent = result.next();

			try (Connection conn = getSQLConnection();) {
				PreparedStatement replaceStatement = conn.prepareStatement("REPLACE INTO " + SQL_TABLE_NAME + " (PersonaID,AccountID,Alive,ActiveUUID) VALUES(?,?,?,?)");
				// Required
				replaceStatement.setInt(1, (int) data.get(PERSONAID));

				if (data.containsKey(ACCOUNTID)) {
					replaceStatement.setInt(2, (int) data.get(ACCOUNTID));
				} else if (resultPresent) {
					replaceStatement.setInt(2, result.getInt("AccountID"));
				} else {
					replaceStatement.setInt(2, 0);
				}

				if (data.containsKey(ALIVE)) {
					replaceStatement.setBoolean(3, (boolean) data.get(ALIVE));
				} else if (resultPresent) {
					replaceStatement.setBoolean(3, result.getBoolean("Alive"));
				} else {
					replaceStatement.setBoolean(3, true);
				}

				if (data.containsKey(ACTIVEUUID)) {
					if (data.get(ACTIVEUUID) != null) {
						replaceStatement.setString(4, ((UUID) data.get(ACTIVEUUID)).toString());
					} else {
						replaceStatement.setString(4, null);
					}
				} else if (resultPresent) {
					replaceStatement.setString(4, result.getString("ActiveUUID"));
				} else {
					replaceStatement.setString(4, null);
				}

				return replaceStatement;
			} catch (Exception e) {
				if (RPPersonas.DEBUGGING) {
					e.printStackTrace();
				}
			}
		} catch (Exception e) {
			if (RPPersonas.DEBUGGING) {
				e.printStackTrace();
			}
		}
		return null;
	}

	public PreparedStatement getDeleteStatement(int personaID) throws SQLException {
		try (Connection conn = getSQLConnection();) {
			return conn.prepareStatement("DELETE FROM " + SQL_TABLE_NAME + " WHERE PersonaID='" + personaID + "'");
		} catch (Exception e) {
			if (RPPersonas.DEBUGGING) {
				e.printStackTrace();
			}
		}
		return null;
	}

	// Removes a persona mapping.
	public void removePersona(int personaID) {
		String stmt = "DELETE FROM " + SQL_TABLE_NAME + " WHERE PersonaID='" + personaID + "';";
		try (Connection conn = getSQLConnection();
			 PreparedStatement ps = conn.prepareStatement(stmt);) {
			ps.executeUpdate();
		} catch (SQLException ex) {
			plugin.getLogger().log(Level.SEVERE, Errors.sqlConnectionExecute(), ex);
		}
	}

	// Removes all persona mappings for an account.
	public void removeAccount(int accountID) {
		String stmt = "DELETE FROM " + SQL_TABLE_NAME + " WHERE AccountID='" + accountID + "';";
		try (Connection conn = getSQLConnection();
			 PreparedStatement ps = conn.prepareStatement(stmt);) {
			ps.executeUpdate();
		} catch (SQLException ex) {
			plugin.getLogger().log(Level.SEVERE, Errors.sqlConnectionExecute(), ex);
		}
	}

	// Retrieves a list of persona IDs for a given account.
	public Map<Integer, UUID> getPersonasOf(int accountID, boolean alive) {
		String stmt;
		byte aliveBoolean = (byte) 0;
		if (alive) {
			aliveBoolean = (byte) 1;
		}
		stmt = "SELECT * FROM " + SQL_TABLE_NAME + " WHERE AccountID='" + accountID + "' AND Alive='" + aliveBoolean + "';";

		try (Connection conn = getSQLConnection();
			 PreparedStatement ps = conn.prepareStatement(stmt);
			 ResultSet rs = ps.executeQuery();) {

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
		}
		return null;
	}

	public int getCurrentPersonaID(UUID uuid) {
		String stmt = "SELECT * FROM " + SQL_TABLE_NAME + " WHERE ActiveUUID='" + uuid.toString() + "';";

		try (Connection conn = getSQLConnection();
			 PreparedStatement ps = conn.prepareStatement(stmt);
			 ResultSet rs = ps.executeQuery();) {
			int result = 0;
			while (rs.next()) {
				if (result <= 0) {
					result = rs.getInt("PersonaID");
				} else {
					DataMapFilter data = new DataMapFilter();
					data.put(PERSONAID, rs.getInt("PersonaID"))
						.put(ACCOUNTID, rs.getInt("AccountID"))
						.put(ALIVE, rs.getBoolean("Alive"))
						.put(ACTIVEUUID, null);
					if (RPPersonas.DEBUGGING) {
						plugin.getLogger().warning("Multiple personas found with the same UUID. Fixing now...");
					}
				}
			}
			return result;
		} catch (SQLException ex) {
			plugin.getLogger().log(Level.SEVERE, Errors.sqlConnectionExecute(), ex);
		}
		return 0;
	}

	public int getAccountOf(int personaID) {
		String stmt = "SELECT * FROM " + SQL_TABLE_NAME + " WHERE PersonaID='" + personaID + "';";
		try (Connection conn = getSQLConnection();
			 PreparedStatement ps = conn.prepareStatement(stmt);
			 ResultSet rs = ps.executeQuery();) {
			int result = 0;
			if (rs.next()) {
				result = rs.getInt("AccountID");
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
				data.put(PERSONAID, result.getInt("PersonaID"))
					.put(ACCOUNTID, to);
				registerOrUpdate(data);
			}
		} catch (SQLException ex) {
			plugin.getLogger().log(Level.SEVERE, Errors.sqlConnectionExecute(), ex);
		}
	}
}
