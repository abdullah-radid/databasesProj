package UI;

import javax.swing.*;
import java.awt.*;


/// Utility functions
public final class Utility {
    // Helper method to add label and field to dialog
    public static void addLabelAndField(JDialog dialog, GridBagConstraints gbc,
                                         String labelText, JComponent field, int row) {
        gbc.gridx = 0;
        gbc.gridy = row;
        dialog.add(new JLabel(labelText), gbc);
        gbc.gridx = 1;
        dialog.add(field, gbc);
    }
}
