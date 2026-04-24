package calendar.view.dialog;

import java.awt.Component;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JTextArea;
import javax.swing.JTextField;

/**
 * Swing implementation of an event creation/edit dialog.
 * Provides a user-friendly interface for creating and editing events.
 * Does not depend on model classes to maintain MVC separation.
 */
public class SwingEventDialog implements IntDialog<EventDialogResult> {
  private Component parent;
  private final int initialYear;
  private final int initialMonth;
  private final int initialDay;
  private final EventDialogResult existingEvent;
  private EventDialogResult result;

  /**
   * Constructs an event dialog for creating a new event.
   *
   * @param initialYear  the initially selected year
   * @param initialMonth the initially selected month
   * @param initialDay   the initially selected day
   */
  public SwingEventDialog(int initialYear, int initialMonth, int initialDay) {
    this.initialYear = initialYear;
    this.initialMonth = initialMonth;
    this.initialDay = initialDay;
    this.existingEvent = null;
  }

  /**
   * Constructs an event dialog for editing an existing event.
   *
   * @param existingEvent the existing event data
   */
  public SwingEventDialog(EventDialogResult existingEvent) {
    this.initialYear = existingEvent.getStartYear();
    this.initialMonth = existingEvent.getStartMonth();
    this.initialDay = existingEvent.getStartDay();
    this.existingEvent = existingEvent;
  }

  @Override
  public void setParent(Component parent) {
    this.parent = parent;
  }

  @Override
  public EventDialogResult showDialog() {
    result = null;

    final JPanel panel = new JPanel(new GridBagLayout());
    GridBagConstraints gbc = new GridBagConstraints();
    gbc.insets = new Insets(5, 5, 5, 5);
    gbc.fill = GridBagConstraints.HORIZONTAL;

    int row = 0;

    // Subject field
    gbc.gridx = 0;
    gbc.gridy = row;
    gbc.anchor = GridBagConstraints.WEST;
    panel.add(new JLabel("Subject:*"), gbc);

    gbc.gridx = 1;
    gbc.weightx = 1.0;
    JTextField subjectField = new JTextField(20);
    if (existingEvent != null) {
      subjectField.setText(existingEvent.getSubject());
    }
    panel.add(subjectField, gbc);

    row++;

    // Start date
    gbc.gridx = 0;
    gbc.gridy = row;
    gbc.weightx = 0.0;
    panel.add(new JLabel("Start Date (YYYY-MM-DD):*"), gbc);

    gbc.gridx = 1;
    gbc.weightx = 1.0;
    JTextField startDateField = new JTextField(10);
    if (existingEvent != null) {
      startDateField.setText(String.format("%04d-%02d-%02d",
          existingEvent.getStartYear(), existingEvent.getStartMonth(),
          existingEvent.getStartDay()));
    } else {
      startDateField.setText(String.format("%04d-%02d-%02d",
          initialYear, initialMonth, initialDay));
    }
    panel.add(startDateField, gbc);

    row++;

    // Start time
    gbc.gridx = 0;
    gbc.gridy = row;
    gbc.weightx = 0.0;
    panel.add(new JLabel("Start Time (HH:MM):*"), gbc);

    gbc.gridx = 1;
    gbc.weightx = 1.0;
    JTextField startTimeField = new JTextField(5);
    if (existingEvent != null) {
      startTimeField.setText(String.format("%02d:%02d",
          existingEvent.getStartHour(), existingEvent.getStartMinute()));
    } else {
      startTimeField.setText("09:00");
    }
    panel.add(startTimeField, gbc);

    row++;

    // End date
    gbc.gridx = 0;
    gbc.gridy = row;
    gbc.weightx = 0.0;
    panel.add(new JLabel("End Date (YYYY-MM-DD):*"), gbc);

    gbc.gridx = 1;
    gbc.weightx = 1.0;
    JTextField endDateField = new JTextField(10);
    if (existingEvent != null) {
      endDateField.setText(String.format("%04d-%02d-%02d",
          existingEvent.getEndYear(), existingEvent.getEndMonth(), existingEvent.getEndDay()));
    } else {
      endDateField.setText(String.format("%04d-%02d-%02d",
          initialYear, initialMonth, initialDay));
    }
    panel.add(endDateField, gbc);

    row++;

    // End time
    gbc.gridx = 0;
    gbc.gridy = row;
    gbc.weightx = 0.0;
    panel.add(new JLabel("End Time (HH:MM):*"), gbc);

    gbc.gridx = 1;
    gbc.weightx = 1.0;
    JTextField endTimeField = new JTextField(5);
    if (existingEvent != null) {
      endTimeField.setText(String.format("%02d:%02d",
          existingEvent.getEndHour(), existingEvent.getEndMinute()));
    } else {
      endTimeField.setText("10:00");
    }
    panel.add(endTimeField, gbc);

    row++;

    // Description field
    gbc.gridx = 0;
    gbc.gridy = row;
    gbc.weightx = 0.0;
    panel.add(new JLabel("Description:"), gbc);

    gbc.gridx = 1;
    gbc.weightx = 1.0;
    JTextArea descriptionArea = new JTextArea(3, 20);
    if (existingEvent != null && existingEvent.getDescription() != null) {
      descriptionArea.setText(existingEvent.getDescription());
    }
    panel.add(descriptionArea, gbc);

    row++;

    // Location dropdown
    gbc.gridx = 0;
    gbc.gridy = row;
    gbc.weightx = 0.0;
    panel.add(new JLabel("Location:"), gbc);

    gbc.gridx = 1;
    gbc.weightx = 1.0;
    String[] locations = {"None", "PHYSICAL", "ONLINE"};
    JComboBox<String> locationCombo = new JComboBox<>(locations);
    if (existingEvent != null && existingEvent.getLocation() != null) {
      locationCombo.setSelectedItem(existingEvent.getLocation());
    }
    panel.add(locationCombo, gbc);

    row++;

    // Status dropdown
    gbc.gridx = 0;
    gbc.gridy = row;
    gbc.weightx = 0.0;
    panel.add(new JLabel("Status:"), gbc);

    gbc.gridx = 1;
    gbc.weightx = 1.0;
    String[] statuses = {"None", "PUBLIC", "PRIVATE"};
    JComboBox<String> statusCombo = new JComboBox<>(statuses);
    if (existingEvent != null && existingEvent.getStatus() != null) {
      statusCombo.setSelectedItem(existingEvent.getStatus());
    }
    panel.add(statusCombo, gbc);

    row++;

    // Recurring checkbox
    gbc.gridx = 0;
    gbc.gridy = row;
    gbc.gridwidth = 2;
    JCheckBox recurringCheckbox = new JCheckBox("Recurring Event");
    if (existingEvent != null) {
      recurringCheckbox.setSelected(existingEvent.isRecurring());
    }
    panel.add(recurringCheckbox, gbc);

    row++;

    // Show dialog and process result
    return showDialogAndProcess(panel, subjectField, startDateField, startTimeField,
        endDateField, endTimeField, descriptionArea, locationCombo, statusCombo,
        recurringCheckbox);
  }

  private EventDialogResult showDialogAndProcess(JPanel panel, JTextField subjectField,
                                                 JTextField startDateField,
                                                 JTextField startTimeField, JTextField endDateField,
                                                 JTextField endTimeField, JTextArea descriptionArea,
                                                 JComboBox<String> locationCombo,
                                                 JComboBox<String> statusCombo,
                                                 JCheckBox recurringCheckbox) {

    int option = JOptionPane.showConfirmDialog(
        parent,
        panel,
        existingEvent != null ? "Edit Event" : "Create New Event",
        JOptionPane.OK_CANCEL_OPTION,
        JOptionPane.PLAIN_MESSAGE
    );

    if (option == JOptionPane.OK_OPTION) {
      try {
        // Parse and validate inputs
        String subject = subjectField.getText().trim();
        if (subject.isEmpty()) {
          throw new IllegalArgumentException("Subject cannot be empty");
        }

        final int[] startDateParts = parseDate(startDateField.getText().trim());
        final int[] startTimeParts = parseTime(startTimeField.getText().trim());
        final int[] endDateParts = parseDate(endDateField.getText().trim());
        final int[] endTimeParts = parseTime(endTimeField.getText().trim());

        String description = descriptionArea.getText().trim();
        if (description.isEmpty()) {
          description = null;
        }

        String location = null;
        String locationStr = (String) locationCombo.getSelectedItem();
        if (!"None".equals(locationStr)) {
          location = locationStr;
        }

        String status = null;
        String statusStr = (String) statusCombo.getSelectedItem();
        if (!"None".equals(statusStr)) {
          status = statusStr;
        }

        boolean isRecurring = recurringCheckbox.isSelected();

        // For now, create a simple non-recurring event
        // Recurring event support can be added later
        result = new EventDialogResult(subject,
            startDateParts[0], startDateParts[1], startDateParts[2],
            startTimeParts[0], startTimeParts[1],
            endDateParts[0], endDateParts[1], endDateParts[2],
            endTimeParts[0], endTimeParts[1],
            description, location, status);

      } catch (Exception e) {
        JOptionPane.showMessageDialog(parent,
            "Invalid input: " + e.getMessage(),
            "Error",
            JOptionPane.ERROR_MESSAGE);
        return showDialog(); // Retry
      }
    }

    return result;
  }

  private int[] parseDate(String dateStr) {
    String[] parts = dateStr.split("-");
    if (parts.length != 3) {
      throw new IllegalArgumentException("Date must be in format YYYY-MM-DD");
    }
    int year = Integer.parseInt(parts[0]);
    int month = Integer.parseInt(parts[1]);
    int day = Integer.parseInt(parts[2]);
    return new int[] {year, month, day};
  }

  private int[] parseTime(String timeStr) {
    String[] parts = timeStr.split(":");
    if (parts.length != 2) {
      throw new IllegalArgumentException("Time must be in format HH:MM");
    }
    int hour = Integer.parseInt(parts[0]);
    int minute = Integer.parseInt(parts[1]);
    return new int[] {hour, minute};
  }
}
