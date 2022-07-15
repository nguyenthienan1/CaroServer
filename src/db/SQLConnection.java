/**
 * 
 */
package db;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.Statement;

public class SQLConnection {
	public Statement st;

	public SQLConnection() throws Exception {
		// Load the JDBC driver.
		Class.forName("com.mysql.jdbc.Driver");
		System.out.println("Driver Loaded.");
		Connection conn = DriverManager.getConnection("jdbc:mysql://localhost/caro", "root", "14012002");
		System.out.println("Got Connection.");
		st = conn.createStatement();
	}

	public void executeSQLCommand(String sql) throws Exception {
		st.executeUpdate(sql);
	}

	public void checkData(String str) throws Exception {
		ResultSet rs = st.executeQuery(str);
		ResultSetMetaData metadata = rs.getMetaData();

		for (int i = 0; i < metadata.getColumnCount(); i++) {
			System.out.print("\t" + metadata.getColumnLabel(i + 1));
		}
		System.out.println("\n----------------------------------");

		while (rs.next()) {
			for (int i = 0; i < metadata.getColumnCount(); i++) {
				Object value = rs.getObject(i + 1);
				if (value == null) {
					System.out.print("\t       ");
				} else {
					System.out.print("\t" + value.toString().trim());
				}
			}
			System.out.println("");
		}
	}
}
