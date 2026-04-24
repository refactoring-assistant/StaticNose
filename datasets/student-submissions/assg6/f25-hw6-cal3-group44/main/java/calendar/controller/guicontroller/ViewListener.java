package calendar.controller.guicontroller;

import calendar.view.CreateEventParams;
import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * Interface for handling view events in the calendar application.
 */
public interface ViewListener {

  /**
   * Handles calendar switch event.
   *
   * @param calendarName the name of the calendar to switch to
   */
  void handleSwitchCalendar(String calendarName);

  /**
   * Handles create calendar event.
   *
   * @param name     the calendar name
   * @param timezone the timezone
   */
  void handleCreateCalendar(String name, String timezone);

  /**
   * Handles edit calendar event.
   *
   * @param property the property to edit
   * @param newValue the new value
   */
  void handleEditCalendar(String property, String newValue);

  /**
   * Handles previous month navigation event.
   */
  void handlePreviousMonth();

  /**
   * Handles next month navigation event.
   */
  void handleNextMonth();

  /**
   * Handles date selection event.
   *
   * @param date the selected date
   */
  void handleDateSelected(LocalDate date);

  /**
   * Handles create event event.
   *
   * @param params the parameters for creating the event
   */
  void handleCreateEvent(CreateEventParams params);

  /**
   * Handles edit event.
   *
   * @param property      the property to edit
   * @param subject       the event subject
   * @param startDateTime the start date and time
   * @param endDateTime   the end date and time
   * @param newValue      the new value
   * @param scope         the edit scope
   */
  void handleEditEvent(String property, String subject, LocalDateTime startDateTime,
                       LocalDateTime endDateTime, String newValue, String scope);

  /**
   * Handles delete event.
   *
   * @param subject       the event subject
   * @param startDateTime the start date and time
   * @param endDateTime   the end date and time
   * @param scope         the delete scope
   */
  void handleDeleteEvent(String subject, LocalDateTime startDateTime,
                         LocalDateTime endDateTime, String scope);

  /**
   * Handles export calendar event.
   *
   * @param fileName the file name
   */
  void handleExportCalendar(String fileName);

  /**
   * Handles request for initial data refresh.
   */
  void handleRefresh();
}
