package calendar.view;

import java.awt.BorderLayout;
import java.awt.FlowLayout;
import java.awt.Frame;
import java.awt.GridLayout;
import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.JTextField;

/**
 * A modal dialog for creating new calendar events.
 * This dialog provides fields for all event details including name, date, time,
 * location, status, and recurrence options.
 */
public class CreateEventDialog extends JDialog {

  private final JTextField nameField;
  private final JCheckBox allDayCheckBox;
  private final JTextField startDateField;
  private final JTextField startTimeField;
  private final JTextField endDateField;
  private final JTextField endTimeField;
  private final JComboBox<String> locationField;
  private final JTextArea descriptionArea;
  private final JComboBox<String> statusDropdown;

  private final JCheckBox recurringCheckBox;
  private final JTextField recurrenceDaysField;
  private final JTextField recurrenceEndField;

  private boolean confirmed = false;

  /**
   * Constructs a new CreateEventDialog.
   *
   * @param owner the Frame from which the dialog is displayed
   */
  public CreateEventDialog(Frame owner) {
    super(owner, "Event Details", true);
    this.setSize(500, 650);
    this.setLayout(new BorderLayout(10, 10));

    JPanel formPanel = new JPanel(new GridLayout(0, 2, 10, 10));
    formPanel.setBorder(BorderFactory.createEmptyBorder(15, 15, 15, 15));

    formPanel.add(new JLabel("Event Name:"));
    nameField = new JTextField();
    formPanel.add(nameField);

    formPanel.add(new JLabel("All Day Event?"));
    allDayCheckBox = new JCheckBox("Yes (08:00 - 17:00)");
    formPanel.add(allDayCheckBox);

    formPanel.add(new JLabel("Start Date (YYYY-MM-DD):"));
    startDateField = new JTextField(java.time.LocalDate.now().toString());
    formPanel.add(startDateField);

    formPanel.add(new JLabel("Start Time (HH:MM):"));
    startTimeField = new JTextField("12:00");
    formPanel.add(startTimeField);

    formPanel.add(new JLabel("End Date (YYYY-MM-DD):"));
    endDateField = new JTextField(java.time.LocalDate.now().toString());
    formPanel.add(endDateField);

    formPanel.add(new JLabel("End Time (HH:MM):"));
    endTimeField = new JTextField("13:00");
    formPanel.add(endTimeField);

    formPanel.add(new JLabel("Location:"));
    locationField = new JComboBox<>(new String[] {"Online", "Physical"});
    formPanel.add(locationField);

    formPanel.add(new JLabel("Status:"));
    statusDropdown = new JComboBox<>(new String[] {"Public", "Private"});
    formPanel.add(statusDropdown);

    formPanel.add(new JLabel("Description:"));
    descriptionArea = new JTextArea(3, 20);
    descriptionArea.setLineWrap(true);
    formPanel.add(new JScrollPane(descriptionArea));

    formPanel.add(new JLabel("Is Recurring?"));
    recurringCheckBox = new JCheckBox("Yes");
    formPanel.add(recurringCheckBox);

    formPanel.add(new JLabel("Days (e.g., MWF):"));
    recurrenceDaysField = new JTextField();
    recurrenceDaysField.setEnabled(false);
    formPanel.add(recurrenceDaysField);

    formPanel.add(new JLabel("End Date or Count:"));
    recurrenceEndField = new JTextField();
    recurrenceEndField.setEnabled(false);
    formPanel.add(recurrenceEndField);

    this.add(formPanel, BorderLayout.CENTER);

    JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
    JButton saveButton = new JButton("Create");
    JButton cancelButton = new JButton("Cancel");

    buttonPanel.add(saveButton);
    buttonPanel.add(cancelButton);
    this.add(buttonPanel, BorderLayout.SOUTH);

    allDayCheckBox.addActionListener(e -> {
      boolean isAllDay = allDayCheckBox.isSelected();

      startTimeField.setEnabled(!isAllDay);
      endTimeField.setEnabled(!isAllDay);
      endDateField.setEnabled(!isAllDay);

      if (isAllDay) {
        startTimeField.setText("08:00");
        endTimeField.setText("17:00");
        endDateField.setText(startDateField.getText());
      }
    });

    recurringCheckBox.addActionListener(e -> {
      boolean isChecked = recurringCheckBox.isSelected();
      recurrenceDaysField.setEnabled(isChecked);
      recurrenceEndField.setEnabled(isChecked);
    });

    saveButton.addActionListener(e -> {
      confirmed = true;
      this.setVisible(false);
    });

    cancelButton.addActionListener(e -> {
      confirmed = false;
      this.setVisible(false);
    });
    this.setLocationRelativeTo(owner);
  }

  /**
   * Checks if the user confirmed the dialog action (clicked Create).
   *
   * @return true if confirmed, false otherwise
   */
  public boolean isConfirmed() {
    return confirmed;
  }

  /**
   * Checks if the user selected the "All Day" option.
   *
   * @return true if all day is selected, false otherwise
   */
  public boolean isAllDay() {
    return allDayCheckBox.isSelected();
  }

  /**
   * Retrieves the event name entered by the user.
   *
   * @return the event name
   */
  public String getEventName() {
    return nameField.getText();
  }

  /**
   * Retrieves the start date entered by the user.
   *
   * @return the start date string
   */
  public String getStartDate() {
    return startDateField.getText();
  }

  /**
   * Retrieves the start time entered by the user.
   *
   * @return the start time string
   */
  public String getStartTime() {
    return startTimeField.getText();
  }

  /**
   * Retrieves the end date entered by the user.
   *
   * @return the end date string
   */
  public String getEndDate() {
    return endDateField.getText();
  }

  /**
   * Retrieves the end time entered by the user.
   *
   * @return the end time string
   */
  public String getEndTime() {
    return endTimeField.getText();
  }

  /**
   * Retrieves the selected location type.
   *
   * @return the selected location string
   */
  public String getEventLocation() {
    return (String) locationField.getSelectedItem();
  }

  /**
   * Retrieves the description entered by the user.
   *
   * @return the description text
   */
  public String getDescription() {
    return descriptionArea.getText();
  }

  /**
   * Retrieves the selected event status.
   *
   * @return the status string
   */
  public String getEventStatus() {
    return (String) statusDropdown.getSelectedItem();
  }

  /**
   * Checks if the recurrence option is selected.
   *
   * @return true if recurring, false otherwise
   */
  public boolean isRecurring() {
    return recurringCheckBox.isSelected();
  }

  /**
   * Retrieves the recurrence days entered by the user.
   *
   * @return the recurrence days string (e.g., "MWF")
   */
  public String getRecurrenceDays() {
    return recurrenceDaysField.getText();
  }

  /**
   * Retrieves the recurrence end condition entered by the user.
   *
   * @return the recurrence end string (date or count)
   */
  public String getRecurrenceEnd() {
    return recurrenceEndField.getText();
  }
}