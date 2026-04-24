package calendar.controllers;

import calendar.models.Calendar;
import calendar.models.CalendarExporter;
import calendar.models.CsvCalendarExporter;
import calendar.models.Event;
import calendar.models.EventProperty;
import calendar.models.RecurrenceRule;
import calendar.views.ObservableView;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Objects;

/**
 * Factory for creating command instances.
 */
public class CommandFactoryImpl implements CommandFactory {

  private final Calendar model;
  private final ObservableView view;

  /**
   * Constructor for CommandFactory.
   *
   * @param model calendar model
   * @param view  calendar view
   */
  public CommandFactoryImpl(Calendar model, ObservableView view) {
    this.model = Objects.requireNonNull(model);
    this.view = Objects.requireNonNull(view);
  }

  @Override
  public Command createExportCsvCommand(String filePath) {
    CalendarExporter csvExporter = new CsvCalendarExporter();
    return new ExportCommand(csvExporter, model, view, filePath);
  }

  @Override
  public Command createCreateAllDayEventCommand(String subject, LocalDate startDate) {
    return new CreateAllDayEventCommand(model, view, subject, startDate);
  }

  @Override
  public Command createCreateEventCommand(String subject, LocalDateTime startDateTime,
      LocalDateTime endDateTime) {
    return new CreateEventCommand(model, view, subject, startDateTime.toLocalDate(),
        startDateTime.toLocalTime(), endDateTime.toLocalDate(), endDateTime.toLocalTime());
  }

  @Override
  public Command createCreateEventSeriesCommand(Event templateEvent, RecurrenceRule rule) {
    return new CreateEventSeriesCommand(model, view, templateEvent, rule);
  }

  @Override
  public Command createEditEventCommand(EventProperty property, String subject,
      LocalDateTime startDateTime, LocalDateTime endDateTime,
      String newValue) {
    return new EditEventCommand(model, view, property, subject, startDateTime, endDateTime,
        newValue);
  }

  @Override
  public Command createEditEventsCommand(EventProperty property, String subject,
      LocalDateTime startDateTime, String newValue) {
    return new EditEventsCommand(model, view, property, subject, startDateTime, newValue);
  }

  @Override
  public Command createEditSeriesCommand(EventProperty property, String subject,
      LocalDateTime startDateTime, String newValue) {
    return new EditSeriesCommand(model, view, property, subject, startDateTime, newValue);
  }

  @Override
  public Command createQueryEventsByDateCommand(LocalDate date) {
    return new QueryEventsByDateCommand(model, view, date);
  }

  @Override
  public Command createQueryEventsByDateRangeCommand(LocalDateTime start, LocalDateTime end) {
    return new QueryEventsByDateRangeCommand(model, view, start, end);
  }

  @Override
  public Command createShowStatusCommand(LocalDateTime date) {
    return new ShowStatusCommand(model, view, date);
  }

}
