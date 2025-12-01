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
    private static final String USERNAME = null;
    private static final String PASSWORD = null; // set your password


    /// Gets mysql connection using the privately set username and pass
    public static Connection getConnection() throws SQLException {
        if (USERNAME == null) System.err.println("You didnt set your database USERNAME in Database.java");
        if (PASSWORD == null) System.err.println("You didnt set your database PASSWORD in Database.java");
        return DriverManager.getConnection(URL, USERNAME, PASSWORD);
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
