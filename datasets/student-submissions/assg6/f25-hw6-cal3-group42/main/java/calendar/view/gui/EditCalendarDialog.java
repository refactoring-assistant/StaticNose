package calendar.view.gui;

import calendar.controller.gui.Features;
import java.awt.BorderLayout;
import java.awt.FlowLayout;
import java.awt.GridLayout;
import java.util.List;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.border.EmptyBorder;

/**
 * Dialog for editing an existing calendar's name or timezone.
 */
class EditCalendarDialog extends JDialog {

  private final JPanel formPanel;
  private final Features features;
  private boolean modified = false;

  private JComboBox<String> calendarSelector;
  private JTextField newNameField;
  private JComboBox<String> newTimezoneCombo;

  /**
   * Constructor to initialize the edit calendar dialogue box.
   *
   * @param parent   the caller JFrame who called this dialogue box
   * @param features the interface defining all callbacks
   */
  public EditCalendarDialog(JFrame parent, Features features) {
    super(parent, "Edit Calendar", true);
    this.features = features;

    setSize(450, 250);
    setLocationRelativeTo(parent);

    setLayout(new BorderLayout(10, 10));
    formPanel = new JPanel(new GridLayout(3, 2, 10, 10));
    formPanel.setBorder(new EmptyBorder(20, 20, 20, 20));
    initializeUi();
  }

  /**
   * Create the UI for this dialogue box.
   */
  private void initializeUi() {

    instructionLabel();
    selectCalendar();
    newName();
    newTimezone();

    add(formPanel, BorderLayout.CENTER);

    JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
    JButton saveButton = new JButton("Save");
    saveButton.addActionListener(e -> saveChanges());
    JButton cancelButton = new JButton("Cancel");
    cancelButton.addActionListener(e -> dispose());

    buttonPanel.add(saveButton);
    buttonPanel.add(cancelButton);
    add(buttonPanel, BorderLayout.SOUTH);
  }

  /**
   * Add an instruction to help user understand the feature.
   */
  private void instructionLabel() {
    JLabel instructionLabel = new JLabel(
        "<html><i>Leave fields empty to keep current values.</i></html>");
    instructionLabel.setBorder(new EmptyBorder(0, 20, 10, 20));
    add(instructionLabel, BorderLayout.NORTH);
  }

  /**
   * Select the calendar to edit.
   */
  private void selectCalendar() {
    formPanel.add(new JLabel("Select Calendar:*"));
    List<String> calendars = features.getCalendarNames();
    calendarSelector = new JComboBox<>(calendars.toArray(new String[0]));
    calendarSelector.setSelectedItem(features.getCurrentCalendarName());
    formPanel.add(calendarSelector);
  }

  /**
   * Give the new name to the calendar, blank if no change.
   */
  private void newName() {
    formPanel.add(new JLabel("New Name:"));
    newNameField = new JTextField(20);
    formPanel.add(newNameField);
  }

  /**
   * Give the new timezone for the calendar, blank if no change.
   */
  private void newTimezone() {
    formPanel.add(new JLabel("New Timezone:"));
    String[] commonTimezones = {
        "",
        "America/New_York",
        "America/Chicago",
        "America/Denver",
        "America/Los_Angeles",
        "Europe/London",
        "Europe/Paris",
        "Asia/Tokyo",
        "Asia/Shanghai",
        "Australia/Sydney"
    };
    newTimezoneCombo = new JComboBox<>(commonTimezones);
    newTimezoneCombo.setEditable(true);
    formPanel.add(newTimezoneCombo);
  }

  /**
   * Save the changes and call the features to implement model changes.
   */
  private void saveChanges() {
    String selectedCalendar = (String) calendarSelector.getSelectedItem();
    if (selectedCalendar == null) {
      JOptionPane.showMessageDialog(this,
          "Please select a calendar to edit", "Validation Error",
          JOptionPane.ERROR_MESSAGE);
      return;
    }

    String newName = newNameField.getText().trim();
    String newTimezone = (String) newTimezoneCombo.getSelectedItem();
    if (newTimezone != null) {
      newTimezone = newTimezone.trim();
    }

    if (newName.isEmpty() && (newTimezone == null || newTimezone.isEmpty())) {
      JOptionPane.showMessageDialog(this,
          "Please provide at least one new value", "Validation Error",
          JOptionPane.ERROR_MESSAGE);
      return;
    }

    try {
      features.editCalendar(selectedCalendar, newName.isEmpty() ? null : newName,
          (newTimezone == null || newTimezone.isEmpty()) ? null : newTimezone);
      modified = true;
      JOptionPane.showMessageDialog(this,
          "Calendar updated successfully!", "Success",
          JOptionPane.INFORMATION_MESSAGE);
      dispose();
    } catch (Exception e) {
      JOptionPane.showMessageDialog(this,
          "Error updating calendar: " + e.getMessage(),
          "Error", JOptionPane.ERROR_MESSAGE);
    }
  }

  /**
   * Returns if this panel was modified or not.
   *
   * @return true if modified
   */
  public boolean wasModified() {
    return modified;
  }
}
