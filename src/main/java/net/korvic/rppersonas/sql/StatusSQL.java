package net.korvic.rppersonas.sql;

import net.korvic.rppersonas.RPPersonas;
import net.korvic.rppersonas.sql.util.DataMapFilter;
import net.korvic.rppersonas.sql.util.Errors;
import net.korvic.rppersonas.statuses.Status;
import net.korvic.rppersonas.statuses.StatusEntry;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;

public class StatusSQL extends BaseSQL {

	private static final String SQL_TABLE_NAME = "rppersonas_statuses";

	public static final String PERSONAID = "personaid";
	public static final String STATUS = "status";
	public static final String SEVERITY = "severity";
	public static final String EXPIRATION = "expiration";

	public StatusSQL(RPPersonas plugin) {
		String SQLTable = "CREATE TABLE IF NOT EXISTS " + SQL_TABLE_NAME + " (\n" +
						  "    PersonaID INT NOT NULL,\n" +
						  "    Status TEXT NOT NULL,\n" +
						  "    Severity TINYINT NOT NULL,\n" +
						  "    Expiration BIGINT NOT NULL\n" +
						  ");";
		createTable(SQLTable);
	}

	protected boolean customStatement() {
		Connection conn = getSQLConnection();
		try {
			if (database == null) {
				throw new NullPointerException();
			}
			long currentTime = System.currentTimeMillis();
			String stmt;
			stmt = "DELETE FROM " + SQL_TABLE_NAME + " WHERE Expiration<='" + currentTime + "';";
			PreparedStatement ps = conn.prepareStatement(stmt);
			ps.executeUpdate();
			ps.close();
		} catch (Exception ex) {
			plugin.getLogger().log(Level.SEVERE, "Unable to retreive connection", ex);
		}
		return true;
	}

	protected void addDataMappings() {
		DataMapFilter.addFilter(PERSONAID, PERSONAID, Integer.class);
		DataMapFilter.addFilter(STATUS, STATUS, Status.class);
		DataMapFilter.addFilter(SEVERITY, SEVERITY, Byte.class);
		DataMapFilter.addFilter(EXPIRATION, EXPIRATION, Long.class);
	}

	public void saveStatus(DataMapFilter data) {
		if (data.containsKey(PERSONAID) && data.containsKey(STATUS)) {
			try {
				plugin.getSaveQueue().executeWithNotification(getSaveStatement(data));
			} catch (Exception ex) {
				plugin.getLogger().log(Level.SEVERE, Errors.sqlConnectionExecute(), ex);
			}
		}
	}

	public PreparedStatement getSaveStatement(DataMapFilter data) throws Exception {
		Connection conn = getSQLConnection();
		if (conn == null) {
			throw new NullPointerException();
		}
		PreparedStatement insertStatement = conn.prepareStatement("INSERT INTO " + SQL_TABLE_NAME + " (PersonaID,Status,Severity,Expiration) VALUES(?,?,?,?)");

		// Required
		insertStatement.setInt(1, (int) data.get(PERSONAID));
		insertStatement.setString(2, ((Status) data.get(STATUS)).getName());

		// If not specified defaults to 1, ranges up to 255
		if (data.containsKey(EXPIRATION)) {
			insertStatement.setByte(3, (byte) data.get(SEVERITY));
		} else {
			insertStatement.setByte(3, (byte) 1);
		}

		// If not specified will be deleted on next pass
		if (data.containsKey(EXPIRATION)) {
			insertStatement.setLong(4, (long) data.get(EXPIRATION));
		} else {
			insertStatement.setLong(4, 0);
		}

		return insertStatement;
	}

	public void deleteStatus(int personaID, StatusEntry entry) {
		Connection conn = getSQLConnection();
		try {
			if (conn == null) {
				throw new NullPointerException();
			}
			PreparedStatement statement = conn.prepareStatement("DELETE FROM " + SQL_TABLE_NAME + " WHERE PersonaID='" + personaID + "' AND Status='" + entry.getStatus().getName() + "' AND Severity='" + entry.getSeverity() + "' AND Expiration='" + entry.getExpiration() + "'");
			plugin.getSaveQueue().executeWithNotification(statement);
		} catch (Exception ex) {
			plugin.getLogger().log(Level.SEVERE, "Unable to retreive connection", ex);
		}
	}

	public List<StatusEntry> getPersonaStatuses(int personaID) {
		Connection conn = getSQLConnection();
		try {
			if (conn == null) {
				throw new NullPointerException();
			}

			PreparedStatement statement = conn.prepareStatement("SELECT * FROM " + SQL_TABLE_NAME + " WHERE PersonaID='" + personaID + "'");
			ResultSet rs = statement.executeQuery();

			List<StatusEntry> output = new ArrayList<>();
			while(rs.next()) {
				Status status = Status.getByName(rs.getString("Status"));
				if (status != null) {
					StatusEntry entry = new StatusEntry(status, rs.getByte("Severity"), rs.getLong("Expiration"), true);
					if (entry.getExpiration() > System.currentTimeMillis()) {
						output.add(entry);
					} else {
						deleteStatus(personaID, entry);
					}
				}
			}
			return output;
		} catch (Exception ex) {
			plugin.getLogger().log(Level.SEVERE, "Unable to retreive connection", ex);
		}
		return null;
	}

}