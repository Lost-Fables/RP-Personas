package net.korvic.rppersonas.sql;

import net.korvic.rppersonas.RPPersonas;
import net.korvic.rppersonas.sql.util.Errors;
import org.bukkit.scheduler.BukkitRunnable;

import java.sql.*;
import java.util.logging.Level;

public abstract class BaseSQL {

	protected static RPPersonas plugin = null;
	protected static Connection connection;

	private static BukkitRunnable runnable = null;

	private static final String HOST = RPPersonas.config.getString("mysql.host");
	private static final String PORT = RPPersonas.config.getString("mysql.port");
	private static final String DATABASE = RPPersonas.config.getString("mysql.database");
	private static final String USER = RPPersonas.config.getString("mysql.user");
	private static final String PASSWORD = RPPersonas.config.getString("mysql.password");
	private static final boolean MARIADB = RPPersonas.config.getBoolean("mysql.mariadb");
	private static final String FLAGS = RPPersonas.config.getString("mysql.flags");

	// INSTANCE //
	protected void load(String SQLTable, String SQLTableName) {
		connection = getSQLConnection();
		try {
			Statement s = connection.createStatement();
			s.execute(SQLTable);
			s.close();
		} catch (SQLException e) {
			e.printStackTrace();
		}
		initialize(SQLTableName);
		if (runnable == null || runnable.isCancelled()) {
			runConnectionMaintainer();
		}
	}

	protected void initialize(String SQLTableName){
		if (!customStatement()) {
			connection = getSQLConnection();
			try {
				String stmt;
				stmt = "SELECT * FROM " + SQLTableName + ";";
				PreparedStatement ps = connection.prepareStatement(stmt);
				ResultSet rs = ps.executeQuery();
				close(ps, rs);
			} catch (SQLException ex) {
				plugin.getLogger().log(Level.SEVERE, "Unable to retreive connection", ex);
			}
		}
		addDataMappings();
	}

	protected abstract boolean customStatement();

	protected abstract void addDataMappings();

	// STATIC //
	protected static Connection getSQLConnection() {
		Connection output = null;
		try {
			if (connection != null && !connection.isClosed()) {
				output = connection;
			} else if (MARIADB) {
				Class.forName("com.mysql.jdbc.Driver");
				String url = "jdbc:mariadb://" + HOST + ":" + PORT + "/" + DATABASE + FLAGS;
				output = DriverManager.getConnection(url, USER, PASSWORD);
			}
		} catch (ClassNotFoundException cnfe) {
			if (RPPersonas.DEBUGGING) {
				plugin.getLogger().log(Level.SEVERE, "Unable to find dependent JDBC drivers in JVM.", cnfe);
			}
		} catch (SQLException ex) {
			if (RPPersonas.DEBUGGING) {
				plugin.getLogger().log(Level.SEVERE, "MariaDB exception on initialize", ex);
			}
		}

		if (output == null) {
			if (MARIADB) {
				plugin.getLogger().warning("Unable to connect with MariaDB for " + HOST + ":" + PORT + "/" + DATABASE /*+ " with information " + USER + ":" + PASSWORD*/ + " | Trying MySQL instead.");
			}

			try {
				Class.forName("com.mysql.jdbc.Driver");
				String url = "jdbc:mysql://" + HOST + ":" + PORT + "/" + DATABASE + FLAGS;
				output = DriverManager.getConnection(url, USER, PASSWORD);
			} catch (ClassNotFoundException cnfe) {
				if (RPPersonas.DEBUGGING) {
					plugin.getLogger().log(Level.SEVERE, "Unable to find dependent JDBC drivers in JVM.", cnfe);
				}
	    	} catch (SQLException ex) {
				if (RPPersonas.DEBUGGING) {
					plugin.getLogger().log(Level.SEVERE, "MySQL exception on initialize", ex);
				}
			}

			if (output == null) {
				plugin.getLogger().warning("Unable to connect with MySQL for " + HOST + ":" + PORT + "/" + DATABASE);
			} else {
				plugin.getLogger().warning("Connected with MySQL.");
			}
		}

		return output;
	}

	protected static void runConnectionMaintainer() {
		runnable = new BukkitRunnable() {
			@Override
			public void run() {
				try {
					if (connection != null && !connection.isClosed()) {
						connection.createStatement().execute("SELECT 1");
					}
				} catch (SQLException e) {
					connection = getSQLConnection();
				}
			}
		};

		runnable.runTaskTimerAsynchronously(plugin, 60 * 20, 60 * 20);
	}

	public static void cancelConnectionMaintainer() {
		runnable.cancel();
		runnable = null;

		try {
			if (connection != null && !connection.isClosed()) {
				connection.close();
			}
		} catch (SQLException e) {
			if (RPPersonas.DEBUGGING) {
				e.printStackTrace();
			}
		}
	}

	protected static void close(PreparedStatement ps, ResultSet rs) {
		try {
			if (ps != null)
				ps.close();
			if (rs != null)
				rs.close();
		} catch (SQLException ex) {
			Errors.close(plugin, ex);
		}
	}

}
