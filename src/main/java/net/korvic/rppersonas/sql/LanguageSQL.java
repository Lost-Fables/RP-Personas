package net.korvic.rppersonas.sql;

import net.korvic.rppersonas.RPPersonas;
import net.korvic.rppersonas.sql.util.DataMapFilter;
import net.korvic.rppersonas.sql.util.Errors;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Level;

public class LanguageSQL extends BaseSQL {

	private static final String SQL_TABLE_NAME = "rppersonas_language";

	public static final String PERSONAID = "personaid";
	public static final String LANGUAGE = "language";
	public static final String LEVEL = "level";

	public LanguageSQL(RPPersonas plugin) {
		if (BaseSQL.plugin == null) {
			BaseSQL.plugin = plugin;
		}

		String SQLTable = "CREATE TABLE IF NOT EXISTS " + SQL_TABLE_NAME + " (\n" +
						  "    PersonaID INT NOT NULL,\n" +
						  "    Language TEXT NOT NULL,\n" +
						  "    Level SMALLINT NOT NULL\n" +
						  ");";
		load(SQLTable, SQL_TABLE_NAME);
	}

	@Override
	protected boolean customStatement() {
		return false;
	}

	protected void addDataMappings() {
		DataMapFilter.addFilter(PERSONAID, PERSONAID, Integer.class);
		DataMapFilter.addFilter(LANGUAGE, LANGUAGE, String.class);
		DataMapFilter.addFilter(LEVEL, LEVEL, Short.class);
	}

	public Map<String, Short> getLanguages(int personaID) {
		connection = getSQLConnection();
		try {
			String stmt = "SELECT * FROM " + SQL_TABLE_NAME + " WHERE PersonaID='" + personaID + "';";
			PreparedStatement ps = connection.prepareStatement(stmt);
			ResultSet rs = ps.executeQuery();

			Map<String, Short> languageLevelMap = new HashMap<>();

			while (rs.next()) {
				languageLevelMap.put(rs.getString("Language"), rs.getShort("Level"));
			}

			close(ps, rs);
			return languageLevelMap;
		} catch (SQLException ex) {
			plugin.getLogger().log(Level.SEVERE, "Unable to retreive connection", ex);
		}
		return null;
	}

	public void registerOrUpdate(DataMapFilter data) {
		unregister(data);
		try {
			plugin.getSaveQueue().addToQueue(getSaveStatement(data));
		} catch (SQLException ex) {
			plugin.getLogger().log(Level.SEVERE, Errors.sqlConnectionExecute(), ex);
		}
	}

	public void unregister(DataMapFilter data) {
		plugin.getSaveQueue().addToQueue(getDeleteLanguageStatement(data));
	}

	public PreparedStatement getSaveStatement(DataMapFilter data) throws SQLException {
		if (data.containsKey(PERSONAID) && data.containsKey(LANGUAGE) && data.containsKey(LEVEL)) {
			Connection conn = getSQLConnection();
			PreparedStatement replaceStatement = null;
			replaceStatement = conn.prepareStatement("INSERT INTO " + SQL_TABLE_NAME + " (PersonaID,Language,Level) VALUES(?,?,?)");

			replaceStatement.setInt(1, (int) data.get(PERSONAID));
			replaceStatement.setString(2, (String) data.get(LANGUAGE));
			replaceStatement.setShort(3, (short) data.get(LEVEL));
			return replaceStatement;
		}
		return null;
	}

	public PreparedStatement getDeleteLanguageStatement(DataMapFilter data) {
		if (data.containsKey(PERSONAID) && data.containsKey(LANGUAGE)) {
			Connection conn = getSQLConnection();
			try {
				return conn.prepareStatement("DELETE FROM " + SQL_TABLE_NAME + " WHERE PersonaID='" + data.get(PERSONAID) + "' AND Language='" + data.get(LANGUAGE) + "'");
			} catch (SQLException ex) {
				plugin.getLogger().log(Level.SEVERE, "Unable to retreive connection", ex);
			}
		}
		return null;
	}

	public PreparedStatement getDeleteStatementByPersonaID(int personaID) {
		Connection conn = getSQLConnection();
		try {
			return conn.prepareStatement("DELETE FROM " + SQL_TABLE_NAME + " WHERE PersonaID='" + personaID + "'");
		} catch (SQLException ex) {
			plugin.getLogger().log(Level.SEVERE, "Unable to retreive connection", ex);
		}
		return null;
	}
}