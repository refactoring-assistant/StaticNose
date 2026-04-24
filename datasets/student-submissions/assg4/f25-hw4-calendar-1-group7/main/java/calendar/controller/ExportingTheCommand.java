package calendar.controller;

import calendar.model.Calendar;
import calendar.view.MyCalendarView;
import java.io.BufferedWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.format.DateTimeFormatter;

/**
 * Command to export calendar to CSV file.
 */
public class ExportingTheCommand implements Command {
  private final String filename;
  private static final DateTimeFormatter DATE_FORMATTER =
      DateTimeFormatter.ofPattern("MM/dd/yyyy");
  private static final DateTimeFormatter TIME_FORMATTER =
      DateTimeFormatter.ofPattern("hh:mm a");

  /**
   * Creates a new ExportCommand.
   *
   * @param filename the output filename
   */
  public ExportingTheCommand(String filename) {
    this.filename = filename;
  }

  @Override
  public void execute(Calendar calendar, MyCalendarView view) {
    try {
      Path path = Paths.get(filename);

      try (BufferedWriter writer = Files.newBufferedWriter(path)) {
        String header = "Subject,Start Date,Start Time,End Date,End Time,All Day Event,"
            + "Description,Location,Private\n";
        writer.write(header);

        calendar.getAllEvents().forEach(event -> {
          try {
            String startDate = event.getStart().format(DATE_FORMATTER);
            String startTime = event.getStart().format(TIME_FORMATTER);
            String endDate = event.getEnd().format(DATE_FORMATTER);
            String endTime = event.getEnd().format(TIME_FORMATTER);
            String allDay = event.isAllDay() ? "True" : "False";
            String description = event.getDescription() != null
                ? event.getDescription() : "";
            String location = event.getLocation() != null
                ? event.getLocation() : "";
            String isPrivate = "private".equalsIgnoreCase(event.getStatus())
                ? "True" : "False";

            writer.write(String.format("%s,%s,%s,%s,%s,%s,%s,%s,%s\n",
                escapeCsv(event.getSubject()),
                startDate,
                startTime,
                endDate,
                endTime,
                allDay,
                escapeCsv(description),
                escapeCsv(location),
                isPrivate));
          } catch (IOException e) {
            throw new RuntimeException(e);
          }
        });
      }

      String absolutePath = path.toAbsolutePath().toString();
      view.displayMessage("Calendar exported to: " + absolutePath);

    } catch (IOException e) {
      view.displayError("Error exporting calendar: " + e.getMessage());
    }
  }

  /**
   * Escapes special characters for CSV format.
   *
   * @param value the value to escape
   * @return the escaped value
   */
  private String escapeCsv(String value) {
    if (value == null) {
      return "";
    }
    if (value.contains(",") || value.contains("\"") || value.contains("\n")) {
      return "\"" + value.replace("\"", "\"\"") + "\"";
    }
    return value;
  }

  @Override
  public boolean validate() {
    return filename != null && !filename.trim().isEmpty();
  }
}