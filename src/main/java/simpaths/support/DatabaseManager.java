package simpaths.support;

import java.io.File;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class DatabaseManager {
    public static Connection getConnection() throws SQLException {
        Connection conn = null;
        try {
            Class.forName("org.h2.Driver");
            conn = DriverManager.getConnection("jdbc:h2:file:./input" + File.separator + "input;TRACE_LEVEL_FILE=0;TRACE_LEVEL_SYSTEM_OUT=0;AUTO_SERVER=TRUE", "sa", "");
        } catch (ClassNotFoundException e) {
            System.out.println( "ERROR: Class not found: " + e.getMessage() + "\nCheck that the input.h2.db "
                + "exists in the input folder.  If not, unzip the input.h2.zip file and store the resulting "
                + "input.h2.db in the input folder!\n");
        } catch(SQLException e) {
            throw new IllegalArgumentException("SQL Exception thrown! " + e.getMessage());
        } finally {
            try {
                if (conn != null) { conn.close(); }
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }
        return conn;
    }
}
