package model;

import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.Locale;

/**
 * Constants used throughout the calendar application.
 * Centralizes magic values and format strings.
 */
public final class CalendarConstants {
  public static final LocalTime ALL_DAY_START = LocalTime.of(8, 0);
  public static final LocalTime ALL_DAY_END = LocalTime.of(17, 0);

  public static final String CSV_HEADER =
      "Subject,Start Date,Start Time,End Date,End Time,All Day Event,Description,Location\n";
  public static final String CSV_DATE_FORMAT = "MM/dd/yyyy";
  public static final String CSV_TIME_FORMAT = "hh:mm a";

  public static final String DATETIME_FORMAT_STRING = "yyyy-MM-dd'T'HH:mm";
  public static final String DATE_FORMAT_STRING = "yyyy-MM-dd";
  public static final String DISPLAY_TIME_FORMAT = "h:mm a";

  public static final DateTimeFormatter CSV_DATE_FORMATTER =
      DateTimeFormatter.ofPattern(CSV_DATE_FORMAT);
  public static final DateTimeFormatter CSV_TIME_FORMATTER =
      DateTimeFormatter.ofPattern(CSV_TIME_FORMAT, Locale.US);
  public static final DateTimeFormatter DATETIME_FORMATTER =
      DateTimeFormatter.ofPattern(DATETIME_FORMAT_STRING);
  public static final DateTimeFormatter DATE_FORMATTER =
      DateTimeFormatter.ofPattern(DATE_FORMAT_STRING);

  public static final String PROMPT = "calendar> ";

  private CalendarConstants() {
  }
}