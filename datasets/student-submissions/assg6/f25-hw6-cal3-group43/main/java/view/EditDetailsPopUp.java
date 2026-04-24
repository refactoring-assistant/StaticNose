package view;

import controller.CalendarController;
import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.GridLayout;
import java.time.Duration;
import java.time.LocalDateTime;
import java.time.Month;
import java.time.Year;
import java.time.format.DateTimeFormatter;
import java.util.Objects;
import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import model.Event;

/**
 * A pop up that allows the user to change the details of an already created event.
 * If they user presses save, it saves the changes. If they press cancel, it closes
 * the window and does not save the changes.
 */
public class EditDetailsPopUp extends AbstractCommandEvents {

  private final CalendarController controller;
  private final Event event;
  private final EventsListPanel eventsListPanel;

  private final String anchorSubject;
  private final String anchorStart;
  private final String anchorEnd;

  private JTextField subjectField;
  private JTextArea descriptionField;

  private JCheckBox allDayCheckBox;

  private static final String CLEAR_VALUE = "none";

  /**
   * Constructs an EditDetailsPopUp that allows the user to change the details
   * of a selected event on the calendar.
   *
   * @param controller the controller
   * @param event      the event that is being edited
   * @param eventsListPanel the list panel to refresh after edit (may be null)
   */
  public EditDetailsPopUp(CalendarController controller, Event event,
                          EventsListPanel eventsListPanel) {
    //super("Edit Event Details");
    this.controller = controller;
    this.event = event;
    this.eventsListPanel = eventsListPanel;

    DateTimeFormatter anchorFmt = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm");
    this.anchorSubject = safe(event.getSubject());
    this.anchorStart = event.getStartTime().format(anchorFmt);
    this.anchorEnd = event.getEndTime().format(anchorFmt);

    buildUi();
    pack();
    setLocationRelativeTo(null);
  }

  private void buildUi() {
    JPanel content = new JPanel(new BorderLayout(10, 10));
    content.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

    JLabel titleLabel = new JLabel("Edit event");
    titleLabel.setFont(titleLabel.getFont().deriveFont(Font.BOLD, 16f));
    content.add(titleLabel, BorderLayout.NORTH);

    JPanel form = new JPanel(new GridLayout(0, 2, 6, 6));

    subjectField = new JTextField(anchorSubject, 20);
    form.add(new JLabel("Subject:"));
    form.add(subjectField);

    buildDatePickers();

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

    form.add(new JLabel("Start date:"));
    form.add(startDatePanel);

    form.add(new JLabel("End date:"));
    form.add(endDatePanel);

    allDayCheckBox = new JCheckBox("All day");
    allDayCheckBox.setSelected(event.getIsAllDay());
    form.add(new JLabel("All day:"));
    form.add(allDayCheckBox);

    buildTimePickers();

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

    form.add(new JLabel("Start time:"));
    form.add(startTimePanel);

    form.add(new JLabel("End time:"));
    form.add(endTimePanel);

    installAllDayControls();

    locationBox = new JComboBox<>(new String[] {"", "Physical", "Online"});
    locationBox.setSelectedItem(matchOption(event.getLocation(), locationBox));
    form.add(new JLabel("Location:"));
    form.add(locationBox);

    statusBox = new JComboBox<>(new String[] {"", "Public", "Private"});
    statusBox.setSelectedItem(matchOption(event.getStatus(), statusBox));
    form.add(new JLabel("Status:"));
    form.add(statusBox);

    descriptionField = new JTextArea(safe(event.getDescription()), 4, 30);
    descriptionField.setLineWrap(true);
    descriptionField.setWrapStyleWord(true);
    JScrollPane descriptionScroll = new JScrollPane(descriptionField);
    descriptionScroll.setPreferredSize(new Dimension(350, 80));

    JPanel descRow = new JPanel(new BorderLayout(5, 0));
    descRow.add(new JLabel("Description:"), BorderLayout.NORTH);
    descRow.add(descriptionScroll, BorderLayout.CENTER);

    JButton saveButton = new JButton("Save");
    JButton cancelButton = new JButton("Cancel");
    saveButton.addActionListener(e -> saveEdit());
    cancelButton.addActionListener(e -> dispose());

    JPanel buttonPanel = new JPanel();
    buttonPanel.add(saveButton);
    buttonPanel.add(cancelButton);

    JPanel centerPanel = new JPanel(new BorderLayout(8, 8));
    centerPanel.add(form, BorderLayout.NORTH);
    centerPanel.add(descRow, BorderLayout.CENTER);

    content.add(centerPanel, BorderLayout.CENTER);
    content.add(buttonPanel, BorderLayout.SOUTH);

    setContentPane(content);
  }

  private void buildDatePickers() {
    startMonthBox = new JComboBox<>(Month.values());
    endMonthBox = new JComboBox<>(Month.values());

    LocalDateTime start = event.getStartTime();
    LocalDateTime end = event.getEndTime();
    startMonthBox.setSelectedItem(start.getMonth());
    endMonthBox.setSelectedItem(end.getMonth());

    int currentYear = Year.now().getValue();
    startYearBox = new JComboBox<>();
    endYearBox = new JComboBox<>();
    for (int y = currentYear - 1; y <= currentYear + 3; y++) {
      startYearBox.addItem(y);
      endYearBox.addItem(y);
    }
    startYearBox.setSelectedItem(start.getYear());
    endYearBox.setSelectedItem(end.getYear());

    startDayBox = new JComboBox<>();
    endDayBox = new JComboBox<>();
    updateDay(startDayBox, start.getYear(), start.getMonth());
    updateDay(endDayBox, end.getYear(), end.getMonth());
    startDayBox.setSelectedItem(start.getDayOfMonth());
    endDayBox.setSelectedItem(end.getDayOfMonth());

    startMonthBox.addActionListener(e -> {
      refreshDays(startYearBox, startMonthBox, startDayBox);
      syncEndDateToStartIfAllDay();
    });
    startYearBox.addActionListener(e -> {
      refreshDays(startYearBox, startMonthBox, startDayBox);
      syncEndDateToStartIfAllDay();
    });
    startDayBox.addActionListener(e -> syncEndDateToStartIfAllDay());
    endMonthBox.addActionListener(e -> refreshDays(endYearBox, endMonthBox, endDayBox));
    endYearBox.addActionListener(e -> refreshDays(endYearBox, endMonthBox, endDayBox));
  }

  private void buildTimePickers() {
    final LocalDateTime start = event.getStartTime();
    final LocalDateTime end = event.getEndTime();

    String[] hourOptions = {
        "01", "02", "03", "04", "05", "06",
        "07", "08", "09", "10", "11", "12"
    };
    String[] minuteOptions = {"00", "15", "30", "45"};
    String[] amPmOptions = {"AM", "PM"};

    startHourBox = new JComboBox<>(hourOptions);
    startMinuteBox = new JComboBox<>(minuteOptions);
    startAmPmBox = new JComboBox<>(amPmOptions);

    endHourBox = new JComboBox<>(hourOptions);
    endMinuteBox = new JComboBox<>(minuteOptions);
    endAmPmBox = new JComboBox<>(amPmOptions);

    setTimeFromLocalDateTime(start, startHourBox, startMinuteBox, startAmPmBox);
    setTimeFromLocalDateTime(end, endHourBox, endMinuteBox, endAmPmBox);
  }

  private void installAllDayControls() {
    allDayCheckBox.addActionListener(e -> toggleAllDay());
    toggleAllDay();
  }

  private void toggleAllDay() {
    boolean allDay = allDayCheckBox.isSelected();
    if (allDay) {
      syncEndDateToStartIfAllDay();
    }
    setEndDateEnabled(!allDay);
    setTimePickersEnabled(!allDay);
  }

  private void syncEndDateToStartIfAllDay() {
    if (allDayCheckBox == null || !allDayCheckBox.isSelected()) {
      return;
    }
    endYearBox.setSelectedItem(startYearBox.getSelectedItem());
    endMonthBox.setSelectedItem(startMonthBox.getSelectedItem());
    endDayBox.setSelectedItem(startDayBox.getSelectedItem());
  }

  private void setEndDateEnabled(boolean enabled) {
    endYearBox.setEnabled(enabled);
    endMonthBox.setEnabled(enabled);
    endDayBox.setEnabled(enabled);
  }

  private void setTimePickersEnabled(boolean enabled) {
    startHourBox.setEnabled(enabled);
    startMinuteBox.setEnabled(enabled);
    startAmPmBox.setEnabled(enabled);
    endHourBox.setEnabled(enabled);
    endMinuteBox.setEnabled(enabled);
    endAmPmBox.setEnabled(enabled);
  }

  private void setTimeFromLocalDateTime(
      LocalDateTime time,
      JComboBox<String> hourBox,
      JComboBox<String> minuteBox,
      JComboBox<String> amPmBox) {

    int hour24 = time.getHour();
    int minute = time.getMinute();

    String amPm;
    int hour12;
    if (hour24 == 0) {
      hour12 = 12;
      amPm = "AM";
    } else if (hour24 < 12) {
      hour12 = hour24;
      amPm = "AM";
    } else if (hour24 == 12) {
      hour12 = 12;
      amPm = "PM";
    } else {
      hour12 = hour24 - 12;
      amPm = "PM";
    }

    String hourStr = String.format("%02d", hour12);
    String minuteStr = String.format("%02d", minute);

    hourBox.setSelectedItem(hourStr);
    minuteBox.setSelectedItem(minuteStr);
    amPmBox.setSelectedItem(amPm);
  }

  private void saveEdit() {
    String newSubject = subjectField.getText().trim();

    DateSelection dates = readDateSelection();
    if (dates == null) {
      return;
    }
    TimeUpdate times = computeNewTimes(dates);
    applyEdits(newSubject, times);
  }

  private DateSelection readDateSelection() {
    Month startMonth = (Month) startMonthBox.getSelectedItem();
    Integer startDay = (Integer) startDayBox.getSelectedItem();
    Integer startYear = (Integer) startYearBox.getSelectedItem();

    Month endMonth = (Month) endMonthBox.getSelectedItem();
    Integer endDay = (Integer) endDayBox.getSelectedItem();
    Integer endYear = (Integer) endYearBox.getSelectedItem();

    if (startMonth == null || startDay == null || startYear == null
        || endMonth == null || endDay == null || endYear == null) {
      javax.swing.JOptionPane.showMessageDialog(this, "Start and end dates are required.");
      return null;
    }
    return new DateSelection(startMonth, startDay, startYear, endMonth, endDay, endYear);
  }

  private TimeUpdate computeNewTimes(DateSelection dates) {
    final boolean wantsAllDay = allDayCheckBox.isSelected();
    final boolean wasAllDay = event.getIsAllDay();
    final Duration currentDuration = Duration.between(event.getStartTime(), event.getEndTime());

    if (wantsAllDay) {
      String start = buildDateTime(dates.startYear, dates.startMonth, dates.startDay, 8, 0);
      String end = buildDateTime(dates.startYear, dates.startMonth, dates.startDay, 17, 0);
      LocalDateTime startDt = LocalDateTime.parse(start);
      LocalDateTime endDt = LocalDateTime.parse(end);
      return new TimeUpdate(start, end, startDt, endDt, wantsAllDay, wasAllDay);
    }

    int startHour12 = Integer.parseInt((String) Objects.requireNonNull(
        startHourBox.getSelectedItem()));
    int startMinute = Integer.parseInt((String) Objects.requireNonNull(
        startMinuteBox.getSelectedItem()));
    String startAmPm = (String) startAmPmBox.getSelectedItem();

    int endHour12 =
        Integer.parseInt((String) Objects.requireNonNull(endHourBox.getSelectedItem()));
    int endMinute = Integer.parseInt((String) Objects.requireNonNull(
        endMinuteBox.getSelectedItem()));
    String endAmPm = (String) endAmPmBox.getSelectedItem();

    int startHour24 = to24Hour(startHour12, startAmPm);
    int endHour24 = to24Hour(endHour12, endAmPm);

    String start = buildDateTime(dates.startYear, dates.startMonth, dates.startDay,
        startHour24, startMinute);
    String end = buildDateTime(dates.endYear, dates.endMonth, dates.endDay, endHour24, endMinute);

    LocalDateTime startDt = LocalDateTime.parse(start);
    LocalDateTime endDt = LocalDateTime.parse(end);
    if (!endDt.isAfter(startDt) && !currentDuration.isNegative() && !currentDuration.isZero()) {
      endDt = startDt.plus(currentDuration);
      end = endDt.toString();
    }
    return new TimeUpdate(start, end, startDt, endDt, wantsAllDay, wasAllDay);
  }

  private void applyEdits(String newSubject, TimeUpdate times) {
    final String newLocation = normalizeOptional((String) locationBox.getSelectedItem());
    final String newStatus = normalizeOptional((String) statusBox.getSelectedItem());
    final String newDescription = descriptionField.getText().trim();

    String currentSubject = anchorSubject;
    String currentStart = anchorStart;
    String currentEnd = anchorEnd;

    boolean startChanged = !times.newStart.isEmpty() && !times.newStart.equals(currentStart);
    boolean endChanged = !times.newEnd.isEmpty() && !times.newEnd.equals(currentEnd);

    if (!newSubject.isEmpty() && !newSubject.equals(currentSubject)) {
      controller.interpret(command("subject", currentSubject, currentStart,
          currentEnd, newSubject));
      currentSubject = newSubject;
    }

    if (startChanged && endChanged && times.newStartDateTime.isAfter(event.getEndTime())) {
      currentEnd = applyEndChange(currentSubject, currentStart, currentEnd, times.newEnd);
      currentStart = applyStartChange(currentSubject, currentStart, currentEnd, times.newStart);
    } else {
      if (startChanged) {
        currentStart = applyStartChange(currentSubject, currentStart, currentEnd, times.newStart);
      }
      if (endChanged) {
        currentEnd = applyEndChange(currentSubject, currentStart, currentEnd, times.newEnd);
      }
    }

    if (times.wantsAllDay != times.wasAllDay) {
      controller.interpret(command("isAllDay", currentSubject, currentStart, currentEnd,
          String.valueOf(times.wantsAllDay)));
    }

    if (!newDescription.isEmpty() && !newDescription.equals(safe(event.getDescription()))) {
      controller.interpret(command("description", currentSubject, currentStart, currentEnd,
          newDescription));
    }

    String currentLocation = normalizeOptional(event.getLocation());
    if (!newLocation.equalsIgnoreCase(currentLocation)) {
      String locValue = newLocation.isEmpty() ? CLEAR_VALUE : newLocation;
      controller.interpret(command("location", currentSubject, currentStart, currentEnd, locValue));
    }

    String currentStatus = normalizeOptional(event.getStatus());
    if (!newStatus.equalsIgnoreCase(currentStatus)) {
      String statusValue = newStatus.isEmpty() ? CLEAR_VALUE : newStatus;
      controller.interpret(command("status", currentSubject, currentStart,
          currentEnd, statusValue));
    }

    if (eventsListPanel != null) {
      eventsListPanel.refreshEvents();
    }
    dispose();
  }

  private String command(String property, String subject, String start, String end, String value) {
    return "edit event " + property + " " + subject
        + " from " + start
        + " to " + end
        + " with " + value;
  }

  private String applyStartChange(String subject, String currentStart, String currentEnd,
                                  String newStart) {
    controller.interpret(command("start", subject, currentStart, currentEnd, newStart));
    return newStart;
  }

  private String applyEndChange(String subject, String currentStart, String currentEnd,
                                String newEnd) {
    controller.interpret(command("end", subject, currentStart, currentEnd, newEnd));
    return newEnd;
  }

  private static final class DateSelection {
    final Month startMonth;
    final int startDay;
    final int startYear;
    final Month endMonth;
    final int endDay;
    final int endYear;

    DateSelection(Month startMonth, int startDay, int startYear,
                  Month endMonth, int endDay, int endYear) {
      this.startMonth = startMonth;
      this.startDay = startDay;
      this.startYear = startYear;
      this.endMonth = endMonth;
      this.endDay = endDay;
      this.endYear = endYear;
    }
  }

  private static final class TimeUpdate {
    final String newStart;
    final String newEnd;
    final LocalDateTime newStartDateTime;
    final LocalDateTime newEndDateTime;
    final boolean wantsAllDay;
    final boolean wasAllDay;

    TimeUpdate(String newStart, String newEnd,
               LocalDateTime newStartDateTime, LocalDateTime newEndDateTime,
               boolean wantsAllDay, boolean wasAllDay) {
      this.newStart = newStart;
      this.newEnd = newEnd;
      this.newStartDateTime = newStartDateTime;
      this.newEndDateTime = newEndDateTime;
      this.wantsAllDay = wantsAllDay;
      this.wasAllDay = wasAllDay;
    }
  }

  private String matchOption(String modelValue, JComboBox<String> box) {
    if (modelValue == null) {
      return "";
    }
    for (int i = 0; i < box.getItemCount(); i++) {
      String option = box.getItemAt(i);
      if (option != null && option.equalsIgnoreCase(modelValue)) {
        return option;
      }
    }
    return "";
  }

  private String normalizeOptional(String value) {
    if (value == null) {
      return "";
    }
    return value.trim().toLowerCase();
  }

  private void refreshDays(JComboBox<Integer> yearBox, JComboBox<Month> monthBox,
                           JComboBox<Integer> dayBox) {
    Integer y = (Integer) yearBox.getSelectedItem();
    Month m = (Month) monthBox.getSelectedItem();
    if (y != null && m != null) {
      updateDay(dayBox, y, m);
    }
  }


}
