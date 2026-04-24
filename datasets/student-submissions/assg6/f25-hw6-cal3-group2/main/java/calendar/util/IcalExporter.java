package calendar.util;

import calendar.event.Event;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.UUID;

/**
 * A utility class for exporting calendar events to iCal format.
 */
public class IcalExporter {
  private static final DateTimeFormatter ICAL_DATE_TIME_FORMAT =
      DateTimeFormatter.ofPattern("yyyyMMdd'T'HHmmss");
  private static final DateTimeFormatter ICAL_DATE_FORMAT =
      DateTimeFormatter.ofPattern("yyyyMMdd");

  /**
   * Exports a list of events to iCal format.
   *
   * @param events the list of events to export
   * @param calendarName the name of the calendar
   * @param timezone the timezone of the calendar
   * @return the iCal formatted string
   */
  public static String exportToIcal(List<Event> events, String calendarName, ZoneId timezone) {
    StringBuilder ical = new StringBuilder();

    ical.append("BEGIN:VCALENDAR\r\n");
    ical.append("VERSION:2.0\r\n");
    ical.append("PRODID:-//Calendar App//EN\r\n");
    for (Event event : events) {
      ical.append(formatEventAsIcal(event, timezone));
    }
    ical.append("END:VCALENDAR\r\n");
    return ical.toString();
  }

  /**
   * Formats a single event as an iCal VEVENT component.
   *
   * @param event the event to format
   * @param timezone the timezone of the calendar
   * @return the VEVENT component string
   */
  private static String formatEventAsIcal(Event event, ZoneId timezone) {
    StringBuilder vevent = new StringBuilder();
    vevent.append("BEGIN:VEVENT\r\n");
    String uid = UUID.randomUUID().toString();
    vevent.append("UID:").append(uid).append("\r\n");
    // time zone convert
    ZonedDateTime nowUtc = ZonedDateTime.now(ZoneId.of("UTC"));
    vevent.append("DTSTAMP:").append(nowUtc.format(ICAL_DATE_TIME_FORMAT)).append("Z\r\n");
    ZonedDateTime startInTimezone = event.getStartDateTime().atZone(timezone);
    ZonedDateTime startUtc = startInTimezone.withZoneSameInstant(ZoneId.of("UTC"));

    if (event.isAllDayEvent()) {
      vevent.append("DTSTART;VALUE=DATE:")
          .append(startInTimezone.format(ICAL_DATE_FORMAT))
          .append("\r\n");
    } else {
      vevent.append("DTSTART:")
          .append(startUtc.format(ICAL_DATE_TIME_FORMAT))
          .append("Z\r\n");
    }

    if (event.getEndDateTime() != null) {
      ZonedDateTime endInTimezone = event.getEndDateTime().atZone(timezone);
      ZonedDateTime endUtc = endInTimezone.withZoneSameInstant(ZoneId.of("UTC"));

      if (event.isAllDayEvent()) {
        vevent.append("DTEND;VALUE=DATE:")
            // RFC 5545
            .append(endInTimezone.plusDays(1).format(ICAL_DATE_FORMAT))
            .append("\r\n");
      } else {
        vevent.append("DTEND:")
            .append(endUtc.format(ICAL_DATE_TIME_FORMAT))
            .append("Z\r\n");
      }
    }
    vevent.append("SUMMARY:").append(escapeText(event.getSubject())).append("\r\n");

    if (event.getDescription() != null && !event.getDescription().isEmpty()) {
      vevent.append("DESCRIPTION:").append(escapeText(event.getDescription())).append("\r\n");
    }

    if (event.getLocation() != null && !event.getLocation().isEmpty()) {
      vevent.append("LOCATION:").append(escapeText(event.getLocation())).append("\r\n");
    }

    String classValue = event.getEventStatus().toString().equalsIgnoreCase("PRIVATE")
        ? "PRIVATE" : "PUBLIC";
    vevent.append("CLASS:").append(classValue).append("\r\n");

    vevent.append("END:VEVENT\r\n");
    return vevent.toString();
  }

  /**
   * Escapes special characters in text for iCal format(\,;\n).
   *
   * @param text the text to escape
   * @return the escaped text
   */
  private static String escapeText(String text) {
    return text.replace("\\", "\\\\")
        .replace(",", "\\,")
        .replace(";", "\\;")
        .replace("\n", "\\n")
        .replace("\r", "");
  }
}