package net.korvic.rppersonas.sql;

import co.lotc.core.util.DataMapFilter;
import net.korvic.rppersonas.RPPersonas;
import net.korvic.rppersonas.sql.util.Errors;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.logging.Level;

public class CorpseSQL extends BaseSQL {

	private static final String SQL_TABLE_NAME = "rppersonas_corpses";

	public static final String CORPSEID = "corpseid";
	public static final String NAME = "name";
	public static final String INVENTORY = "inventory";
	public static final String CREATED = "created";
	public static final String PERSONAID = "personaid";
	public static final String TEXTURE = "texture";

	public CorpseSQL(RPPersonas plugin) {
		String SQLTable = "CREATE TABLE IF NOT EXISTS " + SQL_TABLE_NAME + " (\n" +
						  "    CorpseID INT NOT NULL PRIMARY KEY,\n" +
						  "    Name TEXT NOT NULL,\n" +
						  "    Inventory TEXT NOT NULL,\n" +
						  "    Created BIGINT NOT NULL,\n" +
						  "    PersonaID INT NOT NULL,\n" +
						  "    Texture TEXT\n" +
						  ");";
		createTable(SQLTable);
		addDataMappings();
	}

	protected void addDataMappings() {
		DataMapFilter.addFilter(CORPSEID, Integer.class);
		DataMapFilter.addFilter(NAME, String.class);
		DataMapFilter.addFilter(INVENTORY, String.class);
		DataMapFilter.addFilter(CREATED, Long.class);
		DataMapFilter.addFilter(PERSONAID, Integer.class);
		DataMapFilter.addFilter(TEXTURE, String.class);
	}

	public void loadCorpses() {
		String stmt = "SELECT * FROM " + SQL_TABLE_NAME + ";";
		try (Connection conn = getSQLConnection();
			 PreparedStatement ps = conn.prepareStatement(stmt);
			 ResultSet rs = ps.executeQuery();) {

			while (rs.next()) {
				plugin.getCorpseHandler().loadCorpse(rs.getInt("CorpseID"),
													 rs.getString("Name"),
													 rs.getString("Texture"),
													 rs.getString("Inventory"),
													 rs.getLong("Created"),
													 rs.getInt("PersonaID"));
			}
		} catch (SQLException ex) {
			plugin.getLogger().log(Level.SEVERE, "Unable to retreive connection", ex);
		}
	}

	public void registerOrUpdate(DataMapFilter data) {
		if (data.containsKey(CORPSEID)) {
			try (PreparedStatement stmt = getSaveStatement(data);) {
				plugin.getSaveQueue().executeWithNotification(stmt);
			} catch (SQLException ex) {
				plugin.getLogger().log(Level.SEVERE, Errors.sqlConnectionExecute(), ex);
			}
		}
	}

	public PreparedStatement getSaveStatement(DataMapFilter data) throws SQLException {
		try (Connection conn2 = getSQLConnection();
			 PreparedStatement grabStatement = conn2.prepareStatement("SELECT * FROM " + SQL_TABLE_NAME + " WHERE CorpseID='" + data.get(CORPSEID) + "'");
			 ResultSet result = grabStatement.executeQuery();) {

			boolean resultPresent = result.next();

			try (Connection conn = getSQLConnection();) {
				PreparedStatement replaceStatement = conn.prepareStatement("REPLACE INTO " + SQL_TABLE_NAME + " (CorpseID,Name,Inventory,Created,PersonaID,Texture) VALUES(?,?,?,?,?,?)");
				// Required
				replaceStatement.setInt(1, (int) data.get(CORPSEID));

				if (data.containsKey(NAME)) {
					replaceStatement.setString(2, (String) data.get(NAME));
				} else if (resultPresent) {
					replaceStatement.setString(2, result.getString("Name"));
				} else {
					replaceStatement.setString(2, null);
				}

				if (data.containsKey(INVENTORY)) {
					replaceStatement.setString(3, (String) data.get(INVENTORY));
				} else if (resultPresent) {
					replaceStatement.setString(3, result.getString("Inventory"));
				} else {
					replaceStatement.setString(3, null);
				}

				if (data.containsKey(CREATED)) {
					replaceStatement.setLong(4, (long) data.get(CREATED));
				} else if (resultPresent) {
					replaceStatement.setLong(4, result.getLong("Created"));
				} else {
					replaceStatement.setLong(4, System.currentTimeMillis());
				}

				if (data.containsKey(PERSONAID)) {
					replaceStatement.setInt(5, (int) data.get(PERSONAID));
				} else if (resultPresent) {
					replaceStatement.setInt(5, result.getInt("PersonaID"));
				} else {
					replaceStatement.setInt(5, 0);
				}

				if (data.containsKey(TEXTURE)) {
					replaceStatement.setString(6, (String) data.get(TEXTURE));
				} else if (resultPresent) {
					replaceStatement.setString(6, result.getString("Texture"));
				} else {
					replaceStatement.setString(6, null);
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

	public void deleteByCorpseID(int corpseID) {
		try (Connection conn = getSQLConnection();
			 PreparedStatement statement = conn.prepareStatement("DELETE FROM " + SQL_TABLE_NAME + " WHERE CorpseID='" + corpseID + "'");) {
			statement.executeUpdate();
		} catch (SQLException ex) {
			plugin.getLogger().log(Level.SEVERE, "Unable to retreive connection", ex);
		}
	}

	public void deleteByPersonaID(int personaID) {
		try (Connection conn = getSQLConnection();
			 PreparedStatement statement = conn.prepareStatement("DELETE FROM " + SQL_TABLE_NAME + " WHERE PersonaID='" + personaID + "'");) {
			statement.executeUpdate();
		} catch (SQLException ex) {
			plugin.getLogger().log(Level.SEVERE, "Unable to retreive connection", ex);
		}
	}

}
