package UI;

import UI.Panels.*;

import javax.swing.*;

public class MainPage extends JFrame {

    public MainPage() {
        super("Library Management System");
        this.setSize(1200, 800);
        this.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        this.setLocationRelativeTo(null);

        JTabbedPane tabbedPane = new JTabbedPane();

        tabbedPane.addTab("Members", new MembersPanel());
        tabbedPane.addTab("Staffs",new StaffPanel());
        tabbedPane.addTab("Rooms",new RoomsPanel());
        tabbedPane.addTab("Books", new BooksPanel());

        tabbedPane.addTab("Loans", new LoansPanel());
        tabbedPane.addTab("Fines", new FinesPanel());
        tabbedPane.addTab("Reports", new ReportsPanel());

        this.add(tabbedPane);
        this.setVisible(true);
    }
}
