package calendar.view;

import calendar.enums.UserStatus;
import calendar.event.Event;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

/**
 * Represents the view component for the calendar application.
 */
public class CalendarView implements CalendarViewInterface {
  private static final DateTimeFormatter DATE_FORMATTER =
      DateTimeFormatter.ofPattern("yyyy-MM-dd");
  private static final DateTimeFormatter TIME_FORMATTER =
      DateTimeFormatter.ofPattern("h:mm a");

  private final Appendable output;

  /**
   * Constructor with custom output destination.
   *
   * @param output the destination for all output
   * @throws IllegalArgumentException if the output destination is null
   */
  public CalendarView(Appendable output) throws IllegalArgumentException {
    if (output == null) {
      throw new IllegalArgumentException("Output cannot be null");
    }
    this.output = output;
  }

  /**
   * Default constructor using System.out.
   */
  public CalendarView() {
    this(System.out);
  }

  @Override
  public void displayEventsOnDate(List<Event> events, LocalDate date) {
    try {
      if (events == null || events.isEmpty()) {
        output.append("No events on ").append(DATE_FORMATTER.format(date)).append("\n");
        return;
      }

      output.append("Events on ").append(DATE_FORMATTER.format(date)).append(":\n");
      for (Event event : events) {
        output.append(" • ").append(event.getSubject());
        output.append(" from ").append(TIME_FORMATTER.format(event.getStartDateTime()));
        if (event.getEndDateTime() != null) {
          output.append(" to ").append(TIME_FORMATTER.format(event.getEndDateTime()));
        }
        if (event.getLocation() != null && !event.getLocation().isEmpty()) {
          output.append(" at ").append(event.getLocation());
        }
        output.append("\n");
      }
    } catch (IOException e) {
      throw new RuntimeException("Failed to write output", e);
    }
  }

  @Override
  public void displayEventsBetween(List<Event> events, LocalDateTime start, LocalDateTime end) {
    try {
      if (events == null || events.isEmpty()) {
        output.append("No events between ")
            .append(start.toString())
            .append(" and ")
            .append(end.toString())
            .append("\n");
        return;
      }

      output.append("Events from ")
          .append(start.toString())
          .append(" to ")
          .append(end.toString())
          .append(":\n");

      for (Event event : events) {
        output.append(" • ").append(event.getSubject());
        output.append(" starting on ")
            .append(DATE_FORMATTER.format(event.getStartDateTime().toLocalDate()));
        output.append(" at ")
            .append(TIME_FORMATTER.format(event.getStartDateTime()));

        if (event.getEndDateTime() != null) {
          output.append(", ending on ")
              .append(DATE_FORMATTER.format(event.getEndDateTime().toLocalDate()));
          output.append(" at ")
              .append(TIME_FORMATTER.format(event.getEndDateTime()));
        }

        if (event.getLocation() != null && !event.getLocation().isEmpty()) {
          output.append(" at ").append(event.getLocation());
        }
        output.append("\n");
      }
    } catch (IOException e) {
      throw new RuntimeException("Failed to write output", e);
    }
  }

  @Override
  public void displayUserStatus(UserStatus status) {
    try {
      if (status == UserStatus.BUSY) {
        output.append("busy\n");
      } else {
        output.append("available\n");
      }
    } catch (IOException e) {
      throw new RuntimeException("Failed to write output", e);
    }
  }

  @Override
  public void exportCalendar(String content, String fileName) {
    try {
      Path filePath = Paths.get(fileName);
      Files.write(filePath, content.getBytes());
      output.append("Exported to ").append(filePath.toAbsolutePath().toString()).append("\n");
    } catch (IOException e) {
      displayError("Export failed: " + e.getMessage());
    }
  }

  @Override
  public void displaySuccess(String message) {
    try {
      output.append("Success: ").append(message).append("\n");
    } catch (IOException e) {
      throw new RuntimeException("Failed to write output", e);
    }
  }

  @Override
  public void displayError(String error) {
    try {
      output.append("Error: ").append(error).append("\n");
    } catch (IOException e) {
      throw new RuntimeException("Failed to write error output", e);
    }
  }

  @Override
  public void displayWarning(String warning) {
    try {
      output.append("Warning: ").append(warning).append("\n");
    } catch (IOException e) {
      throw new RuntimeException("Failed to write output", e);
    }
  }

  @Override
  public void displayWelcome() {
    try {
      output.append("=================================\n");
      output.append("  Virtual Calendar Application  \n");
      output.append("=================================\n");
      output.append("Type 'exit' to quit\n\n");
    } catch (IOException e) {
      throw new RuntimeException("Failed to write output", e);
    }
  }
}