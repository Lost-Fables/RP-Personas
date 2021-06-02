package net.korvic.rppersonas.sql;

import co.lotc.core.util.DataMapFilter;
import net.korvic.rppersonas.RPPersonas;
import net.korvic.rppersonas.conversation.RezAppConvo;
import net.korvic.rppersonas.death.Altar;
import net.korvic.rppersonas.resurrection.RezApp;
import net.korvic.rppersonas.sql.util.Errors;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.logging.Level;

public class RezAppSQL extends BaseSQL {

	private static final String SQL_TABLE_NAME = "rppersonas_rezapp";

	public static final String PERSONAID = "personaid";
	public static final String RESPONSES = "responses";
	public static final String KARMA = "karma";
	public static final String KILLS = "kills";
	public static final String DEATHS = "deaths";
	public static final String ALTAR = "altar";
	public static final String DENIED = "denied";

	public RezAppSQL(RPPersonas plugin) {
		String SQLTable = "CREATE TABLE IF NOT EXISTS " + SQL_TABLE_NAME + " (\n" +
						  "    PersonaID INT NOT NULL PRIMARY KEY,\n" +
						  "    Why TEXT NOT NULL,\n" +
						  "    Honest TEXT NOT NULL,\n" +
						  "    Meaning TEXT NOT NULL,\n" +
						  "    Karma INT NOT NULL,\n" +
						  "    Kills INT NOT NULL,\n" +
						  "    Deaths INT NOT NULL,\n" +
						  "    Altar TEXT NOT NULL,\n" +
						  "    Denied BIT NOT NULL\n" +
						  ");";
		createTable(SQLTable);
	}

	protected void addDataMappings() {
		DataMapFilter.addFilter(PERSONAID, Integer.class);
		DataMapFilter.addFilter(RESPONSES, RezAppConvo.RezAppResponses.class);
		DataMapFilter.addFilter(KARMA, Integer.class);
		DataMapFilter.addFilter(KILLS, Integer.class);
		DataMapFilter.addFilter(DEATHS, Integer.class);
		DataMapFilter.addFilter(ALTAR, Altar.class);
		DataMapFilter.addFilter(DENIED, Boolean.class);
	}

	public void loadApps() {
		String stmt = "SELECT * FROM " + SQL_TABLE_NAME + ";";
		try (Connection conn = getSQLConnection();
			 PreparedStatement ps = conn.prepareStatement(stmt);
			 ResultSet rs = ps.executeQuery();) {
			while (rs.next()) {
				DataMapFilter data = grabDataFromResult(rs);
				plugin.getRezHandler().addApp(new RezApp(data));
			}
		} catch (SQLException ex) {
			plugin.getLogger().log(Level.SEVERE, "Unable to retreive connection", ex);
		}
	}

	public void registerOrUpdate(DataMapFilter data) {
		if (data.containsKey(PERSONAID)) {
			try (PreparedStatement stmt = getSaveStatement(data);){
				plugin.getSaveQueue().executeWithNotification(stmt);
			} catch (SQLException ex) {
				plugin.getLogger().log(Level.SEVERE, Errors.sqlConnectionExecute(), ex);
			}
		}
	}

	public PreparedStatement getSaveStatement(DataMapFilter data) throws SQLException {
		try (Connection conn2 = getSQLConnection();
			 PreparedStatement grabStatement = conn2.prepareStatement("SELECT * FROM " + SQL_TABLE_NAME + " WHERE PersonaID='" + data.get(PERSONAID) + "'");
			 ResultSet result = grabStatement.executeQuery();) {

			boolean resultPresent = result.next();

			try (Connection conn = getSQLConnection();) {
				PreparedStatement replaceStatement = conn.prepareStatement("REPLACE INTO " + SQL_TABLE_NAME + " (PersonaID,Why,Honest,Meaning,Karma,Kills,Deaths,Altar,Denied) VALUES(?,?,?,?,?,?,?,?,?)");
				// Required
				replaceStatement.setInt(1, (int) data.get(PERSONAID));

				if (data.containsKey(RESPONSES)) {
					RezAppConvo.RezAppResponses responses = (RezAppConvo.RezAppResponses) data.get(RESPONSES);
					replaceStatement.setString(2, responses.getResponse(1));
					replaceStatement.setString(3, responses.getResponse(2));
					replaceStatement.setString(4, responses.getResponse(3));
				} else if (resultPresent) {
					replaceStatement.setString(2, result.getString("Why"));
					replaceStatement.setString(3, result.getString("Honest"));
					replaceStatement.setString(4, result.getString("Meaning"));
				} else {
					replaceStatement.setString(2, null);
					replaceStatement.setString(3, null);
					replaceStatement.setString(4, null);
				}

				if (data.containsKey(KARMA)) {
					replaceStatement.setInt(5, (int) data.get(KARMA));
				} else if (resultPresent) {
					replaceStatement.setInt(5, result.getInt("Karma"));
				} else {
					replaceStatement.setInt(5, 0);
				}

				if (data.containsKey(KILLS)) {
					replaceStatement.setInt(6, (int) data.get(KILLS));
				} else if (resultPresent) {
					replaceStatement.setInt(6, result.getInt("Kills"));
				} else {
					replaceStatement.setInt(6, 0);
				}

				if (data.containsKey(DEATHS)) {
					replaceStatement.setInt(7, (int) data.get(DEATHS));
				} else if (resultPresent) {
					replaceStatement.setInt(7, result.getInt("Deaths"));
				} else {
					replaceStatement.setInt(7, 0);
				}

				if (data.containsKey(ALTAR)) {
					replaceStatement.setString(8, ((Altar) data.get(ALTAR)).getLabel());
				} else if (resultPresent) {
					replaceStatement.setString(8, result.getString("Altar"));
				} else {
					replaceStatement.setString(8, null);
				}

				if (data.containsKey(DENIED)) {
					replaceStatement.setBoolean(9, (boolean) data.get(DENIED));
				} else if (resultPresent) {
					replaceStatement.setBoolean(9, result.getBoolean("Denied"));
				} else {
					replaceStatement.setBoolean(9, false);
				}
				return replaceStatement;
			}
		}
	}

	public void deleteByID(int personaID) {
		try (Connection conn = getSQLConnection();
			 PreparedStatement statement = conn.prepareStatement("DELETE FROM " + SQL_TABLE_NAME + " WHERE PersonaID='" + personaID + "'");) {
			plugin.getSaveQueue().executeWithNotification(statement);
		} catch (SQLException ex) {
			plugin.getLogger().log(Level.SEVERE, "Unable to retreive connection", ex);
		}
	}

	public DataMapFilter getData(int personaID) {
		DataMapFilter data = null;
		try (Connection conn = getSQLConnection();
			 PreparedStatement statement = conn.prepareStatement("SELECT * FROM " + SQL_TABLE_NAME + " WHERE PersonaID='" + personaID + "'");
			 ResultSet rs = statement.executeQuery();) {
			if (rs.next()) {
				data = grabDataFromResult(rs);
			}
		} catch (SQLException ex) {
			plugin.getLogger().log(Level.SEVERE, "Unable to retreive connection", ex);
		}
		return data;
	}

	private DataMapFilter grabDataFromResult(ResultSet rs) {
		DataMapFilter data = new DataMapFilter();

		try {
			data.put(PERSONAID, rs.getInt("PersonaID"));

			RezAppConvo.RezAppResponses responses = new RezAppConvo.RezAppResponses();
			responses.addEntry(1, rs.getString("Why"));
			responses.addEntry(2, rs.getString("Honest"));
			responses.addEntry(3, rs.getString("Meaning"));
			data.put(RESPONSES, responses);

			data.put(KARMA, rs.getInt("Karma"));
			data.put(KILLS, rs.getInt("Kills"));
			data.put(DEATHS, rs.getInt("Deaths"));
			data.put(ALTAR, plugin.getAltarHandler().getAltar(rs.getString("Altar")));
			data.put(DENIED, rs.getBoolean("Denied"));
		} catch (SQLException ex) {
			plugin.getLogger().log(Level.SEVERE, "Unable to retreive connection", ex);
		}

		return data;
	}

	public boolean hasApplied(int personaID) {
		try (Connection conn = getSQLConnection();
			 PreparedStatement statement = conn.prepareStatement("SELECT * FROM " + SQL_TABLE_NAME + " WHERE PersonaID='" + personaID + "'");
			 ResultSet rs = statement.executeQuery();) {
			if (rs.next()) {
				return true;
			}
		} catch (SQLException ex) {
			plugin.getLogger().log(Level.SEVERE, "Unable to retreive connection", ex);
		}
		return false;
	}
}
