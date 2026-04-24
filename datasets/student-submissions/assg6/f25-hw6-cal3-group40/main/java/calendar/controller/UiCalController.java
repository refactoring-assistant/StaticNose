package calendar.controller;

import calendar.model.CalendarInterface;
import calendar.model.Event;
import calendar.service.CalendarService;
import calendar.view.EventViewData;
import calendar.view.guibased.SwingView;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Controller for the Swing GUI.
 * Implements UiFeatures to handle callbacks from the view.
 */
public class UiCalController implements UiFeatures {

  private final CalendarService service;
  private SwingView view;

  /**
   * Initializes the UiCalController object.
   *
   * @param service The service object to be used.
   */
  public UiCalController(CalendarService service) {
    this.service = service;
  }

  /**
   * Sets the view object for the controller to use.
   *
   * @param view The view object to be used.
   */
  public void setView(SwingView view) {
    this.view = view;
    this.view.addFeatures(this);

    if (service.getAllCalendars().isEmpty()) {
      try {
        service.createCalendar("Default", java.time.ZoneId.systemDefault().getId());
      } catch (Exception e) {
        this.view.showError("Error creating default calendar: " + e.getMessage());
      }
    }

    List<CalendarInterface> calendars = service.getAllCalendars();
    if (!calendars.isEmpty()) {
      selectCalendar(calendars.get(0).getName());
    }
  }

  @Override
  public void createCalendar(String name, String timezone) {
    try {
      service.createCalendar(name, timezone);
      listCalendars();
      selectCalendar(name);
    } catch (IllegalArgumentException | java.time.DateTimeException e) {
      view.showError("Error creating calendar: " + e.getMessage());
    }
  }

  @Override
  public void selectCalendar(String name) {
    try {
      service.useCalendar(name);
      view.setCurrentCalendar(name);
      view.setTimezone(service.getCurrentCalendarTimezone());
      view.refresh();
    } catch (IllegalArgumentException e) {
      view.showError("Error selecting calendar: " + e.getMessage());
    }
  }

  @Override
  public void listCalendars() {
    List<CalendarInterface> calendars = service.getAllCalendars();
    List<CalendarInterface> calendarArray = new ArrayList<>(calendars);
    view.updateCalendarList(calendarArray);
  }

  @Override
  public void getCurrentCalendarName() {
    String name = service.getCurrentCalendarName();
    if (name != null) {
      view.setCurrentCalendar(name);
    }
  }

  @Override
  public void createEvent(String subject, String fromStr, String toStr, String onStr,
                          String description, String location, boolean isPrivate, String repeats,
                          Integer occurrences,
                          String untilStr) {
    try {
      service.createEvent(subject, fromStr, toStr, onStr, description, location, isPrivate, repeats,
          occurrences,
          untilStr);
      view.refresh();
    } catch (Exception e) {
      throw new IllegalArgumentException(e.getMessage());
    }
  }

  @Override
  public void editEvent(String subject, String fromStr, String toStr, String property,
                        String newValueStr, boolean singleEventUpdate, boolean updateAll) {
    try {
      service.editEvent(subject, fromStr, toStr, property, newValueStr, singleEventUpdate,
          updateAll);
      view.refresh();
    } catch (Exception e) {
      throw new IllegalArgumentException(e.getMessage());
    }
  }

  @Override
  public void getEventsOn(LocalDate date) {
    try {
      List<Event> events = service.getEventsOn(date);
      List<EventViewData> viewData =
          events.stream().map(this::toViewData).collect(Collectors.toList());
      view.showEventsForDay(date, viewData);
    } catch (Exception e) {
      view.showError("Error fetching events: " + e.getMessage());
    }
  }

  @Override
  public void getEventsBetween(LocalDateTime start, LocalDateTime end) {
    try {
      List<Event> events = service.getEventsBetween(start, end);
      Map<LocalDate, List<Event>> eventsByDate = events.stream()
          .collect(Collectors
              .groupingBy(
                  e -> e.getStart().atZone(service.getCurrentCalendarTimezone()).toLocalDate()));

      eventsByDate.forEach((date, dayEvents) -> {
        List<EventViewData> viewData =
            dayEvents.stream().map(this::toViewData).collect(Collectors.toList());
        view.showEventsForDay(date, viewData);
      });

    } catch (Exception e) {
      view.showError("Error fetching events for month: " + e.getMessage());
    }
  }

  @Override
  public void editCalendar(String newName, String newTimezone) {
    try {
      String currentName = service.getCurrentCalendarName();
      if (currentName == null) {
        view.showError("No calendar selected.");
        return;
      }

      boolean nameChanged = !currentName.equals(newName);
      if (nameChanged) {
        updateCalendarName(currentName, newName);
      }

      String targetName = nameChanged ? newName : currentName;

      ZoneId currentZone = service.getCurrentCalendarTimezone();
      if (!currentZone.getId().equals(newTimezone)) {
        service.editCalendarTimezone(targetName, newTimezone);
      }

      refreshCalendarView(targetName);

    } catch (Exception e) {
      view.showError("Error editing calendar: " + e.getMessage());
    }
  }

  /**
   * Updates the calendar name and switches to the new calendar.
   *
   * @param currentName the current name
   * @param newName     the new name
   */
  private void updateCalendarName(String currentName, String newName) {
    service.editCalendarName(currentName, newName);
    service.useCalendar(newName);
  }

  /**
   * Refreshes the calendar view after updates.
   *
   * @param targetName the name of the calendar to select
   */
  private void refreshCalendarView(String targetName) {
    listCalendars();
    selectCalendar(targetName);
  }

  @Override
  public void updateEventTime(String subject, String currentStartStr, String newStartStr,
                              String newEndStr,
                              boolean singleEventUpdate, boolean updateAll) {
    try {
      service.updateEventTime(subject, currentStartStr, newStartStr, newEndStr, singleEventUpdate,
          updateAll);
      view.refresh();
    } catch (Exception e) {
      throw new IllegalArgumentException(e.getMessage());
    }
  }

  private EventViewData toViewData(Event event) {
    return new EventViewData(event.getSubject(), event.getStart(), event.getEnd(),
        event.getDescription(),
        event.getLocation(), event.isPrivate(), event.getSeriesId(), event.isSeries());
  }
}
