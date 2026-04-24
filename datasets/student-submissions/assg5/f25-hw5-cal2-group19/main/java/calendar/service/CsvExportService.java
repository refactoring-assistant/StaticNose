package calendar.service;

import calendar.exception.CalendarException;
import calendar.model.InCalendar;
import calendar.model.InEvent;
import calendar.util.CsvUtil;
import java.io.BufferedWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

/**
 * Service for exporting calendar to CSV format.
 * Exports in Google Calendar compatible format.
 */
public class CsvExportService implements InExportService {

  private final InCalendar calendar;

  /**
   * Constructs a CsvExportService with a calendar.
   *
   * @param calendar the calendar to export
   */
  public CsvExportService(InCalendar calendar) {
    if (calendar == null) {
      throw new IllegalArgumentException("Calendar cannot be null");
    }
    this.calendar = calendar;
  }

  @Override
  public Path exportToCsv(Path outputPath) throws CalendarException {
    if (outputPath == null) {
      throw new IllegalArgumentException("Output path cannot be null");
    }

    try {
      if (outputPath.getParent() != null) {
        Files.createDirectories(outputPath.getParent());
      }

      try (BufferedWriter writer = Files.newBufferedWriter(outputPath)) {
        CsvUtil.writeHeader(writer);

        List<InEvent> events = calendar.getAllEvents();
        for (InEvent event : events) {
          CsvUtil.writeEvent(writer, event);
        }
      }

      return outputPath.toAbsolutePath();
    } catch (IOException e) {
      throw new CalendarException("Failed to export calendar to CSV", e);
    }
  }

  @Override
  public Path exportCalendar(InCalendar calendar, Path outputPath) throws CalendarException {
    return null;
  }
}