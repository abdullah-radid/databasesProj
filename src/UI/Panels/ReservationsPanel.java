package UI.Panels;

import API.LibraryAPI;
import UI.Utility;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.sql.SQLException;
import java.util.List;
import java.util.Map;

public class ReservationsPanel extends JPanel{
    // ==================== RESERVATION MANAGEMENT PANEL ====================
    public ReservationsPanel() {
        super(new BorderLayout());

        JPanel topPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        JButton addBtn = new JButton("Add Reservation");
        JButton updateBtn = new JButton("Update Status");
        JButton removeBtn = new JButton("Cancel Reservation");
        JButton refreshBtn = new JButton("Refresh");

        topPanel.add(addBtn);
        topPanel.add(updateBtn);
        topPanel.add(removeBtn);
        topPanel.add(refreshBtn);

        String[] columns = {"Reservation ID", "Member ID", "Book ISBN", "Reservation Date", "Status"};
        DefaultTableModel model = new DefaultTableModel(columns, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };
        JTable table = new JTable(model);
        JScrollPane scrollPane = new JScrollPane(table);

        addBtn.addActionListener(e -> showAddReservationDialog(this, model));
        updateBtn.addActionListener(e -> showUpdateReservationDialog(this, table, model));
        removeBtn.addActionListener(e -> showCancelReservationDialog(this, table, model));
        refreshBtn.addActionListener(e -> refreshReservationTable(model));

        this.add(topPanel, BorderLayout.NORTH);
        this.add(scrollPane, BorderLayout.CENTER);

    }

    private static void showAddReservationDialog(JPanel parent, DefaultTableModel model) {
        JDialog dialog = new JDialog((Frame) SwingUtilities.getWindowAncestor(parent),
                "Add Reservation", true);
        dialog.setSize(350, 150);
        dialog.setLayout(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(5, 5, 5, 5);
        gbc.anchor = GridBagConstraints.WEST;

        JTextField memberIdField = new JTextField(20);
        JTextField isbnField = new JTextField(20);

        int row = 0;
        Utility.addLabelAndField(dialog, gbc, "Member ID:", memberIdField, row++);
        Utility.addLabelAndField(dialog, gbc, "Book ISBN:", isbnField, row++);

        JButton saveBtn = new JButton("Save");
        JButton cancelBtn = new JButton("Cancel");
        gbc.gridx = 0;
        gbc.gridy = row;
        gbc.gridwidth = 2;
        gbc.anchor = GridBagConstraints.CENTER;
        JPanel buttonPanel = new JPanel(new FlowLayout());
        buttonPanel.add(saveBtn);
        buttonPanel.add(cancelBtn);
        dialog.add(buttonPanel, gbc);

        saveBtn.addActionListener(e -> {
            try {
                int memberId = Integer.parseInt(memberIdField.getText());
                String isbn = isbnField.getText();
                if (LibraryAPI.reserveBook(memberId, isbn)) {
                    JOptionPane.showMessageDialog(dialog, "Reservation added successfully!");
                    dialog.dispose();
                }
            } catch (NumberFormatException ex) {
                JOptionPane.showMessageDialog(dialog, "Please enter valid member ID.",
                        "Invalid Input", JOptionPane.ERROR_MESSAGE);
            } catch (SQLException ex) {
                JOptionPane.showMessageDialog(dialog, "Error adding reservation: " + ex.getMessage(),
                        "Error", JOptionPane.ERROR_MESSAGE);
            }
        });

        cancelBtn.addActionListener(e -> dialog.dispose());
        dialog.setLocationRelativeTo(parent);
        dialog.setVisible(true);
    }

    private static void refreshReservationTable(DefaultTableModel model) {
        try {
            List<Map<String, Object>> reservations = LibraryAPI.getAllBookReservations();
            model.setRowCount(0);
            for (Map<String, Object> res : reservations) {
                model.addRow(new Object[]{
                        res.get("reservation_id"),
                        res.get("member_id"),
                        res.get("book_isbn"),
                        res.get("reservation_date"),
                        res.get("status")
                });
            }
        } catch (SQLException e) {
            JOptionPane.showMessageDialog(null, "Error loading reservations: " + e.getMessage(),
                    "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private static void showUpdateReservationDialog(JPanel parent, JTable table, DefaultTableModel model) {
        int selectedRow = table.getSelectedRow();
        if (selectedRow < 0) {
            JOptionPane.showMessageDialog(parent, "Please select a reservation to update.",
                    "No Selection", JOptionPane.WARNING_MESSAGE);
            return;
        }

        int reservationId = (Integer) model.getValueAt(selectedRow, 0);
        String currentStatus = (String) model.getValueAt(selectedRow, 4);

        JDialog dialog = new JDialog((Frame) SwingUtilities.getWindowAncestor(parent),
                "Update Reservation Status", true);
        dialog.setSize(300, 150);
        dialog.setLayout(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(5, 5, 5, 5);
        gbc.anchor = GridBagConstraints.WEST;

        JLabel idLabel = new JLabel("Reservation ID: " + reservationId);
        JComboBox<String> statusCombo = new JComboBox<>(new String[]{"Pending", "Fulfilled", "Cancelled"});
        statusCombo.setSelectedItem(currentStatus);

        gbc.gridx = 0;
        gbc.gridy = 0;
        dialog.add(idLabel, gbc);
        Utility.addLabelAndField(dialog, gbc, "Status:", statusCombo, 1);

        JButton saveBtn = new JButton("Save");
        JButton cancelBtn = new JButton("Cancel");
        gbc.gridx = 0;
        gbc.gridy = 2;
        gbc.gridwidth = 2;
        gbc.anchor = GridBagConstraints.CENTER;
        JPanel buttonPanel = new JPanel(new FlowLayout());
        buttonPanel.add(saveBtn);
        buttonPanel.add(cancelBtn);
        dialog.add(buttonPanel, gbc);

        saveBtn.addActionListener(e -> {
            try {
                if (LibraryAPI.updateReservationStatus(reservationId, (String) statusCombo.getSelectedItem())) {
                    JOptionPane.showMessageDialog(dialog, "Reservation updated successfully!");
                    dialog.dispose();
                    refreshReservationTable(model);
                }
            } catch (SQLException ex) {
                JOptionPane.showMessageDialog(dialog, "Error updating reservation: " + ex.getMessage(),
                        "Error", JOptionPane.ERROR_MESSAGE);
            }
        });

        cancelBtn.addActionListener(e -> dialog.dispose());
        dialog.setLocationRelativeTo(parent);
        dialog.setVisible(true);
    }

    private static void showCancelReservationDialog(JPanel parent, JTable table, DefaultTableModel model) {
        int selectedRow = table.getSelectedRow();
        if (selectedRow < 0) {
            JOptionPane.showMessageDialog(parent, "Please select a reservation to cancel.",
                    "No Selection", JOptionPane.WARNING_MESSAGE);
            return;
        }

        int reservationId = (Integer) model.getValueAt(selectedRow, 0);

        int confirm = JOptionPane.showConfirmDialog(parent,
                "Cancel reservation #" + reservationId + "?",
                "Confirm Cancellation", JOptionPane.YES_NO_OPTION);

        if (confirm == JOptionPane.YES_OPTION) {
            try {
                if (LibraryAPI.updateReservationStatus(reservationId, "Cancelled")) {
                    JOptionPane.showMessageDialog(parent, "Reservation cancelled successfully.");
                    refreshReservationTable(model);
                }
            } catch (SQLException e) {
                JOptionPane.showMessageDialog(parent, "Error cancelling reservation: " + e.getMessage(),
                        "Error", JOptionPane.ERROR_MESSAGE);
            }
        }
    }

}
