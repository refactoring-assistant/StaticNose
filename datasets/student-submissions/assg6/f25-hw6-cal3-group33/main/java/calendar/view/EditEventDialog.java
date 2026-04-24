package calendar.view;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import javax.swing.BorderFactory;
import javax.swing.Box;
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
import javax.swing.JSeparator;
import javax.swing.JSpinner;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.SpinnerNumberModel;

/**
 * Dialog for editing an existing event.
 * Handles both single and recurring events with appropriate edit scopes.
 */
public class EditEventDialog extends JDialog {

  private static final DateTimeFormatter DATE_FORMATTER =
      DateTimeFormatter.ofPattern("MM/dd/yyyy");

  private JTextField eventNameField;
  private JTextField dateField;
  private JTextField locationField;
  private JTextArea descriptionArea;

  private JSpinner startHourSpinner;
  private JSpinner startMinuteSpinner;
  private JComboBox<String> startAmPmCombo;
  private JSpinner endHourSpinner;
  private JSpinner endMinuteSpinner;
  private JComboBox<String> endAmPmCombo;

  private JCheckBox multiDayCheckBox;
  private JTextField endDateField;

  private JRadioButton publicRadio;
  private JRadioButton privateRadio;

  private JPanel recurrenceDisplayPanel;
  private JPanel recurringEditOptionsPanel;
  private JRadioButton editThisOnly;
  private JRadioButton editFutureOccurrences;
  private JRadioButton editAllOccurrences;
  private ButtonGroup editScopeGroup;

  private JButton saveButton;
  private JButton cancelButton;

  private final boolean isRecurringEvent;
  private final LocalDate eventDate;
  private final EventDisplayInfo originalEventData;

  private boolean confirmed = false;
  private EventEditData editData;

  /**
   * Constructs an EditEventDialog.
   *
   * @param parent    the parent frame
   * @param eventData the current event data to pre-fill
   * @param eventDate the date of this occurrence
   */
  public EditEventDialog(JFrame parent, EventDisplayInfo eventData, LocalDate eventDate) {
    super(parent, "Edit Event", true);

    this.isRecurringEvent = eventData.isRecurring();
    this.eventDate = eventDate;
    this.originalEventData = eventData;

    setSize(500, isRecurringEvent ? 800 : 700);
    setMinimumSize(new Dimension(450, isRecurringEvent ? 750 : 650));
    setLocationRelativeTo(parent);
    setLayout(new BorderLayout());

    initializeComponents(eventData);
  }

  /**
   * Initializes all dialog components.
   */
  private void initializeComponents(EventDisplayInfo eventData) {
    JPanel formPanel = createFormPanel(eventData);
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
  private JPanel createFormPanel(EventDisplayInfo eventData) {
    JPanel formPanel = new JPanel(new GridBagLayout());
    formPanel.setBackground(Color.WHITE);
    formPanel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));

    GridBagConstraints gbc = new GridBagConstraints();
    gbc.fill = GridBagConstraints.HORIZONTAL;
    gbc.insets = new Insets(5, 20, 5, 20);
    gbc.weightx = 1.0;

    int row = 0;

    if (isRecurringEvent) {
      gbc.gridx = 0;
      gbc.gridy = row++;
      gbc.gridwidth = 2;
      recurringEditOptionsPanel = createRecurringEditOptionsPanel();
      formPanel.add(recurringEditOptionsPanel, gbc);

      gbc.gridy = row++;
      gbc.insets = new Insets(10, 20, 10, 20);
      formPanel.add(new JSeparator(), gbc);
      gbc.insets = new Insets(5, 20, 5, 20);
    }

    addFieldLabel(formPanel, gbc, row++, "Event Name: *");
    eventNameField = addTextField(formPanel, gbc, row++);
    eventNameField.setText(eventData.getEventName());

    addFieldLabel(formPanel, gbc, row++, "Date: * (MM/DD/YYYY)");
    dateField = addTextField(formPanel, gbc, row++);
    dateField.setText(eventDate.format(DATE_FORMATTER));

    if (isRecurringEvent) {
      dateField.setEditable(false);
      dateField.setBackground(new Color(240, 240, 240));
    }

    if (!isRecurringEvent) {
      multiDayCheckBox = new JCheckBox("Multi-day event");
      multiDayCheckBox.setBackground(Color.WHITE);
      multiDayCheckBox.addActionListener(e -> toggleEndDateField());

      gbc.gridx = 0;
      gbc.gridy = row++;
      gbc.gridwidth = 2;
      gbc.insets = new Insets(10, 20, 5, 20);
      formPanel.add(multiDayCheckBox, gbc);
      gbc.insets = new Insets(5, 20, 5, 20);

      addFieldLabel(formPanel, gbc, row++, "End Date: (MM/DD/YYYY)");
      endDateField = addTextField(formPanel, gbc, row++);
      endDateField.setEnabled(false);
      endDateField.setBackground(new Color(240, 240, 240));

      if (eventData.getEventEndDate() != null
          && !eventData.getEventEndDate().equals(eventData.getStartDateTime().toLocalDate())) {
        multiDayCheckBox.setSelected(true);
        endDateField.setText(eventData.getEventEndDate().format(DATE_FORMATTER));
        endDateField.setEnabled(true);
        endDateField.setBackground(Color.WHITE);
      }
    }

    gbc.gridx = 0;
    gbc.gridy = row++;
    gbc.gridwidth = 2;
    gbc.insets = new Insets(10, 20, 10, 20);
    formPanel.add(new JSeparator(), gbc);
    gbc.insets = new Insets(5, 20, 5, 20);

    addFieldLabel(formPanel, gbc, row++, "Start Time: *");
    gbc.gridx = 0;
    gbc.gridy = row++;
    gbc.gridwidth = 2;
    formPanel.add(createTimePanel(true, parseTimeFromRange(eventData.getTimeRange(), true)), gbc);

    addFieldLabel(formPanel, gbc, row++, "End Time: *");
    gbc.gridx = 0;
    gbc.gridy = row++;
    gbc.gridwidth = 2;
    formPanel.add(createTimePanel(false, parseTimeFromRange(eventData.getTimeRange(), false)), gbc);

    gbc.gridx = 0;
    gbc.gridy = row++;
    gbc.gridwidth = 2;
    gbc.insets = new Insets(10, 20, 10, 20);
    formPanel.add(new JSeparator(), gbc);
    gbc.insets = new Insets(5, 20, 5, 20);

    addFieldLabel(formPanel, gbc, row++, "Location:");
    locationField = addTextField(formPanel, gbc, row++);
    locationField.setText(eventData.getLocation() != null ? eventData.getLocation() : "");

    addFieldLabel(formPanel, gbc, row++, "Description:");
    gbc.gridx = 0;
    gbc.gridy = row++;
    gbc.gridwidth = 2;
    gbc.fill = GridBagConstraints.BOTH;
    gbc.weighty = 1.0;

    descriptionArea = new JTextArea(3, 20);
    descriptionArea.setFont(new Font("SansSerif", Font.PLAIN, 13));
    descriptionArea.setLineWrap(true);
    descriptionArea.setWrapStyleWord(true);
    descriptionArea.setBorder(BorderFactory.createLineBorder(Color.LIGHT_GRAY));

    if (eventData.getDescription() != null && !eventData.getDescription().isEmpty()) {
      descriptionArea.setText(eventData.getDescription());
    }

    JScrollPane descScrollPane = new JScrollPane(descriptionArea);
    descScrollPane.setPreferredSize(new Dimension(0, 80));
    formPanel.add(descScrollPane, gbc);

    gbc.weighty = 0;
    gbc.fill = GridBagConstraints.HORIZONTAL;

    gbc.gridx = 0;
    gbc.gridy = row++;
    gbc.gridwidth = 2;
    gbc.insets = new Insets(10, 20, 10, 20);
    formPanel.add(new JSeparator(), gbc);
    gbc.insets = new Insets(5, 20, 5, 20);

    addFieldLabel(formPanel, gbc, row++, "Status:");
    gbc.gridx = 0;
    gbc.gridy = row++;
    gbc.gridwidth = 2;
    JPanel statusPanel = createStatusPanel();
    formPanel.add(statusPanel, gbc);

    if (eventData.getStatus() != null && eventData.getStatus().equals("PRIVATE")) {
      privateRadio.setSelected(true);
    } else {
      publicRadio.setSelected(true);
    }

    if (isRecurringEvent) {
      gbc.gridx = 0;
      gbc.gridy = row++;
      gbc.gridwidth = 2;
      gbc.insets = new Insets(15, 20, 10, 20);
      formPanel.add(new JSeparator(), gbc);

      gbc.gridy = row++;
      gbc.insets = new Insets(5, 20, 5, 20);
      recurrenceDisplayPanel = createRecurrenceDisplayPanel(eventData);
      formPanel.add(recurrenceDisplayPanel, gbc);
    }

    return formPanel;
  }

  /**
   * Adds a field label to the form.
   */
  private void addFieldLabel(JPanel panel, GridBagConstraints gbc, int row, String text) {
    gbc.gridx = 0;
    gbc.gridy = row;
    gbc.gridwidth = 2;
    JLabel label = new JLabel(text);
    label.setFont(new Font("SansSerif", Font.BOLD, 13));
    panel.add(label, gbc);
  }

  /**
   * Adds a text field to the form.
   */
  private JTextField addTextField(JPanel panel, GridBagConstraints gbc, int row) {
    gbc.gridx = 0;
    gbc.gridy = row;
    gbc.gridwidth = 2;
    JTextField field = new JTextField();
    field.setFont(new Font("SansSerif", Font.PLAIN, 13));
    field.setPreferredSize(new Dimension(0, 30));
    panel.add(field, gbc);
    return field;
  }

  /**
   * Creates the recurring edit options panel.
   */
  private JPanel createRecurringEditOptionsPanel() {
    JPanel panel = new JPanel(new GridBagLayout());
    panel.setBackground(new Color(255, 245, 230));
    panel.setBorder(BorderFactory.createCompoundBorder(
        BorderFactory.createLineBorder(new Color(251, 188, 4), 2),
        BorderFactory.createEmptyBorder(10, 10, 10, 10)
    ));

    GridBagConstraints gbc = new GridBagConstraints();
    gbc.fill = GridBagConstraints.HORIZONTAL;
    gbc.insets = new Insets(3, 5, 3, 5);
    gbc.weightx = 1.0;
    gbc.gridx = 0;
    gbc.gridy = 0;

    JLabel infoLabel = new JLabel("This is a recurring event:");
    infoLabel.setFont(new Font("SansSerif", Font.BOLD, 13));
    panel.add(infoLabel, gbc);

    gbc.gridy++;
    editThisOnly = new JRadioButton("Edit only this occurrence");
    editThisOnly.setFont(new Font("SansSerif", Font.PLAIN, 12));
    editThisOnly.setBackground(new Color(255, 245, 230));
    editThisOnly.setFocusPainted(false);
    editThisOnly.setSelected(true);
    editThisOnly.addActionListener(e -> updateDateFieldEditability());
    panel.add(editThisOnly, gbc);

    gbc.gridy++;
    editFutureOccurrences = new JRadioButton("Edit all occurrences starting from this date");
    editFutureOccurrences.setFont(new Font("SansSerif", Font.PLAIN, 12));
    editFutureOccurrences.setBackground(new Color(255, 245, 230));
    editFutureOccurrences.setFocusPainted(false);
    editFutureOccurrences.addActionListener(e -> updateDateFieldEditability());
    panel.add(editFutureOccurrences, gbc);

    gbc.gridy++;
    editAllOccurrences = new JRadioButton("Edit all occurrences in the series");
    editAllOccurrences.setFont(new Font("SansSerif", Font.PLAIN, 12));
    editAllOccurrences.setBackground(new Color(255, 245, 230));
    editAllOccurrences.setFocusPainted(false);
    editAllOccurrences.addActionListener(e -> updateDateFieldEditability());
    panel.add(editAllOccurrences, gbc);

    editScopeGroup = new ButtonGroup();
    editScopeGroup.add(editThisOnly);
    editScopeGroup.add(editFutureOccurrences);
    editScopeGroup.add(editAllOccurrences);

    return panel;
  }

  /**
   * Creates a read-only display panel for recurrence information.
   */
  private JPanel createRecurrenceDisplayPanel(EventDisplayInfo eventData) {
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

    JLabel headerLabel = new JLabel("Recurrence Rule (view only)");
    headerLabel.setFont(new Font("SansSerif", Font.BOLD, 13));
    panel.add(headerLabel, gbc);

    gbc.gridy++;
    JLabel infoLabel = new JLabel(formatRecurrenceInfo(eventData.getRecurrenceInfo()));
    infoLabel.setFont(new Font("SansSerif", Font.PLAIN, 12));
    infoLabel.setForeground(new Color(80, 80, 80));
    panel.add(infoLabel, gbc);

    return panel;
  }

  /**
   * Creates a time input panel with pre-filled values.
   */
  private JPanel createTimePanel(boolean isStart, LocalTime initialTime) {
    JPanel timePanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 5, 0));
    timePanel.setBackground(Color.WHITE);

    int defaultHour = isStart ? 8 : 5;
    String defaultAmPm = isStart ? "AM" : "PM";

    if (initialTime != null) {
      int hour24 = initialTime.getHour();
      int hour12 = hour24 % 12;
      if (hour12 == 0) {
        hour12 = 12;
      }
      defaultHour = hour12;
      defaultAmPm = (hour24 >= 12) ? "PM" : "AM";
    }

    SpinnerNumberModel hourModel = new SpinnerNumberModel(defaultHour, 1, 12, 1);
    JSpinner hourSpinner = new JSpinner(hourModel);
    hourSpinner.setPreferredSize(new Dimension(60, 30));
    hourSpinner.setFont(new Font("SansSerif", Font.PLAIN, 13));

    int defaultMinute = (initialTime != null) ? initialTime.getMinute() : 0;
    SpinnerNumberModel minuteModel = new SpinnerNumberModel(defaultMinute, 0, 59, 1);
    JSpinner minuteSpinner = new JSpinner(minuteModel);
    minuteSpinner.setPreferredSize(new Dimension(60, 30));
    minuteSpinner.setFont(new Font("SansSerif", Font.PLAIN, 13));

    JComboBox<String> amPmCombo = new JComboBox<>(new String[] {"AM", "PM"});
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
   * Creates the status selection panel.
   */
  private JPanel createStatusPanel() {
    JPanel panel = new JPanel(new FlowLayout(FlowLayout.LEFT, 0, 0));
    panel.setBackground(Color.WHITE);

    publicRadio = new JRadioButton("Public");
    privateRadio = new JRadioButton("Private");

    ButtonGroup statusGroup = new ButtonGroup();
    statusGroup.add(publicRadio);
    statusGroup.add(privateRadio);

    publicRadio.setSelected(true);
    publicRadio.setBackground(Color.WHITE);
    privateRadio.setBackground(Color.WHITE);

    panel.add(publicRadio);
    panel.add(Box.createHorizontalStrut(15));
    panel.add(privateRadio);

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

    saveButton = new JButton("Save Changes");
    saveButton.setFont(new Font("SansSerif", Font.BOLD, 13));
    saveButton.setBackground(new Color(66, 133, 244));
    saveButton.setForeground(Color.WHITE);
    saveButton.setOpaque(true);
    saveButton.setFocusPainted(false);
    saveButton.setBorderPainted(false);
    saveButton.setCursor(new Cursor(Cursor.HAND_CURSOR));
    saveButton.setPreferredSize(new Dimension(140, 35));
    saveButton.addActionListener(e -> handleSave());

    footerPanel.add(cancelButton);
    footerPanel.add(saveButton);

    return footerPanel;
  }

  /**
   * Toggles the end date field visibility based on multi-day checkbox.
   */
  private void toggleEndDateField() {
    boolean isMultiDay = multiDayCheckBox.isSelected();
    endDateField.setEnabled(isMultiDay);

    if (!isMultiDay) {
      endDateField.setText("");
      endDateField.setBackground(new Color(240, 240, 240));
    } else {
      endDateField.setBackground(Color.WHITE);
    }
  }

  /**
   * Updates date field editability based on selected edit scope.
   */
  private void updateDateFieldEditability() {
    if (editThisOnly.isSelected()) {
      dateField.setEditable(true);
      dateField.setBackground(Color.WHITE);
    } else {
      dateField.setEditable(false);
      dateField.setBackground(new Color(240, 240, 240));
    }
  }

  /**
   * Handles the save button click.
   * Validates all inputs and creates EventEditData if valid.
   */
  private void handleSave() {
    try {
      String name = eventNameField.getText().trim();
      if (name.isEmpty()) {
        showError("Event name is required.");
        eventNameField.requestFocus();
        return;
      }

      LocalDate date = null;
      if (dateField.isEditable()) {
        date = parseDate(dateField.getText().trim());
        if (date == null) {
          showError("Invalid date format. Please use MM/DD/YYYY.");
          dateField.requestFocus();
          return;
        }
      }

      LocalDate eventEndDate = null;
      if (!isRecurringEvent && multiDayCheckBox != null && multiDayCheckBox.isSelected()) {
        String endDateStr = endDateField.getText().trim();
        if (endDateStr.isEmpty()) {
          showError("End date is required for multi-day events.");
          endDateField.requestFocus();
          return;
        }

        eventEndDate = parseDate(endDateStr);
        if (eventEndDate == null) {
          showError("Invalid end date format. Use MM/DD/YYYY.");
          endDateField.requestFocus();
          return;
        }

        LocalDate startDate = date != null ? date :
            originalEventData.getStartDateTime().toLocalDate();
        if (eventEndDate.isBefore(startDate)) {
          showError("End date must be on or after start date.");
          endDateField.requestFocus();
          return;
        }
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

      String status = publicRadio.isSelected() ? "PUBLIC" : "PRIVATE";

      EditScope editScope = EditScope.SINGLE;
      if (isRecurringEvent) {
        if (editThisOnly.isSelected()) {
          editScope = EditScope.THIS_ONLY;
        } else if (editFutureOccurrences.isSelected()) {
          editScope = EditScope.FUTURE_OCCURRENCES;
        } else if (editAllOccurrences.isSelected()) {
          editScope = EditScope.ALL_OCCURRENCES;
        }
      }

      String originalName = originalEventData.getEventName();
      LocalDateTime originalStartDateTime = originalEventData.getStartDateTime();
      LocalDateTime originalEndDateTime = originalEventData.getEndDateTime();

      editData = new EventEditData(
          originalName,
          originalStartDateTime,
          originalEndDateTime,
          name,
          date,
          eventEndDate,
          startTime,
          endTime,
          locationField.getText().trim(),
          descriptionArea.getText().trim(),
          status,
          editScope
      );

      confirmed = true;
      dispose();

    } catch (Exception ex) {
      showError("Error saving event: " + ex.getMessage());
      ex.printStackTrace();
    }
  }

  /**
   * Parses time from the time range string.
   */
  private LocalTime parseTimeFromRange(String timeRange, boolean isStart) {
    if (timeRange == null || timeRange.isEmpty()) {
      return null;
    }

    try {
      String[] parts = timeRange.split(" - ");
      if (parts.length != 2) {
        return null;
      }

      String timeStr = isStart ? parts[0].trim() : parts[1].trim();
      String[] timeParts = timeStr.split(" ");
      if (timeParts.length != 2) {
        return null;
      }

      String[] hourMin = timeParts[0].split(":");
      if (hourMin.length != 2) {
        return null;
      }

      int hour = Integer.parseInt(hourMin[0]);
      int minute = Integer.parseInt(hourMin[1]);
      String amPm = timeParts[1];

      if (amPm.equals("PM") && hour != 12) {
        hour += 12;
      } else if (amPm.equals("AM") && hour == 12) {
        hour = 0;
      }

      return LocalTime.of(hour, minute);

    } catch (Exception e) {
      return null;
    }
  }

  /**
   * Formats the recurrence information for display.
   */
  private String formatRecurrenceInfo(String recurrenceInfo) {
    if (recurrenceInfo == null || recurrenceInfo.isEmpty()) {
      return "Repeats on: (Not specified)";
    }

    String formatted = recurrenceInfo
        .replace("Repeats weekly on", "Repeats on:")
        .replace("repeats weekly on", "Repeats on:");

    String[] days = {"MONDAY", "TUESDAY", "WEDNESDAY", "THURSDAY", "FRIDAY", "SATURDAY", "SUNDAY"};
    String[] properDays =
          {"Monday", "Tuesday", "Wednesday", "Thursday", "Friday", "Saturday", "Sunday"};

    for (int i = 0; i < days.length; i++) {
      formatted = formatted.replace(days[i], properDays[i]);
      formatted = formatted.replace(days[i].toLowerCase(), properDays[i]);
    }

    return formatted;
  }

  /**
   * Parses a date string in MM/DD/YYYY format.
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
   * Shows an error message dialog.
   */
  private void showError(String message) {
    JOptionPane.showMessageDialog(this, message, "Validation Error",
        JOptionPane.ERROR_MESSAGE);
  }

  /**
   * Checks if the dialog was confirmed.
   */
  public boolean wasConfirmed() {
    return confirmed;
  }

  /**
   * Gets the edited event data.
   */
  public EventEditData getEditData() {
    return editData;
  }

  /**
   * Enum for edit scope options.
   */
  public enum EditScope {
    SINGLE,
    THIS_ONLY,
    FUTURE_OCCURRENCES,
    ALL_OCCURRENCES
  }

  /**
   * Data class holding event edit data with original identifiers.
   */
  public static class EventEditData {
    private final String originalName;
    private final LocalDateTime originalStartDateTime;
    private final LocalDateTime originalEndDateTime;
    private final String name;
    private final LocalDate date;
    private final LocalDate eventEndDate;
    private final LocalTime startTime;
    private final LocalTime endTime;
    private final String location;
    private final String description;
    private final String status;
    private final EditScope editScope;

    /**
     * Constructs event edit data with original identifiers and new values.
     *
     * @param originalName the current name of the event (used to identify the event)
     * @param originalStartDateTime the current start datetime (used to identify the event)
     * @param originalEndDateTime the current end datetime (used to identify the event)
     * @param name the new event name
     * @param date the new start date (null if not editing date for recurring events)
     * @param eventEndDate the new end date for multi-day events (null if single-day)
     * @param startTime the new start time
     * @param endTime the new end time
     * @param location the new location (can be null or empty)
     * @param description the new description (can be null or empty)
     * @param status the new status ("PUBLIC" or "PRIVATE")
     * @param editScope the scope of the edit (SINGLE, THIS_ONLY, FUTURE_OCCURRENCES,
     *                  or ALL_OCCURRENCES)
     */
    public EventEditData(String originalName,
                         LocalDateTime originalStartDateTime,
                         LocalDateTime originalEndDateTime,
                         String name,
                         LocalDate date,
                         LocalDate eventEndDate,
                         LocalTime startTime,
                         LocalTime endTime,
                         String location,
                         String description,
                         String status,
                         EditScope editScope) {
      this.originalName = originalName;
      this.originalStartDateTime = originalStartDateTime;
      this.originalEndDateTime = originalEndDateTime;
      this.name = name;
      this.date = date;
      this.eventEndDate = eventEndDate;
      this.startTime = startTime;
      this.endTime = endTime;
      this.location = location;
      this.description = description;
      this.status = status;
      this.editScope = editScope;
    }

    public String getOriginalName() {
      return originalName;
    }

    public LocalDateTime getOriginalStartDateTime() {
      return originalStartDateTime;
    }

    public LocalDateTime getOriginalEndDateTime() {
      return originalEndDateTime;
    }

    public String getName() {
      return name;
    }

    public LocalDate getDate() {
      return date;
    }

    public LocalDate getEventEndDate() {
      return eventEndDate;
    }

    public LocalTime getStartTime() {
      return startTime;
    }

    public LocalTime getEndTime() {
      return endTime;
    }

    public String getLocation() {
      return location;
    }

    public String getDescription() {
      return description;
    }

    public String getStatus() {
      return status;
    }

    public EditScope getEditScope() {
      return editScope;
    }
  }
}