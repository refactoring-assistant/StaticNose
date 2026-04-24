package calendar.view.dialogs;

import java.awt.Color;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;

/**
 * Dialog for creating a new calendar with name and timezone.
 * Dark minimalist theme.
 */
public class CreateCalendarDialog extends JDialog {
  // Dark theme colors
  private static final Color BACKGROUND_DARK = new Color(30, 30, 30);
  private static final Color OFF_WHITE = new Color(240, 240, 235);
  private static final Color ACCENT_GRAY = new Color(60, 60, 60);

  private JTextField nameField;
  private JComboBox<String> timezoneComboBox;
  private boolean confirmed;

  /**
   * Constructs a create calendar dialog.
   *
   * @param parent the parent frame
   */
  public CreateCalendarDialog(JFrame parent) {
    super(parent, "Create New Calendar", true);
    this.confirmed = false;

    setupUi();
    getContentPane().setBackground(BACKGROUND_DARK);
    setSize(450, 220);
    setLocationRelativeTo(parent);
  }

  private void setupUi() {
    final JPanel panel = new JPanel(new GridBagLayout());
    panel.setBackground(BACKGROUND_DARK);
    GridBagConstraints gbc = new GridBagConstraints();
    gbc.insets = new Insets(10, 10, 10, 10);
    gbc.fill = GridBagConstraints.HORIZONTAL;

    // Calendar name
    gbc.gridx = 0;
    gbc.gridy = 0;
    JLabel nameLabel = new JLabel("Calendar Name:");
    nameLabel.setForeground(OFF_WHITE);
    panel.add(nameLabel, gbc);

    gbc.gridx = 1;
    nameField = new JTextField(20);
    nameField.setBackground(ACCENT_GRAY);
    nameField.setForeground(OFF_WHITE);
    nameField.setCaretColor(OFF_WHITE);
    panel.add(nameField, gbc);

    // Timezone
    gbc.gridx = 0;
    gbc.gridy = 1;
    JLabel tzLabel = new JLabel("Timezone:");
    tzLabel.setForeground(OFF_WHITE);
    panel.add(tzLabel, gbc);

    gbc.gridx = 1;
    String[] commonTimezones = {
        "America/New_York",
        "America/Chicago",
        "America/Denver",
        "America/Los_Angeles",
        "Europe/London",
        "Europe/Paris",
        "Asia/Tokyo",
        "Asia/Kolkata",
        "Australia/Sydney"
    };
    timezoneComboBox = new JComboBox<>(commonTimezones);
    timezoneComboBox.setEditable(true);
    timezoneComboBox.setBackground(ACCENT_GRAY);
    timezoneComboBox.setForeground(OFF_WHITE);
    panel.add(timezoneComboBox, gbc);

    // Buttons
    gbc.gridx = 0;
    gbc.gridy = 2;
    gbc.gridwidth = 2;
    JPanel buttonPanel = new JPanel();
    buttonPanel.setBackground(BACKGROUND_DARK);

    JButton createButton = createStyledButton("Create");
    createButton.addActionListener(e -> {
      if (nameField.getText().trim().isEmpty()) {
        return;
      }
      confirmed = true;
      dispose();
    });
    buttonPanel.add(createButton);

    JButton cancelButton = createStyledButton("Cancel");
    cancelButton.addActionListener(e -> dispose());
    buttonPanel.add(cancelButton);

    panel.add(buttonPanel, gbc);

    add(panel);
  }

  /**
   * Creates a styled button for the dialog.
   */
  private JButton createStyledButton(String text) {
    JButton button = new JButton(text);
    button.setBackground(ACCENT_GRAY);
    button.setForeground(OFF_WHITE);
    button.setFocusPainted(false);
    button.setBorder(BorderFactory.createCompoundBorder(
        BorderFactory.createLineBorder(new Color(80, 80, 80), 1),
        BorderFactory.createEmptyBorder(8, 20, 8, 20)
    ));
    return button;
  }

  /**
   * Checks if the user confirmed the dialog.
   *
   * @return true if Create was clicked
   */
  public boolean isConfirmed() {
    return confirmed;
  }

  /**
   * Gets the entered calendar name.
   *
   * @return the calendar name
   */
  public String getCalendarName() {
    return nameField.getText().trim();
  }

  /**
   * Gets the selected timezone.
   *
   * @return the timezone string
   */
  public String getTimezone() {
    Object selected = timezoneComboBox.getSelectedItem();
    return selected != null ? selected.toString() : "America/New_York";
  }
}