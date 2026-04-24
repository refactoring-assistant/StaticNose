package view;

import controller.CalendarController;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.FlowLayout;
import java.awt.GridLayout;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.Month;
import java.time.Year;
import javax.swing.BorderFactory;
import javax.swing.BoxLayout;
import javax.swing.ButtonGroup;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
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
 * A pop up that allows an event to be added by filling out the
 * required fields. By default, it sets the toggle for an all day event on
 * for the date that was selected on the calendar. When untoggled, it allows
 * the time to be set.
 */
public final class AddEventPopUp extends AbstractCommandEvents {

  private final CalendarController controller;
  private final LocalDate selectedDate;
  private final EventsListPanel eventsListPanel;

  private JPanel form;
  private JTextField subjectField;

  private JCheckBox allDayCheckBox;

  private JCheckBox repeatCheckBox;
  private JPanel[] repeatDayPanels;
  private String[] repeatDayCodes;
  private JRadioButton repeatForRadio;
  private JRadioButton repeatUntilRadio;
  private JSpinner repeatCountSpinner;
  private JComboBox<Month> repeatUntilMonthBox;
  private JComboBox<Integer> repeatUntilYearBox;
  private JComboBox<Integer> repeatUntilDayBox;

  private JTextArea descriptionArea;

  private JPanel timeStartRow;
  private JPanel timeEndRow;
  private JPanel repeatDaysRow;
  private JPanel repeatRangeRow;

  /**
   * Construct an AddEventPopUp so the user had add an event to the calendar
   * given that they have selected a day to add it to.
   *
   * @param controller the controller
   * @param selectedDate the selected date they want to add the event too
   * @param eventsListPanel the list of events for the selected date
   */
  public AddEventPopUp(CalendarController controller, LocalDate selectedDate,
                       EventsListPanel eventsListPanel) {
    //super("Add Event");
    this.controller = controller;
    this.selectedDate = selectedDate;
    this.eventsListPanel = eventsListPanel;

    buildUi();
    pack();
    setLocationRelativeTo(null);
  }

  private void buildUi() {
    JPanel wrapper = new JPanel(new BorderLayout(5, 5));
    wrapper.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

    form = new JPanel();
    form.setLayout(new BoxLayout(form, BoxLayout.Y_AXIS));

    addSubject();
    addDate();
    addTime();
    addRepeat();
    addLocation();
    addStatus();

    final JPanel descRow = addDescription();

    JButton confirmButton = new JButton("Confirm");
    JButton cancelButton = new JButton("Cancel");

    confirmButton.addActionListener(e -> confirmEvent());
    cancelButton.addActionListener(e -> dispose());

    JPanel buttonsPanel = new JPanel();
    buttonsPanel.add(confirmButton);
    buttonsPanel.add(cancelButton);

    wrapper.setBorder(BorderFactory.createEmptyBorder(12, 12, 12, 12));

    wrapper.add(form, BorderLayout.NORTH);
    wrapper.add(descRow, BorderLayout.CENTER);
    wrapper.add(buttonsPanel, BorderLayout.SOUTH);

    setContentPane(wrapper);
  }

  private JPanel makeRow(java.awt.Component left, java.awt.Component right) {
    return buildRow(left, right);
  }

  private void addSubject() {
    JLabel subjectLabel = new JLabel("Subject");
    subjectField = new JTextField();

    form.add(makeRow(subjectLabel, subjectField));
  }

  private void addDate() {
    final JLabel startLabel = new JLabel("Start date:");
    final JLabel endLabel = new JLabel("End date:");

    startMonthBox = new JComboBox<>(Month.values());
    startMonthBox.setSelectedItem(selectedDate.getMonth());

    endMonthBox = new JComboBox<>(Month.values());
    endMonthBox.setSelectedItem(selectedDate.getMonth());

    int currentYear = Year.now().getValue();
    startYearBox = new JComboBox<>();
    endYearBox = new JComboBox<>();
    int yearIndex = currentYear - 1;
    while (yearIndex <= currentYear + 3) {
      startYearBox.addItem(yearIndex);
      endYearBox.addItem(yearIndex);
      yearIndex = yearIndex + 1;
    }
    startYearBox.setSelectedItem(selectedDate.getYear());
    endYearBox.setSelectedItem(selectedDate.getYear());

    startDayBox = new JComboBox<>();
    endDayBox = new JComboBox<>();

    updateDay(startDayBox, selectedDate.getYear(), selectedDate.getMonth());
    updateDay(endDayBox, selectedDate.getYear(), selectedDate.getMonth());

    startDayBox.setSelectedItem(selectedDate.getDayOfMonth());
    endDayBox.setSelectedItem(selectedDate.getDayOfMonth());

    allDayCheckBox = new JCheckBox("All day");
    allDayCheckBox.setSelected(true);

    Integer initYear = selectedYear(startYearBox);
    Month initMonth = selectedMonth(startMonthBox);
    if (initYear != null) {
      endYearBox.setSelectedItem(initYear);
    }
    if (initMonth != null && initYear != null) {
      endMonthBox.setSelectedItem(initMonth);
      updateDay(endDayBox, initYear, initMonth);
    }
    if (startDayBox.getSelectedItem() != null) {
      endDayBox.setSelectedItem(startDayBox.getSelectedItem());
    }

    endYearBox.setEnabled(false);
    endMonthBox.setEnabled(false);
    endDayBox.setEnabled(false);

    startMonthBox.addActionListener(e -> {
      Integer year = selectedYear(startYearBox);
      Month month = selectedMonth(startMonthBox);
      if (year == null || month == null) {
        return;
      }
      updateDay(startDayBox, year, month);

      if (allDayCheckBox.isSelected()) {
        endYearBox.setSelectedItem(year);
        endMonthBox.setSelectedItem(month);
        updateDay(endDayBox, year, month);
        endDayBox.setSelectedItem(startDayBox.getSelectedItem());
      }
    });

    startYearBox.addActionListener(e -> {
      Integer year = selectedYear(startYearBox);
      Month month = selectedMonth(startMonthBox);
      if (year == null || month == null) {
        return;
      }
      updateDay(startDayBox, year, month);

      if (allDayCheckBox.isSelected()) {
        endYearBox.setSelectedItem(year);
        endMonthBox.setSelectedItem(month);
        updateDay(endDayBox, year, month);
        endDayBox.setSelectedItem(startDayBox.getSelectedItem());
      }
    });

    endMonthBox.addActionListener(e -> {
      Integer year = selectedYear(endYearBox);
      Month month = selectedMonth(endMonthBox);
      if (year == null || month == null) {
        return;
      }
      updateDay(endDayBox, year, month);
    });

    endYearBox.addActionListener(e -> {
      Integer year = selectedYear(endYearBox);
      Month month = selectedMonth(endMonthBox);
      if (year == null || month == null) {
        return;
      }
      updateDay(endDayBox, year, month);
    });

    JPanel startDatePanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 4, 0));
    startDatePanel.add(startMonthBox);
    startDatePanel.add(new JLabel("/"));
    startDatePanel.add(startDayBox);
    startDatePanel.add(new JLabel("/"));
    startDatePanel.add(startYearBox);

    JPanel endDatePanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 4, 0));
    endDatePanel.add(endMonthBox);
    endDatePanel.add(new JLabel("/"));
    endDatePanel.add(endDayBox);
    endDatePanel.add(new JLabel("/"));
    endDatePanel.add(endYearBox);

    form.add(makeRow(startLabel, startDatePanel));
    form.add(makeRow(endLabel, endDatePanel));
    form.add(makeRow(new JLabel(), allDayCheckBox));
  }

  private void addTime() {
    String[] hourOptions = {
        "01", "02", "03", "04", "05", "06",
        "07", "08", "09", "10", "11", "12"};
    String[] minuteOptions = {"00", "15", "30", "45"};
    String[] amPmOptions = {"AM", "PM"};

    startHourBox = new JComboBox<>(hourOptions);
    startMinuteBox = new JComboBox<>(minuteOptions);
    startAmPmBox = new JComboBox<>(amPmOptions);

    endHourBox = new JComboBox<>(hourOptions);
    endMinuteBox = new JComboBox<>(minuteOptions);
    endAmPmBox = new JComboBox<>(amPmOptions);

    startHourBox.setSelectedItem("08");
    startMinuteBox.setSelectedItem("00");
    startAmPmBox.setSelectedItem("AM");

    endHourBox.setSelectedItem("05");
    endMinuteBox.setSelectedItem("00");
    endAmPmBox.setSelectedItem("PM");

    JPanel startTimePanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 4, 0));
    startTimePanel.add(startHourBox);
    startTimePanel.add(new JLabel(":"));
    startTimePanel.add(startMinuteBox);
    startTimePanel.add(startAmPmBox);

    JPanel endTimePanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 4, 0));
    endTimePanel.add(endHourBox);
    endTimePanel.add(new JLabel(":"));
    endTimePanel.add(endMinuteBox);
    endTimePanel.add(endAmPmBox);

    JLabel startTimeLabel = new JLabel("Start time:");
    JLabel endTimeLabel = new JLabel("End time:");
    timeStartRow = makeRow(startTimeLabel, startTimePanel);
    timeEndRow = makeRow(endTimeLabel, endTimePanel);
    setTimeRowsVisible(false);

    allDayCheckBox.addActionListener(e -> {
      if (allDayCheckBox.isSelected()) {
        Integer year = selectedYear(startYearBox);
        Month month = selectedMonth(startMonthBox);
        Integer day = selectedInteger(startDayBox);
        if (year != null) {
          endYearBox.setSelectedItem(year);
        }
        if (month != null) {
          endMonthBox.setSelectedItem(month);
          if (year != null) {
            updateDay(endDayBox, year, month);
          }
        }
        if (day != null) {
          endDayBox.setSelectedItem(day);
        }

        endYearBox.setEnabled(false);
        endMonthBox.setEnabled(false);
        endDayBox.setEnabled(false);
        setTimeRowsVisible(false);
      } else {
        endYearBox.setEnabled(true);
        endMonthBox.setEnabled(true);
        endDayBox.setEnabled(true);

        setTimeRowsVisible(true);
      }
      pack();
    });

    form.add(timeStartRow);
    form.add(timeEndRow);
  }

  private void addRepeat() {
    repeatCheckBox = new JCheckBox("Repeat");
    JPanel repeatDaysPanel = buildRepeatDaysPanel();
    JPanel repeatRangePanel = buildRepeatRangePanel();

    JLabel repeatDaysLabel = new JLabel("Repeat on:");
    JLabel repeatRangeLabel = new JLabel("Repeat range:");

    repeatDaysRow = makeRow(repeatDaysLabel, repeatDaysPanel);
    repeatRangeRow = makeRow(repeatRangeLabel, repeatRangePanel);
    form.add(makeRow(new JLabel(), repeatCheckBox));
    form.add(repeatDaysRow);
    form.add(repeatRangeRow);
    wireRepeatToggle();
    setRepeatRowsVisible(false);
  }

  private JPanel buildRepeatDaysPanel() {
    JPanel repeatDaysPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 4, 0));

    repeatDayCodes = new String[] {"M", "T", "W", "R", "F", "S", "U"};
    repeatDayPanels = new JPanel[repeatDayCodes.length];

    for (int index = 0; index < repeatDayCodes.length; index++) {
      String code = repeatDayCodes[index];
      JPanel dayPanel = new JPanel();
      dayPanel.setPreferredSize(new java.awt.Dimension(28, 28));
      dayPanel.setBackground(Color.WHITE);
      dayPanel.setBorder(BorderFactory.createLineBorder(new Color(210, 210, 210)));

      JLabel dayLabel = new JLabel(code);
      dayPanel.add(dayLabel);
      dayPanel.putClientProperty("selected", Boolean.FALSE);
      dayPanel.addMouseListener(new MouseAdapter() {
        @Override
        public void mouseClicked(MouseEvent e) {
          toggleRepeatDaySelection(dayPanel, dayLabel);
        }
      });

      repeatDayPanels[index] = dayPanel;
      repeatDaysPanel.add(dayPanel);
    }
    return repeatDaysPanel;
  }

  private JPanel buildRepeatRangePanel() {
    repeatForRadio = new JRadioButton("For");
    repeatUntilRadio = new JRadioButton("Until");

    ButtonGroup repeatModeGroup = new ButtonGroup();
    repeatModeGroup.add(repeatForRadio);
    repeatModeGroup.add(repeatUntilRadio);

    repeatForRadio.setSelected(true);

    repeatCountSpinner = new JSpinner(
        new SpinnerNumberModel(5, 1, 365, 1));

    repeatUntilMonthBox = new JComboBox<>(Month.values());
    repeatUntilMonthBox.setSelectedItem(selectedDate.getMonth());

    repeatUntilYearBox = new JComboBox<>();
    int currentYear2 = Year.now().getValue();
    for (int val = currentYear2 - 1; val <= currentYear2 + 3; val++) {
      repeatUntilYearBox.addItem(val);
    }
    repeatUntilYearBox.setSelectedItem(selectedDate.getYear());

    repeatUntilDayBox = new JComboBox<>();
    updateDay(repeatUntilDayBox, selectedDate.getYear(), selectedDate.getMonth());
    repeatUntilDayBox.setSelectedItem(selectedDate.getDayOfMonth());

    repeatUntilMonthBox.addActionListener(e -> refreshRepeatUntilDays());
    repeatUntilYearBox.addActionListener(e -> refreshRepeatUntilDays());

    JPanel repeatUntilDatePanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 4, 0));
    repeatUntilDatePanel.add(repeatUntilMonthBox);
    repeatUntilDatePanel.add(new JLabel("/"));
    repeatUntilDatePanel.add(repeatUntilDayBox);
    repeatUntilDatePanel.add(new JLabel("/"));
    repeatUntilDatePanel.add(repeatUntilYearBox);

    JPanel forRow = new JPanel(new FlowLayout(FlowLayout.LEFT, 4, 0));
    forRow.add(repeatForRadio);
    forRow.add(repeatCountSpinner);
    forRow.add(new JLabel("times"));

    JPanel untilRow = new JPanel(new FlowLayout(FlowLayout.LEFT, 4, 0));
    untilRow.add(repeatUntilRadio);
    untilRow.add(repeatUntilDatePanel);

    JPanel repeatRangePanel = new JPanel(new GridLayout(0, 1, 2, 2));
    repeatRangePanel.add(forRow);
    repeatRangePanel.add(untilRow);

    configureRepeatRangeEnabling();
    return repeatRangePanel;
  }

  private void configureRepeatRangeEnabling() {
    repeatCountSpinner.setEnabled(true);
    repeatUntilMonthBox.setEnabled(false);
    repeatUntilDayBox.setEnabled(false);
    repeatUntilYearBox.setEnabled(false);

    repeatForRadio.addActionListener(e -> {
      repeatCountSpinner.setEnabled(true);
      repeatUntilMonthBox.setEnabled(false);
      repeatUntilDayBox.setEnabled(false);
      repeatUntilYearBox.setEnabled(false);
    });

    repeatUntilRadio.addActionListener(e -> {
      repeatCountSpinner.setEnabled(false);
      repeatUntilMonthBox.setEnabled(true);
      repeatUntilDayBox.setEnabled(true);
      repeatUntilYearBox.setEnabled(true);
    });
  }

  private void wireRepeatToggle() {
    repeatCheckBox.addActionListener(e -> {
      boolean on = repeatCheckBox.isSelected();
      setRepeatRowsVisible(on);
      if (on) {
        ensureRepeatDaySelected();
      }
      pack();
    });
  }

  private void ensureRepeatDaySelected() {
    boolean anySelected = false;
    for (JPanel panel : repeatDayPanels) {
      if (Boolean.TRUE.equals(panel.getClientProperty("selected"))) {
        anySelected = true;
      }
    }
    if (!anySelected) {
      DayOfWeek dow = selectedDate.getDayOfWeek();
      char code;
      switch (dow) {
        case MONDAY:
          code = 'M';
          break;
        case TUESDAY:
          code = 'T';
          break;
        case WEDNESDAY:
          code = 'W';
          break;
        case THURSDAY:
          code = 'R';
          break;
        case FRIDAY:
          code = 'F';
          break;
        case SATURDAY:
          code = 'S';
          break;
        case SUNDAY:
        default:
          code = 'U';
          break;
      }

      for (int i = 0; i < repeatDayCodes.length; i++) {
        if (repeatDayCodes[i].charAt(0) == code) {
          JPanel dp = repeatDayPanels[i];
          dp.setBackground(new Color(230, 230, 230));
          java.awt.Component comp = dp.getComponent(0);
          if (comp instanceof JLabel) {
            comp.setForeground(Color.BLACK);
          }
          dp.putClientProperty("selected", Boolean.TRUE);
          break;
        }
      }
    }
  }

  private void toggleRepeatDaySelection(JPanel dayPanel, JLabel dayLabel) {
    boolean selected = Boolean.TRUE.equals(dayPanel.getClientProperty("selected"));
    if (!selected) {
      dayPanel.setBackground(new Color(245, 245, 245));
      dayLabel.setForeground(Color.BLACK);
      dayPanel.putClientProperty("selected", Boolean.TRUE);
      return;
    }
    int selectedCount = 0;
    for (JPanel p : repeatDayPanels) {
      if (Boolean.TRUE.equals(p.getClientProperty("selected"))) {
        selectedCount++;
      }
    }
    if (selectedCount <= 1) {
      return;
    }
    dayPanel.setBackground(Color.WHITE);
    dayLabel.setForeground(Color.BLACK);
    dayPanel.putClientProperty("selected", Boolean.FALSE);
  }

  private void refreshRepeatUntilDays() {
    Integer y = selectedYear(repeatUntilYearBox);
    Month m = selectedMonth(repeatUntilMonthBox);
    if (y != null && m != null) {
      updateDay(repeatUntilDayBox, y, m);
    }
  }

  private void addLocation() {
    JLabel locationLabel = new JLabel("Location");
    locationBox =
        new JComboBox<>(new String[] {"", "Physical", "Online"});

    form.add(makeRow(locationLabel, locationBox));
  }

  private void addStatus() {
    final JLabel statusLabel = new JLabel("Status");
    statusBox =
        new JComboBox<>(new String[] {"", "Public", "Private"});

    form.add(makeRow(statusLabel, statusBox));
  }

  private JPanel addDescription() {
    final JLabel descriptionLabel = new JLabel("Description");
    descriptionArea = new JTextArea(3, 20);
    descriptionArea.setLineWrap(true);
    descriptionArea.setWrapStyleWord(true);
    JScrollPane descriptionScroll = new JScrollPane(descriptionArea);

    JPanel descRow = new JPanel(new BorderLayout());
    descRow.add(descriptionLabel, BorderLayout.WEST);
    descRow.add(descriptionScroll, BorderLayout.CENTER);
    return descRow;
  }

  private void confirmEvent() {
    String repeatPattern = buildRepeatPattern(repeatDayPanels, repeatDayCodes);

    Integer repeatCount = null;
    String repeatUntilDate = null;

    boolean patternNotEmpty = !repeatPattern.isEmpty();

    if (repeatCheckBox.isSelected() && patternNotEmpty) {
      if (repeatForRadio.isSelected()) {
        repeatCount = (Integer) repeatCountSpinner.getValue();
      } else if (repeatUntilRadio.isSelected()) {
        Integer ry = selectedYear(repeatUntilYearBox);
        Month rm = selectedMonth(repeatUntilMonthBox);
        Integer rd = selectedInteger(repeatUntilDayBox);
        if (ry != null && rm != null && rd != null) {
          repeatUntilDate = String.format(
              "%04d-%02d-%02d", ry, rm.getValue(), rd);
        }
      }
    }

    createEvent(repeatPattern, repeatCount, repeatUntilDate);
  }

  private void createEvent(String repeatPattern, Integer repeatCount, String repeatUntilDate) {
    if (controller.getActiveCalendar() == null) {
      JOptionPane.showMessageDialog(
          this,
          "There is no active calendar - select or create one to use",
          "Cannot add event",
          JOptionPane.WARNING_MESSAGE);
      return;
    }

    String subject = subjectField.getText().trim();
    if (subject.isEmpty()) {
      JOptionPane.showMessageDialog(this, "Subject is required.");
      return;
    }

    Month startMonth = selectedMonth(startMonthBox);
    Integer startDay = selectedInteger(startDayBox);
    Integer startYear = selectedYear(startYearBox);

    Month endMonth = selectedMonth(endMonthBox);
    Integer endDay = selectedInteger(endDayBox);
    Integer endYear = selectedYear(endYearBox);

    if (startMonth == null || startDay == null || startYear == null
        || endMonth == null || endDay == null || endYear == null) {
      JOptionPane.showMessageDialog(this, "Start and end dates are required.");
      return;
    }

    String startText;
    String endText;

    if (allDayCheckBox.isSelected()) {
      startText = buildDateTime(startYear, startMonth, startDay, false);
      endText = buildDateTime(endYear, endMonth, endDay, true);
    } else {
      String startHourStr = selectedString(startHourBox);
      String startMinuteStr = selectedString(startMinuteBox);
      String startAmPm = selectedString(startAmPmBox);
      String endHourStr = selectedString(endHourBox);
      String endMinuteStr = selectedString(endMinuteBox);
      String endAmPm = selectedString(endAmPmBox);

      if (startHourStr == null || startMinuteStr == null || startAmPm == null
          || endHourStr == null || endMinuteStr == null || endAmPm == null) {
        JOptionPane.showMessageDialog(this, "Start and end times are required.");
        return;
      }

      int startHour12 = Integer.parseInt(startHourStr);
      int startMinute = Integer.parseInt(startMinuteStr);

      int endHour12 = Integer.parseInt(endHourStr);
      int endMinute = Integer.parseInt(endMinuteStr);

      int startHour24 = to24Hour(startHour12, startAmPm);
      int endHour24 = to24Hour(endHour12, endAmPm);

      startText = buildDateTime(startYear, startMonth, startDay, startHour24, startMinute);
      endText = buildDateTime(endYear, endMonth, endDay, endHour24, endMinute);
    }

    if (startText.isEmpty() || endText.isEmpty()) {
      JOptionPane.showMessageDialog(this, "Start and end are required.");
      return;
    }

    Object locationSelection = locationBox.getSelectedItem();
    String location;
    if (locationSelection == null) {
      location = "";
    } else {
      location = locationSelection.toString().trim();
    }

    String description = descriptionArea.getText().trim();

    Object statusSelection = statusBox.getSelectedItem();
    String status;
    if (statusSelection == null) {
      status = "";
    } else {
      status = statusSelection.toString().trim();
    }

    boolean hasRepeatPattern =
        repeatPattern != null && !repeatPattern.isEmpty();
    boolean hasRepeatCount =
        repeatCount != null && repeatCount > 0;
    boolean hasRepeatUntilDate =
        repeatUntilDate != null && !repeatUntilDate.isEmpty();
    boolean wantsRepeat = repeatCheckBox.isSelected()
        && hasRepeatPattern
        && (hasRepeatCount || hasRepeatUntilDate);

    if (allDayCheckBox.isSelected()) {
      createAllDayEvent(subject, startText, endText, startYear, startMonth, startDay, wantsRepeat,
          repeatPattern,
          repeatCount, repeatUntilDate, description, location, status);
    } else {
      createTimedEvent(subject, startText, endText, wantsRepeat, repeatPattern, repeatCount,
          repeatUntilDate, description, location, status);
    }

    if (eventsListPanel != null) {
      eventsListPanel.refreshEvents();
    }
    dispose();
  }

  private void createAllDayEvent(String subject, String startText, String endText, int startYear,
                                 Month startMonth, int startDay,
                                 boolean wantsRepeat, String repeatPattern, Integer repeatCount,
                                 String repeatUntilDate, String description, String location,
                                 String status) {

    String dateOnly = String.format(
        "%04d-%02d-%02d", startYear, startMonth.getValue(), startDay);

    String createCommand;

    boolean hasRepeatPattern =
        repeatPattern != null && !repeatPattern.isEmpty();
    boolean hasRepeatCount =
        repeatCount != null && repeatCount > 0;
    boolean hasRepeatUntilDate =
        repeatUntilDate != null && !repeatUntilDate.isEmpty();

    if (!wantsRepeat) {
      createCommand = "create event " + subject + " on " + dateOnly;
    } else if (hasRepeatCount) {
      createCommand = "create event " + subject
          + " on " + dateOnly
          + " repeats " + repeatPattern
          + " for " + repeatCount + " times";
    } else {
      createCommand = "create event " + subject
          + " on " + dateOnly
          + " repeats " + repeatPattern
          + " until " + repeatUntilDate;
    }

    controller.interpret(createCommand);

    if (!description.isEmpty()) {
      String descCommand;
      if (wantsRepeat) {
        descCommand =
            "edit series description " + subject
                + " from " + startText
                + " with " + description;
      } else {
        descCommand = "edit event description " + subject
            + " from " + startText
            + " to " + endText
            + " with " + description;
      }
      controller.interpret(descCommand);
    }

    if (!location.isEmpty()) {
      String locCommand;
      if (wantsRepeat) {
        locCommand = "edit series location " + subject
            + " from " + startText
            + " with " + location;
      } else {
        locCommand = "edit event location " + subject
            + " from " + startText
            + " to " + endText
            + " with " + location;
      }
      controller.interpret(locCommand);
    }

    if (!status.isEmpty()) {
      String statusCommand;
      if (wantsRepeat) {
        statusCommand = "edit series status " + subject
            + " from " + startText
            + " with " + status;
      } else {
        statusCommand = "edit event status " + subject
            + " from " + startText
            + " to " + endText
            + " with " + status;
      }
      controller.interpret(statusCommand);
    }
  }

  private void createTimedEvent(String subject, String startText, String endText,
                                boolean wantsRepeat, String repeatPattern, Integer repeatCount,
                                String repeatUntilDate, String description, String location,
                                String status) {

    boolean hasRepeatPattern =
        repeatPattern != null && !repeatPattern.isEmpty();
    boolean hasRepeatCount =
        repeatCount != null && repeatCount > 0;
    boolean hasRepeatUntilDate =
        repeatUntilDate != null && !repeatUntilDate.isEmpty();

    boolean isSeries = wantsRepeat;

    String createCommand;
    if (isSeries && hasRepeatPattern && hasRepeatCount) {
      createCommand = "create event " + subject
          + " from " + startText
          + " to " + endText
          + " repeats " + repeatPattern
          + " for " + repeatCount + " times";
    } else if (isSeries && hasRepeatPattern && hasRepeatUntilDate) {
      createCommand = "create event " + subject
          + " from " + startText
          + " to " + endText
          + " repeats " + repeatPattern
          + " until " + repeatUntilDate;
    } else {
      createCommand = "create event " + subject
          + " from " + startText
          + " to " + endText;
      isSeries = false;
    }

    controller.interpret(createCommand);

    if (!description.isEmpty()) {
      String descCommandTimed;
      if (isSeries) {
        descCommandTimed = "edit series description " + subject
            + " from " + startText
            + " with " + description;
      } else {
        descCommandTimed = "edit event description " + subject
            + " from " + startText
            + " to " + endText
            + " with " + description;
      }
      controller.interpret(descCommandTimed);
    }

    if (isSeries) {
      if (!description.isEmpty()) {
        controller.interpret(
            "edit series description " + subject
                + " from " + startText
                + " with " + description
        );
      }

      if (!location.isEmpty()) {
        controller.interpret(
            "edit series location " + subject
                + " from " + startText
                + " with " + location
        );
      }

      if (!status.isEmpty()) {
        controller.interpret(
            "edit series status " + subject
                + " from " + startText
                + " with " + status
        );
      }
    } else {
      if (!description.isEmpty()) {
        controller.interpret(
            "edit event description " + subject
                + " from " + startText
                + " to " + endText
                + " with " + description);
      }

      if (!location.isEmpty()) {
        controller.interpret(
            "edit event location " + subject
                + " from " + startText
                + " to " + endText
                + " with " + location);
      }

      if (!status.isEmpty()) {
        controller.interpret(
            "edit event status " + subject
                + " from " + startText
                + " to " + endText
                + " with " + status);
      }
    }
  }


  private String buildDateTime(int year, Month month, int day, boolean isEnd) {
    int hour;
    if (isEnd) {
      hour = 17;
    } else {
      hour = 8;
    }
    int minute = 0;
    return buildDateTime(year, month, day, hour, minute);
  }


  private String buildRepeatPattern(JPanel[] dayPanels, String[] dayCodes) {
    StringBuilder sb = new StringBuilder();
    int i = 0;
    while (i < dayCodes.length) {
      boolean selected =
          Boolean.TRUE.equals(dayPanels[i].getClientProperty("selected"));
      if (selected) {
        sb.append(dayCodes[i]);
      }
      i = i + 1;
    }
    return sb.toString();
  }

  private void setTimeRowsVisible(boolean show) {
    timeStartRow.setVisible(show);
    timeEndRow.setVisible(show);
    form.revalidate();
    form.repaint();
  }

  private void setRepeatRowsVisible(boolean show) {
    repeatDaysRow.setVisible(show);
    repeatRangeRow.setVisible(show);
    form.revalidate();
    form.repaint();
  }

  private Integer selectedYear(JComboBox<Integer> box) {
    Object val = box.getSelectedItem();
    return (val instanceof Integer) ? (Integer) val : null;
  }

  private Month selectedMonth(JComboBox<Month> box) {
    Object val = box.getSelectedItem();
    return (val instanceof Month) ? (Month) val : null;
  }

  private Integer selectedInteger(JComboBox<Integer> box) {
    Object val = box.getSelectedItem();
    return (val instanceof Integer) ? (Integer) val : null;
  }

  private String selectedString(JComboBox<String> box) {
    Object val = box.getSelectedItem();
    return (val instanceof String) ? (String) val : null;
  }
}
