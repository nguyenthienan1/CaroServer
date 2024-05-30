/**
 * 
 */
package db;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.sql.Statement;

public class SQLConnection {
	public Statement statement;

	public SQLConnection() throws Exception {
		Connection conn = DriverManager.getConnection("jdbc:mariadb://localhost/caro", "root", "");
		statement = conn.createStatement();
	}

	public boolean executeSQLUpdate(String sql) {
		try {
			statement.executeUpdate(sql);
		} catch (SQLException e) {
			System.out.print("Can't update db");
			e.printStackTrace();
			return false;
		}
		return true;
	}

	public void checkData(String str) throws Exception {
		ResultSet rs = statement.executeQuery(str);
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
			System.out.println("");
		}
	}
}
