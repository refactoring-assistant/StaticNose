package calendar.view;

import calendar.model.event.EventStatus;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.HashSet;
import java.util.Set;
import javax.swing.BorderFactory;
import javax.swing.ButtonGroup;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JRadioButton;
import javax.swing.JScrollPane;
import javax.swing.JSpinner;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.SpinnerNumberModel;

/**
 * Dialog for creating a new event.
 * Collects event details including name, date, time, location, and recurrence options.
 */
public class CreateEventDialog extends JDialog {

  private static final DateTimeFormatter DATE_FORMATTER =
      DateTimeFormatter.ofPattern("MM/dd/yyyy");

  private JTextField eventNameField;
  private JTextField dateField;
  private boolean isDateFixed;
  private JTextField locationField;
  private JTextArea descriptionArea;
  private JCheckBox multiDayCheckBox;
  private JTextField endDateMultiDayField;
  private JRadioButton publicRadio;
  private JRadioButton privateRadio;

  private JSpinner startHourSpinner;
  private JSpinner startMinuteSpinner;
  private JComboBox<String> startAmPmCombo;
  private JSpinner endHourSpinner;
  private JSpinner endMinuteSpinner;
  private JComboBox<String> endAmPmCombo;

  private JCheckBox recurringCheckBox;
  private JPanel recurrencePanel;
  private JCheckBox[] dayCheckBoxes;
  private JRadioButton endAfterOccurrences;
  private JRadioButton endOnDate;
  private JSpinner occurrencesSpinner;
  private JTextField endDateField;

  private JButton createButton;
  private JButton cancelButton;

  private boolean confirmed = false;
  private EventFormData eventData;

  /**
   * Constructs a CreateEventDialog.
   *
   * @param parent the parent frame
   * @param initialDate the initial date to pre-fill (null for empty)
   */
  public CreateEventDialog(JFrame parent, LocalDate initialDate) {
    super(parent, "Create Event", true);

    this.isDateFixed = (initialDate != null);

    setSize(500, 680);
    setMinimumSize(new Dimension(450, 650));
    setLocationRelativeTo(parent);
    setLayout(new BorderLayout());

    initializeComponents(initialDate);
  }

  /**
   * Initializes all dialog components.
   */
  private void initializeComponents(LocalDate initialDate) {
    JPanel formPanel = createFormPanel(initialDate);
    JScrollPane scrollPane = new JScrollPane(formPanel);
    scrollPane.setBorder(null);
    scrollPane.getVerticalScrollBar().setUnitIncrement(16);
    add(scrollPane, BorderLayout.CENTER);

    JPanel footerPanel = createFooterPanel();
    add(footerPanel, BorderLayout.SOUTH);
  }

  /**
   * Creates the main form panel with all input fields.
   */
  private JPanel createFormPanel(LocalDate initialDate) {
    JPanel formPanel = new JPanel(new GridBagLayout());
    formPanel.setBackground(Color.WHITE);
    formPanel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));

    GridBagConstraints gbc = new GridBagConstraints();
    gbc.fill = GridBagConstraints.HORIZONTAL;
    gbc.insets = new Insets(5, 5, 5, 5);
    gbc.weightx = 1.0;

    int row = 0;

    addFieldLabel(formPanel, gbc, row++, "Event Name: *");
    eventNameField = addTextField(formPanel, gbc, row++);

    addFieldLabel(formPanel, gbc, row++, "Date: * (MM/DD/YYYY)");
    dateField = addTextField(formPanel, gbc, row++);
    if (isDateFixed && initialDate != null) {
      dateField.setText(initialDate.format(DATE_FORMATTER));
      dateField.setEditable(false);
      dateField.setBackground(new Color(240, 240, 240));
    } else {
      dateField.setText(initialDate != null ? initialDate.format(DATE_FORMATTER) : "");
    }

    gbc.gridy = row++;
    gbc.gridwidth = 2;
    multiDayCheckBox = new JCheckBox("Multi-day event");
    multiDayCheckBox.setFont(new Font("SansSerif", Font.PLAIN, 13));
    multiDayCheckBox.setBackground(Color.WHITE);
    multiDayCheckBox.setFocusPainted(false);
    multiDayCheckBox.addActionListener(e -> toggleEndDateField());
    formPanel.add(multiDayCheckBox, gbc);

    addFieldLabel(formPanel, gbc, row++, "    End Date: (MM/DD/YYYY)");
    endDateMultiDayField = addTextField(formPanel, gbc, row++);
    endDateMultiDayField.setVisible(false);
    endDateMultiDayField.setEnabled(false);

    addFieldLabel(formPanel, gbc, row++, "Start Time: *");
    gbc.gridy = row++;
    gbc.gridwidth = 2;
    formPanel.add(createTimePanel(true), gbc);

    addFieldLabel(formPanel, gbc, row++, "End Time: *");
    gbc.gridy = row++;
    gbc.gridwidth = 2;
    formPanel.add(createTimePanel(false), gbc);

    addFieldLabel(formPanel, gbc, row++, "Location:");
    locationField = addTextField(formPanel, gbc, row++);

    addFieldLabel(formPanel, gbc, row++, "Description:");
    gbc.gridy = row++;
    gbc.gridwidth = 2;
    descriptionArea = new JTextArea(3, 20);
    descriptionArea.setFont(new Font("SansSerif", Font.PLAIN, 13));
    descriptionArea.setLineWrap(true);
    descriptionArea.setWrapStyleWord(true);
    descriptionArea.setBorder(BorderFactory.createLineBorder(Color.LIGHT_GRAY));
    JScrollPane descScroll = new JScrollPane(descriptionArea);
    descScroll.setPreferredSize(new Dimension(0, 70));
    formPanel.add(descScroll, gbc);

    addFieldLabel(formPanel, gbc, row++, "Status:");
    gbc.gridy = row++;
    gbc.gridwidth = 2;
    JPanel statusPanel = createStatusPanel();
    formPanel.add(statusPanel, gbc);

    gbc.gridy = row++;
    gbc.gridwidth = 2;
    recurringCheckBox = new JCheckBox("Recurring Event");
    recurringCheckBox.setFont(new Font("SansSerif", Font.BOLD, 13));
    recurringCheckBox.setBackground(Color.WHITE);
    recurringCheckBox.setFocusPainted(false);
    recurringCheckBox.addActionListener(e -> toggleRecurrencePanel());
    formPanel.add(recurringCheckBox, gbc);

    gbc.gridy = row++;
    gbc.gridwidth = 2;
    recurrencePanel = createRecurrencePanel();
    recurrencePanel.setVisible(false);
    formPanel.add(recurrencePanel, gbc);

    return formPanel;
  }

  /**
   * Adds a field label to the form.
   */
  private void addFieldLabel(JPanel panel, GridBagConstraints gbc, int row, String text) {
    gbc.gridx = 0;
    gbc.gridy = row;
    gbc.gridwidth = 1;
    JLabel label = new JLabel(text);
    label.setFont(new Font("SansSerif", Font.BOLD, 13));
    panel.add(label, gbc);
  }

  /**
   * Adds a text field to the form.
   */
  private JTextField addTextField(JPanel panel, GridBagConstraints gbc, int row) {
    gbc.gridy = row;
    gbc.gridwidth = 2;
    JTextField field = new JTextField();
    field.setFont(new Font("SansSerif", Font.PLAIN, 13));
    field.setPreferredSize(new Dimension(0, 30));
    panel.add(field, gbc);
    return field;
  }

  /**
   * Creates a time input panel with spinners.
   *
   * @param isStart true for start time (default 8 AM), false for end time (default 5 PM)
   */
  private JPanel createTimePanel(boolean isStart) {
    JPanel timePanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 5, 0));
    timePanel.setBackground(Color.WHITE);

    int defaultHour = isStart ? 8 : 5;
    SpinnerNumberModel hourModel = new SpinnerNumberModel(defaultHour, 1, 12, 1);
    JSpinner hourSpinner = new JSpinner(hourModel);
    hourSpinner.setPreferredSize(new Dimension(60, 30));
    hourSpinner.setFont(new Font("SansSerif", Font.PLAIN, 13));

    SpinnerNumberModel minuteModel = new SpinnerNumberModel(0, 0, 59, 1);
    JSpinner minuteSpinner = new JSpinner(minuteModel);
    minuteSpinner.setPreferredSize(new Dimension(60, 30));
    minuteSpinner.setFont(new Font("SansSerif", Font.PLAIN, 13));

    String defaultAmPm = isStart ? "AM" : "PM";
    JComboBox<String> amPmCombo = new JComboBox<>(new String[]{"AM", "PM"});
    amPmCombo.setSelectedItem(defaultAmPm);
    amPmCombo.setPreferredSize(new Dimension(70, 30));
    amPmCombo.setFont(new Font("SansSerif", Font.PLAIN, 13));

    timePanel.add(hourSpinner);
    timePanel.add(new JLabel(":"));
    timePanel.add(minuteSpinner);
    timePanel.add(amPmCombo);

    if (isStart) {
      startHourSpinner = hourSpinner;
      startMinuteSpinner = minuteSpinner;
      startAmPmCombo = amPmCombo;
    } else {
      endHourSpinner = hourSpinner;
      endMinuteSpinner = minuteSpinner;
      endAmPmCombo = amPmCombo;
    }

    return timePanel;
  }

  /**
   * Creates the recurrence options panel.
   */
  private JPanel createRecurrencePanel() {
    JPanel panel = new JPanel(new GridBagLayout());
    panel.setBackground(new Color(245, 245, 245));
    panel.setBorder(BorderFactory.createCompoundBorder(
        BorderFactory.createLineBorder(Color.LIGHT_GRAY),
        BorderFactory.createEmptyBorder(10, 10, 10, 10)
    ));

    GridBagConstraints gbc = new GridBagConstraints();
    gbc.fill = GridBagConstraints.HORIZONTAL;
    gbc.insets = new Insets(5, 5, 5, 5);
    gbc.weightx = 1.0;
    gbc.gridx = 0;
    gbc.gridy = 0;

    JLabel repeatLabel = new JLabel("Repeat on:");
    repeatLabel.setFont(new Font("SansSerif", Font.BOLD, 12));
    panel.add(repeatLabel, gbc);

    gbc.gridy++;
    panel.add(createDaysPanel(), gbc);

    gbc.gridy++;
    JLabel endsLabel = new JLabel("Ends:");
    endsLabel.setFont(new Font("SansSerif", Font.BOLD, 12));
    panel.add(endsLabel, gbc);

    gbc.gridy++;
    panel.add(createOccurrencesPanel(), gbc);

    gbc.gridy++;
    panel.add(createEndDatePanel(), gbc);

    ButtonGroup endGroup = new ButtonGroup();
    endGroup.add(endAfterOccurrences);
    endGroup.add(endOnDate);

    return panel;
  }

  /**
   * Creates the days of week checkbox panel.
   */
  private JPanel createDaysPanel() {
    JPanel daysPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 5));
    daysPanel.setBackground(new Color(245, 245, 245));

    String[] dayLabels = {"Sun", "Mon", "Tue", "Wed", "Thu", "Fri", "Sat"};
    dayCheckBoxes = new JCheckBox[7];

    for (int i = 0; i < 7; i++) {
      JCheckBox cb = new JCheckBox(dayLabels[i]);
      cb.setBackground(new Color(245, 245, 245));
      cb.setFont(new Font("SansSerif", Font.PLAIN, 11));
      cb.setFocusPainted(false);
      dayCheckBoxes[i] = cb;
      daysPanel.add(cb);
    }

    return daysPanel;
  }

  /**
   * Creates the "After N occurrences" panel.
   */
  private JPanel createOccurrencesPanel() {
    JPanel panel = new JPanel(new FlowLayout(FlowLayout.LEFT, 5, 0));
    panel.setBackground(new Color(245, 245, 245));

    endAfterOccurrences = new JRadioButton("After");
    endAfterOccurrences.setBackground(new Color(245, 245, 245));
    endAfterOccurrences.setFont(new Font("SansSerif", Font.PLAIN, 12));
    endAfterOccurrences.setFocusPainted(false);
    endAfterOccurrences.setSelected(true);

    occurrencesSpinner = new JSpinner(new SpinnerNumberModel(5, 1, 100, 1));
    occurrencesSpinner.setPreferredSize(new Dimension(60, 25));

    endAfterOccurrences.addActionListener(e -> {
      occurrencesSpinner.setEnabled(true);
      endDateField.setEnabled(false);
    });

    panel.add(endAfterOccurrences);
    panel.add(occurrencesSpinner);
    panel.add(new JLabel("occurrences"));

    return panel;
  }

  /**
   * Creates the "On date" panel.
   */
  private JPanel createEndDatePanel() {
    JPanel panel = new JPanel(new FlowLayout(FlowLayout.LEFT, 5, 0));
    panel.setBackground(new Color(245, 245, 245));

    endOnDate = new JRadioButton("On date");
    endOnDate.setBackground(new Color(245, 245, 245));
    endOnDate.setFont(new Font("SansSerif", Font.PLAIN, 12));
    endOnDate.setFocusPainted(false);

    endDateField = new JTextField(12);
    endDateField.setFont(new Font("SansSerif", Font.PLAIN, 12));
    endDateField.setEnabled(false);

    endOnDate.addActionListener(e -> {
      occurrencesSpinner.setEnabled(false);
      endDateField.setEnabled(true);
    });

    panel.add(endOnDate);
    panel.add(endDateField);
    panel.add(new JLabel("(MM/DD/YYYY)"));

    return panel;
  }

  /**
   * Creates the footer panel with action buttons.
   */
  private JPanel createFooterPanel() {
    JPanel footerPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 10));
    footerPanel.setBackground(Color.WHITE);
    footerPanel.setBorder(BorderFactory.createMatteBorder(1, 0, 0, 0, Color.LIGHT_GRAY));

    cancelButton = new JButton("Cancel");
    cancelButton.setFont(new Font("SansSerif", Font.PLAIN, 13));
    cancelButton.setFocusPainted(false);
    cancelButton.setCursor(new Cursor(Cursor.HAND_CURSOR));
    cancelButton.setPreferredSize(new Dimension(90, 35));
    cancelButton.addActionListener(e -> {
      confirmed = false;
      dispose();
    });

    createButton = new JButton("Create Event");
    createButton.setFont(new Font("SansSerif", Font.BOLD, 13));
    createButton.setBackground(new Color(66, 133, 244));
    createButton.setForeground(Color.WHITE);
    createButton.setOpaque(true);
    createButton.setFocusPainted(false);
    createButton.setBorderPainted(false);
    createButton.setCursor(new Cursor(Cursor.HAND_CURSOR));
    createButton.setPreferredSize(new Dimension(130, 35));
    createButton.addActionListener(e -> handleCreate());

    footerPanel.add(cancelButton);
    footerPanel.add(createButton);

    return footerPanel;
  }

  /**
   * Creates the status selection panel with radio buttons.
   */
  private JPanel createStatusPanel() {
    JPanel panel = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 0));
    panel.setBackground(Color.WHITE);

    publicRadio = new JRadioButton("Public");
    publicRadio.setFont(new Font("SansSerif", Font.PLAIN, 13));
    publicRadio.setBackground(Color.WHITE);
    publicRadio.setFocusPainted(false);
    publicRadio.setSelected(true);

    privateRadio = new JRadioButton("Private");
    privateRadio.setFont(new Font("SansSerif", Font.PLAIN, 13));
    privateRadio.setBackground(Color.WHITE);
    privateRadio.setFocusPainted(false);

    ButtonGroup statusGroup = new ButtonGroup();
    statusGroup.add(publicRadio);
    statusGroup.add(privateRadio);

    panel.add(publicRadio);
    panel.add(privateRadio);

    return panel;
  }

  /**
   * Toggles visibility of end date field based on multi-day checkbox.
   */
  private void toggleEndDateField() {
    boolean isMultiDay = multiDayCheckBox.isSelected();
    endDateMultiDayField.setVisible(isMultiDay);
    endDateMultiDayField.setEnabled(isMultiDay);

    if (recurringCheckBox.isSelected() && isMultiDay) {
      multiDayCheckBox.setSelected(false);
      endDateMultiDayField.setVisible(false);
      endDateMultiDayField.setEnabled(false);
      showError("Recurring events cannot be multi-day.");
    }

    revalidate();
    repaint();
  }

  /**
   * Toggles the visibility of the recurrence panel.
   */
  private void toggleRecurrencePanel() {
    boolean isRecurring = recurringCheckBox.isSelected();
    recurrencePanel.setVisible(isRecurring);

    if (isRecurring && multiDayCheckBox.isSelected()) {
      multiDayCheckBox.setSelected(false);
      endDateMultiDayField.setVisible(false);
      endDateMultiDayField.setEnabled(false);
    }
    multiDayCheckBox.setEnabled(!isRecurring);

    revalidate();
    repaint();
  }

  /**
   * Handles the create button click.
   * Validates all inputs and creates EventFormData if valid.
   */
  private void handleCreate() {
    try {
      String name = eventNameField.getText().trim();
      if (name.isEmpty()) {
        showError("Event name is required.");
        eventNameField.requestFocus();
        return;
      }

      LocalDate date = parseDate(dateField.getText().trim());
      if (date == null) {
        showError("Invalid date format. Please use MM/DD/YYYY.");
        dateField.requestFocus();
        return;
      }

      LocalTime startTime = parseTime(
          (Integer) startHourSpinner.getValue(),
          (Integer) startMinuteSpinner.getValue(),
          (String) startAmPmCombo.getSelectedItem()
      );

      LocalTime endTime = parseTime(
          (Integer) endHourSpinner.getValue(),
          (Integer) endMinuteSpinner.getValue(),
          (String) endAmPmCombo.getSelectedItem()
      );

      if (!endTime.isAfter(startTime)) {
        showError("End time must be after start time.");
        return;
      }

      String description = descriptionArea.getText().trim();

      LocalDate endDate = date;
      if (multiDayCheckBox.isSelected()) {
        endDate = parseDate(endDateMultiDayField.getText().trim());
        if (endDate == null) {
          showError("Invalid end date format. Please use MM/DD/YYYY.");
          endDateMultiDayField.requestFocus();
          return;
        }
        if (endDate.isBefore(date)) {
          showError("End date must be on or after start date.");
          endDateMultiDayField.requestFocus();
          return;
        }
      }

      EventStatus status = publicRadio.isSelected() ? EventStatus.PUBLIC : EventStatus.PRIVATE;


      boolean isRecurring = recurringCheckBox.isSelected();
      Set<DayOfWeek> recurringDays = null;
      RecurrenceEndType endType = null;
      Object endValue = null;

      if (isRecurring) {
        recurringDays = getSelectedDays();
        if (recurringDays.isEmpty()) {
          showError("Please select at least one day for recurring event.");
          return;
        }

        if (endAfterOccurrences.isSelected()) {
          endType = RecurrenceEndType.BY_COUNT;
          endValue = (Integer) occurrencesSpinner.getValue();
        } else {
          endType = RecurrenceEndType.BY_DATE;

          LocalDate endDateSeries = parseDate(endDateField.getText().trim());
          if (endDateSeries == null) {
            showError("Invalid end date format. Please use MM/DD/YYYY.");
            endDateField.requestFocus();
            return;
          }

          if (!endDateSeries.isAfter(date)) {
            showError("End date must be after the event start date ("
                + date.format(DATE_FORMATTER) + ").");
            endDateField.requestFocus();
            return;
          }

          endValue = endDateSeries;
        }
      }

      String location = locationField.getText().trim();

      eventData = new EventFormData(
          name, location,
          date, endDate,
          startTime, endTime,
          description,
          status,
          isRecurring, recurringDays, endType, endValue
      );

      confirmed = true;
      dispose();

    } catch (Exception ex) {
      showError("Error creating event: " + ex.getMessage());
      ex.printStackTrace();
    }
  }

  /**
   * Parses a date string in MM/DD/YYYY format.
   *
   * @return LocalDate if valid, null otherwise
   */
  private LocalDate parseDate(String dateStr) {
    if (dateStr == null || dateStr.isEmpty()) {
      return null;
    }

    try {
      return LocalDate.parse(dateStr, DATE_FORMATTER);
    } catch (DateTimeParseException e) {
      return null;
    }
  }

  /**
   * Converts 12-hour time to LocalTime (24-hour format).
   */
  private LocalTime parseTime(int hour, int minute, String amPm) {
    int hour24 = hour;
    if (amPm.equals("PM") && hour != 12) {
      hour24 += 12;
    } else if (amPm.equals("AM") && hour == 12) {
      hour24 = 0;
    }
    return LocalTime.of(hour24, minute);
  }

  /**
   * Gets the selected days of week from checkboxes.
   */
  private Set<DayOfWeek> getSelectedDays() {
    Set<DayOfWeek> days = new HashSet<>();
    DayOfWeek[] dayValues = {
        DayOfWeek.SUNDAY, DayOfWeek.MONDAY, DayOfWeek.TUESDAY,
        DayOfWeek.WEDNESDAY, DayOfWeek.THURSDAY, DayOfWeek.FRIDAY,
        DayOfWeek.SATURDAY
    };

    for (int i = 0; i < 7; i++) {
      if (dayCheckBoxes[i].isSelected()) {
        days.add(dayValues[i]);
      }
    }
    return days;
  }

  /**
   * Shows an error message dialog.
   */
  private void showError(String message) {
    JOptionPane.showMessageDialog(this, message, "Validation Error",
        JOptionPane.ERROR_MESSAGE);
  }

  /**
   * Checks if the dialog was confirmed.
   *
   * @return true if Create button clicked, false if cancelled
   */
  public boolean wasConfirmed() {
    return confirmed;
  }

  /**
   * Gets the event data entered by the user.
   *
   * @return EventFormData if confirmed, null otherwise
   */
  public EventFormData getEventData() {
    return eventData;
  }

  /**
   * Data class holding all event form data.
   */
  public static class EventFormData {
    private final String name;
    private final String location;
    private final LocalDate date;
    private final LocalDate endDate;
    private final LocalTime startTime;
    private final LocalTime endTime;
    private final String description;
    private final EventStatus status;
    private final boolean isRecurring;
    private final Set<DayOfWeek> recurringDays;
    private final RecurrenceEndType endType;
    private final Object endValue;

    /**
     * Constructs event form data with all event properties.
     *
     * @param name the event name/subject
     * @param location the event location (can be null or empty)
     * @param date the start date of the event
     * @param endDate the end date for multi-day events (same as date if single-day)
     * @param startTime the start time of the event
     * @param endTime the end time of the event
     * @param description the event description (can be null or empty)
     * @param status the event status (PUBLIC or PRIVATE)
     * @param isRecurring true if this is a recurring event, false for single event
     * @param recurringDays set of days of week for recurring events (null if not recurring)
     * @param endType how the recurring series ends (BY_COUNT or BY_DATE, null if not recurring)
     * @param endValue the end value: Integer for count, LocalDate for date (null if not recurring)
     */
    public EventFormData(String name, String location, LocalDate date, LocalDate endDate,
                         LocalTime startTime, LocalTime endTime, String description,
                         EventStatus status,
                         boolean isRecurring, Set<DayOfWeek> recurringDays,
                         RecurrenceEndType endType, Object endValue) {
      this.name = name;
      this.location = location;
      this.date = date;
      this.endDate = endDate;
      this.startTime = startTime;
      this.endTime = endTime;
      this.description = description;
      this.status = status;
      this.isRecurring = isRecurring;
      this.recurringDays = recurringDays;
      this.endType = endType;
      this.endValue = endValue;
    }

    public String getName() {
      return name;
    }

    public String getLocation() {
      return location;
    }

    public LocalDate getDate() {
      return date;
    }

    public LocalDate getEndDate() {
      return endDate;
    }

    public LocalTime getStartTime() {
      return startTime;
    }

    public LocalTime getEndTime() {
      return endTime;
    }

    public String getDescription() {
      return description;
    }

    public EventStatus getStatus() {
      return status;
    }

    public boolean isRecurring() {
      return isRecurring;
    }

    public Set<DayOfWeek> getRecurringDays() {
      return recurringDays;
    }

    public RecurrenceEndType getEndType() {
      return endType;
    }

    public Object getEndValue() {
      return endValue;
    }
  }

  /**
   * Enum for recurrence end type.
   */
  public enum RecurrenceEndType {
    BY_COUNT,
    BY_DATE
  }
}