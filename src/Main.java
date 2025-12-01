import API.*;

import java.sql.SQLException;

public class Main {

    public static void main(String[] args) throws SQLException {
        LibraryAPI api = new LibraryAPI();
        if (api.testConnection()) {
            System.out.println("Database is Working");
        } else {
            System.err.println("Database is Failing");

        }

    }
}