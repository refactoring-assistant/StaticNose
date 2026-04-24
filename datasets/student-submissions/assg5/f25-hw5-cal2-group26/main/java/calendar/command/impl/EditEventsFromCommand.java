package calendar.command.impl;

import calendar.command.CommandInterface;
import calendar.controller.CalendarControllerInterface;
import java.time.ZonedDateTime;

/**
 * A command class for editing events in the calendar starting from a specific date.
 * Implements the CommandInterface to define the behavior for modifying a specified property
 * of events with a given subject that occur on or after the provided start date.
 */
public class EditEventsFromCommand implements CommandInterface {
  private final CalendarControllerInterface controller;
  private final String subject;
  private final ZonedDateTime start;
  private final String property;
  private final Object newValue;

  /**
   * Constructs an EditEventsFromCommand instance with the specified parameters. Initializes
   * a command to edit a specified property of events matching the given subject
   * and occurring on or after the provided start date.
   *
   * @param subject the subject or title of the events to be edited.
   * @param start the ZonedDateTime specifying the start date from which events should be edited.
   * @param property the name of the property to be modified (e.g., status, subject).
   * @param newValue the new value to set for the specified property.
   */
  public EditEventsFromCommand(CalendarControllerInterface controller, String subject,
                               ZonedDateTime start,
                               String property, Object newValue) {
    this.controller = controller;
    this.subject = subject;
    this.start = start;
    this.property = property;
    this.newValue = newValue;
  }

  @Override
  public String execute() {
    try {
      controller.editEventsFromHere(subject, start, property, newValue);
      return "Events from this occurrence edited successfully: " + subject;
    } catch (Exception e) {
      return "Error: " + e.getMessage();
    }
  }

  @Override
  public String getDescription() {
    return "Edit events from this occurrence forward";
  }
}
