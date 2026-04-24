package calendar.controller.commands;

import calendar.model.Calendar;
import java.time.LocalDateTime;

/**
 * Command to edit an event and all future events in its series.
 */
public class EditEventsCommand implements CalendarCommand {

  private final String property;
  private final String subject;
  private final LocalDateTime start;
  private final String newValue;

  /**
   * Constructs an EditEventsCommand.
   *
   * @param property the event property to edit
   * @param subject  the subject of the event to edit
   * @param start    the start time of the event
   * @param newValue the new value to set for the property
   */
  public EditEventsCommand(String property, String subject, LocalDateTime start,
                                 String newValue) {
    this.property = property;
    this.subject = subject;
    this.start = start;
    this.newValue = newValue;
  }

  @Override
  public String execute(Calendar model) {
    try {
      model.editEventsFromThisForward(subject, start, property, newValue);
      return "Updated event and future occurrences: " + subject;
    } catch (IllegalStateException | IllegalArgumentException e) {
      return "Error: " + e.getMessage();
    }
  }
}