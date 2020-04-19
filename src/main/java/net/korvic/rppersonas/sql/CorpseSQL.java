package net.korvic.rppersonas.sql;

import co.lotc.core.bukkit.util.InventoryUtil;
import net.korvic.rppersonas.RPPersonas;
import org.bukkit.inventory.Inventory;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Map;
import java.util.logging.Level;

public class CorpseSQL extends BaseSQL {

	private static final String SQLTableName = "rppersonas_corpses";

	public CorpseSQL(RPPersonas plugin) {
		if (BaseSQL.plugin == null) {
			BaseSQL.plugin = plugin;
		}

		String SQLTable = "CREATE TABLE IF NOT EXISTS " + SQLTableName + " (\n" +
						  "    CorpseID INT NOT NULL PRIMARY KEY,\n" +
						  "    Name TEXT NOT NULL,\n" +
						  "    Inventory TEXT NOT NULL,\n" +
						  "    Created BIGINT NOT NULL,\n" +
						  "    Texture TEXT\n" +
						  ");";
		load(SQLTable, SQLTableName);
	}

	@Override
	protected boolean customStatement() {
		return false;
	}

	public void loadCorpses() {
		connection = getSQLConnection();
		try {
			String stmt = "SELECT * FROM " + SQLTableName + ";";
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

	public void registerOrUpdate(Map<Object, Object> data) {
		if (data.containsKey("corpseid")) {
			try {
				plugin.getSaveQueue().addToQueue(getSaveStatement(data));
			} catch (SQLException ex) {
				plugin.getLogger().log(Level.SEVERE, Errors.sqlConnectionExecute(), ex);
			}
		}
	}

	public PreparedStatement getSaveStatement(Map<Object, Object> data) throws SQLException {
		Connection conn = null;
		PreparedStatement grabStatement = null;
		PreparedStatement replaceStatement = null;
		conn = getSQLConnection();

		grabStatement = conn.prepareStatement("SELECT * FROM " + SQLTableName + " WHERE CorpseID='" + data.get("corpseid") + "'");
		ResultSet result = grabStatement.executeQuery();
		boolean resultPresent = result.next();

		conn = getSQLConnection();
		replaceStatement = conn.prepareStatement("REPLACE INTO " + SQLTableName + " (CorpseID,Name,Inventory,Created,Texture) VALUES(?,?,?,?,?)");


		// Required
		replaceStatement.setInt(1, (int) data.get("corpseid"));

		if (data.containsKey("name")) {
			replaceStatement.setString(2, (String) data.get("name"));
		} else if (resultPresent) {
			replaceStatement.setString(2, result.getString("Name"));
		} else {
			replaceStatement.setString(2, null);
		}

		if (data.containsKey("inventory")) {
			Inventory inv = (Inventory) data.get("inventory");
			replaceStatement.setString(3, InventoryUtil.serializeItems(inv));
		} else if (resultPresent) {
			replaceStatement.setString(3, result.getString("Inventory"));
		} else {
			replaceStatement.setString(3, null);
		}

		if (data.containsKey("created")) {
			replaceStatement.setLong(4, (long) data.get("created"));
		} else if (resultPresent) {
			replaceStatement.setLong(4, result.getLong("Created"));
		} else {
			replaceStatement.setLong(4, System.currentTimeMillis());
		}

		if (data.containsKey("texture")) {
			replaceStatement.setString(5, (String) data.get("texture"));
		} else if (resultPresent) {
			replaceStatement.setString(5, result.getString("Texture"));
		} else {
			replaceStatement.setString(5, null);
		}

		grabStatement.close();
		return replaceStatement;
	}
}
