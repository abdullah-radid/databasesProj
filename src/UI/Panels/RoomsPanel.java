package UI.Panels;

import API.DTO.RoomRecord;
import API.RoomsAPI;
import UI.Utility;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.sql.SQLException;
import java.util.List;

public class RoomsPanel extends JPanel {

    public RoomsPanel() {
        super(new BorderLayout());

        JPanel topPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        JButton addBtn = new JButton("Add Room");
        JButton refreshBtn = new JButton("Refresh");

        topPanel.add(addBtn);
        topPanel.add(refreshBtn);

        String[] columns = { "Room ID", "Room Name", "Capacity" };
        DefaultTableModel model = new DefaultTableModel(columns, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };
        JTable table = new JTable(model);
        JScrollPane scrollPane = new JScrollPane(table);

        refreshBtn.addActionListener(e -> refreshTable(model));
        addBtn.addActionListener(e -> showAddDialog(this, model));

        refreshTable(model);

        this.add(topPanel, BorderLayout.NORTH);
        this.add(scrollPane, BorderLayout.CENTER);
    }

    private void refreshTable(DefaultTableModel model) {
        try {
            List<RoomRecord> rooms = RoomsAPI.getAllRooms();
            model.setRowCount(0);
            for (RoomRecord room : rooms) {
                model.addRow(new Object[] { room.roomId(), room.roomName(), room.capacity() });
            }
        } catch (SQLException e) {
            JOptionPane.showMessageDialog(this, "Error loading rooms: " + e.getMessage(), "Error",
                    JOptionPane.ERROR_MESSAGE);
        }
    }

    private void showAddDialog(JPanel parent, DefaultTableModel model) {
        JDialog dialog = new JDialog((Frame) SwingUtilities.getWindowAncestor(parent), "Add New Room", true);
        dialog.setSize(300, 200);
        dialog.setLayout(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(5, 5, 5, 5);
        gbc.anchor = GridBagConstraints.WEST;

        JTextField nameField = new JTextField(15);
        JTextField capacityField = new JTextField(5);

        int row = 0;
        Utility.addLabelAndField(dialog, gbc, "Room Name:", nameField, row++);
        Utility.addLabelAndField(dialog, gbc, "Capacity:", capacityField, row++);

        JButton saveBtn = new JButton("Save");
        gbc.gridx = 1;
        gbc.gridy = row;
        dialog.add(saveBtn, gbc);

        saveBtn.addActionListener(e -> {
            try {
                int cap = Integer.parseInt(capacityField.getText().trim());
                int id = RoomsAPI.addRoom(nameField.getText().trim(), cap);
                if (id > 0) {
                    JOptionPane.showMessageDialog(dialog, "Room added! ID: " + id);
                    dialog.dispose();
                    refreshTable(model);
                }
            } catch (NumberFormatException ex) {
                JOptionPane.showMessageDialog(dialog, "Capacity must be a number.", "Input Error",
                        JOptionPane.ERROR_MESSAGE);
            } catch (SQLException ex) {
                JOptionPane.showMessageDialog(dialog, "Error: " + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
            }
        });

        dialog.setLocationRelativeTo(parent);
        dialog.setVisible(true);
    }
}