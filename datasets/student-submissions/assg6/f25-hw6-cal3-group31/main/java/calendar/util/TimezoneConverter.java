package calendar.util;

import calendar.model.Date;
import calendar.model.Time;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;

/**
 * Utility class for converting dates and times between different timezones.
 * Used when copying events between calendars with different timezones.
 */
public class TimezoneConverter {

  /**
   * Private constructor to prevent instantiation of utility class.
   */
  private TimezoneConverter() {
    throw new UnsupportedOperationException("Utility class cannot be instantiated");
  }

  /**
   * Converts a date and time from one timezone to another.
   *
   * @param date     the date to convert
   * @param time     the time to convert
   * @param fromZone the source timezone
   * @param toZone   the target timezone
   * @return a DateTimePair containing the converted date and time
   */
  public static DateTimePair convert(Date date, Time time, ZoneId fromZone, ZoneId toZone) {
    if (date == null || time == null) {
      throw new IllegalArgumentException("Date and time cannot be null");
    }
    if (fromZone == null || toZone == null) {
      throw new IllegalArgumentException("Timezones cannot be null");
    }

    // Convert custom Date and Time to LocalDateTime
    LocalDateTime localDateTime = LocalDateTime.of(
        date.getYear(),
        date.getMonth(),
        date.getDay(),
        time.getHour(),
        time.getMinute()
    );

    // Create ZonedDateTime in source timezone
    ZonedDateTime sourceTime = ZonedDateTime.of(localDateTime, fromZone);

    // Convert to target timezone
    ZonedDateTime targetTime = sourceTime.withZoneSameInstant(toZone);

    // Convert back to custom Date and Time
    return toDateAndTime(targetTime);
  }

  /**
   * Converts a ZonedDateTime to a DateTimePair containing custom Date and Time objects.
   *
   * @param zonedDateTime the ZonedDateTime to convert
   * @return a DateTimePair containing the converted date and time
   */
  public static DateTimePair toDateAndTime(ZonedDateTime zonedDateTime) {
    if (zonedDateTime == null) {
      throw new IllegalArgumentException("ZonedDateTime cannot be null");
    }

    Date date = new Date(
        zonedDateTime.getYear(),
        zonedDateTime.getMonthValue(),
        zonedDateTime.getDayOfMonth()
    );

    Time time = new Time(
        zonedDateTime.getHour(),
        zonedDateTime.getMinute()
    );

    return new DateTimePair(date, time);
  }

  /**
   * Converts a custom Date and Time to a ZonedDateTime in the specified timezone.
   *
   * @param date     the date to convert
   * @param time     the time to convert
   * @param timezone the timezone
   * @return a ZonedDateTime representing the date and time in the specified timezone
   */
  public static ZonedDateTime toZonedDateTime(Date date, Time time, ZoneId timezone) {
    if (date == null || time == null) {
      throw new IllegalArgumentException("Date and time cannot be null");
    }
    if (timezone == null) {
      throw new IllegalArgumentException("Timezone cannot be null");
    }

    LocalDateTime localDateTime = LocalDateTime.of(
        date.getYear(),
        date.getMonth(),
        date.getDay(),
        time.getHour(),
        time.getMinute()
    );

    return ZonedDateTime.of(localDateTime, timezone);
  }

  /**
   * Simple container class for holding a Date and Time pair.
   */
  public static class DateTimePair {
    private final Date date;
    private final Time time;

    /**
     * Constructs a DateTimePair.
     *
     * @param date the date
     * @param time the time
     */
    public DateTimePair(Date date, Time time) {
      this.date = date;
      this.time = time;
    }

    /**
     * Gets the date.
     *
     * @return the date
     */
    public Date getDate() {
      return date;
    }

    /**
     * Gets the time.
     *
     * @return the time
     */
    public Time getTime() {
      return time;
    }
  }
}

