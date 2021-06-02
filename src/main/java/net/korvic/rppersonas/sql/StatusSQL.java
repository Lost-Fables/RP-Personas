package net.korvic.rppersonas.sql;

import co.lotc.core.util.DataMapFilter;
import net.korvic.rppersonas.RPPersonas;
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
		updateData();
	}

	protected void updateData() {
		long currentTime = System.currentTimeMillis();
		String stmt = "DELETE FROM " + SQL_TABLE_NAME + " WHERE Expiration<='" + currentTime + "';";
		try (Connection conn = getSQLConnection();
			 PreparedStatement ps = conn.prepareStatement(stmt);) {
			ps.executeUpdate();
		} catch (Exception ex) {
			plugin.getLogger().log(Level.SEVERE, "Unable to retreive connection", ex);
		}
	}

	protected void addDataMappings() {
		DataMapFilter.addFilter(PERSONAID, Integer.class);
		DataMapFilter.addFilter(STATUS, Status.class);
		DataMapFilter.addFilter(SEVERITY, Byte.class);
		DataMapFilter.addFilter(EXPIRATION, Long.class);
	}

	public void saveStatus(DataMapFilter data) {
		if (data.containsKey(PERSONAID) && data.containsKey(STATUS)) {
			try (PreparedStatement stmt = getSaveStatement(data);){
				plugin.getSaveQueue().executeWithNotification(stmt);
			} catch (Exception ex) {
				plugin.getLogger().log(Level.SEVERE, Errors.sqlConnectionExecute(), ex);
			}
		}
	}

	public PreparedStatement getSaveStatement(DataMapFilter data) {
		try (Connection conn = getSQLConnection();) {
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
		} catch (Exception ex) {
			plugin.getLogger().log(Level.SEVERE, Errors.sqlConnectionExecute(), ex);
		}
		return null;
	}

	public void deleteStatus(int personaID, StatusEntry entry) {
		try (Connection conn = getSQLConnection();
			 PreparedStatement statement = conn.prepareStatement("DELETE FROM " + SQL_TABLE_NAME + " WHERE PersonaID='" + personaID + "' AND Status='" + entry.getStatus().getName() + "' AND Severity='" + entry.getSeverity() + "' AND Expiration='" + entry.getExpiration() + "'");) {
			plugin.getSaveQueue().executeWithNotification(statement);
		} catch (Exception ex) {
			plugin.getLogger().log(Level.SEVERE, "Unable to retreive connection", ex);
		}
	}

	public List<StatusEntry> getPersonaStatuses(int personaID) {
		try (Connection conn = getSQLConnection();
			 PreparedStatement statement = conn.prepareStatement("SELECT * FROM " + SQL_TABLE_NAME + " WHERE PersonaID='" + personaID + "'");
			 ResultSet rs = statement.executeQuery();) {

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