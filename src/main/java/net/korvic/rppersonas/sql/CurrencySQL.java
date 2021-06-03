package net.korvic.rppersonas.sql;

import co.lotc.core.util.DataMapFilter;
import net.korvic.rppersonas.RPPersonas;
import net.korvic.rppersonas.sql.util.Errors;
import net.korvic.rppersonas.sql.util.SaveTracker;

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
		String SQLTable = "CREATE TABLE IF NOT EXISTS " + SQL_TABLE_NAME + " (\n" +
						  "    PersonaID INT NOT NULL PRIMARY KEY,\n" +
						  "    Money REAL NOT NULL,\n" +
						  "    Bank REAL NOT NULL\n" +
						  ");";
		createTable(SQLTable);
		addDataMappings();
	}

	protected void addDataMappings() {
		DataMapFilter.addFilter(PERSONAID, Integer.class);
		DataMapFilter.addFilter(MONEY, Float.class);
		DataMapFilter.addFilter(BANK, Float.class);
	}

	public Map<String, Object> getData(int personaID) {
		String stmt = "SELECT * FROM " + SQL_TABLE_NAME + " WHERE PersonaID='" + personaID + "';";
		try (Connection conn = getSQLConnection();
			 PreparedStatement ps = conn.prepareStatement(stmt);
			 ResultSet rs = ps.executeQuery();) {

			Map<String, Object> output = new HashMap<>();

			if (rs.next()) {
				output.put(PERSONAID, personaID);
				output.put(MONEY, rs.getFloat("Money"));
				output.put(BANK, rs.getFloat("Bank"));
			}

			return output;
		} catch (SQLException ex) {
			plugin.getLogger().log(Level.SEVERE, Errors.sqlConnectionExecute(), ex);
		}
		return null;
	}

	public void registerOrUpdate(DataMapFilter data) {
		if (data.containsKey(PERSONAID)) {
			saveData(data);
		}
	}

	public void saveData(DataMapFilter data) {
		try (Connection conn2 = getSQLConnection();
			 PreparedStatement grabStatement = conn2.prepareStatement("SELECT * FROM " + SQL_TABLE_NAME + " WHERE PersonaID='" + data.get(PERSONAID) + "'");
			 ResultSet result = grabStatement.executeQuery();) {

			boolean resultPresent = result.next();

			try (Connection conn = getSQLConnection();
				PreparedStatement replaceStatement = conn.prepareStatement("REPLACE INTO " + SQL_TABLE_NAME + " (PersonaID,Money,Bank) VALUES(?,?,?)");) {

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
				SaveTracker.executeWithTracker(replaceStatement);
			} catch (SQLException ex) {
				plugin.getLogger().log(Level.SEVERE, Errors.sqlConnectionExecute(), ex);
			}
		} catch (SQLException ex) {
			plugin.getLogger().log(Level.SEVERE, Errors.sqlConnectionExecute(), ex);
		}
	}

}
