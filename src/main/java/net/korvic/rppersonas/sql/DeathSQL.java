package net.korvic.rppersonas.sql;

import net.korvic.rppersonas.RPPersonas;
import org.bukkit.Location;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Map;
import java.util.UUID;
import java.util.logging.Level;

public class DeathSQL extends BaseSQL {

	private static final String SQLTableName = "rppersonas_deaths";

	public DeathSQL(RPPersonas plugin) {
		if (BaseSQL.plugin == null) {
			BaseSQL.plugin = plugin;
		}

		String SQLTable = "CREATE TABLE IF NOT EXISTS " + SQLTableName + " (\n" +
						  "    VictimPersona INT NOT NULL,\n" +
						  "    VictimAccount INT NOT NULL,\n" +
						  "    VictimUUID TEXT NOT NULL,\n" +
						  "    KillerPersona INT NOT NULL,\n" +
						  "    KillerAccount INT NOT NULL,\n" +
						  "    KillerUUID TEXT NOT NULL,\n" +
						  "    World TEXT NOT NULL,\n" +
						  "    LocationX INT NOT NULL,\n" +
						  "    LocationY INT NOT NULL,\n" +
						  "    LocationZ INT NOT NULL,\n" +
						  "    Time BIGINT NOT NULL,\n" +
						  "    StaffInflicted BIT NOT NULL,\n" +
						  "    Refunder TEXT\n" +
						  ");";
		load(SQLTable, SQLTableName);
	}

	@Override
	protected boolean customStatement() {
		return false;
	}

	// Updates or Inserts a new mapping for an account.
	public void registerOrUpdate(Map<Object, Object> data) {
		if (data.containsKey("accountid")) {
			try {
				plugin.getSaveQueue().addToQueue(getSaveStatement(data));
			} catch (SQLException ex) {
				plugin.getLogger().log(Level.SEVERE, Errors.sqlConnectionExecute(), ex);
			}
		}
	}

	public PreparedStatement getSaveStatement(Map<Object, Object> data) throws SQLException {
		Connection conn = null;
		PreparedStatement replaceStatement = null;
		conn = getSQLConnection();

		conn = getSQLConnection();
		replaceStatement = conn.prepareStatement("INSERT INTO " + SQLTableName + " (VictimPersona,VictimAccount,VictimUUID,KillerPersona,KillerAccount,KillerUUID,World,LocationX,LocationY,LocationZ,Time,StaffInflicted,Refunder) VALUES(?,?,?,?,?,?,?,?,?,?,?,?,?)");


		// Required
		if (data.containsKey("victimpersonaid")) {
			replaceStatement.setInt(1, (int) data.get("victimpersonaid"));
		} else {
			replaceStatement.setInt(1, 0);
		}

		if (data.containsKey("victimaccountid")) {
			replaceStatement.setInt(2, (int) data.get("victimaccountid"));
		} else {
			replaceStatement.setInt(2, 0);
		}

		if (data.containsKey("victimuuid")) {
			replaceStatement.setString(3, data.get("victimuuid").toString());
		} else {
			replaceStatement.setString(3, null);
		}

		if (data.containsKey("killerpersonaid")) {
			replaceStatement.setInt(4, (int) data.get("killerpersonaid"));
		} else {
			replaceStatement.setInt(4, 0);
		}

		if (data.containsKey("killeraccountid")) {
			replaceStatement.setInt(5, (int) data.get("killeraccountid"));
		} else {
			replaceStatement.setInt(5, 0);
		}

		if (data.containsKey("killeruuid")) {
			replaceStatement.setString(6, data.get("killeruuid").toString());
		} else {
			replaceStatement.setString(6, null);
		}

		if (data.containsKey("location")) {
			Location loc = (Location) data.get("location");
			replaceStatement.setString(7, loc.getWorld().getName());
			replaceStatement.setInt(8, loc.getBlockX());
			replaceStatement.setInt(9, loc.getBlockY());
			replaceStatement.setInt(10, loc.getBlockZ());
		} else {
			replaceStatement.setString(7, null);
			replaceStatement.setInt(8, 0);
			replaceStatement.setInt(9, 0);
			replaceStatement.setInt(10, 0);
		}

		if (data.containsKey("created")) {
			replaceStatement.setLong(11, (long) data.get("created"));
		} else {
			replaceStatement.setLong(11, 0);
		}

		if (data.containsKey("staff")) {
			replaceStatement.setBoolean(12, (boolean) data.get("staff"));
		} else {
			replaceStatement.setBoolean(12, false);
		}

		if (data.containsKey("refunder")) {
			replaceStatement.setString(13, data.get("refunder").toString());
		} else {
			replaceStatement.setString(13, null);
		}

		return replaceStatement;
	}

}
