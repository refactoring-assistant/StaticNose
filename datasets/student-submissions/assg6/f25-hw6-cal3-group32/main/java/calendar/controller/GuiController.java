package calendar.controller;

import calendar.model.Calendar;
import calendar.model.Event;
import calendar.service.CalendarManager;
import calendar.service.CalendarOperations;
import calendar.service.EventCopyService;
import calendar.service.export.CsvExporter;
import calendar.service.export.IcalExporter;
import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.Comparator;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * Controller for Gui mode.
 * Handles Gui events and coordinates between view and model.
 */
public class GuiController {
  private final CalendarManager manager;
  private final CalendarOperations operations;
  private final EventCopyService copyService;
  private final CsvExporter csvExporter;
  private final IcalExporter icalExporter;
  private Calendar currentCalendar;
  private LocalDate currentMonth;
  private calendar.view.gui.CalendarGui gui;

  /**This is a lorem ipsum javadoc with good one to the sunrise.*/
  public GuiController() {
    this.manager = new CalendarManager();
    this.operations = new CalendarOperations();
    this.copyService = new EventCopyService();
    this.csvExporter = new CsvExporter();
    this.icalExporter = new IcalExporter();
    this.currentMonth = LocalDate.now();

    ZoneId systemTz = ZoneId.systemDefault();
    manager.createCalendar("My Calendar", systemTz);
    currentCalendar = manager.getCalendar("My Calendar");
  }

  /**This is a lorem ipsum javadoc with good one to the sunrise.*/
  public void show() {
    this.gui = new calendar.view.gui.CalendarGui(this);
    gui.setVisible(true);
  }

  /**This is a lorem ipsum javadoc with good one to the sunrise.*/
  public void onDayClicked(LocalDate date) {
    if (gui == null) {
      return;
    }
    calendar.view.gui.EventDialog dialog =
        new calendar.view.gui.EventDialog(gui, this, date);
    dialog.setVisible(true);

    if (dialog.wasConfirmed()) {
      try {
        if (dialog.isRecurring()) {
          operations.createRecurringSeries(
              currentCalendar,
              dialog.getSubject(),
              dialog.getStartTime(),
              dialog.getEndTime(),
              dialog.getWeekdays(),
              dialog.getOccurrences(),
              dialog.getUntilDate()
          );
        } else {
          operations.createEvent(
              currentCalendar,
              dialog.getSubject(),
              dialog.getStartTime(),
              dialog.getEndTime(),
              dialog.getEventDescription(),
              dialog.getEventLocation(),
              true
          );
        }
        gui.refreshMonth();
        showSuccess("Event created successfully");
      } catch (IllegalArgumentException ex) {
        showError(convertErrorMessage(ex.getMessage()));
      }
    }
  }

  /**This is a lorem ipsum javadoc with good one to the sunrise.*/
  public void onEventClicked(Event event) {
    if (gui == null) {
      return;
    }
    calendar.view.gui.EventDialog dialog =
        new calendar.view.gui.EventDialog(gui, this, event);
    dialog.setVisible(true);

    if (dialog.wasConfirmed()) {
      try {
        handleEventEdit(dialog);
        gui.refreshMonth();
        showSuccess("Event updated successfully");
      } catch (IllegalArgumentException ex) {
        showError(convertErrorMessage(ex.getMessage()));
      }
    }
  }

  /**This is a lorem ipsum javadoc with good one to the sunrise.*/
  public void onEventDeleted(Event event) {
    if (gui == null) {
      return;
    }

    if (event.isPartOfSeries()) {
      String[] options = {"This event only", "This and future events",
          "All events in series"};
      int choice = javax.swing.JOptionPane.showOptionDialog(
          gui,
          "This is a recurring event. What would you like to delete?",
          "Delete Recurring Event",
          javax.swing.JOptionPane.DEFAULT_OPTION,
          javax.swing.JOptionPane.WARNING_MESSAGE,
          null,
          options,
          options[0]
      );

      if (choice < 0) {
        return;
      }

      try {
        if (choice == 0) {
          deleteSingleEvent(event);
        } else if (choice == 1) {
          deleteEventsForward(event);
        } else {
          deleteEntireSeries(event);
        }
        gui.refreshMonth();
        showSuccess("Event deleted successfully");
      } catch (IllegalArgumentException ex) {
        showError(ex.getMessage());
      }
    } else {
      int confirm = javax.swing.JOptionPane.showConfirmDialog(
          gui,
          "Delete event \"" + event.getSubject() + "\"?",
          "Confirm Delete",
          javax.swing.JOptionPane.YES_NO_OPTION,
          javax.swing.JOptionPane.WARNING_MESSAGE
      );

      if (confirm == javax.swing.JOptionPane.YES_OPTION) {
        try {
          currentCalendar.removeEvent(event);
          gui.refreshMonth();
          showSuccess("Event deleted successfully");
        } catch (IllegalArgumentException ex) {
          showError(ex.getMessage());
        }
      }
    }
  }

  private void handleEventEdit(calendar.view.gui.EventDialog dialog) {
    Event e = dialog.getEditingEvent();

    if (e.isPartOfSeries()) {
      int choice = dialog.getEditChoice();
      if (choice == 0) {
        editSingleEventInSeries(dialog, e);
      } else if (choice == 1) {
        editSeriesForward(dialog, e);
      } else {
        editEntireSeries(dialog, e);
      }
    } else {
      editSingleEvent(dialog, e);
    }
  }

  private void editSingleEvent(calendar.view.gui.EventDialog dialog, Event e) {
    if (dialog.isRecurring()) {
      convertToRecurring(dialog, e);
    } else {
      operations.editEventMultipleProperties(
          currentCalendar, e,
          dialog.getSubject(),
          dialog.getStartTime(),
          dialog.getEndTime(),
          dialog.getEventDescription(),
          dialog.getEventLocation()
      );
    }
  }

  private void editSingleEventInSeries(calendar.view.gui.EventDialog dialog, Event e) {
    e.setSeriesId(null);

    operations.editEventMultipleProperties(
        currentCalendar, e,
        dialog.getSubject(),
        dialog.getStartTime(),
        dialog.getEndTime(),
        dialog.getEventDescription(),
        dialog.getEventLocation()
    );
  }

  private void convertToRecurring(calendar.view.gui.EventDialog dialog, Event e) {
    currentCalendar.removeEvent(e);

    operations.createRecurringSeries(
        currentCalendar,
        dialog.getSubject(),
        dialog.getStartTime(),
        dialog.getEndTime(),
        dialog.getWeekdays(),
        dialog.getOccurrences(),
        dialog.getUntilDate()
    );
  }

  private void editSeriesForward(calendar.view.gui.EventDialog dialog, Event e) {
    if (!dialog.isRecurring()) {
      convertForwardToNormal(dialog, e);
    } else if (hasComplexChanges(dialog, e) || hasRecurrenceChanges(dialog, e)) {
      recreateSeriesForward(dialog, e);
    } else {
      editForwardSimple(dialog, e);
    }
  }

  private void editEntireSeries(calendar.view.gui.EventDialog dialog, Event e) {
    if (!dialog.isRecurring()) {
      convertSeriesToNormal(dialog, e);
    } else if (hasComplexChanges(dialog, e) || hasRecurrenceChanges(dialog, e)) {
      recreateEntireSeries(dialog, e);
    } else {
      editSeriesSimple(dialog, e);
    }
  }

  private void convertForwardToNormal(calendar.view.gui.EventDialog dialog, Event e) {
    List<Event> toDelete = getEventsInSeries(e.getSeriesId()).stream()
        .filter(evt -> !evt.getStart().isBefore(e.getStart()))
        .collect(Collectors.toList());

    for (Event evt : toDelete) {
      currentCalendar.removeEvent(evt);
    }

    operations.createEvent(
        currentCalendar,
        dialog.getSubject(),
        dialog.getStartTime(),
        dialog.getEndTime(),
        dialog.getEventDescription(),
        dialog.getEventLocation(),
        true
    );
  }

  private void convertSeriesToNormal(calendar.view.gui.EventDialog dialog, Event e) {
    List<Event> toDelete = getEventsInSeries(e.getSeriesId());

    for (Event evt : toDelete) {
      currentCalendar.removeEvent(evt);
    }

    operations.createEvent(
        currentCalendar,
        dialog.getSubject(),
        dialog.getStartTime(),
        dialog.getEndTime(),
        dialog.getEventDescription(),
        dialog.getEventLocation(),
        true
    );
  }

  private boolean hasComplexChanges(calendar.view.gui.EventDialog dialog, Event e) {
    boolean timeChanged = !dialog.getStartTime().toLocalTime()
        .equals(e.getStart().toLocalTime())
        || !dialog.getEndTime().toLocalTime()
        .equals(e.getEnd().toLocalTime());
    boolean subjectChanged = !dialog.getSubject().equals(e.getSubject());
    return timeChanged || subjectChanged;
  }

  private boolean hasRecurrenceChanges(calendar.view.gui.EventDialog dialog, Event e) {
    List<Event> series = getEventsInSeries(e.getSeriesId());
    if (series.isEmpty()) {
      return false;
    }

    String oldWeekdays = inferWeekdays(series);
    String newWeekdays = dialog.getWeekdays();
    boolean weekdaysChanged = !oldWeekdays.equals(newWeekdays);

    Integer newOcc = dialog.getOccurrences();
    boolean countChanged = newOcc != null && newOcc != series.size();

    LocalDate newUntil = dialog.getUntilDate();
    boolean untilChanged = newUntil != null;

    return weekdaysChanged || countChanged || untilChanged;
  }

  private void editForwardSimple(calendar.view.gui.EventDialog dialog, Event e) {
    List<Event> toEdit = getEventsInSeries(e.getSeriesId()).stream()
        .filter(evt -> !evt.getStart().isBefore(e.getStart()))
        .collect(Collectors.toList());

    for (Event evt : toEdit) {
      currentCalendar.editEvent(evt, "description", dialog.getEventDescription());
      currentCalendar.editEvent(evt, "location", dialog.getEventLocation());
    }
  }

  private void editSeriesSimple(calendar.view.gui.EventDialog dialog, Event e) {
    List<Event> toEdit = getEventsInSeries(e.getSeriesId());

    for (Event evt : toEdit) {
      currentCalendar.editEvent(evt, "description", dialog.getEventDescription());
      currentCalendar.editEvent(evt, "location", dialog.getEventLocation());
    }
  }

  private void recreateSeriesForward(calendar.view.gui.EventDialog dialog, Event e) {
    List<Event> toDelete = getEventsInSeries(e.getSeriesId()).stream()
        .filter(evt -> !evt.getStart().isBefore(e.getStart()))
        .sorted(Comparator.comparing(Event::getStart))
        .collect(Collectors.toList());

    if (toDelete.isEmpty()) {
      return;
    }

    int actualCount = toDelete.size();
    String desc = dialog.getEventDescription();
    String loc = dialog.getEventLocation();
    LocalDate firstDate = e.getStart().toLocalDate();

    for (Event evt : toDelete) {
      currentCalendar.removeEvent(evt);
    }

    operations.createRecurringSeries(
        currentCalendar,
        dialog.getSubject(),
        dialog.getStartTime().with(firstDate),
        dialog.getEndTime().with(firstDate),
        dialog.getWeekdays(),
        actualCount,
        null
    );

    if (desc != null && !desc.isEmpty() || loc != null && !loc.isEmpty()) {
      applyDescLocationToNewSeries(dialog.getSubject(),
          dialog.getStartTime().with(firstDate), desc, loc);
    }
  }

  private void recreateEntireSeries(calendar.view.gui.EventDialog dialog, Event e) {
    List<Event> toDelete = getEventsInSeries(e.getSeriesId()).stream()
        .sorted(Comparator.comparing(Event::getStart))
        .collect(Collectors.toList());

    if (toDelete.isEmpty()) {
      return;
    }

    String desc = dialog.getEventDescription();
    String loc = dialog.getEventLocation();
    LocalDate firstDate = toDelete.get(0).getStart().toLocalDate();

    for (Event evt : toDelete) {
      currentCalendar.removeEvent(evt);
    }

    operations.createRecurringSeries(
        currentCalendar,
        dialog.getSubject(),
        dialog.getStartTime().with(firstDate),
        dialog.getEndTime().with(firstDate),
        dialog.getWeekdays(),
        dialog.getOccurrences(),
        dialog.getUntilDate()
    );

    if (desc != null && !desc.isEmpty() || loc != null && !loc.isEmpty()) {
      applyDescLocationToNewSeries(dialog.getSubject(),
          dialog.getStartTime().with(firstDate), desc, loc);
    }
  }

  private void applyDescLocationToNewSeries(String subj, ZonedDateTime start,
                                            String desc, String loc) {
    try {
      Event firstEvent = currentCalendar.findEvent(subj, start);
      if (firstEvent.getSeriesId() != null) {
        List<Event> series = getEventsInSeries(firstEvent.getSeriesId());
        for (Event evt : series) {
          if (desc != null && !desc.isEmpty()) {
            currentCalendar.editEvent(evt, "description", desc);
          }
          if (loc != null && !loc.isEmpty()) {
            currentCalendar.editEvent(evt, "location", loc);
          }
        }
      }
    } catch (IllegalArgumentException ex) {
      // Event not found, skip
    }
  }

  private void deleteSingleEvent(Event e) {
    currentCalendar.removeEvent(e);
  }

  private void deleteEventsForward(Event e) {
    List<Event> toDelete = getEventsInSeries(e.getSeriesId()).stream()
        .filter(evt -> !evt.getStart().isBefore(e.getStart()))
        .collect(Collectors.toList());

    for (Event evt : toDelete) {
      currentCalendar.removeEvent(evt);
    }
  }

  private void deleteEntireSeries(Event e) {
    List<Event> toDelete = getEventsInSeries(e.getSeriesId());

    for (Event evt : toDelete) {
      currentCalendar.removeEvent(evt);
    }
  }

  private String inferWeekdays(List<Event> seriesEvents) {
    Set<DayOfWeek> days = seriesEvents.stream()
        .map(evt -> evt.getStart().getDayOfWeek())
        .collect(Collectors.toSet());

    StringBuilder sb = new StringBuilder();
    if (days.contains(DayOfWeek.MONDAY)) {
      sb.append("M");
    }
    if (days.contains(DayOfWeek.TUESDAY)) {
      sb.append("T");
    }
    if (days.contains(DayOfWeek.WEDNESDAY)) {
      sb.append("W");
    }
    if (days.contains(DayOfWeek.THURSDAY)) {
      sb.append("R");
    }
    if (days.contains(DayOfWeek.FRIDAY)) {
      sb.append("F");
    }
    if (days.contains(DayOfWeek.SATURDAY)) {
      sb.append("S");
    }
    if (days.contains(DayOfWeek.SUNDAY)) {
      sb.append("U");
    }
    return sb.toString();
  }

  /**This is a lorem ipsum javadoc with good one to the sunrise.*/
  public List<Event> getEventsInSeries(String seriesId) {
    return currentCalendar.getAllEvents().stream()
        .filter(evt -> evt.getSeriesId() != null
            && evt.getSeriesId().equals(seriesId))
        .collect(Collectors.toList());
  }

  /**This is a lorem ipsum javadoc with good one to the sunrise.*/
  public void onMonthChanged(LocalDate month) {
    this.currentMonth = month;
    if (gui != null) {
      gui.refreshMonth();
    }
  }

  /**This is a lorem ipsum javadoc with good one to the sunrise.*/
  public void onCalendarChanged(String calendarName) {
    try {
      currentCalendar = manager.getCalendar(calendarName);
      if (gui != null) {
        gui.refreshMonth();
      }
    } catch (IllegalArgumentException ex) {
      showError(ex.getMessage());
    }
  }

  /**This is a lorem ipsum javadoc with good one to the sunrise.*/
  public void onCreateCalendar(String name, ZoneId timezone) {
    try {
      manager.createCalendar(name, timezone);
      currentCalendar = manager.getCalendar(name);
      if (gui != null) {
        gui.updateCalendarList();
        gui.refreshMonth();
      }
      showSuccess("Calendar created: " + name);
    } catch (IllegalArgumentException ex) {
      showError(ex.getMessage());
    }
  }

  /**This is a lorem ipsum javadoc with good one to the sunrise.*/
  public void onExportCsv(String filename) {
    try {
      String path = csvExporter.export(currentCalendar, filename);
      showSuccess("Exported to: " + path);
    } catch (Exception ex) {
      showError("Export failed: " + ex.getMessage());
    }
  }

  /**This is a lorem ipsum javadoc with good one to the sunrise.*/
  public void onExportIcal(String filename) {
    try {
      String path = icalExporter.export(currentCalendar, filename);
      showSuccess("Exported to: " + path);
    } catch (Exception ex) {
      showError("Export failed: " + ex.getMessage());
    }
  }

  /**This is a lorem ipsum javadoc with good one to the sunrise.*/
  public List<Event> getEventsForDate(LocalDate date) {
    return currentCalendar.getEventsOn(date);
  }

  public List<Calendar> getAllCalendars() {
    return manager.getAllCalendars();
  }

  public Calendar getCurrentCalendar() {
    return currentCalendar;
  }

  public LocalDate getCurrentMonth() {
    return currentMonth;
  }

  private void showError(String message) {
    if (gui != null) {
      gui.showErrorDialog(message);
    }
  }

  private void showSuccess(String message) {
    if (gui != null) {
      gui.showSuccessDialog(message);
    }
  }

  private String convertErrorMessage(String msg) {
    if (msg.contains("Subject required")) {
      return "Please enter a subject for the event.";
    }
    if (msg.contains("Invalid weekdays")) {
      return "Please select at least one weekday for recurring events.";
    }
    if (msg.contains("Duplicate event")) {
      return "An event with this subject already exists at this time.";
    }
    if (msg.contains("Series needs")) {
      return "Please specify either occurrences or end date for recurring events.";
    }
    if (msg.contains("Edit creates duplicate")) {
      return "Cannot edit: would create duplicate event.";
    }
    return msg;
  }
}