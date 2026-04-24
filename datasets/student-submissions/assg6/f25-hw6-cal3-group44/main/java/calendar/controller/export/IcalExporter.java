package calendar.controller.export;

import calendar.model.interfaces.EventReadOnly;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeFormatterBuilder;
import java.util.List;
import java.util.Map;

/**
 * Exports events to .ical (iCalendar) format.
 */
public class IcalExporter implements FileExporter {

  @Override
  public String export(String fileName, Map<LocalDate, List<EventReadOnly>> events) {


    File file = new File(fileName);

    DateTimeFormatter dateTimeFormatter = new DateTimeFormatterBuilder()
        .appendPattern("yyyyMMdd'T'HHmmss")
        .toFormatter();

    try (FileWriter fileWriter = new FileWriter(file)) {

      fileWriter.write("BEGIN:VCALENDAR\n");
      fileWriter.write("VERSION:2.0\n");
      fileWriter.write("PRODID:-//MyCalendarApp//EN\n");

      for (Map.Entry<LocalDate, List<EventReadOnly>> entry : events.entrySet()) {
        for (EventReadOnly event : entry.getValue()) {

          fileWriter.write("BEGIN:VEVENT\n");
          fileWriter.write("SUMMARY:" + event.getSubject() + "\n");
          fileWriter.write("DTSTART:" + event.getStartDateTime().format(dateTimeFormatter) + "\n");
          fileWriter.write("DTEND:" + event.getEndDateTime().format(dateTimeFormatter) + "\n");
          fileWriter.write("END:VEVENT\n");
        }
      }

      fileWriter.write("END:VCALENDAR\n");

      return file.getAbsolutePath();

    } catch (IOException e) {
      throw new RuntimeException("Error writing to file: " + fileName, e);
    }
  }
}
