package net.korvic.rppersonas.sql;

import co.lotc.core.bukkit.util.InventoryUtil;
import net.korvic.rppersonas.RPPersonas;
import net.korvic.rppersonas.sql.extras.DataBuffer;
import net.korvic.rppersonas.sql.extras.Errors;
import org.bukkit.inventory.Inventory;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Map;
import java.util.logging.Level;

public class CorpseSQL extends BaseSQL {

	private static final String SQL_TABLE_NAME = "rppersonas_corpses";

	public static final String CORPSEID = "corpseid";
	public static final String NAME = "name";
	public static final String INVENTORY = "inventory";
	public static final String CREATED = "created";
	public static final String TEXTURE = "texture";

	public CorpseSQL(RPPersonas plugin) {
		if (BaseSQL.plugin == null) {
			BaseSQL.plugin = plugin;
		}

		String SQLTable = "CREATE TABLE IF NOT EXISTS " + SQL_TABLE_NAME + " (\n" +
						  "    CorpseID INT NOT NULL PRIMARY KEY,\n" +
						  "    Name TEXT NOT NULL,\n" +
						  "    Inventory TEXT NOT NULL,\n" +
						  "    Created BIGINT NOT NULL,\n" +
						  "    Texture TEXT\n" +
						  ");";
		load(SQLTable, SQL_TABLE_NAME);
	}

	@Override
	protected boolean customStatement() {
		return false;
	}

	protected void addDataMappings() {
		DataBuffer.addMapping(CORPSEID, CORPSEID, Integer.class);
		DataBuffer.addMapping(NAME, NAME, String.class);
		DataBuffer.addMapping(INVENTORY, INVENTORY, Inventory.class);
		DataBuffer.addMapping(CREATED, CREATED, Long.class);
		DataBuffer.addMapping(TEXTURE, TEXTURE, String.class);
	}

	public void loadCorpses() {
		connection = getSQLConnection();
		try {
			String stmt = "SELECT * FROM " + SQL_TABLE_NAME + ";";
			PreparedStatement ps = connection.prepareStatement(stmt);
			ResultSet rs = ps.executeQuery();

			while (rs.next()) {
				plugin.getCorpseHandler().loadCorpse(rs.getInt("CorpseID"), rs.getString("Name"), rs.getString("Texture"), rs.getString("Inventory"), rs.getLong("Created"));
			}

			close(ps, rs);
		} catch (SQLException ex) {
			plugin.getLogger().log(Level.SEVERE, "Unable to retreive connection", ex);
		}
	}

	public void registerOrUpdate(DataBuffer data) {
		registerOrUpdate(data.getData());
	}

	private void registerOrUpdate(Map<String, Object> data) {
		if (data.containsKey(CORPSEID)) {
			try {
				plugin.getSaveQueue().addToQueue(getSaveStatement(data));
			} catch (SQLException ex) {
				plugin.getLogger().log(Level.SEVERE, Errors.sqlConnectionExecute(), ex);
			}
		}
	}

	public PreparedStatement getSaveStatement(Map<String, Object> data) throws SQLException {
		Connection conn = null;
		PreparedStatement grabStatement = null;
		PreparedStatement replaceStatement = null;
		conn = getSQLConnection();

		grabStatement = conn.prepareStatement("SELECT * FROM " + SQL_TABLE_NAME + " WHERE CorpseID='" + data.get(CORPSEID) + "'");
		ResultSet result = grabStatement.executeQuery();
		boolean resultPresent = result.next();

		conn = getSQLConnection();
		replaceStatement = conn.prepareStatement("REPLACE INTO " + SQL_TABLE_NAME + " (CorpseID,Name,Inventory,Created,Texture) VALUES(?,?,?,?,?)");


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
			Inventory inv = (Inventory) data.get(INVENTORY);
			replaceStatement.setString(3, InventoryUtil.serializeItems(inv));
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

		if (data.containsKey(TEXTURE)) {
			replaceStatement.setString(5, (String) data.get(TEXTURE));
		} else if (resultPresent) {
			replaceStatement.setString(5, result.getString("Texture"));
		} else {
			replaceStatement.setString(5, null);
		}

		grabStatement.close();
		return replaceStatement;
	}
}
