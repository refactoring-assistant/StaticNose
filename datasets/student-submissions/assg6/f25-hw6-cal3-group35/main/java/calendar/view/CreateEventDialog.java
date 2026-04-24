package calendar.view;

import java.awt.BorderLayout;
import java.awt.GridLayout;
import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.EnumSet;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JSpinner;
import javax.swing.JTextField;
import javax.swing.SpinnerNumberModel;
import javax.swing.border.EmptyBorder;

/**
 * Dialog for creating a new event.
 */
public class CreateEventDialog extends JDialog {

  private final JTextField subjectField;
  private final JComboBox<Integer> startHour;
  private final JComboBox<Integer> startMinute;
  private final JComboBox<Integer> endHour;
  private final JComboBox<Integer> endMinute;
  private final JTextField locationField;
  private final JTextField descriptionField;
  private final JCheckBox recurringCheckbox;
  private final JCheckBox[] dayCheckboxes;
  private final JSpinner countSpinner;
  private final LocalDate selectedDate;
  private boolean confirmed;

  /**
   * Constructs the dialog.
   *
   * @param parent the parent frame
   * @param date the selected date for the event
   */
  public CreateEventDialog(JFrame parent, LocalDate date) {
    super(parent, "Create Event", true);
    this.selectedDate = date;

    setSize(450, 500);
    setLocationRelativeTo(parent);
    setLayout(new BorderLayout(10, 10));

    JPanel fieldsPanel = new JPanel(new GridLayout(0, 2, 10, 10));
    fieldsPanel.setBorder(new EmptyBorder(20, 20, 10, 20));

    fieldsPanel.add(new JLabel("Event Subject:"));
    subjectField = new JTextField(20);
    fieldsPanel.add(subjectField);

    fieldsPanel.add(new JLabel("Date:"));
    fieldsPanel.add(new JLabel(date.toString()));

    fieldsPanel.add(new JLabel("Start Time (HH:MM):"));
    JPanel startTimePanel = new JPanel();
    startHour = createHourComboBox();
    startMinute = createMinuteComboBox();
    startTimePanel.add(startHour);
    startTimePanel.add(new JLabel(":"));
    startTimePanel.add(startMinute);
    fieldsPanel.add(startTimePanel);

    fieldsPanel.add(new JLabel("End Time (HH:MM):"));
    JPanel endTimePanel = new JPanel();
    endHour = createHourComboBox();
    endMinute = createMinuteComboBox();
    endTimePanel.add(endHour);
    endTimePanel.add(new JLabel(":"));
    endTimePanel.add(endMinute);
    fieldsPanel.add(endTimePanel);

    fieldsPanel.add(new JLabel("Location (optional):"));
    locationField = new JTextField(20);
    fieldsPanel.add(locationField);

    fieldsPanel.add(new JLabel("Description (optional):"));
    descriptionField = new JTextField(20);
    fieldsPanel.add(descriptionField);

    fieldsPanel.add(new JLabel("Recurring:"));
    recurringCheckbox = new JCheckBox();
    recurringCheckbox.addActionListener(e -> toggleRecurrence());
    fieldsPanel.add(recurringCheckbox);

    fieldsPanel.add(new JLabel("Repeat on days:"));
    JPanel daysPanel = new JPanel();
    dayCheckboxes = new JCheckBox[7];
    String[] dayLabels = {"Mon", "Tue", "Wed", "Thu", "Fri", "Sat", "Sun"};
    for (int i = 0; i < 7; i++) {
      dayCheckboxes[i] = new JCheckBox(dayLabels[i]);
      dayCheckboxes[i].setEnabled(false);
      daysPanel.add(dayCheckboxes[i]);
    }
    fieldsPanel.add(daysPanel);

    fieldsPanel.add(new JLabel("Number of occurrences:"));
    countSpinner = new JSpinner(new SpinnerNumberModel(1, 1, 100, 1));
    countSpinner.setEnabled(false);
    fieldsPanel.add(countSpinner);

    add(fieldsPanel, BorderLayout.CENTER);

    JPanel buttonPanel = new JPanel();
    buttonPanel.setBorder(new EmptyBorder(10, 20, 20, 20));

    JButton createButton = new JButton("Create");
    createButton.addActionListener(e -> {
      if (validateInput()) {
        confirmed = true;
        dispose();
      }
    });

    JButton cancelButton = new JButton("Cancel");
    cancelButton.addActionListener(e -> dispose());

    buttonPanel.add(createButton);
    buttonPanel.add(cancelButton);

    add(buttonPanel, BorderLayout.SOUTH);

    this.confirmed = false;
  }

  /**
   * Creates a combo box for hours.
   *
   * @return the hour combo box
   */
  private JComboBox<Integer> createHourComboBox() {
    Integer[] hours = new Integer[24];
    for (int i = 0; i < 24; i++) {
      hours[i] = i;
    }
    return new JComboBox<>(hours);
  }

  /**
   * Creates a combo box for minutes.
   *
   * @return the minute combo box
   */
  private JComboBox<Integer> createMinuteComboBox() {
    Integer[] minutes = new Integer[12];
    for (int i = 0; i < 12; i++) {
      minutes[i] = i * 5;
    }
    return new JComboBox<>(minutes);
  }

  /**
   * Toggles recurrence options based on checkbox state.
   */
  private void toggleRecurrence() {
    boolean enabled = recurringCheckbox.isSelected();
    for (JCheckBox checkbox : dayCheckboxes) {
      checkbox.setEnabled(enabled);
    }
    countSpinner.setEnabled(enabled);
  }

  /**
   * Validates user input.
   *
   * @return true if valid
   */
  private boolean validateInput() {
    if (subjectField.getText().trim().isEmpty()) {
      javax.swing.JOptionPane.showMessageDialog(this,
          "Event subject is required", "Validation Error",
          javax.swing.JOptionPane.ERROR_MESSAGE);
      return false;
    }

    if (recurringCheckbox.isSelected()) {
      boolean anyDaySelected = false;
      for (JCheckBox checkbox : dayCheckboxes) {
        if (checkbox.isSelected()) {
          anyDaySelected = true;
          break;
        }
      }
      if (!anyDaySelected) {
        javax.swing.JOptionPane.showMessageDialog(this,
            "Please select at least one day for recurring events",
            "Validation Error",
            javax.swing.JOptionPane.ERROR_MESSAGE);
        return false;
      }
    }

    return true;
  }

  /**
   * Checks if the dialog was confirmed.
   *
   * @return true if confirmed
   */
  public boolean isConfirmed() {
    return confirmed;
  }

  /**
   * Gets the event subject.
   *
   * @return the subject
   */
  public String getEventSubject() {
    return subjectField.getText().trim();
  }

  /**
   * Gets the start date-time.
   *
   * @return the start date-time
   */
  public LocalDateTime getStartDateTime() {
    return LocalDateTime.of(selectedDate,
        LocalTime.of((Integer) startHour.getSelectedItem(),
            (Integer) startMinute.getSelectedItem()));
  }

  /**
   * Gets the end date-time.
   *
   * @return the end date-time
   */
  public LocalDateTime getEndDateTime() {
    return LocalDateTime.of(selectedDate,
        LocalTime.of((Integer) endHour.getSelectedItem(),
            (Integer) endMinute.getSelectedItem()));
  }

  /**
   * Gets the event location.
   *
   * @return the location or null if empty
   */
  public String getEventLocation() {
    String loc = locationField.getText().trim();
    return loc.isEmpty() ? null : loc;
  }

  /**
   * Gets the description.
   *
   * @return the description or null if empty
   */
  public String getDescription() {
    String desc = descriptionField.getText().trim();
    return desc.isEmpty() ? null : desc;
  }

  /**
   * Checks if the event is recurring.
   *
   * @return true if recurring
   */
  public boolean isRecurring() {
    return recurringCheckbox.isSelected();
  }

  /**
   * Gets the selected recurring days.
   *
   * @return set of days or null if not recurring
   */
  public EnumSet<DayOfWeek> getRecurringDays() {
    if (!recurringCheckbox.isSelected()) {
      return null;
    }

    EnumSet<DayOfWeek> days = EnumSet.noneOf(DayOfWeek.class);
    DayOfWeek[] daysOfWeek = {
        DayOfWeek.MONDAY, DayOfWeek.TUESDAY, DayOfWeek.WEDNESDAY,
        DayOfWeek.THURSDAY, DayOfWeek.FRIDAY, DayOfWeek.SATURDAY, DayOfWeek.SUNDAY
    };

    for (int i = 0; i < dayCheckboxes.length; i++) {
      if (dayCheckboxes[i].isSelected()) {
        days.add(daysOfWeek[i]);
      }
    }

    return days;
  }

  /**
   * Gets the recurrence count.
   *
   * @return the count or null if not recurring
   */
  public Integer getRecurrenceCount() {
    if (!recurringCheckbox.isSelected()) {
      return null;
    }
    return (Integer) countSpinner.getValue();
  }

  /**
   * Gets the recurrence end date.
   *
   * @return the end date or null
   */
  public LocalDate getRecurrenceEndDate() {
    return null;
  }
}