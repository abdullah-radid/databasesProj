package UI;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.sql.SQLException;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import API.*;
import API.DTO.*;
import UI.Panels.*;


public class MainPage extends JFrame {




    // Main Application Window
    public MainPage() {
        super("Library Management System");
        this.setSize(1200, 800);
        this.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        this.setLocationRelativeTo(null);

        JTabbedPane tabbedPane = new JTabbedPane();

        // Add all panels
        tabbedPane.addTab("Books", new BooksPanel());
        tabbedPane.addTab("Members",new MembersPanel());
        tabbedPane.addTab("Loans", new LoansPanel());
        tabbedPane.addTab("Fines", new FinesPanel());
        tabbedPane.addTab("Reservations", new ReservationsPanel());
        tabbedPane.addTab("Study Rooms", createStudyRoomPanel());
        tabbedPane.addTab("Reports", new ReportsPanel());

        this.add(tabbedPane);
        this.setVisible(true);
    }


    // ==================== STUDY ROOM PANEL ====================
    private static JPanel createStudyRoomPanel() {
        JPanel panel = new JPanel(new BorderLayout());

        JPanel topPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        JButton viewReservationsBtn = new JButton("View Room Reservations");
        JButton searchByDateBtn = new JButton("Search by Date");

        topPanel.add(viewReservationsBtn);
        topPanel.add(searchByDateBtn);

        String[] columns = {"Reservation ID", "Room Type", "Time Slot", "Reservation Date", "Member Name", "Status"};
        DefaultTableModel model = new DefaultTableModel(columns, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };
        JTable table = new JTable(model);
        JScrollPane scrollPane = new JScrollPane(table);

        viewReservationsBtn.addActionListener(e -> {
            try {
                List<Map<String, Object>> reservations = LibraryAPI.searchReservations(null, null);
                model.setRowCount(0);
                for (Map<String, Object> res : reservations) {
                    model.addRow(new Object[]{
                            res.get("reservation_id"),
                            res.get("room_type"),
                            res.get("time_slot"),
                            res.get("reservation_date"),
                            res.get("Member_Name"),
                            res.get("status")
                    });
                }
            } catch (SQLException ex) {
                JOptionPane.showMessageDialog(panel, "Error loading reservations: " + ex.getMessage(),
                        "Error", JOptionPane.ERROR_MESSAGE);
            }
        });

        searchByDateBtn.addActionListener(e -> showSearchRoomByDateDialog(panel, model));

        panel.add(topPanel, BorderLayout.NORTH);
        panel.add(scrollPane, BorderLayout.CENTER);

        return panel;
    }

    private static void showSearchRoomByDateDialog(JPanel parent, DefaultTableModel model) {
        JDialog dialog = new JDialog((Frame) SwingUtilities.getWindowAncestor(parent),
                "Search Room Reservations by Date", true);
        dialog.setSize(300, 150);
        dialog.setLayout(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(5, 5, 5, 5);
        gbc.anchor = GridBagConstraints.WEST;

        JTextField dateField = new JTextField(20);
        dateField.setToolTipText("Format: YYYY-MM-DD");
        Utility.addLabelAndField(dialog, gbc, "Date (YYYY-MM-DD):", dateField, 0);

        JButton searchBtn = new JButton("Search");
        JButton cancelBtn = new JButton("Cancel");
        gbc.gridx = 0;
        gbc.gridy = 1;
        gbc.gridwidth = 2;
        gbc.anchor = GridBagConstraints.CENTER;
        JPanel buttonPanel = new JPanel(new FlowLayout());
        buttonPanel.add(searchBtn);
        buttonPanel.add(cancelBtn);
        dialog.add(buttonPanel, gbc);

        searchBtn.addActionListener(e -> {
            try {
                java.sql.Date searchDate = java.sql.Date.valueOf(dateField.getText());
                List<Map<String, Object>> reservations = LibraryAPI.searchReservations(searchDate, null);
                model.setRowCount(0);
                for (Map<String, Object> res : reservations) {
                    model.addRow(new Object[]{
                            res.get("reservation_id"),
                            res.get("room_type"),
                            res.get("time_slot"),
                            res.get("reservation_date"),
                            res.get("Member_Name"),
                            res.get("status")
                    });
                }
                dialog.dispose();
            } catch (IllegalArgumentException ex) {
                JOptionPane.showMessageDialog(dialog, "Invalid date format. Use YYYY-MM-DD",
                        "Error", JOptionPane.ERROR_MESSAGE);
            } catch (SQLException ex) {
                JOptionPane.showMessageDialog(dialog, "Error searching: " + ex.getMessage(),
                        "Error", JOptionPane.ERROR_MESSAGE);
            }
        });

        cancelBtn.addActionListener(e -> dialog.dispose());
        dialog.setLocationRelativeTo(parent);
        dialog.setVisible(true);
    }


}
