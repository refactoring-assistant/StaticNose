package calendarcontroller.commands;

import calendarmodel.CalendarModel;
import calendarmodel.Event;
import calendarview.CalendarView;
import java.io.BufferedWriter;
import java.io.StringWriter;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import multicalendarmodel.ZonedCalendarModel;

/**
 * Command to export a zoned calendar to a .csv file.
 *
 * <p>This class parses input matching the pattern
 * {@code export cal filename.csv}.</p>
 */
public class ExportCsvCommand extends AbstractCalendarCommand {

  private static final Pattern PATTERN = Pattern.compile(
      "export cal (\\S+\\.csv)", REGEX_FLAGS);

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
      String fileData = generateCsv(activeCalendar);
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

  private String generateCsv(ZonedCalendarModel calendar) {
    List<Event> events = calendar.getAllEvents();
    StringWriter sw = new StringWriter();
    sw.append("Subject,Start Date,Start Time,End Date,End Time,All Day Event,"
        + "Description,Location,Private" + System.lineSeparator());
    DateTimeFormatter dateFormatter = DateTimeFormatter.ofPattern("MM/dd/yyyy");
    DateTimeFormatter timeFormatter = DateTimeFormatter.ofPattern("hh:mm a");
    for (Event e : events) {
      boolean isAllDay = e.getStartTime().toLocalTime().equals(LocalTime.of(8, 0))
          && e.getEndTime().toLocalTime().equals(LocalTime.of(17, 0))
          && e.getStartTime().toLocalDate().equals(e.getEndTime().toLocalDate());
      boolean isPrivate = "Private".equalsIgnoreCase(e.getStatus());
      String locationStr = (e.getLocation() == null) ? "" : e.getLocation().name();
      sw.append(csvEscape(e.getSubject())).append(",")
          .append(e.getStartTime().format(dateFormatter)).append(",")
          .append(e.getStartTime().format(timeFormatter)).append(",")
          .append(e.getEndTime().format(dateFormatter)).append(",")
          .append(e.getEndTime().format(timeFormatter)).append(",")
          .append(isAllDay ? "True" : "False").append(",")
          .append(csvEscape(e.getDescription())).append(",")
          .append(csvEscape(locationStr)).append(",")
          .append(isPrivate ? "True" : "False").append(System.lineSeparator());
    }
    return sw.toString();
  }

  private String csvEscape(String data) {
    if (data == null) {
      return "";
    }
    if (data.contains(",") || data.contains("\"") || data.contains("\n")) {
      return "\"" + data.replace("\"", "\"\"") + "\"";
    }
    return data;
  }
}