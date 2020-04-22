package net.korvic.rppersonas.sql;

import net.korvic.rppersonas.RPPersonas;
import net.korvic.rppersonas.sql.extras.DataMapFilter;
import net.korvic.rppersonas.sql.extras.Errors;

import java.sql.*;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Level;

public class CurrencySQL extends BaseSQL {

	private static final String SQL_TABLE_NAME = "rppersonas_currency";

	public static final String PERSONAID = "personaid";
	public static final String MONEY = "money";
	public static final String BANK = "bank";

	public CurrencySQL(RPPersonas plugin) {
		if (BaseSQL.plugin == null) {
			BaseSQL.plugin = plugin;
		}

		String SQLTable = "CREATE TABLE IF NOT EXISTS " + SQL_TABLE_NAME + " (\n" +
						  "    PersonaID INT NOT NULL PRIMARY KEY,\n" +
						  "    Money REAL NOT NULL,\n" +
						  "    Bank REAL NOT NULL\n" +
						  ");";
		this.load(SQLTable, SQL_TABLE_NAME);
	}

	@Override
	protected boolean customStatement() {
		return false;
	}

	protected void addDataMappings() {
		DataMapFilter.addFilter(PERSONAID, PERSONAID, Integer.class);
		DataMapFilter.addFilter(MONEY, MONEY, Float.class);
		DataMapFilter.addFilter(BANK, BANK, Float.class);
	}

	public Map<String, Object> getData(int personaID) {
		Connection conn = null;
		PreparedStatement ps = null;
		ResultSet rs = null;

		try {
			conn = getSQLConnection();
			String stmt;
			stmt = "SELECT * FROM " + SQL_TABLE_NAME + " WHERE PersonaID='" + personaID + "';";

			ps = conn.prepareStatement(stmt);
			rs = ps.executeQuery();

			Map<String, Object> output = new HashMap<>();

			if (rs.next()) {
				output.put(PERSONAID, personaID);
				output.put(MONEY, rs.getFloat("Money"));
				output.put(BANK, rs.getFloat("Bank"));
			}

			return output;
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

	public void registerOrUpdate(DataMapFilter data) {
		if (data.containsKey(PERSONAID)) {
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

		grabStatement = conn.prepareStatement("SELECT * FROM " + SQL_TABLE_NAME + " WHERE PersonaID='" + data.get(PERSONAID) + "'");
		ResultSet result = grabStatement.executeQuery();
		boolean resultPresent = result.next();

		conn = getSQLConnection();
		replaceStatement = conn.prepareStatement("REPLACE INTO " + SQL_TABLE_NAME + " (PersonaID,Money,Bank) VALUES(?,?,?)");


		// Required
		replaceStatement.setInt(1, (int) data.get(PERSONAID));

		if (data.containsKey(MONEY)) {
			replaceStatement.setFloat(2, (float) data.get(MONEY));
		} else if (resultPresent) {
			replaceStatement.setFloat(2, result.getFloat("Money"));
		} else {
			replaceStatement.setFloat(2, 0);
		}

		if (data.containsKey(BANK)) {
			replaceStatement.setFloat(3, (float) data.get(BANK));
		} else if (resultPresent) {
			replaceStatement.setFloat(3, result.getFloat("Bank"));
		} else {
			replaceStatement.setFloat(3, 0);
		}

		grabStatement.close();
		return replaceStatement;
	}

}
