package calendar.controller.handlers;

import calendar.controller.commanddata.PrintCommandData;
import calendar.model.interfaces.CalendarEditable;
import calendar.model.interfaces.EventReadOnly;
import java.util.List;
import java.util.Objects;

/**
 * Handler class that performs the logic for printing events.
 * Takes parsed command data and interacts with the model.
 */
public class PrintEventHandler {

  private final CalendarEditable calendarModel;

  /**
   * Constructor for PrintEventHandler.
   *
   * @param calendarModel the calendar model to interact with
   */
  public PrintEventHandler(CalendarEditable calendarModel) {
    this.calendarModel = Objects.requireNonNull(calendarModel);
  }

  /**
   * Executes the print event logic based on the parsed command data.
   *
   * @param data the parsed command data
   * @return the result string
   */
  public String handle(PrintCommandData data) {
    List<EventReadOnly> print = calendarModel.getEvents(data.getStartDateTime(),
        data.getEndDateTime());
    if (print.isEmpty()) {
      throw new IllegalArgumentException("Error: No Events found with the given details");
    }
    if (data.isOnCommand()) {
      return outputFormatOn(print);
    }
    return outputFormatFrom(print);
  }

  private String outputFormatOn(List<EventReadOnly> print) {
    StringBuilder sb = new StringBuilder();
    for (EventReadOnly event : print) {
      sb.append("> Subject: ").append(event.getSubject()).append(", ");
      sb.append("StartDateTime: ").append(event.getStartDateTime()).append(", ");
      sb.append("EndDateTime: ").append(event.getEndDateTime()).append(", ");
      sb.append("Location: ").append(event.getLocation());
      sb.append(System.lineSeparator());
    }
    return sb.toString();
  }

  private String outputFormatFrom(List<EventReadOnly> print) {
    StringBuilder sb = new StringBuilder();
    for (EventReadOnly event : print) {
      sb.append("> ").append(event.getSubject()).append(" starting on ");
      sb.append(event.getStartDateTime().toLocalDate()).append(" at ");
      sb.append(event.getStartDateTime().toLocalTime());
      sb.append(", ending on ");
      sb.append(event.getEndDateTime().toLocalDate()).append(" at ");
      sb.append(event.getEndDateTime().toLocalTime());
      sb.append(System.lineSeparator());
    }
    return sb.toString();
  }
}

