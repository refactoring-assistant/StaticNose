package calendar.view;

import calendar.model.event.Ievent;
import java.time.format.DateTimeFormatter;

/**
 * Utility class for formatting events for display.
 */
public class EventFormatter {

  private static final DateTimeFormatter DATE_FORMATTER =
      DateTimeFormatter.ofPattern("yyyy-MM-dd");
  private static final DateTimeFormatter TIME_FORMATTER =
      DateTimeFormatter.ofPattern("HH:mm");

  /**
   * Format event for display in date range query.
   * Format: * Subject starting on YYYY-MM-DD at HH:mm, ending on YYYY-MM-DD at HH:mm [Location]
   *
   * @param event the event to format
   * @return formatted string
   */
  public static String formatEventForRange(Ievent event) {
    StringBuilder sb = new StringBuilder("* ");  // Changed from bullet to asterisk
    sb.append(event.getSubject());
    sb.append(" starting on ");
    sb.append(event.getStartDateTime().format(DATE_FORMATTER));
    sb.append(" at ");
    sb.append(event.getStartDateTime().format(TIME_FORMATTER));
    sb.append(", ending on ");
    sb.append(event.getEndDateTime().format(DATE_FORMATTER));
    sb.append(" at ");
    sb.append(event.getEndDateTime().format(TIME_FORMATTER));

    if (event.getLocation() != null && !event.getLocation().isEmpty()) {
      sb.append(" [").append(event.getLocation()).append("]");
    }

    return sb.toString();
  }

  /**
   * Format event for display in single date query.
   * Format: * Subject (HH:mm - HH:mm) [Location]
   *
   * @param event the event to format
   * @return formatted string
   */
  public static String formatEventForDate(Ievent event) {
    StringBuilder sb = new StringBuilder("* ");  // Changed from bullet to asterisk
    sb.append(event.getSubject());
    sb.append(" (");
    sb.append(event.getStartDateTime().format(TIME_FORMATTER));
    sb.append(" - ");
    sb.append(event.getEndDateTime().format(TIME_FORMATTER));
    sb.append(")");

    if (event.getLocation() != null && !event.getLocation().isEmpty()) {
      sb.append(" [").append(event.getLocation()).append("]");
    }

    return sb.toString();
  }
}