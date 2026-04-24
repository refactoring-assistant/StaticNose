package calendarcontroller.commands;

import calendarmodel.CalendarModel;
import calendarmodel.Event;
import calendarview.CalendarView;
import java.io.BufferedWriter;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.UUID;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import multicalendarmodel.ZonedCalendarModel;

/**
 * Command to export a zoned calendar to a .ical file.
 *
 * <p>This class parses input matching the pattern
 * {@code export cal filename.ical} or {@code export cal filename.ics}.</p>
 */
public class ExportIcalCommand extends AbstractCalendarCommand {

  private static final Pattern PATTERN = Pattern.compile(
      "export cal (\\S+\\.(?:ical|ics))", REGEX_FLAGS);

  /**
   * {@inheritDoc}
   */
  @Override
  protected boolean matches(String inputLine) {
    return PATTERN.matcher(inputLine).matches();
  }

  /**
   * {@inheritDoc}
   */
  @Override
  protected boolean executeWithModel(String inputLine, CalendarModel model, CalendarView view) {
    Matcher matcher = PATTERN.matcher(inputLine);
    if (!matcher.matches()) {
      return false;
    }
    if (!(model instanceof ZonedCalendarModel)) {
      view.displayError("Error: export cal command is only for zoned calendars.");
      return true;
    }
    ZonedCalendarModel activeCalendar = (ZonedCalendarModel) model;
    String fileName = matcher.group(1);
    try {
      String fileData = generateIcal(activeCalendar);
      Path filePath = Paths.get(fileName);
      try (BufferedWriter writer = Files.newBufferedWriter(filePath)) {
        writer.write(fileData);
      }
      view.displayExportSuccess(filePath.toAbsolutePath().toString());

    } catch (Exception e) {
      view.displayError("Error exporting calendar: " + e.getMessage());
    }
    return true;
  }

  private String generateIcal(ZonedCalendarModel calendar) {
    StringBuilder sb = new StringBuilder();
    sb.append("BEGIN:VCALENDAR\r\n");
    sb.append("VERSION:2.0\r\n");
    sb.append("PRODID:-//YourApp//EN\r\n");
    ZoneId zone = calendar.getZone();
    sb.append("BEGIN:VTIMEZONE\r\n");
    sb.append("TZID:").append(zone.getId()).append("\r\n");
    sb.append("END:VTIMEZONE\r\n");
    List<Event> events = calendar.getAllEvents();
    DateTimeFormatter icalFormatter = DateTimeFormatter.ofPattern("yyyyMMdd'T'HHmmss");
    for (Event event : events) {
      sb.append("BEGIN:VEVENT\r\n");
      sb.append("UID:").append(UUID.randomUUID().toString()).append("\r\n");
      sb.append("DTSTART;TZID=").append(zone.getId()).append(":")
          .append(event.getStartTime().format(icalFormatter)).append("\r\n");
      sb.append("DTEND;TZID=").append(zone.getId()).append(":")
          .append(event.getEndTime().format(icalFormatter)).append("\r\n");
      sb.append("SUMMARY:").append(icalEscape(event.getSubject())).append("\r\n");
      if (event.getDescription() != null) {
        sb.append("DESCRIPTION:").append(icalEscape(event.getDescription())).append("\r\n");
      }
      if (event.getLocation() != null) {
        sb.append("LOCATION:").append(icalEscape(event.getLocation().name())).append("\r\n");
      }
      sb.append("END:VEVENT\r\n");
    }
    sb.append("END:VCALENDAR\r\n");
    return sb.toString();
  }

  private String icalEscape(String text) {
    if (text == null) {
      return "";
    }
    return text.replace("\\", "\\\\")
        .replace(";", "\\;")
        .replace(",", "\\,")
        .replace("\n", "\\n");
  }
}