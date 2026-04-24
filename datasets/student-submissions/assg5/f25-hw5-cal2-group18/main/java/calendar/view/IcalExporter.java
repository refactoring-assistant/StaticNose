package calendar.view;

import calendar.model.Event;
import calendar.model.EventStatus;
import java.io.FileWriter;
import java.io.IOException;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Utility class for exporting calendar events to iCal format.
 */
public class IcalExporter {

  private static final DateTimeFormatter ICAL_DATE_TIME_FORMAT =
      DateTimeFormatter.ofPattern("yyyyMMdd'T'HHmmss");

  /**
   * Exports events to an iCal file.
   *
   * @param events   the events to export
   * @param filePath the output file path
   * @param timezone the calendar timezone
   * @throws IOException if writing fails
   */
  public void exportEvents(List<Event> events, String filePath, ZoneId timezone)
      throws IOException {
    try (FileWriter writer = new FileWriter(filePath)) {
      writer.write("BEGIN:VCALENDAR\r\n");
      writer.write("VERSION:2.0\r\n");
      writer.write("PRODID:-//Calendar App//EN\r\n");
      writer.write("CALSCALE:GREGORIAN\r\n");
      writer.write("METHOD:PUBLISH\r\n");

      Map<String, String> seriesUidMap = new HashMap<>();

      for (Event event : events) {
        writeEvent(writer, event, timezone, seriesUidMap);
      }

      writer.write("END:VCALENDAR\r\n");
    }
  }

  private void writeEvent(FileWriter writer, Event event, ZoneId timezone,
                          Map<String, String> seriesUidMap) throws IOException {
    writer.write("BEGIN:VEVENT\r\n");

    String uid = generateUid(event, seriesUidMap);
    writer.write("UID:" + uid + "\r\n");

    String timestamp = ZonedDateTime.now(timezone).format(ICAL_DATE_TIME_FORMAT);
    writer.write("DTSTAMP:" + timestamp + "\r\n");

    String start = event.getStartDateTime().format(ICAL_DATE_TIME_FORMAT);
    writer.write("DTSTART:" + start + "\r\n");

    String end = event.getEndDateTime().format(ICAL_DATE_TIME_FORMAT);
    writer.write("DTEND:" + end + "\r\n");

    writer.write("SUMMARY:" + escapeText(event.getSubject()) + "\r\n");

    if (event.getDescription() != null && !event.getDescription().isEmpty()) {
      writer.write("DESCRIPTION:" + escapeText(event.getDescription()) + "\r\n");
    }

    if (event.getLocation() != null && !event.getLocation().isEmpty()) {
      writer.write("LOCATION:" + escapeText(event.getLocation()) + "\r\n");
    }

    String classification = event.getStatus() == EventStatus.PRIVATE ? "PRIVATE" : "PUBLIC";
    writer.write("CLASS:" + classification + "\r\n");

    writer.write("STATUS:CONFIRMED\r\n");
    writer.write("TRANSP:OPAQUE\r\n");

    writer.write("END:VEVENT\r\n");
  }

  /**
   * Generates a unique identifier for an event.
   * Events in the same series will share the same UID.
   */
  private String generateUid(Event event, Map<String, String> seriesUidMap) {
    String baseUid;

    if (event.getSeriesId() != null) {
      if (seriesUidMap.containsKey(event.getSeriesId())) {
        baseUid = seriesUidMap.get(event.getSeriesId());
      } else {
        baseUid = generateBaseUid(event.getSubject(), event.getSeriesId());
        seriesUidMap.put(event.getSeriesId(), baseUid);
      }
      String suffix =
          event.getStartDateTime().format(DateTimeFormatter.ofPattern("yyyyMMddHHmmss"));
      return baseUid + "-" + suffix + "@calendarapp.com";
    }
    return generateBaseUid(event.getSubject(), event.getStartDateTime().toString())
        + "@calendarapp.com";
  }

  /**
   * Generates a base UID without suffix, used for a series.
   */
  private String generateBaseUid(String subject, String uniquePart) {
    String base = subject + "-" + uniquePart;
    return base.replaceAll("[^a-zA-Z0-9-]", "");
  }

  /**
   * Escapes special characters in iCal text fields.
   */
  private String escapeText(String text) {
    if (text == null) {
      return "";
    }
    return text.replace("\\", "\\\\")
        .replace(";", "\\;")
        .replace(",", "\\,")
        .replace("\n", "\\n")
        .replace("\r", "");
  }
}