package calendar.controller.event;

import calendar.controller.Command;
import calendar.model.Calendar;
import calendar.model.CalendarApplication;
import calendar.model.EventSeries;
import calendar.model.EventSingle;
import calendar.model.utils.DateTimeCheck;
import calendar.model.utils.DayOfWeek;
import calendar.view.CalendarView;
import java.time.DateTimeException;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.EnumSet;
import java.util.List;

/**
 * Command for creating event.
 * Implements the Command interface and uses Active Calendar.
 */
public class CreateEventCommand implements Command {

  private final String subject;
  private final String startDateTimeStr;
  private final String endDateTimeStr;
  private final String onDateStr;
  private final String weekdaysStr;
  private final String occurrencesStr;
  private final String untilDateStr;

  /**
   * Constructs a new command for creating an event or series.
   * Unused parameters should be passed as null.
   *
   * @param subject          Subject of the event.
   * @param startDateTimeStr Start date/time (YYYY-MM-DDTHH:mm).
   * @param endDateTimeStr   End date/time (YYYY-MM-DDTHH:mm).
   * @param onDateStr        "All day" event date (YYYY-MM-DD).
   * @param weekdaysStr      Recurrence days (e.g., "MRU").
   * @param occurrencesStr   Number of recurrences (e.g., "6").
   * @param untilDateStr     Recurrence end date (YYYY-MM-DD).
   */
  public CreateEventCommand(String subject, String startDateTimeStr, String endDateTimeStr,
                            String onDateStr, String weekdaysStr, String occurrencesStr,
                            String untilDateStr) {
    this.subject = subject;
    this.startDateTimeStr = startDateTimeStr;
    this.endDateTimeStr = endDateTimeStr;
    this.onDateStr = onDateStr;
    this.weekdaysStr = weekdaysStr;
    this.occurrencesStr = occurrencesStr;
    this.untilDateStr = untilDateStr;
  }


  @Override
  public void execute(CalendarApplication model, CalendarView view) {
    try {
      Calendar activeCalendar = model.getActiveCalendar();

      LocalDateTime start;
      LocalDateTime end;

      if (onDateStr != null) {
        LocalDate date = DateTimeCheck.parseDate(onDateStr);
        start = DateTimeCheck.createAllDayStartTime(date);
        end = DateTimeCheck.createAllDayEndTime(date);
      } else if (startDateTimeStr != null && endDateTimeStr != null) {
        start = DateTimeCheck.parseDateTime(startDateTimeStr);
        end = DateTimeCheck.parseDateTime(endDateTimeStr);
      } else {
        throw new IllegalArgumentException("Invalid command arguments. "
            + "Must specify 'on <date>' or 'from <dateTime> to <dateTime>'.");
      }

      if (weekdaysStr == null) {
        EventSingle.Builder builder = new EventSingle.Builder(subject, start)
            .withEnd(end);
        activeCalendar.createSingleEvent(builder.build());
      } else {
        List<DayOfWeek> days = parseWeekdays(weekdaysStr);
        LocalDate startDate = start.toLocalDate();
        LocalTime startTime = start.toLocalTime();
        LocalTime endTime = end.toLocalTime();

        EventSeries.Builder seriesBuilder = new EventSeries.Builder(
            subject, startDate, startTime, endTime, days);

        if (occurrencesStr != null) {
          int occ = Integer.parseInt(occurrencesStr);
          seriesBuilder.forOccurrences(occ);
        } else if (untilDateStr != null) {
          LocalDate until = DateTimeCheck.parseDate(untilDateStr);
          seriesBuilder.until(until);
        } else {
          throw new IllegalArgumentException(
              "Series must specify 'for <N> times' or 'until <date>'.");
        }
        activeCalendar.createEventSeries(seriesBuilder.build());
      }

      view.displaySuccess("Event created successfully.");

    } catch (IllegalArgumentException
             | DateTimeException
             | NullPointerException
             | IllegalStateException e) {
      view.displayError(e.getMessage());
    }
  }

  private List<DayOfWeek> parseWeekdays(String codes) {
    if (codes == null || codes.trim().isEmpty()) {
      throw new IllegalArgumentException("Day of week codes string cannot be null or empty.");
    }

    EnumSet<DayOfWeek> days = EnumSet.noneOf(DayOfWeek.class);

    for (char code : codes.toUpperCase().toCharArray()) {
      switch (code) {
        case 'M':
          days.add(DayOfWeek.MONDAY);
          break;
        case 'T':
          days.add(DayOfWeek.TUESDAY);
          break;
        case 'W':
          days.add(DayOfWeek.WEDNESDAY);
          break;
        case 'R':
          days.add(DayOfWeek.THURSDAY);
          break;
        case 'F':
          days.add(DayOfWeek.FRIDAY);
          break;
        case 'S':
          days.add(DayOfWeek.SATURDAY);
          break;
        case 'U':
          days.add(DayOfWeek.SUNDAY);
          break;
        default:
          throw new IllegalArgumentException("Invalid day code: '" + code + "'. "
              + "Allowed codes are M, T, W, R, F, S, U.");
      }
    }
    return new ArrayList<>(days);
  }
}