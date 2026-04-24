package calendar.view.gui;

import calendar.controller.GuiController;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.ZonedDateTime;
import java.util.List;
import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.ButtonGroup;
import javax.swing.JButton;
import javax.swing.JCheckBox;
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
 * Dialog for creating and editing events.
 * Supports single and recurring events with full configuration options.
 */
public class EventDialog extends JDialog {
  private static final Color BACKGROUND = new Color(248, 249, 250);
  private static final Color PANEL_WHITE = Color.WHITE;
  private static final Color TEXT_DARK = new Color(31, 41, 55);
  private static final Color TEXT_MEDIUM = new Color(55, 65, 81);
  private static final Color TEXT_LIGHT = new Color(75, 85, 99);
  private static final Color BORDER_COLOR = new Color(209, 213, 219);
  private static final Color BUTTON_PRIMARY = new Color(59, 130, 246);
  private static final Color BUTTON_PRIMARY_HOVER = new Color(37, 99, 235);
  private static final int DIALOG_WIDTH = 580;
  private static final int DIALOG_HEIGHT = 650;
  private static final int FIELD_WIDTH = 520;
  private static final int FIELD_HEIGHT = 35;
  private static final int SPINNER_WIDTH = 60;
  private static final int SPINNER_HEIGHT = 32;
  private static final int RECURRING_PANEL_HEIGHT = 200;
  private static final int DESCRIPTION_ROWS = 4;
  private static final int DESCRIPTION_HEIGHT = 100;
  private static final int BUTTON_WIDTH = 90;
  private static final int BUTTON_HEIGHT = 38;
  private static final int HOUR_MIN = 0;
  private static final int HOUR_MAX = 23;
  private static final int HOUR_STEP = 1;
  private static final int MIN_MIN = 0;
  private static final int MIN_MAX = 59;
  private static final int MIN_STEP = 15;
  private static final int OCC_MIN = 1;
  private static final int OCC_MAX = 1000;
  private static final int OCC_DEFAULT = 3;
  private static final int YEAR_MIN = 2025;
  private static final int YEAR_MAX = 2100;
  private static final int MONTH_MIN = 1;
  private static final int MONTH_MAX = 12;
  private static final int DAY_MIN = 1;
  private static final int DAY_MAX = 31;
  private static final int DEFAULT_START_HOUR = 8;
  private static final int DEFAULT_END_HOUR = 17;

  private GuiController controller;
  private LocalDate date;
  private boolean confirmed = false;
  private calendar.model.Event editingEvent;
  private boolean isEditMode;
  private int editChoice = 0;

  private JTextField subjectField;
  private JSpinner startHour;
  private JSpinner startMin;
  private JSpinner endHour;
  private JSpinner endMin;
  private JTextField locationField;
  private JTextArea descriptionArea;
  private JCheckBox recurringCheckbox;
  private JCheckBox[] weekdayCheckboxes;
  private JRadioButton forOccurrencesRadio;
  private JRadioButton untilDateRadio;
  private JSpinner occurrencesSpinner;
  private JSpinner untilYearSpinner;
  private JSpinner untilMonthSpinner;
  private JSpinner untilDaySpinner;
  private JPanel recurringPanel;

  /**
   * Creates event dialog for new event on specified date.
   *
   * @param parent the parent frame
   * @param controller the GUI controller
   * @param date the date for new event
   */
  public EventDialog(JFrame parent, GuiController controller, LocalDate date) {
    super(parent, "Create Event on " + date, true);
    this.controller = controller;
    this.date = date;
    this.isEditMode = false;

    setSize(DIALOG_WIDTH, DIALOG_HEIGHT);
    setLocationRelativeTo(parent);

    initComponents();
  }

  /**
   * Creates event dialog for editing existing event.
   *
   * @param parent the parent frame
   * @param controller the GUI controller
   * @param event the event to edit
   */
  public EventDialog(JFrame parent, GuiController controller,
                     calendar.model.Event event) {
    super(parent, "Edit Event", true);
    this.controller = controller;
    this.date = event.getStart().toLocalDate();
    this.editingEvent = event;
    this.isEditMode = true;

    setSize(DIALOG_WIDTH, DIALOG_HEIGHT);
    setLocationRelativeTo(parent);

    initComponents();
    populateFields(event);
  }

  private void initComponents() {
    setLayout(new BorderLayout(0, 0));
    getContentPane().setBackground(BACKGROUND);

    JPanel formPanel = new JPanel();
    formPanel.setLayout(new BoxLayout(formPanel, BoxLayout.Y_AXIS));
    formPanel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));
    formPanel.setBackground(BACKGROUND);

    formPanel.add(createSubjectPanel());
    formPanel.add(Box.createVerticalStrut(12));
    formPanel.add(createTimePanel());
    formPanel.add(Box.createVerticalStrut(15));
    formPanel.add(createRecurringCheckbox());
    formPanel.add(Box.createVerticalStrut(10));
    formPanel.add(createRecurringPanel());
    formPanel.add(Box.createVerticalStrut(15));
    formPanel.add(createOptionalFieldsPanel());

    JScrollPane scrollPane = new JScrollPane(formPanel);
    scrollPane.setBorder(BorderFactory.createEmptyBorder());
    scrollPane.getVerticalScrollBar().setUnitIncrement(16);
    add(scrollPane, BorderLayout.CENTER);

    add(createButtonPanel(), BorderLayout.SOUTH);
  }

  private JPanel createSubjectPanel() {
    JPanel panel = new JPanel();
    panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
    panel.setBackground(BACKGROUND);
    panel.setAlignmentX(LEFT_ALIGNMENT);

    JLabel label = new JLabel("Subject");
    label.setFont(new Font(Font.SANS_SERIF, Font.BOLD, 13));
    label.setForeground(TEXT_DARK);
    label.setBorder(BorderFactory.createEmptyBorder(0, 0, 5, 0));
    label.setAlignmentX(LEFT_ALIGNMENT);

    subjectField = new JTextField();
    subjectField.setFont(new Font(Font.SANS_SERIF, Font.PLAIN, 14));
    subjectField.setPreferredSize(new Dimension(FIELD_WIDTH, FIELD_HEIGHT));
    subjectField.setMaximumSize(new Dimension(FIELD_WIDTH, FIELD_HEIGHT));
    subjectField.setBorder(BorderFactory.createCompoundBorder(
        BorderFactory.createLineBorder(BORDER_COLOR, 1),
        BorderFactory.createEmptyBorder(5, 10, 5, 10)
    ));
    subjectField.setAlignmentX(LEFT_ALIGNMENT);

    panel.add(label);
    panel.add(subjectField);
    return panel;
  }

  private JPanel createTimePanel() {
    JPanel panel = new JPanel();
    panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
    panel.setBackground(BACKGROUND);
    panel.setAlignmentX(LEFT_ALIGNMENT);

    JPanel startPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 5));
    startPanel.setBackground(BACKGROUND);

    JLabel startLabel = new JLabel("Start:");
    startLabel.setFont(new Font(Font.SANS_SERIF, Font.BOLD, 13));
    startLabel.setForeground(TEXT_DARK);

    startHour = new JSpinner(new SpinnerNumberModel(
        DEFAULT_START_HOUR, HOUR_MIN, HOUR_MAX, HOUR_STEP));
    startMin = new JSpinner(new SpinnerNumberModel(
        MIN_MIN, MIN_MIN, MIN_MAX, MIN_STEP));
    styleSpinner(startHour);
    styleSpinner(startMin);

    startPanel.add(startLabel);
    startPanel.add(startHour);
    startPanel.add(new JLabel(":"));
    startPanel.add(startMin);

    JPanel endPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 5));
    endPanel.setBackground(BACKGROUND);

    JLabel endLabel = new JLabel("End:");
    endLabel.setFont(new Font(Font.SANS_SERIF, Font.BOLD, 13));
    endLabel.setForeground(TEXT_DARK);

    endHour = new JSpinner(new SpinnerNumberModel(
        DEFAULT_END_HOUR, HOUR_MIN, HOUR_MAX, HOUR_STEP));
    endMin = new JSpinner(new SpinnerNumberModel(
        MIN_MIN, MIN_MIN, MIN_MAX, MIN_STEP));
    styleSpinner(endHour);
    styleSpinner(endMin);

    endPanel.add(endLabel);
    endPanel.add(endHour);
    endPanel.add(new JLabel(":"));
    endPanel.add(endMin);

    panel.add(startPanel);
    panel.add(endPanel);
    return panel;
  }

  private void styleSpinner(JSpinner spinner) {
    spinner.setFont(new Font(Font.SANS_SERIF, Font.PLAIN, 14));
    spinner.setPreferredSize(new Dimension(SPINNER_WIDTH, SPINNER_HEIGHT));
    ((JSpinner.DefaultEditor) spinner.getEditor()).getTextField()
        .setHorizontalAlignment(JTextField.CENTER);
  }

  private JCheckBox createRecurringCheckbox() {
    recurringCheckbox = new JCheckBox("Make this a recurring event");
    recurringCheckbox.setFont(new Font(Font.SANS_SERIF, Font.BOLD, 13));
    recurringCheckbox.setForeground(TEXT_DARK);
    recurringCheckbox.setBackground(BACKGROUND);
    recurringCheckbox.setCursor(new Cursor(Cursor.HAND_CURSOR));
    recurringCheckbox.setAlignmentX(LEFT_ALIGNMENT);
    recurringCheckbox.addActionListener(e -> toggleRecurringPanel());
    return recurringCheckbox;
  }

  private JPanel createRecurringPanel() {
    recurringPanel = new JPanel();
    recurringPanel.setLayout(new BoxLayout(recurringPanel, BoxLayout.Y_AXIS));
    recurringPanel.setBackground(PANEL_WHITE);
    recurringPanel.setBorder(BorderFactory.createCompoundBorder(
        BorderFactory.createLineBorder(BORDER_COLOR, 1),
        BorderFactory.createEmptyBorder(15, 15, 15, 15)
    ));
    recurringPanel.setVisible(false);
    recurringPanel.setAlignmentX(LEFT_ALIGNMENT);
    recurringPanel.setMaximumSize(new Dimension(FIELD_WIDTH, RECURRING_PANEL_HEIGHT));

    recurringPanel.add(createWeekdayPanel());
    recurringPanel.add(Box.createVerticalStrut(12));
    recurringPanel.add(createEndsPanel());

    return recurringPanel;
  }

  private JPanel createWeekdayPanel() {
    JPanel panel = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 5));
    panel.setBackground(PANEL_WHITE);

    JLabel label = new JLabel("Repeat on:");
    label.setFont(new Font(Font.SANS_SERIF, Font.BOLD, 12));
    label.setForeground(TEXT_MEDIUM);
    panel.add(label);

    weekdayCheckboxes = new JCheckBox[7];
    String[] labels = {"M", "T", "W", "R", "F", "S", "U"};
    for (int i = 0; i < 7; i++) {
      weekdayCheckboxes[i] = new JCheckBox(labels[i]);
      weekdayCheckboxes[i].setFont(new Font(Font.SANS_SERIF, Font.BOLD, 11));
      weekdayCheckboxes[i].setBackground(PANEL_WHITE);
      weekdayCheckboxes[i].setCursor(new Cursor(Cursor.HAND_CURSOR));
      panel.add(weekdayCheckboxes[i]);
    }

    return panel;
  }

  private JPanel createEndsPanel() {
    JPanel panel = new JPanel();
    panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
    panel.setBackground(PANEL_WHITE);

    final ButtonGroup endsGroup = new ButtonGroup();

    JPanel forPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 3));
    forPanel.setBackground(PANEL_WHITE);
    forOccurrencesRadio = new JRadioButton("After");
    forOccurrencesRadio.setFont(new Font(Font.SANS_SERIF, Font.PLAIN, 12));
    forOccurrencesRadio.setBackground(PANEL_WHITE);
    forOccurrencesRadio.setCursor(new Cursor(Cursor.HAND_CURSOR));

    occurrencesSpinner = new JSpinner(new SpinnerNumberModel(
        OCC_DEFAULT, OCC_MIN, OCC_MAX, HOUR_STEP));
    styleSpinner(occurrencesSpinner);

    JLabel occLabel = new JLabel("occurrences");
    occLabel.setFont(new Font(Font.SANS_SERIF, Font.PLAIN, 12));

    forPanel.add(forOccurrencesRadio);
    forPanel.add(occurrencesSpinner);
    forPanel.add(occLabel);
    endsGroup.add(forOccurrencesRadio);

    JPanel untilPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 3));
    untilPanel.setBackground(PANEL_WHITE);
    untilDateRadio = new JRadioButton("Until");
    untilDateRadio.setFont(new Font(Font.SANS_SERIF, Font.PLAIN, 12));
    untilDateRadio.setBackground(PANEL_WHITE);
    untilDateRadio.setCursor(new Cursor(Cursor.HAND_CURSOR));

    LocalDate defaultUntil = date.plusWeeks(1);
    untilYearSpinner = new JSpinner(new SpinnerNumberModel(
        defaultUntil.getYear(), YEAR_MIN, YEAR_MAX, HOUR_STEP));
    untilMonthSpinner = new JSpinner(new SpinnerNumberModel(
        defaultUntil.getMonthValue(), MONTH_MIN, MONTH_MAX, HOUR_STEP));
    untilDaySpinner = new JSpinner(new SpinnerNumberModel(
        defaultUntil.getDayOfMonth(), DAY_MIN, DAY_MAX, HOUR_STEP));

    styleSpinner(untilYearSpinner);
    styleSpinner(untilMonthSpinner);
    styleSpinner(untilDaySpinner);

    untilPanel.add(untilDateRadio);
    untilPanel.add(untilYearSpinner);
    untilPanel.add(new JLabel("/"));
    untilPanel.add(untilMonthSpinner);
    untilPanel.add(new JLabel("/"));
    untilPanel.add(untilDaySpinner);
    endsGroup.add(untilDateRadio);

    forOccurrencesRadio.setSelected(true);

    panel.add(forPanel);
    panel.add(untilPanel);

    return panel;
  }

  private JPanel createOptionalFieldsPanel() {
    JPanel panel = new JPanel();
    panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
    panel.setBackground(BACKGROUND);
    panel.setAlignmentX(LEFT_ALIGNMENT);

    JLabel locLabel = new JLabel("Location");
    locLabel.setFont(new Font(Font.SANS_SERIF, Font.BOLD, 13));
    locLabel.setForeground(TEXT_DARK);
    locLabel.setBorder(BorderFactory.createEmptyBorder(0, 0, 5, 0));
    locLabel.setAlignmentX(LEFT_ALIGNMENT);

    locationField = new JTextField();
    locationField.setFont(new Font(Font.SANS_SERIF, Font.PLAIN, 14));
    locationField.setPreferredSize(new Dimension(FIELD_WIDTH, FIELD_HEIGHT));
    locationField.setMaximumSize(new Dimension(FIELD_WIDTH, FIELD_HEIGHT));
    locationField.setBorder(BorderFactory.createCompoundBorder(
        BorderFactory.createLineBorder(BORDER_COLOR, 1),
        BorderFactory.createEmptyBorder(5, 10, 5, 10)
    ));
    locationField.setAlignmentX(LEFT_ALIGNMENT);

    JLabel descLabel = new JLabel("Description");
    descLabel.setFont(new Font(Font.SANS_SERIF, Font.BOLD, 13));
    descLabel.setForeground(TEXT_DARK);
    descLabel.setBorder(BorderFactory.createEmptyBorder(12, 0, 5, 0));
    descLabel.setAlignmentX(LEFT_ALIGNMENT);

    descriptionArea = new JTextArea(DESCRIPTION_ROWS, 30);
    descriptionArea.setFont(new Font(Font.SANS_SERIF, Font.PLAIN, 13));
    descriptionArea.setLineWrap(true);
    descriptionArea.setWrapStyleWord(true);

    JScrollPane scrollPane = new JScrollPane(descriptionArea);
    scrollPane.setBorder(BorderFactory.createLineBorder(BORDER_COLOR, 1));
    scrollPane.setPreferredSize(new Dimension(FIELD_WIDTH, DESCRIPTION_HEIGHT));
    scrollPane.setMaximumSize(new Dimension(FIELD_WIDTH, DESCRIPTION_HEIGHT));
    scrollPane.setAlignmentX(LEFT_ALIGNMENT);

    panel.add(locLabel);
    panel.add(locationField);
    panel.add(Box.createVerticalStrut(5));
    panel.add(descLabel);
    panel.add(scrollPane);

    return panel;
  }

  private JPanel createButtonPanel() {
    JPanel panel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 12, 18));
    panel.setBackground(PANEL_WHITE);
    panel.setBorder(BorderFactory.createMatteBorder(1, 0, 0, 0, BORDER_COLOR));

    JButton cancelBtn = new JButton("Cancel");
    cancelBtn.setFont(new Font(Font.SANS_SERIF, Font.PLAIN, 13));
    cancelBtn.setForeground(TEXT_LIGHT);
    cancelBtn.setBackground(PANEL_WHITE);
    cancelBtn.setPreferredSize(new Dimension(BUTTON_WIDTH, BUTTON_HEIGHT));
    cancelBtn.setFocusPainted(false);
    cancelBtn.setCursor(new Cursor(Cursor.HAND_CURSOR));
    cancelBtn.setOpaque(true);
    cancelBtn.setBorderPainted(true);
    cancelBtn.setBorder(BorderFactory.createLineBorder(BORDER_COLOR, 1));
    cancelBtn.addActionListener(e -> dispose());

    JButton createBtn = new JButton(isEditMode ? "Save" : "Create");
    createBtn.setFont(new Font(Font.SANS_SERIF, Font.BOLD, 13));
    createBtn.setForeground(PANEL_WHITE);
    createBtn.setBackground(BUTTON_PRIMARY);
    createBtn.setPreferredSize(new Dimension(BUTTON_WIDTH, BUTTON_HEIGHT));
    createBtn.setFocusPainted(false);
    createBtn.setCursor(new Cursor(Cursor.HAND_CURSOR));
    createBtn.setOpaque(true);
    createBtn.setBorderPainted(false);
    createBtn.addActionListener(e -> onCreate());

    createBtn.addMouseListener(new java.awt.event.MouseAdapter() {
      public void mouseEntered(java.awt.event.MouseEvent e) {
        createBtn.setBackground(BUTTON_PRIMARY_HOVER);
      }

      public void mouseExited(java.awt.event.MouseEvent e) {
        createBtn.setBackground(BUTTON_PRIMARY);
      }
    });

    panel.add(cancelBtn);
    panel.add(createBtn);

    return panel;
  }

  private void toggleRecurringPanel() {
    recurringPanel.setVisible(recurringCheckbox.isSelected());
    pack();
  }

  private void onCreate() {
    if (subjectField.getText().trim().isEmpty()) {
      JOptionPane.showMessageDialog(this,
          "Please enter a subject", "Error", JOptionPane.ERROR_MESSAGE);
      return;
    }

    ZonedDateTime start = getStartTime();
    ZonedDateTime end = getEndTime();
    if (!end.isAfter(start)) {
      JOptionPane.showMessageDialog(this,
          "End time must be after start time", "Error", JOptionPane.ERROR_MESSAGE);
      return;
    }

    if (recurringCheckbox.isSelected() && getWeekdays().isEmpty()) {
      JOptionPane.showMessageDialog(this,
          "Please select at least one weekday", "Error", JOptionPane.ERROR_MESSAGE);
      return;
    }

    if (isEditMode && editingEvent.isPartOfSeries()) {
      showEditSeriesOptions();
      return;
    }

    confirmed = true;
    dispose();
  }

  private void showEditSeriesOptions() {
    String[] options = {"This event only", "This and future events",
        "All events in series"};
    int choice = JOptionPane.showOptionDialog(this,
        "This is a recurring event. What would you like to edit?",
        "Edit Recurring Event",
        JOptionPane.DEFAULT_OPTION,
        JOptionPane.QUESTION_MESSAGE,
        null,
        options,
        options[0]);

    if (choice >= 0) {
      editChoice = choice;

      if (choice == 0) {
        recurringCheckbox.setSelected(false);
        recurringCheckbox.setEnabled(false);
        toggleRecurringPanel();
      }

      confirmed = true;
      dispose();
    }
  }

  private void populateFields(calendar.model.Event e) {
    subjectField.setText(e.getSubject());

    int sh = e.getStart().getHour();
    int sm = e.getStart().getMinute();
    startHour.setValue(sh);
    startMin.setValue(sm);

    int eh = e.getEnd().getHour();
    int em = e.getEnd().getMinute();
    endHour.setValue(eh);
    endMin.setValue(em);

    if (e.getDescription() != null) {
      descriptionArea.setText(e.getDescription());
    }
    if (e.getLocation() != null) {
      locationField.setText(e.getLocation());
    }

    if (e.isPartOfSeries()) {
      recurringCheckbox.setSelected(true);
      recurringCheckbox.setEnabled(true);
      toggleRecurringPanel();

      List<calendar.model.Event> series = controller.getEventsInSeries(e.getSeriesId());

      setWeekdayCheckboxes(series);

      forOccurrencesRadio.setSelected(true);
      occurrencesSpinner.setValue(series.size());

      LocalDate defaultUntil = e.getStart().toLocalDate().plusWeeks(1);
      untilYearSpinner.setValue(defaultUntil.getYear());
      untilMonthSpinner.setValue(defaultUntil.getMonthValue());
      untilDaySpinner.setValue(defaultUntil.getDayOfMonth());
    }
  }

  private void setWeekdayCheckboxes(List<calendar.model.Event> series) {
    java.util.Set<DayOfWeek> days = series.stream()
        .map(evt -> evt.getStart().getDayOfWeek())
        .collect(java.util.stream.Collectors.toSet());

    weekdayCheckboxes[0].setSelected(days.contains(DayOfWeek.MONDAY));
    weekdayCheckboxes[1].setSelected(days.contains(DayOfWeek.TUESDAY));
    weekdayCheckboxes[2].setSelected(days.contains(DayOfWeek.WEDNESDAY));
    weekdayCheckboxes[3].setSelected(days.contains(DayOfWeek.THURSDAY));
    weekdayCheckboxes[4].setSelected(days.contains(DayOfWeek.FRIDAY));
    weekdayCheckboxes[5].setSelected(days.contains(DayOfWeek.SATURDAY));
    weekdayCheckboxes[6].setSelected(days.contains(DayOfWeek.SUNDAY));
  }

  /**
   * Returns whether user confirmed the dialog.
   *
   * @return true if confirmed, false if cancelled
   */
  public boolean wasConfirmed() {
    return confirmed;
  }

  /**
   * Gets the event subject entered by user.
   *
   * @return the event subject
   */
  public String getSubject() {
    return subjectField.getText().trim();
  }

  /**
   * Returns whether event is configured as recurring.
   *
   * @return true if recurring checkbox is selected
   */
  public boolean isRecurring() {
    return recurringCheckbox.isSelected();
  }

  /**
   * Gets the event start time from dialog inputs.
   *
   * @return the start time in calendar timezone
   */
  public ZonedDateTime getStartTime() {
    int h = (Integer) startHour.getValue();
    int m = (Integer) startMin.getValue();
    return date.atTime(h, m).atZone(controller.getCurrentCalendar().getTimezone());
  }

  /**
   * Gets the event end time from dialog inputs.
   *
   * @return the end time in calendar timezone
   */
  public ZonedDateTime getEndTime() {
    int h = (Integer) endHour.getValue();
    int m = (Integer) endMin.getValue();
    return date.atTime(h, m).atZone(controller.getCurrentCalendar().getTimezone());
  }

  /**
   * Gets the weekday pattern for recurring events.
   *
   * @return weekday string (e.g., "MWF") or empty if none selected
   */
  public String getWeekdays() {
    StringBuilder sb = new StringBuilder();
    String[] chars = {"M", "T", "W", "R", "F", "S", "U"};
    for (int i = 0; i < 7; i++) {
      if (weekdayCheckboxes[i].isSelected()) {
        sb.append(chars[i]);
      }
    }
    return sb.toString();
  }

  /**
   * Gets the occurrence count if "After N occurrences" is selected.
   *
   * @return occurrence count or null if not selected
   */
  public Integer getOccurrences() {
    return forOccurrencesRadio.isSelected()
        ? (Integer) occurrencesSpinner.getValue()
        : null;
  }

  /**
   * Gets the end date if "Until date" is selected.
   *
   * @return end date or null if not selected
   */
  public LocalDate getUntilDate() {
    if (!untilDateRadio.isSelected()) {
      return null;
    }
    int y = (Integer) untilYearSpinner.getValue();
    int m = (Integer) untilMonthSpinner.getValue();
    int d = (Integer) untilDaySpinner.getValue();
    return LocalDate.of(y, m, d);
  }

  /**
   * Gets the event location entered by user.
   *
   * @return the location or empty string
   */
  public String getEventLocation() {
    return locationField.getText().trim();
  }

  /**
   * Gets the event description entered by user.
   *
   * @return the description or empty string
   */
  public String getEventDescription() {
    return descriptionArea.getText().trim();
  }

  /**
   * Returns whether dialog is in edit mode.
   *
   * @return true if editing existing event
   */
  public boolean isEditMode() {
    return isEditMode;
  }

  /**
   * Gets the event being edited.
   *
   * @return the editing event or null if creating new
   */
  public calendar.model.Event getEditingEvent() {
    return editingEvent;
  }

  /**
   * Gets the user's choice for editing recurring events.
   *
   * @return 0 for single, 1 for forward, 2 for entire series
   */
  public int getEditChoice() {
    return editChoice;
  }
}