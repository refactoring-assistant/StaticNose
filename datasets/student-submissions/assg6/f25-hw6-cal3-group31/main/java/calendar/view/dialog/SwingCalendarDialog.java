package calendar.view.dialog;

import java.awt.Component;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JTextField;

/**
 * Swing implementation of a calendar creation dialog.
 * Provides a user-friendly interface for creating new calendars.
 * Does not depend on model classes to maintain MVC separation.
 */
public class SwingCalendarDialog implements IntDialog<CalendarDialogResult> {
  private final Set<String> existingCalendarNames;
  private Component parent;
  private CalendarDialogResult result;

  /**
   * Constructs a calendar dialog.
   *
   * @param existingCalendarNames set of existing calendar names for validation
   */
  public SwingCalendarDialog(Set<String> existingCalendarNames) {
    this.existingCalendarNames = existingCalendarNames;
  }

  @Override
  public void setParent(Component parent) {
    this.parent = parent;
  }

  @Override
  public CalendarDialogResult showDialog() {
    result = null;

    final JPanel panel = new JPanel(new GridBagLayout());
    GridBagConstraints gbc = new GridBagConstraints();
    gbc.insets = new Insets(5, 5, 5, 5);
    gbc.fill = GridBagConstraints.HORIZONTAL;

    // Calendar name field
    gbc.gridx = 0;
    gbc.gridy = 0;
    gbc.anchor = GridBagConstraints.WEST;
    panel.add(new JLabel("Calendar Name:"), gbc);

    gbc.gridx = 1;
    gbc.weightx = 1.0;
    JTextField nameField = new JTextField(20);
    panel.add(nameField, gbc);

    // Timezone selection
    gbc.gridx = 0;
    gbc.gridy = 1;
    gbc.weightx = 0.0;
    panel.add(new JLabel("Timezone:"), gbc);

    gbc.gridx = 1;
    gbc.weightx = 1.0;

    // Get common timezones
    List<String> commonTimezones = getCommonTimezones();
    JComboBox<String> timezoneCombo = new JComboBox<>(commonTimezones.toArray(new String[0]));
    timezoneCombo.setEditable(true);

    // Set default to system timezone
    timezoneCombo.setSelectedItem(ZoneId.systemDefault().getId());
    panel.add(timezoneCombo, gbc);

    // Show dialog
    int option = JOptionPane.showConfirmDialog(
        parent,
        panel,
        "Create New Calendar",
        JOptionPane.OK_CANCEL_OPTION,
        JOptionPane.PLAIN_MESSAGE
    );

    if (option == JOptionPane.OK_OPTION) {
      String name = nameField.getText().trim();
      String timezoneStr = (String) timezoneCombo.getSelectedItem();

      // Validate input
      if (name.isEmpty()) {
        JOptionPane.showMessageDialog(parent,
            "Calendar name cannot be empty.",
            "Invalid Input",
            JOptionPane.ERROR_MESSAGE);
        return showDialog(); // Retry
      }

      // Validate timezone
      try {
        ZoneId.of(timezoneStr); // Just validate, don't store
      } catch (Exception e) {
        JOptionPane.showMessageDialog(parent,
            "Invalid timezone: " + timezoneStr + "\n"
                + "Please use a valid timezone ID (e.g., America/New_York, Europe/London, UTC)",
            "Invalid Timezone",
            JOptionPane.ERROR_MESSAGE);
        return showDialog(); // Retry
      }

      // Check if calendar already exists
      if (existingCalendarNames.contains(name)) {
        JOptionPane.showMessageDialog(parent,
            "A calendar with the name '" + name + "' already exists.",
            "Duplicate Calendar",
            JOptionPane.ERROR_MESSAGE);
        return showDialog(); // Retry
      }

      result = new CalendarDialogResult(name, timezoneStr);
    }

    return result;
  }

  /**
   * Gets a list of common timezones for the dropdown.
   *
   * @return list of common timezone IDs
   */
  private List<String> getCommonTimezones() {
    List<String> timezones = new ArrayList<>();
    timezones.add("UTC");
    timezones.add("America/New_York");
    timezones.add("America/Chicago");
    timezones.add("America/Denver");
    timezones.add("America/Los_Angeles");
    timezones.add("Europe/London");
    timezones.add("Europe/Paris");
    timezones.add("Asia/Tokyo");
    timezones.add("Asia/Shanghai");
    timezones.add("Australia/Sydney");
    return timezones;
  }
}

