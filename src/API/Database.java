package API;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

/**
 * DB helper for MySQL using DriverManager.
 * Adjust USERNAME, PASSWORD to your info.
 *  @apiNote java doesnt have static classes so I just marked this final
*/
public final class Database {
    private static final String URL = "jdbc:mysql://localhost:3306/library_db";
    private static final String USERNAME = "root";
    private static final String PASSWORD = null; // set your password


    /// Gets mysql connection using the privately set username and pass
    public static Connection getConnection() throws SQLException {
        if (USERNAME == null) System.err.println("You didnt set your database USERNAME in Database.java");
        if (PASSWORD == null) System.err.println("You didnt set your database PASSWORD in Database.java");
        return DriverManager.getConnection(URL, USERNAME, PASSWORD);
    }

    /**
     * Tests database connection.
     * @return true if all good
     * @throws SQLException if theres a problem connected to the db <br>
     * If there is then make sure mysql is running, and make sure you set USERNAME and PASSWORD in this file
     */
    public static boolean testConnection() {
        try (Connection conn = Database.getConnection()) {
            System.out.println("Connected to " + conn.getMetaData().getDatabaseProductName());
            return conn != null && !conn.isClosed();
        } catch (SQLException e) {
            System.err.println("Failed to connect to the database\nMake sure the username and password in API\\Database.java is correct");
            return false;
        }
    }


    /// @apiNote  close ResultSet and parent Connection
    public static ResultSet executeQuery(Connection conn, String sql, Object... params) throws SQLException {
        PreparedStatement ps = conn.prepareStatement(sql);
        setParams(ps, params);
        return ps.executeQuery();
    }


    public static int executeUpdate(Connection conn, String sql, Object... params) throws SQLException {
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            setParams(ps, params);
            return ps.executeUpdate();
        }
    }

    public static CallableStatement prepareCall(Connection conn, String callSql) throws SQLException {
        return conn.prepareCall(callSql);
    }

    private static void setParams(PreparedStatement ps, Object... params) throws SQLException {
        if (params == null) return;
        for (int i = 0; i < params.length; i++) {
            ps.setObject(i + 1, params[i]);
        }
    }
}
