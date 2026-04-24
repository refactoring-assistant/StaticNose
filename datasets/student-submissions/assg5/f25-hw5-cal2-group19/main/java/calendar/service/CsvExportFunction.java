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
import java.util.Objects;

/**
 * Strategy for exporting calendar to CSV format.
 * Compatible with Google Calendar import format.
 */
public class CsvExportFunction implements InExportFunction {

  @Override
  public Path export(InCalendar calendar, Path outputPath) throws CalendarException {
    Objects.requireNonNull(calendar, "Calendar cannot be null");
    Objects.requireNonNull(outputPath, "Output path cannot be null");

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
      throw new CalendarException("Failed to export calendar to CSV format", e);
    }
  }

  @Override
  public String getFileExtension() {
    return "csv";
  }
}