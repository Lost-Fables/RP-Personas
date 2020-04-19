package net.korvic.rppersonas.sql;

import net.korvic.rppersonas.RPPersonas;

import java.sql.*;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Level;

public class CurrencySQL extends BaseSQL {

	private static final String SQLTableName = "rppersonas_currency";

	public CurrencySQL(RPPersonas plugin) {
		if (BaseSQL.plugin == null) {
			BaseSQL.plugin = plugin;
		}

		String SQLTable = "CREATE TABLE IF NOT EXISTS " + SQLTableName + " (\n" +
						  "    PersonaID INT NOT NULL PRIMARY KEY,\n" +
						  "    Money REAL NOT NULL,\n" +
						  "    Bank REAL NOT NULL\n" +
						  ");";
		this.load(SQLTable, SQLTableName);
	}

	@Override
	protected boolean customStatement() {
		return false;
	}

	public Map<Object, Object> getData(int personaID) {
		Connection conn = null;
		PreparedStatement ps = null;
		ResultSet rs = null;

		try {
			conn = getSQLConnection();
			String stmt;
			stmt = "SELECT * FROM " + SQLTableName + " WHERE PersonaID='" + personaID + "';";

			ps = conn.prepareStatement(stmt);
			rs = ps.executeQuery();

			Map<Object, Object> output = new HashMap<>();

			if (rs.next()) {
				output.put("personaid", personaID);
				output.put("money", rs.getFloat("Money"));
				output.put("bank", rs.getFloat("Bank"));
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

	public void setData(Map<Object, Object> data) {
		if (data.containsKey("personaid")) {
			PreparedStatement ps = null;
			try {
				ps = getSaveStatement(data);
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
	}

	public PreparedStatement getSaveStatement(Map<Object, Object> data) throws SQLException {
		Connection conn = null;
		PreparedStatement grabStatement = null;
		PreparedStatement replaceStatement = null;
		conn = getSQLConnection();

		grabStatement = conn.prepareStatement("SELECT * FROM " + SQLTableName + " WHERE PersonaID='" + data.get("personaid") + "'");
		ResultSet result = grabStatement.executeQuery();
		boolean resultPresent = result.next();

		conn = getSQLConnection();
		replaceStatement = conn.prepareStatement("REPLACE INTO " + SQLTableName + " (PersonaID,Money,Bank) VALUES(?,?,?)");


		// Required
		replaceStatement.setInt(1, (int) data.get("personaid"));

		if (data.containsKey("money")) {
			replaceStatement.setFloat(2, (float) data.get("money"));
		} else if (resultPresent) {
			replaceStatement.setFloat(2, result.getFloat("Money"));
		} else {
			replaceStatement.setFloat(2, 0);
		}

		if (data.containsKey("bank")) {
			replaceStatement.setFloat(3, (float) data.get("bank"));
		} else if (resultPresent) {
			replaceStatement.setFloat(3, result.getFloat("Bank"));
		} else {
			replaceStatement.setFloat(3, 0);
		}

		grabStatement.close();
		return replaceStatement;
	}

}
