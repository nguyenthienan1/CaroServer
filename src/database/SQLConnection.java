/**
 * 
 */
package database;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.sql.Statement;

public class SQLConnection {
	private static SQLConnection instance;
	private Connection connection;
	public static String host = "localhost";
	public static String db = "caro";
	public static String user = "root";
	public static String pass = "";

	public SQLConnection() {
	}

	public static SQLConnection gI() {
		return instance;
	}

	public static void connect() throws SQLException {
		instance = new SQLConnection();
		instance.setupConnection();
		System.out.println("Connect database successful");
	}

	private void setupConnection() throws SQLException {
		String url = "jdbc:mariadb://" + host + "/" + db;
		connection = DriverManager.getConnection(url, user, pass);
	}

	public Connection getConnection() {
		return connection;
	}

	public void checkData(String str) {
		try (Statement statement = connection.createStatement()) {
			try (ResultSet rs = statement.executeQuery(str)) {
				ResultSetMetaData metadata = rs.getMetaData();

				for (int i = 0; i < metadata.getColumnCount(); i++) {
					System.out.print("\t" + metadata.getColumnLabel(i + 1));
				}
				System.out.println("\n----------------------------------");

				while (rs.next()) {
					for (int i = 0; i < metadata.getColumnCount(); i++) {
						Object value = rs.getObject(i + 1);
						if (value == null) {
							System.out.print("\t");
						} else {
							System.out.print("\t" + value.toString());
						}
					}
					System.out.println();
				}
			}
		} catch (SQLException e) {
		}
	}
}
