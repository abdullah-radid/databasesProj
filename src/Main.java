import API.*;

import java.sql.SQLException;

public class Main {

    public static void main(String[] args)  {
        LibraryAPI api = new LibraryAPI();
        if (api.testConnection()) {
            System.out.println("Connected to Database");
        } else {
            System.err.println("Failed to Connect to Database");

        }


    }
}