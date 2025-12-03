import API.*;
import UI.MainPage;

import java.sql.SQLException;

public class Main {

    public static void main(String[] args)  {
        if (Database.testConnection()) {
            System.out.println("Connected to Database");
        } else {
            System.err.println("Failed to Connect to Database");

        }

        new MainPage();


    }
}