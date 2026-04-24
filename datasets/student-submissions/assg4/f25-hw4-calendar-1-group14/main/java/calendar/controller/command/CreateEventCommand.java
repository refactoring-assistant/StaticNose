package calendar.controller.command;

import calendar.model.calendar.Icalendar;
import calendar.model.datetime.DayOfWeek;
import calendar.model.event.Event;
import calendar.model.event.EventSeries;
import calendar.model.event.Ievent;
import calendar.view.IcalendarView;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * Command to create events in the calendar.
 * Supports single events with start and end time or all-day events,
 * and recurring events by number of times or until date.
 */
public class CreateEventCommand implements Icommand {

  private final String subject;
  private final LocalDateTime startDateTime;
  private final LocalDateTime endDateTime;
  private final LocalDate allDayDate;

  private final boolean isRecurring;
  private final Set<String> weekdayStrings;
  private final Integer repeatTimes;
  private final LocalDate repeatUntilDate;

  /**
   * Constructor for single event with times.
   *
   * @param subject the event subject
   * @param start the start date and time
   * @param end the end date and time
   */
  public CreateEventCommand(String subject, LocalDateTime start, LocalDateTime end) {
    this.subject = subject;
    this.startDateTime = start;
    this.endDateTime = end;
    this.allDayDate = null;
    this.isRecurring = false;
    this.weekdayStrings = null;
    this.repeatTimes = null;
    this.repeatUntilDate = null;
  }

  /**
   * Constructor for all-day single event.
   *
   * @param subject the event subject
   * @param date the date of the all-day event
   */
  public CreateEventCommand(String subject, LocalDate date) {
    this.subject = subject;
    this.allDayDate = date;
    this.startDateTime = null;
    this.endDateTime = null;
    this.isRecurring = false;
    this.weekdayStrings = null;
    this.repeatTimes = null;
    this.repeatUntilDate = null;
  }

  /**
   * Constructor for recurring event with times for a specific number of times.
   *
   * @param subject the event subject
   * @param start the start date and time
   * @param end the end date and time
   * @param weekdays the set of weekday strings (M, T, W, R, F, S, U)
   * @param repeatTimes the number of times to repeat
   */
  public CreateEventCommand(String subject, LocalDateTime start, LocalDateTime end,
                            Set<String> weekdays, int repeatTimes) {
    this.subject = subject;
    this.startDateTime = start;
    this.endDateTime = end;
    this.allDayDate = null;
    this.isRecurring = true;
    this.weekdayStrings = weekdays;
    this.repeatTimes = repeatTimes;
    this.repeatUntilDate = null;
  }

  /**
   * Constructor for recurring event with times until a specific date.
   *
   * @param subject the event subject
   * @param start the start date and time
   * @param end the end date and time
   * @param weekdays the set of weekday strings (M, T, W, R, F, S, U)
   * @param untilDate the end date for recurrence
   */
  public CreateEventCommand(String subject, LocalDateTime start, LocalDateTime end,
                            Set<String> weekdays, LocalDate untilDate) {
    this.subject = subject;
    this.startDateTime = start;
    this.endDateTime = end;
    this.allDayDate = null;
    this.isRecurring = true;
    this.weekdayStrings = weekdays;
    this.repeatTimes = null;
    this.repeatUntilDate = untilDate;
  }

  /**
   * Constructor for recurring all-day event for a specific number of times.
   *
   * @param subject the event subject
   * @param date the date of the first occurrence
   * @param weekdays the set of weekday strings (M, T, W, R, F, S, U)
   * @param repeatTimes the number of times to repeat
   */
  public CreateEventCommand(String subject, LocalDate date, Set<String> weekdays, int repeatTimes) {
    this.subject = subject;
    this.allDayDate = date;
    this.startDateTime = null;
    this.endDateTime = null;
    this.isRecurring = true;
    this.weekdayStrings = weekdays;
    this.repeatTimes = repeatTimes;
    this.repeatUntilDate = null;
  }

  /**
   * Constructor for recurring all-day event until a specific date.
   *
   * @param subject the event subject
   * @param date the date of the first occurrence
   * @param weekdays the set of weekday strings (M, T, W, R, F, S, U)
   * @param untilDate the end date for recurrence
   */
  public CreateEventCommand(String subject, LocalDate date,
                            Set<String> weekdays, LocalDate untilDate) {
    this.subject = subject;
    this.allDayDate = date;
    this.startDateTime = null;
    this.endDateTime = null;
    this.isRecurring = true;
    this.weekdayStrings = weekdays;
    this.repeatTimes = null;
    this.repeatUntilDate = untilDate;
  }

  /**
   * Executes the create event command.
   *
   * @param calendar the calendar to add the event to
   * @param view the view to display messages to
   * @throws Exception if the event cannot be created
   */
  @Override
  public void execute(Icalendar calendar, IcalendarView view) throws Exception {
    try {
      if (!isRecurring) {
        Ievent event;
        if (allDayDate != null) {
          event = Event.allDayEvent(subject, allDayDate);
        } else {
          event = new Event(subject, startDateTime, endDateTime);
        }

        calendar.addEvent(event);
        view.displayMessage("Event created successfully.");

      } else {
        Set<DayOfWeek> weekdays = weekdayStrings.stream()
            .map(this::convertToDayOfWeek)
            .collect(Collectors.toSet());

        LocalDateTime firstStart;
        LocalDateTime firstEnd;

        if (allDayDate != null) {
          firstStart = allDayDate.atTime(8, 0);
          firstEnd = allDayDate.atTime(17, 0);
        } else {
          firstStart = startDateTime;
          firstEnd = endDateTime;
        }

        EventSeries series = new EventSeries(
            subject,
            firstStart,
            firstEnd,
            weekdays,
            null,
            null,
            true
        );

        if (repeatTimes != null) {
          series.generateOccurrences(repeatTimes);
        } else {
          series.generateUntilDate(repeatUntilDate);
        }

        calendar.addEventSeries(series);
        view.displayMessage("Event series created successfully.");
      }

    } catch (IllegalStateException e) {
      view.displayError("Cannot create event: " + e.getMessage());
      throw e;
    } catch (IllegalArgumentException e) {
      view.displayError("Cannot create event: " + e.getMessage());
      throw e;
    }
  }

  /**
   * Convert single character weekday string to DayOfWeek enum.
   *
   * @param dayChar the weekday character (M, T, W, R, F, S, U)
   * @return the corresponding DayOfWeek enum value
   * @throws IllegalArgumentException if the weekday character is invalid
   */
  private DayOfWeek convertToDayOfWeek(String dayChar) {
    switch (dayChar.toUpperCase()) {
      case "M": return DayOfWeek.MONDAY;
      case "T": return DayOfWeek.TUESDAY;
      case "W": return DayOfWeek.WEDNESDAY;
      case "R": return DayOfWeek.THURSDAY;
      case "F": return DayOfWeek.FRIDAY;
      case "S": return DayOfWeek.SATURDAY;
      case "U": return DayOfWeek.SUNDAY;
      default: throw new IllegalArgumentException("Invalid weekday: " + dayChar);
    }
  }
}