package calendar.controller;

import calendar.calendarentity.CalendarEntityInterface;
import calendar.event.Event;
import calendar.event.EventContext;
import calendar.event.EventInterface;
import calendar.event.EventWrapper;
import calendar.model.CalendarInterface;
import calendar.model.MultiCalendarManagerInterface;
import calendar.view.CalendarViewInterface;
import java.io.IOException;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * The controller implementation for GUI mode supporting Features.
 */
public class GuiCalendarController implements Features {
  private MultiCalendarManagerInterface model;
  private CalendarViewInterface view;
  private int currentYear;
  private int currentMonth;

  /**
   * The Constructor for GuiCalendarController.
   *
   * @param model the calendar manager
   * @param view the calendar view
   */
  public GuiCalendarController(MultiCalendarManagerInterface model, CalendarViewInterface view) {
    this.model = model;
    this.view = view;
    LocalDate today = LocalDate.now();
    this.currentYear = today.getYear();
    this.currentMonth = today.getMonthValue();
    view.addFeatures(this);
  }

  /**
   * The executioner of setting default calendar.
   */
  public void go() throws IOException {
    String defaultTimeZone = ZoneId.systemDefault().getId();
    CalendarEntityInterface entity = model.createCalendar("Default", defaultTimeZone);
    model.useThisCalendarEntity(model.getCalendarEntity("Default"));

    LocalDate today = LocalDate.now();
    view.displayMonthView(today.getYear(), today.getMonthValue(), new HashMap<>());
    view.displayCurrentCalendar("Default", defaultTimeZone);
    view.makeVisible();
  }

  @Override
  public void createCalendar(String calendarName, String timeZone) {
    model.createCalendar(calendarName, timeZone);
  }

  @Override
  public void createEvent(EventContext context) {

    CalendarEntityInterface entity = model.getCurrentCalendarEntity();
    if (entity == null) {
      view.displayError("No calendar selected. Please select or create a calendar first.");
      return;
    }
    try {
      EventInterface event = entity.getCalendar().createSingleEvent(context, null);
      event.editDescription(context.getDescription());
      event.editLocation(context.getLocation());
      event.editEventStatus(context.getEventStatus());
      view.displaySuccess("Event '" + context.getSubject() + "' created successfully!");
      refreshCurrentMonth();
    } catch (IllegalArgumentException e) {
      if (e.getMessage().equals("subject or startDateTime cannot be empty")) {
        view.displayError("Failed to create single event: " + "Event Name cannot be empty");
      } else {
        view.displayError("Failed to create single event: " + e.getMessage());
      }
    }
  }

  @Override
  public void createEventSeries(EventContext context,
                                String weekdays, int occurrences, String seriesEndDate) {
    try {
      CalendarEntityInterface currentEntity = model.getCurrentCalendarEntity();

      if (currentEntity == null) {
        view.displayError("No calendar selected. Please select or create a calendar first.");
        return;
      }
      currentEntity.getCalendar().createEventSeries(context,
          weekdays, occurrences, seriesEndDate
      );

      view.displaySuccess("Event series '" + context.getSubject() + "' created successfully!");

      // Refresh current month view
      refreshCurrentMonth();

    } catch (IllegalArgumentException e) {
      if (e.getMessage().equals("subject or startDateTime cannot be empty")) {
        view.displayError("Failed to create event series: Event Series Name cannot be empty");
      } else {
        view.displayError("Failed to create event series: " + e.getMessage());
      }
    }
  }

  @Override
  public Collection<String> getAllCalendarNames() {
    Collection<String> calendarNames = new ArrayList<>();
    for (CalendarEntityInterface entity : model.getAllCalendars()) {
      calendarNames.add(entity.getCalendarName());
    }
    return calendarNames;
  }

  @Override
  public void switchCalendar(String calendarName) {
    CalendarEntityInterface entity = model.getCalendarEntity(calendarName);
    if (entity != null) {
      model.useThisCalendarEntity(entity);
      model.useThisCalendarEntity(entity);

      refreshCurrentMonth();
      view.displayCurrentCalendar(entity.getCalendarName(), entity.getTimeZone().toString());
    }
  }

  @Override
  public void editEvent(String subject, String startDateTime, String endDateTime,
                        EventContext newContext, String calendarName) {
    CalendarEntityInterface entity;
    if (calendarName == null) {
      entity = model.getCurrentCalendarEntity();
    } else {
      entity = model.getCalendarEntity(calendarName);
    }
    if (entity == null) {
      view.displayError("No calendar selected. Please select or create a calendar first.");
      return;
    }
    try {
      EventInterface event = entity.getCalendar().editSingleEvent(
          subject, startDateTime, endDateTime, newContext);
      view.displaySuccess("Event '" + subject + "' updated successfully!");
      refreshCurrentMonth();
    } catch (IllegalArgumentException e) {
      if (e.getMessage().equals("Event on the new subject, start date/time,"
          + " end date/time already exists")) {
        view.displayError("Failed to edit single event: "
            + "Event after edition conflicts with existing event");
      } else {
        view.displayError("Failed to edit single event: " + e.getMessage());
      }
    }
  }

  @Override
  public void editCalendarProperty(String calendarName, String propertyName, String propertyValue) {
    try {
      model.editCalendar(calendarName, propertyName, propertyValue);

      CalendarEntityInterface currentEntity = model.getCurrentCalendarEntity();
      if (currentEntity != null) {
        view.displayCurrentCalendar(
            currentEntity.getCalendarName(),
            currentEntity.getTimeZone().toString()
        );
        List<String> allCalendarNames = new ArrayList<>();
        for (CalendarEntityInterface entity : model.getAllCalendars()) {
          allCalendarNames.add(entity.getCalendarName());
        }
        view.displayAvailableCalendars(allCalendarNames);
        refreshCurrentMonth();
      }
    } catch (IllegalArgumentException e) {
      view.displayError("Failed to edit calendar: " + e.getMessage());
    }
  }

  @Override
  public Collection<EventWrapper> getEventsAcrossCalendar(String keyword) {
    return model.getEventsAcrossCalendar(keyword);
  }

  @Override
  public void navigateToMonth(int year, int month) {
    this.currentYear = year;
    this.currentMonth = month;
    refreshCurrentMonth();
  }

  /**
   * Refresh the current month view with events from the model.
   */
  private void refreshCurrentMonth() {
    try {
      CalendarEntityInterface currentEntity = model.getCurrentCalendarEntity();
      if (currentEntity == null) {
        view.displayMonthView(currentYear, currentMonth, new HashMap<>());
        return;
      }
      Map<LocalDate, List<Event>> eventsMap = new HashMap<>();

      LocalDate firstDay = LocalDate.of(currentYear, currentMonth, 1);
      LocalDate lastDay = firstDay.plusMonths(1).minusDays(1);

      for (LocalDate date = firstDay; !date.isAfter(lastDay); date = date.plusDays(1)) {
        List<Event> eventsOnDate = currentEntity.getCalendar().getEventsOnDate(date);
        if (eventsOnDate != null && !eventsOnDate.isEmpty()) {
          eventsMap.put(date, eventsOnDate);
        }
      }
      view.displayMonthView(currentYear, currentMonth, eventsMap);
    } catch (Exception e) {
      view.displayError("Failed to refresh calendar: " + e.getMessage());
      view.displayMonthView(currentYear, currentMonth, new HashMap<>());
    }
  }

  @Override
  public void viewEventsOnDate(LocalDate date) {
    // try {
    CalendarEntityInterface currentEntity = model.getCurrentCalendarEntity();

    if (currentEntity == null) {
      view.displayError("No calendar selected!");
      return;
    }

    List<Event> events = currentEntity.getCalendar().getEventsOnDate(date);
    view.displayEventsOnDate(events, date);

    //    } catch (Exception e) {
    //      view.displayError("Failed to load events: " + e.getMessage());
    //    } // method calls don't throw any exception
  }

  @Override
  public void exportCalendar(String fileName) {
    try {
      CalendarEntityInterface currentEntity = model.getCurrentCalendarEntity();

      if (currentEntity == null) {
        view.displayError("No calendar selected!");
        return;
      }
      String content;
      if (fileName.toLowerCase().endsWith(".csv")) {
        content = currentEntity.getCalendar().exportToCsv();
      } else if (fileName.toLowerCase().endsWith(".ical")) {
        content = currentEntity.getCalendar().exportToIcal(
            currentEntity.getCalendarName(),
            currentEntity.getTimeZone());
      } else {
        view.displayError("Invalid file format. Use .csv or .ical");
        return;
      }
      view.exportCalendar(content, fileName);

    } catch (Exception e) {
      view.displayError("Failed to export calendar: " + e.getMessage());
    }
  }

  @Override
  public String getCurrentCalendarName() {
    CalendarEntityInterface entity = model.getCurrentCalendarEntity();
    return entity != null ? entity.getCalendarName() : "Default";
  }

  @Override
  public String getCurrentCalendarTimezone() {
    CalendarEntityInterface entity = model.getCurrentCalendarEntity();
    return entity != null ? entity.getTimeZone().getId() : ZoneId.systemDefault().getId();
  }

  @Override
  public void editEventSeries(String subject, String startDateTime, String endDateTime,
                              EventContext newContext, String calendarName) {
    try {
      CalendarEntityInterface entity;
      if (calendarName == null || calendarName.isEmpty()) {
        entity = model.getCurrentCalendarEntity();
      } else {
        entity = model.getCalendarEntity(calendarName);
      }
      if (entity == null) {
        view.displayError("No calendar selected. Please select or create a calendar first.");
        return;
      }
      CalendarInterface calendar = entity.getCalendar();
      EventInterface updatedSeries = calendar.editEventSeries(
          subject,
          startDateTime,
          endDateTime,
          newContext
      );
      String displayName = subject;
      if (newContext.getSubject() != null && !newContext.getSubject().isEmpty()) {
        displayName = newContext.getSubject();
      }
      if (updatedSeries != null) {
        view.displaySuccess("Event series '" + displayName + "' updated successfully!");
        refreshCurrentMonth();
      } else {
        view.displayError("Failed to update event series.");
      }

    } catch (IllegalArgumentException e) {
      if (e.getMessage().equals("Event on the new subject, start date/time,"
          + " end date/time already exists")) {
        view.displayError("Failed to edit event series: "
            + "Event after edition conflicts with existing event");
      } else {
        view.displayError("Failed to edit event series: " + e.getMessage());
      }
    } catch (Exception e) {
      view.displayError("Unexpected error: " + e.getMessage());
    }
  }
}
