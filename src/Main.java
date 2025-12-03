import API.*;
import UI.MainPage;

import javax.swing.*;
import java.sql.SQLException;

public class Main {

    public static void main(String[] args)  {

        /// true if working
        if (!Database.testConnection() && JOptionPane.YES_OPTION != JOptionPane.showConfirmDialog(
                    null,
                    "Do you still want to open the UI?",
                    "DB Connection Failed",
                    JOptionPane.YES_NO_OPTION
            ))System.exit(1);
        new MainPage();


    }
}