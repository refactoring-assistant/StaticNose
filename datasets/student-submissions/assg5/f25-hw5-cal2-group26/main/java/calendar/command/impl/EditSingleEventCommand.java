package calendar.command.impl;

import calendar.command.CommandInterface;
import calendar.controller.CalendarControllerInterface;
import java.time.ZonedDateTime;

/**
 * Command that edits the single events.
 */
public class EditSingleEventCommand implements CommandInterface {
  private final CalendarControllerInterface controller;
  private final String subject;
  private final ZonedDateTime start;
  private final ZonedDateTime end;
  private final String property;
  private final Object newValue;

  /**
   * Creates a command to edit a single event.
   *
   * @param subject   The name of the event to edit.
   * @param start     The start time of the event.
   * @param end       The end time of the event.
   * @param property  The property to change (e.g., subject, start, end).
   * @param newValue  The new value to set for the property.
   */
  public EditSingleEventCommand(CalendarControllerInterface controller, String subject,
                                ZonedDateTime start, ZonedDateTime end,
                                String property, Object newValue) {
    this.controller = controller;
    this.subject = subject;
    this.start = start;
    this.end = end;
    this.property = property;
    this.newValue = newValue;
  }

  @Override
  public String execute() {
    try {
      controller.editSingleEvent(subject, start, end, property, newValue);
      return "Single event edited successfully: " + subject;
    } catch (Exception e) {
      return "Error: " + e.getMessage();
    }
  }

  @Override
  public String getDescription() {
    return "Edit a single event";
  }
}
