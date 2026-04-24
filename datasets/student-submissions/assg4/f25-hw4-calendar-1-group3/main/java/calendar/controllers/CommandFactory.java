package calendar.controllers;

import calendar.models.Event;
import calendar.models.EventProperty;
import calendar.models.RecurrenceRule;
import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * Factory interface for creating command instances.
 */
public interface CommandFactory {

  /**
   * Creates an ExportCommand for exporting calendar to CSV.
   *
   * @param filePath path to export CSV file
   * @return ExportCommand instance
   */
  Command createExportCsvCommand(String filePath);

  /**
   * Creates a CreateAllDayEventCommand.
   *
   * @param subject   event subject
   * @param startDate event start date
   * @return CreateAllDayEventCommand instance
   */
  Command createCreateAllDayEventCommand(String subject, LocalDate startDate);

  /**
   * Creates a CreateEventCommand.
   *
   * @param subject       event subject
   * @param startDateTime event start date and time
   * @param endDateTime   event end date and time
   * @return CreateEventCommand instance
   */
  Command createCreateEventCommand(String subject, LocalDateTime startDateTime,
      LocalDateTime endDateTime);

  /**
   * Creates a CreateEventSeriesCommand.
   *
   * @param templateEvent template event for the series
   * @param rule          recurrence rule for the series
   * @return CreateEventSeriesCommand instance
   */
  Command createCreateEventSeriesCommand(Event templateEvent, RecurrenceRule rule);

  /**
   * Creates a EditEventCommand.
   *
   * @param property      event property to edit
   * @param subject       subject of the event which is to be edited
   * @param startDateTime startDateTime of the event which is to be edited
   * @param endDateTime   newValue new value for the property
   * @param newValue      new value for the property
   * @return EditEventCommand instance
   */
  Command createEditEventCommand(EventProperty property, String subject,
      LocalDateTime startDateTime, LocalDateTime endDateTime,
      String newValue);

  /**
   * Creates a EditEventsCommand.
   *
   * @param property      event property to edit
   * @param subject       subject of the events to be edited
   * @param startDateTime startDateTime of the events to be edited
   * @param newValue      new value for the property
   * @return EditEventsCommand instance
   */
  Command createEditEventsCommand(EventProperty property, String subject,
      LocalDateTime startDateTime, String newValue);

  /**
   * Creates an EditSeriesCommand.
   *
   * @param property      event property to edit
   * @param subject       subject of the event which is to be edited
   * @param startDateTime startDateTime of the event which is to be edited
   * @param newValue      newValue new value for the property
   * @return EditSeriesCommand instance
   */
  Command createEditSeriesCommand(EventProperty property, String subject,
      LocalDateTime startDateTime, String newValue);

  /**
   * Creates a QueryEventsByDateCommand.
   *
   * @param date date to query events
   * @return QueryEventsByDateCommand instance
   */
  Command createQueryEventsByDateCommand(LocalDate date);

  /**
   * Creates a QueryEventsByDateRangeCommand.
   *
   * @param start start date and time of the range
   * @param end   end date and time of the range
   * @return QueryEventsByDateRangeCommand instance
   */
  Command createQueryEventsByDateRangeCommand(LocalDateTime start, LocalDateTime end);

  /**
   * Creates a ShowStatusCommand.
   *
   * @param date date and time to show status
   * @return ShowStatusCommand instance
   */
  Command createShowStatusCommand(LocalDateTime date);

}
