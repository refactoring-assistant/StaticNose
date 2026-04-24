package calendar.controller;

import calendar.model.CalendarEvent;
import calendar.model.CalendarModel;
import calendar.model.CalendarSystem;
import calendar.model.Event;
import calendar.view.GuiCalendarView;
import calendar.view.SwingCalendarView;
import calendar.view.dialogs.CreateCalendarDialog;
import calendar.view.dialogs.CreateEventDialog;
import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.YearMonth;
import java.time.ZoneId;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * Controller for GUI-based calendar operations.
 * Handles user interactions from Swing components.
 */
public class GuiCalendarController {
  private final CalendarSystem system;
  private final CalendarContext context;
  private SwingCalendarView view;

  /**
   * Constructs a GUI calendar controller.
   *
   * @param system the calendar system
   */
  public GuiCalendarController(CalendarSystem system) {
    this.system = system;
    this.context = new CalendarContext(system);

    // Create default calendar in user's timezone
    String defaultTimezone = ZoneId.systemDefault().getId();
    try {
      system.createCalendar("Default", defaultTimezone);
      context.setCurrentCalendar("Default");
    } catch (Exception e) {
      // Ignore if already exists
    }
  }

  /**
   * Sets the view for this controller.
   *
   * @param view the GUI view
   */
  public void setView(SwingCalendarView view) {
    this.view = view;
  }

  /**
   * Initializes the GUI with current state.
   */
  public void initialize() {
    updateCalendarList();
    refreshView();
  }

  /**
   * Refreshes the entire view.
   */
  public void refreshView() {
    if (view == null || !context.hasCurrentCalendar()) {
      return;
    }

    try {
      CalendarModel calendar = context.getCurrentCalendar();
      List<CalendarEvent> events = calendar.getAllEvents();

      view.updateMonthView(view.getCurrentMonth(), events,
          context.getCurrentCalendarName());

      if (view.getSelectedDate() != null) {
        updateSelectedDay(view.getSelectedDate());
      }
    } catch (Exception e) {
      view.showErrorDialog("Error", e.getMessage());
    }
  }

  /**
   * Handles previous month button click.
   */
  public void onPreviousMonth() {
    YearMonth newMonth = view.getCurrentMonth().minusMonths(1);
    updateMonth(newMonth);
  }

  /**
   * Handles next month button click.
   */
  public void onNextMonth() {
    YearMonth newMonth = view.getCurrentMonth().plusMonths(1);
    updateMonth(newMonth);
  }

  /**
   * Handles day selection in month view.
   *
   * @param date the selected date
   */
  public void onDaySelected(LocalDate date) {
    updateSelectedDay(date);
  }

  /**
   * Handles calendar selection from dropdown.
   *
   * @param calendarName the selected calendar name
   */
  public void onCalendarSelected(String calendarName) {
    if (calendarName == null || calendarName.isEmpty()) {
      return;
    }

    try {
      context.setCurrentCalendar(calendarName);
      refreshView();
    } catch (Exception e) {
      view.showErrorDialog("Error", e.getMessage());
    }
  }

  /**
   * Handles create calendar button click.
   */
  public void onCreateCalendar() {
    CreateCalendarDialog dialog = new CreateCalendarDialog(view);
    dialog.setVisible(true);

    if (dialog.isConfirmed()) {
      try {
        system.createCalendar(dialog.getCalendarName(), dialog.getTimezone());
        context.setCurrentCalendar(dialog.getCalendarName());
        updateCalendarList();
        refreshView();
        view.showInfoDialog("Success", "Calendar created successfully");
      } catch (Exception e) {
        view.showErrorDialog("Error Creating Calendar", e.getMessage());
      }
    }
  }

  /**
   * Handles create event button click.
   *
   * @param date the date for the new event
   */
  public void onCreateEvent(LocalDate date) {
    if (!context.hasCurrentCalendar()) {
      view.showErrorDialog("No Calendar",
          "Please create or select a calendar first");
      return;
    }

    CreateEventDialog dialog = new CreateEventDialog(view, date);
    dialog.setVisible(true);

    if (dialog.isConfirmed()) {
      try {
        String subject = dialog.getSubject();
        if (subject == null || subject.trim().isEmpty()) {
          view.showErrorDialog("Invalid Input", "Subject cannot be empty");
          return;
        }

        CalendarModel calendar = context.getCurrentCalendar();

        if (dialog.isRecurring()) {
          createRecurringEvent(dialog, date, calendar);
        } else {
          createSingleEvent(dialog, date, calendar);
        }

        refreshView();
        view.showInfoDialog("Success", "Event created successfully");

      } catch (Exception e) {
        view.showErrorDialog("Error Creating Event", e.getMessage());
      }
    }
  }

  /**
   * Handles edit events button click.
   *
   * @param date the date to edit events for
   */
  public void onEditEvents(LocalDate date) {
    if (!context.hasCurrentCalendar()) {
      view.showErrorDialog("No Calendar",
          "Please create or select a calendar first");
      return;
    }

    try {
      CalendarModel calendar = context.getCurrentCalendar();
      List<CalendarEvent> events = calendar.getEventsOnDate(date);

      if (events.isEmpty()) {
        view.showInfoDialog("No Events", "No events on this date to edit");
        return;
      }

      // Show edit dialog (simplified - just shows event list)
      StringBuilder eventList = new StringBuilder("Events on " + date + ":\n\n");
      for (int i = 0; i < events.size(); i++) {
        CalendarEvent event = events.get(i);
        eventList.append((i + 1)).append(". ").append(event.getSubject())
            .append(" (").append(event.getStartDateTime().toLocalTime())
            .append(" - ").append(event.getEndDateTime().toLocalTime())
            .append(")\n");
      }

      view.showInfoDialog("Events on " + date, eventList.toString());

    } catch (Exception e) {
      view.showErrorDialog("Error", e.getMessage());
    }
  }

  /**
   * Creates a single event from dialog data.
   */
  private void createSingleEvent(CreateEventDialog dialog, LocalDate date,
                                 CalendarModel calendar) {
    LocalDateTime start = LocalDateTime.of(date, dialog.getStartTime());
    LocalDateTime end = LocalDateTime.of(date, dialog.getEndTime());

    CalendarEvent event = new Event(dialog.getSubject(), start, end);

    if (dialog.getDescription() != null && !dialog.getDescription().isEmpty()) {
      event.setDescription(dialog.getDescription());
    }
    if (dialog.getEventLocation() != null && !dialog.getEventLocation().isEmpty()) {
      event.setLocation(dialog.getEventLocation());
    }
    event.setStatus(dialog.getStatus());

    calendar.addEvent(event);
  }

  /**
   * Creates a recurring event series from dialog data.
   */
  private void createRecurringEvent(CreateEventDialog dialog, LocalDate date,
                                    CalendarModel calendar) {
    final LocalDateTime start = LocalDateTime.of(date, dialog.getStartTime());
    final LocalDateTime end = LocalDateTime.of(date, dialog.getEndTime());

    // Convert boolean[] to Set<DayOfWeek>
    boolean[] selectedDays = dialog.getSelectedDays();
    Set<DayOfWeek> daysOfWeek = new HashSet<>();
    DayOfWeek[] dayValues = {DayOfWeek.MONDAY, DayOfWeek.TUESDAY, DayOfWeek.WEDNESDAY,
        DayOfWeek.THURSDAY, DayOfWeek.FRIDAY, DayOfWeek.SATURDAY, DayOfWeek.SUNDAY};

    for (int i = 0; i < 7; i++) {
      if (selectedDays[i]) {
        daysOfWeek.add(dayValues[i]);
      }
    }

    if (daysOfWeek.isEmpty()) {
      throw new IllegalArgumentException("Must select at least one day for recurring event");
    }

    Integer occurrences = null;
    LocalDate untilDate = null;

    if (dialog.isCountBased()) {
      occurrences = dialog.getOccurrences();
    } else {
      untilDate = LocalDate.parse(dialog.getUntilDate());
    }

    String seriesId = calendar.addEventSeries(dialog.getSubject(), start, end,
        daysOfWeek, occurrences, untilDate);

    // Apply optional fields to all events in series
    List<CalendarEvent> allEvents = calendar.getAllEvents();
    for (CalendarEvent event : allEvents) {
      if (seriesId.equals(event.getSeriesId())) {
        if (dialog.getDescription() != null && !dialog.getDescription().isEmpty()) {
          event.setDescription(dialog.getDescription());
        }
        if (dialog.getEventLocation() != null && !dialog.getEventLocation().isEmpty()) {
          event.setLocation(dialog.getEventLocation());
        }
        event.setStatus(dialog.getStatus());
      }
    }
  }

  private void updateMonth(YearMonth month) {
    if (!context.hasCurrentCalendar()) {
      return;
    }

    try {
      CalendarModel calendar = context.getCurrentCalendar();
      List<CalendarEvent> events = calendar.getAllEvents();

      view.updateMonthView(month, events, context.getCurrentCalendarName());
    } catch (Exception e) {
      view.showErrorDialog("Error", e.getMessage());
    }
  }

  private void updateSelectedDay(LocalDate date) {
    if (!context.hasCurrentCalendar()) {
      return;
    }

    try {
      CalendarModel calendar = context.getCurrentCalendar();
      List<CalendarEvent> events = calendar.getEventsOnDate(date);

      view.updateDayEvents(date, events);
    } catch (Exception e) {
      view.showErrorDialog("Error", e.getMessage());
    }
  }

  private void updateCalendarList() {
    List<String> calendarNames = system.getAllCalendarNames();
    String current = context.hasCurrentCalendar()
        ? context.getCurrentCalendarName()
        : null;

    view.updateCalendarList(calendarNames, current);
  }
}