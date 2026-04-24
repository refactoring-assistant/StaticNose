package controller.command;

import controller.CommandResult;
import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Set;
import model.EventSeriesManager;
import model.Icalendar;

/**
 * Command implementation for creating recurring event series in the calendar.
 * Supports both occurrence-based and date-based recurrence patterns.
 */
public class CreateSeriesCommand implements Command {
  private final String subject;
  private final LocalDateTime startDateTime;
  private final LocalDateTime endDateTime;
  private final String weekdays;
  private final int occurrenceCount;
  private final LocalDate untilDate;

  /**
   * Private constructor - instances are created through the Builder pattern.
   *
   * @param builder the builder containing series properties
   */
  private CreateSeriesCommand(Builder builder) {
    this.subject = builder.subject;
    this.startDateTime = builder.startDateTime;
    this.endDateTime = builder.endDateTime;
    this.weekdays = builder.weekdays;
    this.occurrenceCount = builder.occurrenceCount;
    this.untilDate = builder.untilDate;
  }

  /**
   * Creates a new Builder instance for constructing CreateSeriesCommand objects.
   *
   * @return a new Builder instance
   */
  public static Builder builder() {
    return new Builder();
  }

  /**
   * Parses weekday characters into DayOfWeek enum values.
   *
   * @param weekdays string containing weekday characters (M,T,W,R,F,S,U)
   * @return a set of DayOfWeek values
   */
  private Set<DayOfWeek> parseWeekdays(String weekdays) {
    Set<DayOfWeek> days = new HashSet<>();
    for (char c : weekdays.toCharArray()) {
      switch (c) {
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
          break;
      }
    }
    return days;
  }

  @Override
  public CommandResult execute(Icalendar calendar) {
    try {
      EventSeriesManager.Builder builder = EventSeriesManager.getBuilder()
          .subject(subject)
          .startDateTime(startDateTime)
          .endDateTime(endDateTime)
          .repeatDays(parseWeekdays(weekdays));

      if (occurrenceCount > 0) {
        builder.occurrences(occurrenceCount);
      } else if (untilDate != null) {
        builder.untilDate(untilDate);
      }

      EventSeriesManager series = builder.build();
      calendar.addEventSeries(series);

      String message;
      if (occurrenceCount > 0) {
        message = String.format("Event series created: %s (%d occurrences)",
            subject, occurrenceCount);
      } else {
        message = String.format("Event series created: %s (until %s)",
            subject, untilDate);
      }

      return new CommandResult(true, message);
    } catch (Exception e) {
      return new CommandResult(false, "Error creating series: " + e.getMessage());
    }
  }

  /**
   * Builder class for constructing CreateSeriesCommand instances.
   * Provides validation for mutually exclusive recurrence patterns.
   */
  public static class Builder {
    private String subject;
    private LocalDateTime startDateTime;
    private LocalDateTime endDateTime;
    private String weekdays;
    private int occurrenceCount = 0;
    private LocalDate untilDate = null;

    private Builder() {
    }

    /**
     * Sets the event subject.
     *
     * @param subject the event subject (required)
     * @return this builder for method chaining
     */
    public Builder subject(String subject) {
      this.subject = subject;
      return this;
    }

    /**
     * Sets the event start date and time.
     *
     * @param startDateTime the start date/time (required)
     * @return this builder for method chaining
     */
    public Builder startDateTime(LocalDateTime startDateTime) {
      this.startDateTime = startDateTime;
      return this;
    }

    /**
     * Sets the event end date and time.
     *
     * @param endDateTime the end date/time (required)
     * @return this builder for method chaining
     */
    public Builder endDateTime(LocalDateTime endDateTime) {
      this.endDateTime = endDateTime;
      return this;
    }

    /**
     * Sets the weekdays on which the recurring event should occur.
     *
     * @param weekdays the weekdays on which the event repeats
     * @return this Builder instance for method chaining
     */
    public Builder weekdays(String weekdays) {
      this.weekdays = weekdays;
      return this;
    }

    /**
     * Sets the number of times the recurring event should occur.
     *
     * @param occurrenceCount the total number of occurrences for the event
     * @return this Builder instance for method chaining
     */
    public Builder occurrenceCount(int occurrenceCount) {
      this.occurrenceCount = occurrenceCount;
      return this;
    }

    /**
     * Sets the end date for the recurring event.
     *
     * @param untilDate the date on which the recurrence should stop
     * @return this Builder instance for method chaining
     */
    public Builder untilDate(LocalDate untilDate) {
      this.untilDate = untilDate;
      return this;
    }

    /**
     * Builds the CreateSeriesCommand with validation.
     *
     * @return a new command instance
     * @throws IllegalArgumentException if validation fails
     */
    public CreateSeriesCommand build() {
      if (subject == null || subject.trim().isEmpty()) {
        throw new IllegalArgumentException("Subject is required");
      }
      if (startDateTime == null) {
        throw new IllegalArgumentException("Start date/time is required");
      }
      if (endDateTime == null) {
        throw new IllegalArgumentException("End date/time is required");
      }
      if (!startDateTime.toLocalDate().equals(endDateTime.toLocalDate())) {
        throw new IllegalArgumentException("Series events must start and end on the same day");
      }
      if (weekdays == null || weekdays.isEmpty()) {
        throw new IllegalArgumentException("Weekdays are required");
      }
      if (occurrenceCount <= 0 && untilDate == null) {
        throw new IllegalArgumentException("Either occurrence count or until date is required");
      }
      if (occurrenceCount > 0 && untilDate != null) {
        throw new IllegalArgumentException("Cannot specify both occurrence count and until date");
      }

      return new CreateSeriesCommand(this);
    }
  }

}