package calendar.view.dialogs;

import java.awt.Color;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.time.LocalDate;
import java.time.LocalTime;
import javax.swing.BorderFactory;
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

/**
 * Dialog for creating a new event (single or recurring).
 * Dark minimalist theme.
 */
public class CreateEventDialog extends JDialog {
  // Dark theme colors
  private static final Color BACKGROUND_DARK = new Color(30, 30, 30);
  private static final Color OFF_WHITE = new Color(240, 240, 235);
  private static final Color ACCENT_GRAY = new Color(60, 60, 60);

  private JTextField subjectField;
  private JSpinner startHourSpinner;
  private JSpinner startMinuteSpinner;
  private JSpinner endHourSpinner;
  private JSpinner endMinuteSpinner;
  private JCheckBox allDayCheckbox;
  private JTextField descriptionField;
  private JTextField locationField;
  private JComboBox<String> statusComboBox;

  // Recurring event fields
  private JCheckBox recurringCheckbox;
  private JCheckBox[] dayCheckboxes;
  private JComboBox<String> recurrenceTypeComboBox;
  private JSpinner occurrencesSpinner;
  private JTextField untilDateField;

  private boolean confirmed;
  private final LocalDate preselectedDate;

  /**
   * Constructs a create event dialog.
   *
   * @param parent the parent frame
   * @param date the preselected date
   */
  public CreateEventDialog(JFrame parent, LocalDate date) {
    super(parent, "Create Event", true);
    this.preselectedDate = date;
    this.confirmed = false;

    setupUi();
    getContentPane().setBackground(BACKGROUND_DARK);
    setSize(500, 700);
    setLocationRelativeTo(parent);
  }

  private void setupUi() {
    final JPanel panel = new JPanel(new GridBagLayout());
    panel.setBackground(BACKGROUND_DARK);
    GridBagConstraints gbc = new GridBagConstraints();
    gbc.insets = new Insets(5, 5, 5, 5);
    gbc.fill = GridBagConstraints.HORIZONTAL;

    int row = 0;

    // Subject
    gbc.gridx = 0;
    gbc.gridy = row;
    panel.add(createStyledLabel("Subject:"), gbc);

    gbc.gridx = 1;
    gbc.gridwidth = 2;
    subjectField = createStyledTextField(20);
    panel.add(subjectField, gbc);

    row++;
    gbc.gridwidth = 1;

    // Date
    gbc.gridx = 0;
    gbc.gridy = row;
    panel.add(createStyledLabel("Date:"), gbc);

    gbc.gridx = 1;
    gbc.gridwidth = 2;
    JLabel dateLabel = createStyledLabel(preselectedDate.toString());
    panel.add(dateLabel, gbc);

    row++;
    gbc.gridwidth = 1;

    // All-day checkbox
    gbc.gridx = 0;
    gbc.gridy = row;
    panel.add(createStyledLabel("All Day:"), gbc);

    gbc.gridx = 1;
    allDayCheckbox = createStyledCheckbox();
    allDayCheckbox.addActionListener(e -> toggleAllDay());
    panel.add(allDayCheckbox, gbc);

    row++;

    // Start time
    gbc.gridx = 0;
    gbc.gridy = row;
    panel.add(createStyledLabel("Start Time:"), gbc);

    gbc.gridx = 1;
    JPanel startTimePanel = new JPanel();
    startTimePanel.setBackground(BACKGROUND_DARK);
    startHourSpinner = new JSpinner(new SpinnerNumberModel(9, 0, 23, 1));
    startMinuteSpinner = new JSpinner(new SpinnerNumberModel(0, 0, 59, 15));
    styleSpinner(startHourSpinner);
    styleSpinner(startMinuteSpinner);
    startTimePanel.add(startHourSpinner);
    startTimePanel.add(createStyledLabel(":"));
    startTimePanel.add(startMinuteSpinner);
    panel.add(startTimePanel, gbc);

    row++;

    // End time
    gbc.gridx = 0;
    gbc.gridy = row;
    panel.add(createStyledLabel("End Time:"), gbc);

    gbc.gridx = 1;
    JPanel endTimePanel = new JPanel();
    endTimePanel.setBackground(BACKGROUND_DARK);
    endHourSpinner = new JSpinner(new SpinnerNumberModel(10, 0, 23, 1));
    endMinuteSpinner = new JSpinner(new SpinnerNumberModel(0, 0, 59, 15));
    styleSpinner(endHourSpinner);
    styleSpinner(endMinuteSpinner);
    endTimePanel.add(endHourSpinner);
    endTimePanel.add(createStyledLabel(":"));
    endTimePanel.add(endMinuteSpinner);
    panel.add(endTimePanel, gbc);

    row++;

    // Description
    gbc.gridx = 0;
    gbc.gridy = row;
    panel.add(createStyledLabel("Description:"), gbc);

    gbc.gridx = 1;
    gbc.gridwidth = 2;
    descriptionField = createStyledTextField(20);
    panel.add(descriptionField, gbc);

    row++;
    gbc.gridwidth = 1;

    // Location
    gbc.gridx = 0;
    gbc.gridy = row;
    panel.add(createStyledLabel("Location:"), gbc);

    gbc.gridx = 1;
    gbc.gridwidth = 2;
    locationField = createStyledTextField(20);
    panel.add(locationField, gbc);

    row++;
    gbc.gridwidth = 1;

    // Status
    gbc.gridx = 0;
    gbc.gridy = row;
    panel.add(createStyledLabel("Status:"), gbc);

    gbc.gridx = 1;
    statusComboBox = new JComboBox<>(new String[]{"public", "private"});
    statusComboBox.setBackground(ACCENT_GRAY);
    statusComboBox.setForeground(OFF_WHITE);
    panel.add(statusComboBox, gbc);

    row++;

    // Recurring checkbox
    gbc.gridx = 0;
    gbc.gridy = row;
    panel.add(createStyledLabel("Recurring:"), gbc);

    gbc.gridx = 1;
    recurringCheckbox = createStyledCheckbox();
    panel.add(recurringCheckbox, gbc);

    row++;

    // Days of week (initially hidden)
    gbc.gridx = 0;
    gbc.gridy = row;
    JLabel daysLabel = createStyledLabel("Repeat on:");
    panel.add(daysLabel, gbc);

    gbc.gridx = 1;
    gbc.gridwidth = 2;
    JPanel daysPanel = new JPanel();
    daysPanel.setBackground(BACKGROUND_DARK);
    String[] dayNames = {"M", "T", "W", "R", "F", "S", "U"};
    dayCheckboxes = new JCheckBox[7];
    for (int i = 0; i < 7; i++) {
      dayCheckboxes[i] = createStyledCheckbox();
      dayCheckboxes[i].setText(dayNames[i]);
      daysPanel.add(dayCheckboxes[i]);
    }
    daysPanel.setVisible(false);
    daysLabel.setVisible(false);
    panel.add(daysPanel, gbc);

    row++;
    gbc.gridwidth = 1;

    // Recurrence type
    gbc.gridx = 0;
    gbc.gridy = row;
    JLabel recTypeLabel = createStyledLabel("End:");
    recTypeLabel.setVisible(false);
    panel.add(recTypeLabel, gbc);

    gbc.gridx = 1;
    recurrenceTypeComboBox = new JComboBox<>(new String[]{"After N times", "Until date"});
    recurrenceTypeComboBox.setBackground(ACCENT_GRAY);
    recurrenceTypeComboBox.setForeground(OFF_WHITE);
    recurrenceTypeComboBox.setVisible(false);
    panel.add(recurrenceTypeComboBox, gbc);

    row++;

    // Occurrences
    gbc.gridx = 0;
    gbc.gridy = row;
    JLabel occLabel = createStyledLabel("Occurrences:");
    occLabel.setVisible(false);
    panel.add(occLabel, gbc);

    gbc.gridx = 1;
    occurrencesSpinner = new JSpinner(new SpinnerNumberModel(5, 1, 100, 1));
    styleSpinner(occurrencesSpinner);
    occurrencesSpinner.setVisible(false);
    panel.add(occurrencesSpinner, gbc);

    row++;

    // Until date
    gbc.gridx = 0;
    gbc.gridy = row;
    JLabel untilLabel = createStyledLabel("Until (YYYY-MM-DD):");
    untilLabel.setVisible(false);
    panel.add(untilLabel, gbc);

    gbc.gridx = 1;
    untilDateField = createStyledTextField(10);
    untilDateField.setVisible(false);
    panel.add(untilDateField, gbc);

    row++;

    // Toggle visibility logic for recurring options
    final JLabel[] recurringLabels = {daysLabel, recTypeLabel, occLabel, untilLabel};
    final JPanel[] recurringPanels = {daysPanel};

    recurringCheckbox.addActionListener(e -> {
      boolean visible = recurringCheckbox.isSelected();
      for (JLabel label : recurringLabels) {
        label.setVisible(visible);
      }
      for (JPanel p : recurringPanels) {
        p.setVisible(visible);
      }
      if (visible) {
        recurrenceTypeComboBox.setVisible(true);
        updateRecurrenceFields();
      } else {
        recurrenceTypeComboBox.setVisible(false);
        occurrencesSpinner.setVisible(false);
        untilDateField.setVisible(false);
      }
      pack();
    });

    recurrenceTypeComboBox.addActionListener(e -> updateRecurrenceFields());

    // Buttons
    gbc.gridx = 0;
    gbc.gridy = row;
    gbc.gridwidth = 3;
    JPanel buttonPanel = new JPanel();
    buttonPanel.setBackground(BACKGROUND_DARK);

    JButton createButton = createStyledButton("Create");
    createButton.addActionListener(e -> {
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
   * Updates visibility of recurrence fields based on type.
   */
  private void updateRecurrenceFields() {
    if (recurrenceTypeComboBox.getSelectedIndex() == 0) {
      occurrencesSpinner.setVisible(true);
      untilDateField.setVisible(false);
    } else {
      occurrencesSpinner.setVisible(false);
      untilDateField.setVisible(true);
    }
  }

  /**
   * Toggles all-day event mode.
   */
  private void toggleAllDay() {
    boolean allDay = allDayCheckbox.isSelected();
    startHourSpinner.setEnabled(!allDay);
    startMinuteSpinner.setEnabled(!allDay);
    endHourSpinner.setEnabled(!allDay);
    endMinuteSpinner.setEnabled(!allDay);

    if (allDay) {
      startHourSpinner.setValue(8);
      startMinuteSpinner.setValue(0);
      endHourSpinner.setValue(17);
      endMinuteSpinner.setValue(0);
    }
  }

  /**
   * Creates a styled label with dark theme.
   */
  private JLabel createStyledLabel(String text) {
    JLabel label = new JLabel(text);
    label.setForeground(OFF_WHITE);
    return label;
  }

  /**
   * Creates a styled text field with dark theme.
   */
  private JTextField createStyledTextField(int columns) {
    JTextField field = new JTextField(columns);
    field.setBackground(ACCENT_GRAY);
    field.setForeground(OFF_WHITE);
    field.setCaretColor(OFF_WHITE);
    return field;
  }

  /**
   * Creates a styled checkbox with dark theme.
   */
  private JCheckBox createStyledCheckbox() {
    JCheckBox checkbox = new JCheckBox();
    checkbox.setBackground(BACKGROUND_DARK);
    checkbox.setForeground(OFF_WHITE);
    return checkbox;
  }

  /**
   * Styles a spinner with dark theme.
   */
  private void styleSpinner(JSpinner spinner) {
    spinner.getEditor().getComponent(0).setBackground(ACCENT_GRAY);
    spinner.getEditor().getComponent(0).setForeground(OFF_WHITE);
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
   * Checks if user confirmed the dialog.
   *
   * @return true if confirmed
   */
  public boolean isConfirmed() {
    return confirmed;
  }

  /**
   * Gets the entered subject.
   *
   * @return the event subject
   */
  public String getSubject() {
    return subjectField.getText();
  }

  /**
   * Gets the start time.
   *
   * @return the start time
   */
  public LocalTime getStartTime() {
    int hour = (Integer) startHourSpinner.getValue();
    int minute = (Integer) startMinuteSpinner.getValue();
    return LocalTime.of(hour, minute);
  }

  /**
   * Gets the end time.
   *
   * @return the end time
   */
  public LocalTime getEndTime() {
    int hour = (Integer) endHourSpinner.getValue();
    int minute = (Integer) endMinuteSpinner.getValue();
    return LocalTime.of(hour, minute);
  }

  /**
   * Checks if event is all-day.
   *
   * @return true if all-day
   */
  public boolean isAllDay() {
    return allDayCheckbox.isSelected();
  }

  /**
   * Gets the description.
   *
   * @return the description
   */
  public String getDescription() {
    return descriptionField.getText();
  }

  /**
   * Gets the event location.
   *
   * @return the location
   */
  public String getEventLocation() {
    return locationField.getText();
  }

  /**
   * Gets the status.
   *
   * @return the status
   */
  public String getStatus() {
    return (String) statusComboBox.getSelectedItem();
  }

  /**
   * Checks if event is recurring.
   *
   * @return true if recurring
   */
  public boolean isRecurring() {
    return recurringCheckbox.isSelected();
  }

  /**
   * Gets selected days for recurring event.
   *
   * @return array of 7 booleans for each day
   */
  public boolean[] getSelectedDays() {
    boolean[] days = new boolean[7];
    for (int i = 0; i < 7; i++) {
      days[i] = dayCheckboxes[i].isSelected();
    }
    return days;
  }

  /**
   * Checks if recurrence is count-based.
   *
   * @return true if count-based, false if date-based
   */
  public boolean isCountBased() {
    return recurrenceTypeComboBox.getSelectedIndex() == 0;
  }

  /**
   * Gets the number of occurrences.
   *
   * @return the occurrence count
   */
  public int getOccurrences() {
    return (Integer) occurrencesSpinner.getValue();
  }

  /**
   * Gets the until date string.
   *
   * @return the until date
   */
  public String getUntilDate() {
    return untilDateField.getText();
  }
}