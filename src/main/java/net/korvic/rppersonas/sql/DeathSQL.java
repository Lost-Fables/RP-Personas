package net.korvic.rppersonas.sql;

import co.lotc.core.util.DataMapFilter;
import net.korvic.rppersonas.RPPersonas;
import net.korvic.rppersonas.sql.util.Errors;
import net.korvic.rppersonas.sql.util.SaveTracker;
import org.bukkit.Location;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.UUID;
import java.util.logging.Level;

public class DeathSQL extends BaseSQL {

	private static final String SQL_TABLE_NAME = "rppersonas_deaths";

	public static final String DEATHID = "deathid";

	public static final String VICTIM_PERSONAID = "victimpersonaid";
	public static final String VICTIM_ACCOUNTID = "victimaccountid";
	public static final String VICTIM_UUID = "victimuuid";

	public static final String KILLER_PERSONAID = "killerpersonaid";
	public static final String KILLER_ACCOUNTID = "killeraccountid";
	public static final String KILLER_UUID = "killeruuid";

	public static final String LOCATION = "location";
	public static final String CREATED = "created";
	public static final String STAFF = "staff";
	public static final String REFUNDER = "refunder";

	private int highestDeathID = 1;

	public DeathSQL(RPPersonas plugin) {
		String SQLTable = "CREATE TABLE IF NOT EXISTS " + SQL_TABLE_NAME + " (\n" +
						  "    DeathID INT NOT NULL PRIMARY KEY,\n" +
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
		createTable(SQLTable);
		updateData();
		addDataMappings();
	}

	protected void updateData() {
		String stmt = "SELECT * FROM " + SQL_TABLE_NAME + " WHERE DeathID=(SELECT MAX(DeathID) FROM " + SQL_TABLE_NAME + ");";
		try (Connection conn = getSQLConnection();
			 PreparedStatement ps = conn.prepareStatement(stmt);
			 ResultSet rs = ps.executeQuery();) {
			if (rs.next()) {
				updateHighestDeathID(rs.getInt("DeathID"));
			}
		} catch (SQLException ex) {
			plugin.getLogger().log(Level.SEVERE, "Unable to retreive connection", ex);
		}
	}

	protected void addDataMappings() {
		DataMapFilter.addFilter(DEATHID, Integer.class);

		DataMapFilter.addFilter(VICTIM_PERSONAID, Integer.class);
		DataMapFilter.addFilter(VICTIM_ACCOUNTID, Integer.class);
		DataMapFilter.addFilter(VICTIM_UUID, UUID.class);

		DataMapFilter.addFilter(KILLER_PERSONAID, Integer.class);
		DataMapFilter.addFilter(KILLER_ACCOUNTID, Integer.class);
		DataMapFilter.addFilter(KILLER_UUID, UUID.class);

		DataMapFilter.addFilter(LOCATION, Location.class);
		DataMapFilter.addFilter(CREATED, Long.class);
		DataMapFilter.addFilter(STAFF, Boolean.class);
		DataMapFilter.addFilter(REFUNDER, UUID.class);
	}

	private void updateHighestDeathID(int deathID) {
		if (deathID >= highestDeathID) {
			highestDeathID = deathID + 1;
		}
	}

	// Updates or Inserts a new mapping for an account.
	public void registerOrUpdate(DataMapFilter data) {
		if (!data.containsKey(DEATHID)) {
			data.put(DEATHID, highestDeathID);
			updateHighestDeathID(highestDeathID);
		}
		saveData(data);
	}

	public void saveData(DataMapFilter data) {
		try (Connection conn2 = getSQLConnection();
			 PreparedStatement grabStatement = conn2.prepareStatement("SELECT * FROM " + SQL_TABLE_NAME + " WHERE PersonaID='" + data.get(DEATHID) + "'");
			 ResultSet result = grabStatement.executeQuery();) {

			boolean resultPresent = result.next();

			try (Connection conn = getSQLConnection();) {
				PreparedStatement replaceStatement = conn.prepareStatement("REPLACE INTO " + SQL_TABLE_NAME +
																		   " (DeathID,VictimPersona,VictimAccount,VictimUUID,KillerPersona,KillerAccount,KillerUUID,World,LocationX,LocationY,LocationZ,Time,StaffInflicted,Refunder)" +
																		   " VALUES(?,?,?,?,?,?,?,?,?,?,?,?,?,?)");
				// Required
				replaceStatement.setInt(1, (int) data.get(DEATHID));

				if (data.containsKey(VICTIM_PERSONAID)) {
					replaceStatement.setInt(2, (int) data.get(VICTIM_PERSONAID));
				} else if (resultPresent) {
					replaceStatement.setInt(2, result.getInt("VictimPersona"));
				} else {
					replaceStatement.setInt(2, 0);
				}

				if (data.containsKey(VICTIM_ACCOUNTID)) {
					replaceStatement.setInt(3, (int) data.get(VICTIM_ACCOUNTID));
				} else if (resultPresent) {
					replaceStatement.setInt(3, result.getInt("VictimAccount"));
				} else {
					replaceStatement.setInt(3, 0);
				}

				if (data.containsKey(VICTIM_UUID)) {
					replaceStatement.setString(4, data.get(VICTIM_UUID).toString());
				} else if (resultPresent) {
					replaceStatement.setString(4, result.getString("VictimUUID"));
				} else {
					replaceStatement.setString(4, null);
				}

				if (data.containsKey(KILLER_PERSONAID)) {
					replaceStatement.setInt(5, (int) data.get(KILLER_PERSONAID));
				} else if (resultPresent) {
					replaceStatement.setInt(5, result.getInt("KillerPersona"));
				} else {
					replaceStatement.setInt(5, 0);
				}

				if (data.containsKey(KILLER_ACCOUNTID)) {
					replaceStatement.setInt(6, (int) data.get(KILLER_ACCOUNTID));
				} else if (resultPresent) {
					replaceStatement.setInt(6, result.getInt("KillerAccount"));
				} else {
					replaceStatement.setInt(6, 0);
				}

				if (data.containsKey(KILLER_UUID)) {
					replaceStatement.setString(7, data.get(KILLER_UUID).toString());
				} else if (resultPresent) {
					replaceStatement.setString(7, result.getString("KillerUUID"));
				} else {
					replaceStatement.setString(7, null);
				}

				if (data.containsKey(LOCATION)) {
					Location loc = (Location) data.get(LOCATION);
					replaceStatement.setString(8, loc.getWorld().getName());
					replaceStatement.setInt(9, loc.getBlockX());
					replaceStatement.setInt(10, loc.getBlockY());
					replaceStatement.setInt(11, loc.getBlockZ());
				} else if (resultPresent) {
					replaceStatement.setString(8, result.getString("World"));
					replaceStatement.setInt(9, result.getInt("LocationX"));
					replaceStatement.setInt(10, result.getInt("LocationY"));
					replaceStatement.setInt(11, result.getInt("LocationZ"));
				} else {
					replaceStatement.setString(8, null);
					replaceStatement.setInt(9, 0);
					replaceStatement.setInt(10, 0);
					replaceStatement.setInt(11, 0);
				}

				if (data.containsKey(CREATED)) {
					replaceStatement.setLong(12, (long) data.get(CREATED));
				} else if (resultPresent) {
					replaceStatement.setLong(12, result.getLong("Time"));
				} else {
					replaceStatement.setLong(12, System.currentTimeMillis());
				}

				if (data.containsKey(STAFF)) {
					replaceStatement.setBoolean(13, (boolean) data.get(STAFF));
				} else if (resultPresent) {
					replaceStatement.setBoolean(13, result.getBoolean("StaffInflicted"));
				} else {
					replaceStatement.setBoolean(13, false);
				}

				if (data.containsKey(REFUNDER)) {
					replaceStatement.setString(14, data.get(REFUNDER).toString());
				} else if (resultPresent) {
					replaceStatement.setString(14, result.getString("Refunder"));
				} else {
					replaceStatement.setString(14, null);
				}

				SaveTracker.executeWithTracker(replaceStatement);
			} catch (SQLException ex) {
				plugin.getLogger().log(Level.SEVERE, Errors.sqlConnectionExecute(), ex);
			}
		} catch (SQLException ex) {
			plugin.getLogger().log(Level.SEVERE, Errors.sqlConnectionExecute(), ex);
		}
	}

	public int getKills(int personaID) {
		int output = 0;

		try (Connection conn = getSQLConnection();
			 PreparedStatement stmt = conn.prepareStatement("SELECT * FROM " + SQL_TABLE_NAME + " WHERE KillerPersona='" + personaID + "'");
			 ResultSet rs = stmt.executeQuery();) {
			while (rs.next()) {
				output++;
			}
		} catch (Exception ex) {
			plugin.getLogger().log(Level.SEVERE, Errors.sqlConnectionExecute(), ex);
		}
		return output;
	}

	public int getDeaths(int personaID) {
		int output = 0;

		try (Connection conn = getSQLConnection();
			 PreparedStatement stmt = conn.prepareStatement("SELECT * FROM " + SQL_TABLE_NAME + " WHERE VictimPersona='" + personaID + "'");
			 ResultSet rs = stmt.executeQuery();) {
			while (rs.next()) {
				output++;
			}
		} catch (Exception ex) {
			plugin.getLogger().log(Level.SEVERE, Errors.sqlConnectionExecute(), ex);
		}
		return output;
	}

	public void moveAllAccounts(int from, int to) {
		moveVictims(from, to);
		moveKillers(from, to);
	}

	private void moveVictims(int from, int to) {
		try (Connection conn = getSQLConnection();
			 PreparedStatement grabStatement = conn.prepareStatement("SELECT * FROM " + SQL_TABLE_NAME + " WHERE VictimAccount='" + from + "'");
			 ResultSet result = grabStatement.executeQuery();) {

			while (result.next()) {
				DataMapFilter data = new DataMapFilter();
				data.put(DEATHID, result.getInt("DeathID"))
					.put(VICTIM_ACCOUNTID, to);
				saveData(data);
			}
		} catch (SQLException ex) {
			plugin.getLogger().log(Level.SEVERE, Errors.sqlConnectionExecute(), ex);
		}
	}

	private void moveKillers(int from, int to) {
		try (Connection conn = getSQLConnection();
			 PreparedStatement grabStatement = conn.prepareStatement("SELECT * FROM " + SQL_TABLE_NAME + " WHERE KillerAccount='" + from + "'");
			 ResultSet result = grabStatement.executeQuery();) {

			while (result.next()) {
				DataMapFilter data = new DataMapFilter();
				data.put(DEATHID, result.getInt("DeathID"))
					.put(KILLER_ACCOUNTID, to);
				saveData(data);
			}
		} catch (SQLException ex) {
			plugin.getLogger().log(Level.SEVERE, Errors.sqlConnectionExecute(), ex);
		}
	}
}
