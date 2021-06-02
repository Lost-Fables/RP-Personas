package net.korvic.rppersonas.sql;

import co.lotc.core.util.HikariPool;
import net.korvic.rppersonas.RPPersonas;

import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;

public abstract class BaseSQL {

	protected final static String TABLE_PREFIX = "rpppersonas_";
	protected static HikariPool database = null;

	public static void init(String host, int port, String databaseName, String flags, String username, String password) {
		database = HikariPool.getHikariPool(host, port, databaseName, flags,
											username, password);
	}

	protected static void createTable(String SQLTable) {
		if (database != null) {
			try {
				Connection connection = database.getConnection();
				Statement s = connection.createStatement();
				s.execute(SQLTable);
				s.close();
			} catch (SQLException e) {
				e.printStackTrace();
			}
		} else {
			RPPersonas.getInstance().getLogger().warning("Fatal Error: No valid database connection on table creation.");
		}
	}


	/*protected static RPPersonas plugin = null;
	protected static HikariPool database;

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
				database = getSQLConnection();
		try {
			Statement s = database.createStatement();
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
			database = getSQLConnection();
			try {
				String stmt;
				stmt = "SELECT * FROM " + SQLTableName + ";";
				PreparedStatement ps = database.prepareStatement(stmt);
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
				plugin.getLogger().warning("Unable to connect with MariaDB for " + HOST + ":" + PORT + "/" + DATABASE + FLAGS+ " with information " + USER + ":" + PASSWORD + " | Trying MySQL instead.");
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
				plugin.getLogger().warning("Unable to connect with MySQL for " + HOST + ":" + PORT + "/" + DATABASE + FLAGS);
			} else {
				plugin.getLogger().info("Connected with MySQL.");
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
    */
}
