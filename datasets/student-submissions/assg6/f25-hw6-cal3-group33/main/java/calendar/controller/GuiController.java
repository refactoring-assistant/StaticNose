package calendar.controller;

import calendar.controller.command.Command;
import calendar.controller.command.CreateCalendarCommand;
import calendar.controller.command.CreateEventCommand;
import calendar.controller.command.CreateRepeatingEventCommand;
import calendar.controller.command.EditCalendarCommand;
import calendar.controller.command.EditEventsFromCommand;
import calendar.controller.command.EditSeriesCommand;
import calendar.controller.command.EditSingleEventCommand;
import calendar.controller.command.ExportCalendarCommand;
import calendar.controller.command.UseCalendarCommand;
import calendar.exceptions.NoCalendarInUseException;
import calendar.model.calendar.EditScope;
import calendar.model.calendar.ReadOnlyCalendar;
import calendar.model.event.EventInterface;
import calendar.model.manager.CalendarManager;
import calendar.model.util.DateTimeParser;
import calendar.view.GuiCalendarInterface;
import java.awt.Color;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.YearMonth;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * GUI controller that handles user interactions from the graphical interface.
 * Implements the CalendarFeatures interface to provide high-level user actions
 * and coordinates between the calendar model and GUI view.
 */
public class GuiController implements CalendarFeatures, CalendarController {

  private GuiCalendarInterface view;
  private CalendarManager manager;

  /**
   * Constructs a GUIController with the specified manager and view.
   *
   * @param manager the calendar manager to interact with the model
   * @param view    the GUI view to display information to the user
   */
  public GuiController(CalendarManager manager, GuiCalendarInterface view) {
    this.manager = manager;
    this.view = view;
    view.setFeatures(this);
  }

  /**
   * Starts the GUI controller by making the view visible.
   */
  @Override
  public void run() {
    view.setVisible(true);
  }

  /**
   * Creates a new calendar with the specified name and timezone.
   *
   * @param calendarName the name for the new calendar
   * @param timezone     the timezone in IANA format (e.g., "America/New_York")
   */
  @Override
  public void createCalendar(String calendarName, String timezone) {
    try {
      Command cmd = new CreateCalendarCommand(calendarName, timezone);
      cmd.execute(manager, view);
    } catch (Exception e) {
      view.displayError(e.getMessage());
    }
  }

  /**
   * Edits calendar properties.
   * Handles both name and timezone changes, editing timezone first for proper lookup.
   *
   * @param originalName the current calendar name
   * @param newName      the new calendar name
   * @param newTimezone  the new timezone
   */
  @Override
  public void editCalendar(String originalName, String newName, String newTimezone) {
    try {
      ReadOnlyCalendar currentCalendar = manager.getCalendar(originalName);
      String currentTimezone = currentCalendar.getCalendarTimeZone().getId();

      boolean nameChanged = !originalName.equals(newName);
      boolean timezoneChanged = !currentTimezone.equals(newTimezone);
      if (timezoneChanged) {
        Command cmd = new EditCalendarCommand(originalName, "timezone", newTimezone);
        cmd.execute(manager, view);
      }
      if (nameChanged) {
        Command cmd = new EditCalendarCommand(originalName, "name", newName);
        cmd.execute(manager, view);
      }
      if (!nameChanged && !timezoneChanged) {
        view.displayMessage("No changes made to calendar");
      }

    } catch (Exception e) {
      view.displayError(e.getMessage());
    }
  }

  /**
   * Selects a calendar to be the currently active calendar.
   *
   * @param calendarName the name of the calendar to select
   */
  @Override
  public void selectCalendar(String calendarName) {
    try {
      Command cmd = new UseCalendarCommand(calendarName);
      cmd.execute(manager, view);
    } catch (Exception e) {
      view.displayError(e.getMessage());
    }
  }

  @Override
  public void selectDay(LocalDate date) {
    try {
      String dateString = date.toString();
      ReadOnlyCalendar calendar = manager.getCurrentCalendar();
      List<EventInterface> events = calendar.getEvents(dateString);

      view.showDayEventsFromModel(date, events);

    } catch (Exception e) {
      view.displayError(e.getMessage());
    }
  }

  /**
   * Exports the current calendar to a file.
   *
   * @param format the export format ("ical" or "csv")
   */
  @Override
  public void exportCalendar(String format) {
    try {
      String currentCalendarName = manager.getCurrentCalendar().getCalendarName();
      String fileName = currentCalendarName + "_" + LocalDate.now() + "." + format;

      String projectDir = System.getProperty("user.dir");
      String filePath = projectDir + "/" + fileName;

      Command cmd = new ExportCalendarCommand(filePath);
      cmd.execute(manager, view);

      view.showExportSuccess(filePath);

    } catch (Exception e) {
      view.displayError(e.getMessage());
    }
  }

  /**
   * Creates a single timed event with optional details.
   *
   * @param subject     the event subject
   * @param start       the start date and time
   * @param end         the end date and time
   * @param location    the location (optional, can be null)
   * @param description the description (optional, can be null)
   * @param status      the status (optional, can be null)
   */
  @Override
  public void createSingleEvent(String subject, LocalDateTime start, LocalDateTime end,
                                String location, String description, String status) {
    try {
      ZoneId currentTimezone = manager.getCurrentCalendar().getCalendarTimeZone();

      ZonedDateTime zonedStart = start.atZone(currentTimezone);
      ZonedDateTime zonedEnd = end.atZone(currentTimezone);

      String startStr = DateTimeParser.formatDateTimeForCommand(zonedStart);
      String endStr = DateTimeParser.formatDateTimeForCommand(zonedEnd);

      Command createCmd = new CreateEventCommand(subject, startStr, endStr);
      createCmd.execute(manager, view);

      addOptionalFields(subject, startStr, endStr, location, description, status);

    } catch (Exception e) {
      view.displayError(e.getMessage());
    }
  }

  /**
   * Creates a recurring timed event with optional details.
   * Must specify either count or untilDate, but not both.
   *
   * @param subject     the event subject
   * @param start       the start date and time
   * @param end         the end date and time
   * @param weekdays    the weekdays to repeat on (e.g., "MWF")
   * @param count       the number of occurrences (null if using untilDate)
   * @param untilDate   the end date for recurrence (null if using count)
   * @param location    the location (optional, can be null)
   * @param description the description (optional, can be null)
   * @param status      the status (optional, can be null)
   */
  @Override
  public void createRecurringEvent(String subject, LocalDateTime start, LocalDateTime end,
                                   String weekdays, Integer count, String untilDate,
                                   String location, String description, String status) {
    try {
      if (!validateRecurringParameters(count, untilDate, weekdays)) {
        return;
      }

      ZoneId currentTimezone = manager.getCurrentCalendar().getCalendarTimeZone();

      ZonedDateTime zonedStart = start.atZone(currentTimezone);
      ZonedDateTime zonedEnd = end.atZone(currentTimezone);

      String startStr = DateTimeParser.formatDateTimeForCommand(zonedStart);
      String endStr = DateTimeParser.formatDateTimeForCommand(zonedEnd);

      Command createCmd;
      if (count != null) {
        createCmd = new CreateRepeatingEventCommand(subject, startStr, endStr,
            weekdays, count);
      } else {
        createCmd = new CreateRepeatingEventCommand(subject, startStr, endStr,
            weekdays, untilDate);
      }
      createCmd.execute(manager, view);

      addOptionalFields(subject, startStr, endStr, location, description, status);

    } catch (Exception e) {
      view.displayError(e.getMessage());
    }
  }

  /**
   * Edits an existing event with new values.
   * Only properties that differ from original values will be updated.
   *
   * @param originalSubject the current subject of the event
   * @param originalStart   the current start date and time
   * @param originalEnd     the current end date and time
   * @param newSubject      the new subject
   * @param newStart        the new start date and time
   * @param newEnd          the new end date and time
   * @param newLocation     the new location (can be null or empty)
   * @param newDescription  the new description (can be null or empty)
   * @param newStatus       the new status ("PUBLIC" or "PRIVATE")
   * @param scope           the edit scope (SINGLE, FROM_POINT, or ENTIRE_SERIES)
   */
  @Override
  public void editEvent(String originalSubject, LocalDateTime originalStart,
                        LocalDateTime originalEnd,
                        String newSubject, LocalDateTime newStart, LocalDateTime newEnd,
                        String newLocation, String newDescription, String newStatus,
                        calendar.model.calendar.EditScope scope) {
    try {
      ZoneId timezone = manager.getCurrentCalendar().getCalendarTimeZone();

      ZonedDateTime zonedOriginalStart = originalStart.atZone(timezone);
      ZonedDateTime zonedOriginalEnd = originalEnd.atZone(timezone);
      String originalStartStr = DateTimeParser.formatDateTimeForCommand(zonedOriginalStart);
      String originalEndStr = DateTimeParser.formatDateTimeForCommand(zonedOriginalEnd);

      String currentSubject = originalSubject;
      String currentStartStr = originalStartStr;
      String currentEndStr = originalEndStr;


      if (newLocation != null && !newLocation.isEmpty()) {
        executeEdit("location", currentSubject, currentStartStr, currentEndStr,
            newLocation, scope);
      }

      if (newDescription != null && !newDescription.isEmpty()) {
        executeEdit("description", currentSubject, currentStartStr, currentEndStr,
            newDescription, scope);
      }

      if (newStatus != null) {
        executeEdit("status", currentSubject, currentStartStr, currentEndStr,
            newStatus, scope);
      }


      if (!originalSubject.equals(newSubject)) {
        executeEdit("subject", currentSubject, currentStartStr, currentEndStr,
            newSubject, scope);
        currentSubject = newSubject;
      }

      boolean startChanged = !originalStart.equals(newStart);
      boolean endChanged = !originalEnd.equals(newEnd);

      if (startChanged || endChanged) {
        ZonedDateTime zonedNewStart = (startChanged ? newStart : originalStart).atZone(timezone);
        ZonedDateTime zonedNewEnd = (endChanged ? newEnd : originalEnd).atZone(timezone);

        String newStartStr = DateTimeParser.formatDateTimeForCommand(zonedNewStart);
        String newEndStr = DateTimeParser.formatDateTimeForCommand(zonedNewEnd);

        if (startChanged && endChanged && newStart.isAfter(originalEnd)) {
          executeEdit("end", currentSubject, currentStartStr, currentEndStr, newEndStr, scope);
          currentEndStr = newEndStr;

          executeEdit("start", currentSubject, currentStartStr, currentEndStr, newStartStr, scope);
          currentStartStr = newStartStr;
        } else {
          if (startChanged) {
            executeEdit("start", currentSubject, currentStartStr, currentEndStr, newStartStr,
                scope);
            currentStartStr = newStartStr;
          }

          if (endChanged) {
            executeEdit("end", currentSubject, currentStartStr, currentEndStr, newEndStr, scope);
            currentEndStr = newEndStr;
          }
        }
      }

    } catch (Exception e) {
      view.displayError(e.getMessage());
    }
  }

  /**
   * Adds optional fields to a created event.
   */
  private void addOptionalFields(String subject, String startStr, String endStr,
                                 String location, String description, String status)
      throws Exception {

    if (location != null && !location.isEmpty()) {
      Command editLocationCmd = new EditSingleEventCommand("location", subject,
          startStr, endStr, location);
      editLocationCmd.execute(manager, view);
    }

    if (description != null && !description.isEmpty()) {
      Command editDescCmd = new EditSingleEventCommand("description", subject,
          startStr, endStr, description);
      editDescCmd.execute(manager, view);
    }

    if (status != null && !status.isEmpty()) {
      Command editStatusCmd = new EditSingleEventCommand("status", subject,
          startStr, endStr, status);
      editStatusCmd.execute(manager, view);
    }
  }

  /**
   * Validates that recurring event parameters are correctly specified.
   * Exactly one of count or untilDate must be provided.
   * Weekdays cannot be negative.
   */
  private boolean validateRecurringParameters(Integer count, String untilDate, String weekdays) {
    if (count == null && untilDate == null) {
      view.displayError("Must provide either count or until date for recurring event");
      return false;
    }
    if (count != null && untilDate != null) {
      view.displayError("Cannot provide both count and until date");
      return false;
    }
    if (weekdays == null || weekdays.trim().isEmpty()) {
      view.displayError("Cannot create recurring events without weekdays");
      return false;
    }
    return true;
  }

  /**
   * Executes an edit command with the appropriate scope.
   */
  private void executeEdit(String property, String subject, String startStr, String endStr,
                           String newValue, EditScope scope) throws Exception {
    Command cmd;

    if (scope == EditScope.SINGLE) {
      cmd = new EditSingleEventCommand(property, subject, startStr, endStr, newValue);
    } else if (scope == EditScope.FROM_POINT) {
      cmd = new EditEventsFromCommand(property, subject, startStr, newValue);
    } else {
      cmd = new EditSeriesCommand(property, subject, startStr, newValue);
    }
    cmd.execute(manager, view);
  }

  @Override
  public void refreshCurrentMonth() {
    try {
      ReadOnlyCalendar calendar = manager.getCurrentCalendar();

      YearMonth currentMonth = view.getCurrentMonth();

      LocalDate firstDay = currentMonth.atDay(1);
      int startDayOfWeek = firstDay.getDayOfWeek().getValue() % 7;
      LocalDate rangeStart = firstDay.minusDays(startDayOfWeek);

      LocalDate lastDay = currentMonth.atEndOfMonth();
      int endDayOfWeek = lastDay.getDayOfWeek().getValue() % 7;
      int daysToAdd = 6 - endDayOfWeek;
      LocalDate rangeEnd = lastDay.plusDays(daysToAdd);

      ZoneId timezone = calendar.getCalendarTimeZone();
      ZonedDateTime startDateTime = rangeStart.atStartOfDay(timezone);
      ZonedDateTime endDateTime = rangeEnd.atTime(23, 59).atZone(timezone);

      String startDateStr = DateTimeParser.formatDateTimeForCommand(startDateTime);
      String endDateStr = DateTimeParser.formatDateTimeForCommand(endDateTime);

      List<EventInterface> events = calendar.getEvents(startDateStr, endDateStr);

      Map<LocalDate, Integer> eventsPerDay = new HashMap<>();
      for (EventInterface event : events) {
        LocalDate startDate = event.getStartDateTime().toLocalDate();
        LocalDate endDate = event.getEndDateTime().toLocalDate();

        LocalDate currentDate = startDate;
        while (!currentDate.isAfter(endDate)) {
          eventsPerDay.put(currentDate, eventsPerDay.getOrDefault(currentDate, 0) + 1);
          currentDate = currentDate.plusDays(1);
        }
      }

      Color calendarColor = view.getCalendarColor(calendar.getCalendarName());
      view.updateEventIndicators(eventsPerDay, calendarColor);

    } catch (Exception e) {
      view.displayError(e.getMessage());
    }
  }

  @Override
  public List<String> getAllCalendarNames() {
    Set<String> names = manager.getAllCalendarNames();
    return new ArrayList<>(names);
  }

  @Override
  public String getCurrentCalendarName() {
    return manager.getCurrentCalendarName();
  }

  @Override
  public String getCurrentCalendarTimezone() throws NoCalendarInUseException {
    return manager.getCurrentCalendar().getCalendarTimeZone().getId();
  }

}