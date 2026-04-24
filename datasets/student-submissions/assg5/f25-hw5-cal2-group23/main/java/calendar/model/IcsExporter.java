package calendar.model;

import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.UUID;

/** Class to export calendar events to ICalendar format. */

public class IcsExporter {
  private static final DateTimeFormatter ICS_DATE_TIME =
      DateTimeFormatter.ofPattern("yyyyMMdd'T'HHmmss");

  /** Converts list of events into single iCalendar formatted string. */

  public static String exportToIcal(List<Event> events, String calendarName, ZoneId zoneId) {
    StringBuilder sb = new StringBuilder();

    // VCALENDAR Header
    sb.append("BEGIN:VCALENDAR\r\n");
    sb.append("VERSION:2.0\r\n");
    sb.append("PRODID:-//COMP5010 Calendar App//NONSGML v1.0//EN\r\n");
    sb.append("X-WR-CALNAME:").append(calendarName).append("\r\n");

    // Timezone Definition (VTIMEZONE)
    sb.append("X-WR-TIMEZONE:").append(zoneId.getId()).append("\r\n");

    // VEVENT Bodies
    for (Event e : events) {
      sb.append(formatEventForIcs(e, zoneId));
    }

    // VCALENDAR Footer
    sb.append("END:VCALENDAR\r\n");

    return sb.toString();
  }

  /** Formats a single event into a VEVENT block in ICS format.. */

  private static String formatEventForIcs(Event e, ZoneId zoneId) {
    StringBuilder sb = new StringBuilder();

    ZonedDateTime startZdt = ZonedDateTime.of(e.getStartTime(), zoneId);

    sb.append("BEGIN:VEVENT\r\n");

    String uid = e.getSeriesId() != null
        ? e.getSeriesId() + "@" + startZdt.toInstant().toEpochMilli()
        : UUID.randomUUID().toString() + "@comp5010.com";
    sb.append("UID:").append(uid).append("\r\n");

    sb.append("DTSTAMP:").append(ZonedDateTime.now(ZoneId.of("UTC"))
        .format(ICS_DATE_TIME)).append("Z\r\n");

    sb.append("SUMMARY:").append(e.getEventName()).append("\r\n");

    if (e.getNotes() != null && !e.getNotes().isEmpty()) {
      sb.append("DESCRIPTION:").append(e.getNotes()).append("\r\n");
    }

    if (e.getLocation() != null && !e.getLocation().isEmpty()) {
      sb.append("LOCATION:").append(e.getLocation()).append("\r\n");
    }

    if (e.getStatus() != null && e.getStatus().equalsIgnoreCase("private")) {
      sb.append("CLASS:PRIVATE\r\n");
    } else {
      sb.append("CLASS:PUBLIC\r\n");
    }

    ZonedDateTime endZdt = ZonedDateTime.of(e.getActualEndTime(), zoneId);
    if (e.isAllDay()) {
      DateTimeFormatter date = DateTimeFormatter.ofPattern("yyyyMMdd");
      sb.append("DTSTART;VALUE=DATE:").append(startZdt.format(date)).append("\r\n");
      sb.append("DTEND;VALUE=DATE:").append(endZdt.toLocalDate().plusDays(1)
          .format(date)).append("\r\n");
    } else {
      sb.append("DTSTART;TZID=").append(zoneId.getId()).append(":").append(startZdt
          .format(ICS_DATE_TIME)).append("\r\n");
      sb.append("DTEND;TZID=").append(zoneId.getId()).append(":").append(endZdt
          .format(ICS_DATE_TIME)).append("\r\n");
    }

    sb.append("END:VEVENT\r\n");
    return sb.toString();
  }
}
