package calendar.controller.event;

import calendar.controller.Command;
import calendar.model.Calendar;
import calendar.model.CalendarApplication;
import calendar.model.EventSingle;
import calendar.model.utils.DateTimeCheck;
import calendar.view.CalendarView;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Command for queries.
 * Implements the Command interface and uses Active Calendar.
 */
public class QueryEventsCommand implements Command {

  private final QueryType queryType;
  private final String dateStr;
  private final String startDateTimeStr;
  private final String endDateTimeStr;

  /**
   * Supported query types.
   */
  public enum QueryType {
    EVENTS_ON_DATE,
    EVENTS_IN_RANGE,
    STATUS_AT_TIME
  }

  /**
   * Constructor for date-based queries (events on a date or status at a time).
   *
   * @param queryType query type (must be EVENTS_ON_DATE or STATUS_AT_TIME)
   * @param dateStr   date or datetime string (YYYY-MM-DD or YYYY-MM-DDThh:mm)
   */
  public QueryEventsCommand(QueryType queryType, String dateStr) {
    if (queryType != QueryType.EVENTS_ON_DATE && queryType != QueryType.STATUS_AT_TIME) {
      throw new IllegalArgumentException("Constructor only for date-based queries");
    }
    this.queryType = queryType;
    this.dateStr = dateStr;
    this.startDateTimeStr = null;
    this.endDateTimeStr = null;
  }

  /**
   * Constructor for range queries (events between two datetimes).
   *
   * @param queryType        must be EVENTS_IN_RANGE
   * @param startDateTimeStr start datetime (YYYY-MM-DDThh:mm)
   * @param endDateTimeStr   end datetime (YYYY-MM-DDThh:mm)
   */
  public QueryEventsCommand(QueryType queryType, String startDateTimeStr, String endDateTimeStr) {
    if (queryType != QueryType.EVENTS_IN_RANGE) {
      throw new IllegalArgumentException("Constructor only for range queries");
    }
    this.queryType = queryType;
    this.dateStr = null;
    this.startDateTimeStr = startDateTimeStr;
    this.endDateTimeStr = endDateTimeStr;
  }


  @Override
  public void execute(CalendarApplication model, CalendarView view) {
    try {
      Calendar activeCalendar = model.getActiveCalendar();

      switch (queryType) {
        case EVENTS_ON_DATE:
          handleEventsOnDate(activeCalendar, view);
          break;
        case EVENTS_IN_RANGE:
          handleEventsInRange(activeCalendar, view);
          break;
        case STATUS_AT_TIME:
        default:
          handleStatusAtTime(activeCalendar, view);
      }
    } catch (IllegalArgumentException | IllegalStateException e) {
      view.displayError(e.getMessage());
    } catch (Exception e) {
      view.displayError("An unexpected error occurred: " + e.getMessage());
    }
  }

  private void handleEventsOnDate(Calendar model, CalendarView view) {
    LocalDate date = DateTimeCheck.parseDate(dateStr);
    List<EventSingle> events = model.getEventsOn(date);

    if (events.isEmpty()) {
      view.displayEvents(List.of("No events found on " + formatDate(date)));
      return;
    }

    view.displayEvents(events.stream()
        .map(this::formatEventForDateDisplay)
        .collect(Collectors.toList()));
  }

  private void handleEventsInRange(Calendar model, CalendarView view) {
    LocalDateTime start = DateTimeCheck.parseDateTime(startDateTimeStr);
    LocalDateTime end = DateTimeCheck.parseDateTime(endDateTimeStr);
    DateTimeCheck.validateStartBeforeEnd(start, end);

    List<EventSingle> events = model.getEventsInRange(start, end);

    if (events.isEmpty()) {
      view.displayEvents(List.of("No events found between "
          + formatDateTime(start) + " and " + formatDateTime(end)));
      return;
    }

    view.displayEvents(events.stream()
        .map(this::formatEventForRangeDisplay)
        .collect(Collectors.toList()));
  }

  private void handleStatusAtTime(Calendar model, CalendarView view) {
    LocalDateTime dateTime = DateTimeCheck.parseDateTime(dateStr);
    boolean isBusy = model.isBusy(dateTime);
    view.displayStatus(isBusy ? "Busy" : "Available");
  }

  private String formatEventForDateDisplay(EventSingle event) {
    DateTimeFormatter timeFormatter = DateTimeFormatter.ofPattern("h:mm a");
    String startTime = event.getStart().format(timeFormatter);
    String endTime = event.getEnd().format(timeFormatter);
    String location = event.getLocation();

    StringBuilder sb = new StringBuilder();
    sb.append(event.getSubject())
        .append(" from ").append(startTime)
        .append(" to ").append(endTime);

    if (location != null && !location.isBlank()) {
      sb.append(" at ").append(location);
    }

    return sb.toString();
  }

  private String formatEventForRangeDisplay(EventSingle event) {
    DateTimeFormatter dateFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");
    DateTimeFormatter timeFormatter = DateTimeFormatter.ofPattern("h:mm a");

    String startDate = event.getStart().toLocalDate().format(dateFormatter);
    String startTime = event.getStart().format(timeFormatter);
    String endDate = event.getEnd().toLocalDate().format(dateFormatter);
    String endTime = event.getEnd().format(timeFormatter);
    String location = event.getLocation();

    StringBuilder sb = new StringBuilder();
    sb.append(event.getSubject())
        .append(" starting on ").append(startDate).append(" at ").append(startTime)
        .append(", ending on ").append(endDate).append(" at ").append(endTime);

    if (location != null && !location.isBlank()) {
      sb.append(" at ").append(location);
    }

    return sb.toString();
  }

  private String formatDate(LocalDate date) {
    return date.format(DateTimeFormatter.ofPattern("yyyy-MM-dd"));
  }

  private String formatDateTime(LocalDateTime dateTime) {
    return dateTime.format(DateTimeFormatter.ofPattern("yyyy-MM-dd 'at' h:mm a"));
  }
}