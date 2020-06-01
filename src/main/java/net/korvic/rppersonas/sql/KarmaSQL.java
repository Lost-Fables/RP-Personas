package net.korvic.rppersonas.sql;

import net.korvic.rppersonas.RPPersonas;
import net.korvic.rppersonas.sql.util.DataMapFilter;
import net.korvic.rppersonas.sql.util.Errors;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.logging.Level;

public class KarmaSQL extends BaseSQL {

	private static final String SQL_TABLE_NAME = "rppersonas_karma";

	public static final String PERSONAID = "personaid";
	public static final String KARMAID = "karmaid";
	public static final String ACTION = "action";
	public static final String MODIFIER = "modifier";

	public KarmaSQL(RPPersonas plugin) {
		if (BaseSQL.plugin == null) {
			BaseSQL.plugin = plugin;
		}

		String SQLTable = "CREATE TABLE IF NOT EXISTS " + SQL_TABLE_NAME + " (\n" +
						  "    PersonaID INT NOT NULL,\n" +
						  "    KarmaID INT NOT NULL,\n" +
						  "    Action TEXT NOT NULL,\n" +
						  "    Modifier REAL NOT NULL,\n" +
						  "    PRIMARY KEY(PersonaID,KarmaID)\n" +
						  ");";
		load(SQLTable, SQL_TABLE_NAME);
	}

	@Override
	protected boolean customStatement() {
		return false;
	}

	protected void addDataMappings() {
		DataMapFilter.addFilter(PERSONAID, PERSONAID, Integer.class);
		DataMapFilter.addFilter(KARMAID, KARMAID, Integer.class);
		DataMapFilter.addFilter(ACTION, ACTION, String.class);
		DataMapFilter.addFilter(MODIFIER, MODIFIER, Float.class);
	}

	public void registerOrUpdate(DataMapFilter data) {
		if (data.containsKey(PERSONAID) && data.containsKey(KARMAID)) {
			try {
				plugin.getSaveQueue().addToQueue(getSaveStatement(data));
			} catch (SQLException ex) {
				plugin.getLogger().log(Level.SEVERE, Errors.sqlConnectionExecute(), ex);
			}
		}
	}

	public PreparedStatement getSaveStatement(DataMapFilter data) throws SQLException {
		Connection conn = null;
		PreparedStatement grabStatement = null;
		PreparedStatement replaceStatement = null;
		conn = getSQLConnection();

		grabStatement = conn.prepareStatement("SELECT * FROM " + SQL_TABLE_NAME + " WHERE PersonaID='" + data.get(PERSONAID) + "' AND KarmaID='" + data.get(KARMAID) + "'");
		ResultSet result = grabStatement.executeQuery();
		boolean resultPresent = result.next();

		conn = getSQLConnection();
		replaceStatement = conn.prepareStatement("REPLACE INTO " + SQL_TABLE_NAME + " (PersonaID,KarmaID,Action,Modifier) VALUES(?,?,?,?)");


		// Required
		replaceStatement.setInt(1, (int) data.get(PERSONAID));
		replaceStatement.setInt(2, (int) data.get(KARMAID));

		if (data.containsKey(ACTION)) {
			replaceStatement.setString(3, (String) data.get(ACTION));
		} else if (resultPresent) {
			replaceStatement.setString(3, result.getString("Action"));
		} else {
			replaceStatement.setString(3, null);
		}

		if (data.containsKey(MODIFIER)) {
			replaceStatement.setInt(4, (int) data.get(MODIFIER));
		} else if (resultPresent) {
			replaceStatement.setInt(4, result.getInt("Modifier"));
		} else {
			replaceStatement.setInt(4, 0);
		}

		grabStatement.close();
		return replaceStatement;
	}

	public void deleteByIDs(DataMapFilter data) {
		if (data.containsKey(PERSONAID) && data.containsKey(KARMAID)) {
			Connection conn = getSQLConnection();
			try {
				PreparedStatement statement = conn.prepareStatement("DELETE FROM " + SQL_TABLE_NAME + " WHERE PersonaID='" + data.get(PERSONAID) + "' AND KarmaID='" + data.get(KARMAID) + "'");
				plugin.getSaveQueue().addToQueue(statement);
			} catch (SQLException ex) {
				plugin.getLogger().log(Level.SEVERE, "Unable to retreive connection", ex);
			}
		}
	}

	public int calculateKarma(int personaID) {
		int output = 0;
		Connection conn = getSQLConnection();
		try {
			PreparedStatement statement = conn.prepareStatement("SELECT * FROM " + SQL_TABLE_NAME + " WHERE PersonaID='" + personaID + "'");
			ResultSet rs = statement.executeQuery();

			while (rs.next()) {
				output += rs.getInt("Modifier");
			}
		} catch (SQLException ex) {
			plugin.getLogger().log(Level.SEVERE, "Unable to retreive connection", ex);
		}
		return output;
	}

}
